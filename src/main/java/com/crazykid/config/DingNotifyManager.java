package com.crazykid.config;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 钉钉消息发送封装
 *
 * @author arthur
 * @date 2020/11/2 6:09 下午
 * @see <a href="https://ding-doc.dingtalk.com/doc#/serverapi2/qf2nxq/d535db33">自定义机器人webhook</a>
 */
@Component
public class DingNotifyManager {
    /**
     * 消息中必须包含这个关键词,因为在创建机器人的时候配置了该关键词
     */
    private final static String DINGDING_PREFIX = "支付网关-";
    @Autowired
    private Environment environment;
    @Resource(name = "proxy-ding-client")
    private DingTalkClient proxyClient;
    @Resource(name = "default-ding-client")
    private DingTalkClient defaultClient;

    private String getPrefix() {
        String[] activeProfiles = environment.getActiveProfiles();
        String env = Optional.of(activeProfiles).flatMap(a -> Arrays.stream(a).findFirst()).orElse(null);
        return DINGDING_PREFIX + env + "-";
    }

    /**
     * 测试环境/本地,不需要正向代理;正式和预发,使用正向代理
     *
     * @return
     */
    private DingTalkClient getClient() {
        String[] activeProfiles = environment.getActiveProfiles();
        String env = Optional.of(activeProfiles).flatMap(a -> Arrays.stream(a).findFirst()).orElse(null);
        //todo 这里默认配置了这几个环境不使用正向代理, 可以自行更改
        if ("default".equals(env) || "dev".equals(env) || "test".equals(env) || "uat".equals(env)) {
            return defaultClient;
        }
        if (proxyClient instanceof DefaultDingTalkClient) {
            // log.info("usingProxyClient:{}", ((DefaultDingTalkClient) proxyClient).getProxy().address());
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
    private boolean handlerNotify(DingTalkClient client, Consumer<OapiRobotSendRequest> consumer) {
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        consumer.accept(request);
        return sendNotify(client, request);
    }

    /**
     * 发送组装好的通知
     *
     * @param client
     * @param request
     * @return
     */
    private boolean sendNotify(DingTalkClient client, OapiRobotSendRequest request) {
        boolean success = true;
        try {
            OapiRobotSendResponse response = client.execute(request);
            success = response.isSuccess();
            if (!success) {
                // log.info("sendDingNotifyFailed request:{}, response:{}", request, response);
            }
        } catch (ApiException e) {
            // log.error("sendDingNotifyError request:{}, error:{}", request, e);
            success = false;
        }
        return success;
    }

    /**
     * 发送纯文本
     *
     * @param content
     * @param atAll
     * @return
     */
    public boolean sendCleanText(String content, boolean atAll) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        return handlerNotify(getClient(), (OapiRobotSendRequest request) -> {
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

    /**
     * 发送带连接的通知
     *
     * @param title
     * @param content
     * @param picUrl
     * @param linkUrl
     * @return
     */
    public boolean sendLink(String title, String content, String picUrl, String linkUrl) {
        return handlerNotify(getClient(), (OapiRobotSendRequest request) -> {
            request.setMsgtype("link");
            OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();
            link.setMessageUrl(linkUrl);
            link.setPicUrl(picUrl);
            link.setTitle(getPrefix() + title);
            link.setText(content);
            request.setLink(link);
        });
    }

    /**
     * 发送markdown格式通知
     *
     * @param title        显示在对话列表,但不显示在具体窗口中的文案
     * @param markdownText 显示在具体窗口中的文案,markdown语法支持 {@code #}, {@code >}, {@code ****}, {@code **},<br/>
     *                     {@code [text](http://www.baidu.com)}, {@code ![image](http://name.com/pic.jpg)}, {@code -}, {@code 1.}
     * @return
     */
    public boolean sendMarkdown(String title, String markdownText) {
        return handlerNotify(getClient(), (OapiRobotSendRequest request) -> {
            request.setMsgtype("markdown");
            OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
            markdown.setTitle(getPrefix() + title);
            markdown.setText(markdownText);
            request.setMarkdown(markdown);
        });
    }
}
