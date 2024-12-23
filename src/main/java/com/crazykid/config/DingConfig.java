package com.crazykid.config;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * 钉钉机器人配置
 *
 * @author arthur
 * @date 2020/11/2 5:39 下午
 */
@ConfigurationProperties(prefix = "ding")
@Configuration
public class DingConfig {

    private final Logger log = LoggerFactory.getLogger(DingConfig.class);

    /**
     * 机器人的链接地址
     */
    private String webhook;

    /**
     * 消息中必须包含这个关键词,因为在创建机器人的时候配置了该关键词
     */
    private String prefix;

    /**
     * 默认艾特的手机号列表
     */
    private List<String> defaultAtPhoneList;

    /**
     * 正向代理 地址加端口,逗号分隔
     */
    private String proxyIpList;

    /**
     * 正向代理 使用的环境信息,例如prod,pre, 逗号分隔
     */
    private String proxyEnvList;

    /**
     * 是否强制开启at功能而不区分环境
     * 如果null 或者 false,除预发和正式环境外不会有at功能.
     * 如果true, 根据at all的入参 或者 at phone list决定是否有at功能
     */
    private Boolean atActionIgnoredEnvByForce;

    /**
     * 获取client
     *
     * @return
     */
    @Bean("default-ding-client")
    public DingTalkClient getClient() {
        return new DefaultDingTalkClient(webhook);
    }

    /**
     * 获取client
     *
     * @return
     */
    @Bean("proxy-ding-client")
    public DingTalkClient getProxyClient() {
        List<InetSocketAddress> proxyAddress = getProxyAddress();
        log.info("ding-alarm-proxy-address:{}", proxyAddress);
        if (proxyAddress.isEmpty()) {
            log.info("cannot-create-proxy-ding-client, replace by default client");
            return getClient();
        }

        InetSocketAddress address = proxyAddress.get(0);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        DefaultProxyDingTalkClient client = new DefaultProxyDingTalkClient(webhook, proxy);
        log.info("create-proxy-ding-client, host:{}, port:{}", address.getHostString(), address.getPort());
        return client;
    }

    /**
     * 获取配置中需要正向代理的环境信息
     *
     * @return
     */
    public List<String> getNeedProxyEnv() {
        List<String> envList = new ArrayList<>();
        String proxyEnvList = getProxyEnvList();
        log.debug("ding alarm proxy environment:{}", proxyEnvList);
        if (StringUtils.isEmpty(proxyEnvList)) {
            log.debug("ding alarm proxy environment is empty. use default ding client always");
            return envList;
        }

        String[] split = proxyEnvList.split(",");
        for (String s : split) {
            if (!StringUtils.isEmpty(s)) {
                envList.add(s);
            }
        }
        return envList;
    }

    /**
     * 获取正向代理的ip池子
     *
     * @return
     */
    private List<InetSocketAddress> getProxyAddress() {
        String proxyIpList = getProxyIpList();
        List<InetSocketAddress> list = new ArrayList<>();
        if (proxyIpList == null || proxyIpList.isEmpty()) {
            return list;
        }

        String[] split = proxyIpList.split(",");
        for (String s : split) {
            String[] one = s.split(":");
            list.add(new InetSocketAddress(one[0], Integer.parseInt(one[1])));
        }
        return list;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getDefaultAtPhoneList() {
        return defaultAtPhoneList;
    }

    public void setDefaultAtPhoneList(List<String> defaultAtPhoneList) {
        this.defaultAtPhoneList = defaultAtPhoneList;
    }

    public String getProxyIpList() {
        return proxyIpList;
    }

    public void setProxyIpList(String proxyIpList) {
        this.proxyIpList = proxyIpList;
    }

    public String getProxyEnvList() {
        return proxyEnvList;
    }

    public void setProxyEnvList(String proxyEnvList) {
        this.proxyEnvList = proxyEnvList;
    }

    public Boolean getAtActionIgnoredEnvByForce() {
        return atActionIgnoredEnvByForce;
    }

    public void setAtActionIgnoredEnvByForce(Boolean atActionIgnoredEnvByForce) {
        this.atActionIgnoredEnvByForce = atActionIgnoredEnvByForce;
    }
}
