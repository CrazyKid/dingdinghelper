package com.crazykid.support;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkConstants;
import com.dingtalk.api.DingTalkSignatureUtil;
import com.taobao.api.*;
import com.taobao.api.internal.parser.json.ObjectJsonParser;
import com.taobao.api.internal.util.*;

import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author arthur
 * @date 2022/1/18 8:14 下午
 */
public class DefaultProxyDingTalkClient extends DefaultDingTalkClient {

    public DefaultProxyDingTalkClient() {
        super(null);
        this.setProxy(null);
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public DefaultProxyDingTalkClient(String serverUrl, Proxy proxy) {
        super(serverUrl);
        this.setProxy(proxy);
    }

    @Override
    public <T extends TaobaoResponse> T execute(TaobaoRequest<T> request, String session) throws ApiException {
        if(request.getTopApiCallType() == null || request.getTopApiCallType().equals(DingTalkConstants.CALL_TYPE_TOP)) {
            return super.execute(request, session);
        } else {
            return executeOApi(request, session);
        }
    }

    @Override
    public <T extends TaobaoResponse> T execute(TaobaoRequest<T> request, String accessKey, String accessSecret, String suiteTicket, String corpId) throws ApiException {
        if(request.getTopApiCallType() == null || request.getTopApiCallType().equals(DingTalkConstants.CALL_TYPE_TOP)) {
            return super.execute(request,null);
        } else {
            return executeOApi(request,null, accessKey, accessSecret,suiteTicket, corpId);
        }
    }


    private <T extends TaobaoResponse> T executeOApi(TaobaoRequest<T> request, String session) throws ApiException {
        return executeOApi(request, session, null, null, null, null);
    }

    private <T extends TaobaoResponse> T executeOApi(TaobaoRequest<T> request, String session, String accessKey, String accessSecret, String suiteTicket, String corpId) throws ApiException {
        long start = System.currentTimeMillis();
        // 构建响应解释器
        TaobaoParser<T> parser = null;
        if (this.needEnableParser) {
            parser = new ObjectJsonParser<T>(request.getResponseClass(), true);
        }

        // 本地校验请求参数
        if (this.needCheckRequest) {
            try {
                request.check();
            } catch (ApiRuleException e) {
                T localResponse = null;
                try {
                    localResponse = request.getResponseClass().newInstance();
                } catch (Exception xe) {
                    throw new ApiException(xe);
                }
                localResponse.setErrorCode(e.getErrCode());
                localResponse.setMsg(e.getErrMsg());
                return localResponse;
            }
        }

        RequestParametersHolder requestHolder = new RequestParametersHolder();
        TaobaoHashMap appParams = new TaobaoHashMap(request.getTextParams());
        requestHolder.setApplicationParams(appParams);

        // 添加协议级请求参数
        TaobaoHashMap protocalMustParams = new TaobaoHashMap();
        protocalMustParams.put(DingTalkConstants.ACCESS_TOKEN, session);
        requestHolder.setProtocalMustParams(protocalMustParams);

        try {
            String fullUrl;
            // 签名优先
            if(accessKey != null) {
                Long timestamp = System.currentTimeMillis();
                // 验证签名有效性
                String canonicalString = DingTalkSignatureUtil.getCanonicalStringForIsv(timestamp, suiteTicket);
                String signature = DingTalkSignatureUtil.computeSignature(accessSecret, canonicalString);
                Map<String, String > ps = new HashMap<String, String>();
                ps.put("accessKey", accessKey);
                ps.put("signature", signature);
                ps.put("timestamp", timestamp+"");
                if(suiteTicket != null) {
                    ps.put("suiteTicket", suiteTicket);
                }
                if(corpId != null){
                    ps.put("corpId", corpId);
                }

                String queryStr =DingTalkSignatureUtil.paramToQueryString(ps, "utf-8");
                if (this.serverUrl.indexOf("?") > 0) {
                    fullUrl = this.serverUrl + "&"+queryStr;
                } else {
                    fullUrl = this.serverUrl + "?"+queryStr;
                }
            }else{
                if (this.serverUrl.indexOf("?") > 0) {
                    fullUrl = this.serverUrl + (session != null && session.length() > 0 ? ("&access_token=" + session) : "");
                } else {
                    fullUrl = this.serverUrl + (session != null && session.length() > 0 ? ("?access_token=" + session) : "");
                }
            }

            HttpResponseData data = null;
            // 是否需要压缩响应
            if (this.useGzipEncoding) {
                request.getHeaderMap().put(Constants.ACCEPT_ENCODING, Constants.CONTENT_ENCODING_GZIP);
            }

            if("GET".equals(request.getTopHttpMethod())) {
                data = WebV2Utils.doGet(fullUrl, appParams, connectTimeout, readTimeout);
            } else {
                // 是否需要上传文件
                if (request instanceof TaobaoUploadRequest) {
                    TaobaoUploadRequest<T> uRequest = (TaobaoUploadRequest<T>) request;
                    Map<String, FileItem> fileParams = TaobaoUtils.cleanupMap(uRequest.getFileParams());
                    //todo 这里添加代理, 可以进入WebV2Utils 类中,参考接口调用时, 传参proxy的地方,自己改写下
                    data = WebV2Utils.doPost(fullUrl, appParams, fileParams, Constants.CHARSET_UTF8, connectTimeout, readTimeout, request.getHeaderMap());
                } else {

                    Map<String, Object> jsonParams = new HashMap<String, Object>();
                    for (Map.Entry<String, String> paramEntry : appParams.entrySet()) {
                        String key = paramEntry.getKey();
                        String value = paramEntry.getValue();
                        if(value.startsWith("[") && value.endsWith("]")) {
                            List<Map<String, Object>> childMap = (List<Map<String, Object>>)TaobaoUtils.jsonToObject(value);
                            jsonParams.put(key, childMap);
                        } else if(value.startsWith("{") && value.endsWith("}")) {
                            Map<String, Object> childMap = (Map<String, Object>)TaobaoUtils.jsonToObject(value);
                            jsonParams.put(key, childMap);
                        } else {
                            jsonParams.put(key, value);
                        }
                    }

                    data = doPostWithJson(fullUrl, jsonParams, Constants.CHARSET_UTF8, connectTimeout, readTimeout);
                }
            }
            requestHolder.setResponseBody(data.getBody());
            requestHolder.setResponseHeaders(data.getHeaders());
        } catch (IOException e) {
            TaobaoLogger.logApiError("_dingtalk_", request.getApiMethodName(), serverUrl, requestHolder.getAllParams(), System.currentTimeMillis() - start, e.toString());
            throw new ApiException(e);
        }

        T tRsp = null;
        if (this.needEnableParser) {
            tRsp = parser.parse(requestHolder.getResponseBody(), Constants.RESPONSE_TYPE_DINGTALK_OAPI);
            tRsp.setBody(requestHolder.getResponseBody());
        } else {
            try {
                tRsp = request.getResponseClass().newInstance();
                tRsp.setBody(requestHolder.getResponseBody());
            } catch (Exception e) {
                throw new ApiException(e);
            }
        }

        tRsp.setParams(appParams);
        if (!tRsp.isSuccess()) {
            TaobaoLogger.logApiError("_dingtalk_", request.getApiMethodName(), serverUrl, requestHolder.getAllParams(), System.currentTimeMillis() - start, tRsp.getBody());
        }
        return tRsp;
    }

    /**
     * 执行请求
     * content_type: aplication/json
     *
     * @param url
     * @param params
     * @param charset
     * @param connectTimeout
     * @param readTimeout
     * @return
     * @throws IOException
     */
    private HttpResponseData doPostWithJson(String url, Map<String, Object> params, String charset, int connectTimeout, int readTimeout) throws IOException {
        String ctype = "application/json;charset=" + charset;
        byte[] content = {};

        String body = TaobaoUtils.objectToJson(params);
        if (body != null) {
            content = body.getBytes(charset);
        }
        return WebV2Utils.doPost(url, ctype, content, connectTimeout, readTimeout, null, this.getProxy());
    }
}
