package com.crazykid.service.impl;

import com.crazykid.config.DefaultProxyDingTalkClient;
import com.crazykid.config.DingConfig;
import com.crazykid.service.DingAlarmService;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author arthur
 * @date 2022/1/19 3:26 下午
 */
@Service
public class DefaultDingAlarmServiceImpl implements DingAlarmService {

    private Logger log = LoggerFactory.getLogger(DefaultDingAlarmServiceImpl.class);

    @Autowired
    private Environment environment;
    @Resource(name = "proxy-ding-client")
    private DingTalkClient proxyClient;
    @Resource(name = "default-ding-client")
    private DingTalkClient defaultClient;
    @Resource
    private DingConfig dingConfig;

    /**
     * 获取当前的profile
     *
     * @return
     */
    private String getCurrentEnv() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Optional.of(activeProfiles).flatMap(a -> Arrays.stream(a).findFirst()).orElse(null);
    }

    /**
     * 获取支付信息的前缀
     *
     * @return
     */
    private String getPrefix() {
        return Optional.ofNullable(dingConfig.getPrefix()).orElse("") + "-" + getCurrentEnv() + "-";
    }

    /**
     * 测试环境/本地,不需要正向代理;正式和预发,使用正向代理
     *
     * @return
     */
    private DingTalkClient getClient() {
        if (!dingConfig.getNeedProxyEnv().contains(getCurrentEnv())) {
            return defaultClient;
        }
        if (proxyClient instanceof DefaultProxyDingTalkClient) {
            log.info("usingProxyClient:{}",
                    Optional.ofNullable(((DefaultProxyDingTalkClient) proxyClient).getProxy()).map(
                            Proxy::address).orElse(null));
        }
        return proxyClient;
    }

    /**
     * 处理通知发送
     *
     * @param client
     * @param consumer
     * @return
     */
    private boolean handlerAlarm(DingTalkClient client, Consumer<OapiRobotSendRequest> consumer) {
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        consumer.accept(request);
        return sendAlarm(client, request);
    }

    /**
     * 发送组装好的通知
     *
     * @param client
     * @param request
     * @return
     */
    private boolean sendAlarm(DingTalkClient client, OapiRobotSendRequest request) {
        boolean success = true;
        try {
            OapiRobotSendResponse response = client.execute(request);
            success = response.isSuccess();
            if (!success) {
                log.info("sendDingAlarmFailed request:{}, response:{}", request, response);
            }
        } catch (ApiException e) {
            log.error("sendDingAlarmError request:{}, error:{}", request, e);
            success = false;
        }
        return success;
    }

    @Override
    public boolean sendCleanText(String content, boolean atAll) {

        if (StringUtils.isEmpty(content)) {
            return false;
        }
        return handlerAlarm(getClient(), (OapiRobotSendRequest request) -> {
            request.setMsgtype("text");
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent(getPrefix() + content);
            request.setText(text);
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            //按照手机号at
            //        at.setAtMobiles(Arrays.asList("132xxxxxxxx"));
            at.setIsAtAll(atAll);
            request.setAt(at);
        });
    }

    @Override
    public boolean sendLink(String title, String content, String picUrl, String linkUrl) {
        return handlerAlarm(getClient(), (OapiRobotSendRequest request) -> {
            request.setMsgtype("link");
            OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();
            link.setMessageUrl(linkUrl);
            link.setPicUrl(picUrl);
            link.setTitle(getPrefix() + title);
            link.setText(content);
            request.setLink(link);
        });
    }

    @Override
    public boolean sendMarkdown(String title, String markdownText) {
        return handlerAlarm(getClient(), (OapiRobotSendRequest request) -> {
            request.setMsgtype("markdown");
            OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
            markdown.setTitle(getPrefix() + title);
            markdown.setText(markdownText);
            request.setMarkdown(markdown);
        });
    }
}
