package com.crazykid.config;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    /**
     * 机器人的链接地址
     */
    private String webhook;

    /**
     * 正向代理 地址加端口,逗号分隔
     */
    private String proxyIpList;

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
        // log.info("ding-notify-proxy-address:{}", proxyAddress);
        if (proxyAddress.isEmpty()) {
            // log.info("cannot-create-proxy-ding-client, replace by default client");
            return getClient();
        }
        InetSocketAddress address = proxyAddress.get(0);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        DefaultProxyDingTalkClient client = new DefaultProxyDingTalkClient(webhook, proxy);
        // log.info("create-proxy-ding-client, host:{}, port:{}", address.getHostString(), address.getPort());
        return client;
    }

    /**
     * 获取正向代理的ip池子
     *
     * @return
     */
    private List<InetSocketAddress> getProxyAddress() {
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

    public String getProxyIpList() {
        return proxyIpList;
    }

    public void setProxyIpList(String proxyIpList) {
        this.proxyIpList = proxyIpList;
    }

}
