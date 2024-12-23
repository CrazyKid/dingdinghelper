package com.crazykid.config;

import com.crazykid.support.DefaultProxyDingTalkClient;
import com.crazykid.utils.AlarmProxyAddressUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

/**
 * 钉钉机器人配置
 *
 * @author arthur
 * @date 2020/11/2 5:39 下午
 */
@Configuration
@ConditionalOnBean({ DingProperties.class })
public class DingConfig {

    private final Logger log = LoggerFactory.getLogger(DingConfig.class);

    @Resource
    private DingProperties dingProperties;

    @PostConstruct
    private void register() {
        log.info("DingConfig register ======>");
    }

    /**
     * 获取client
     *
     * @return
     */
    @Bean("default-ding-client")
    public DingTalkClient getClient() {
        return new DefaultDingTalkClient(dingProperties.getWebhook());
    }

    /**
     * 获取client
     *
     * @return
     */
    @Bean("proxy-ding-client")
    public DingTalkClient getProxyClient() {
        List<InetSocketAddress> proxyAddress = AlarmProxyAddressUtils.getProxyAddress(dingProperties.getProxyIpList());
        log.info("ding-alarm-proxy-address:{}", proxyAddress);
        if (proxyAddress.isEmpty()) {
            log.info("cannot-create-proxy-ding-client, replace by default client");
            return getClient();
        }

        InetSocketAddress address = proxyAddress.get(0);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        DefaultProxyDingTalkClient client = new DefaultProxyDingTalkClient(dingProperties.getWebhook(), proxy);
        log.info("create-proxy-ding-client, host:{}, port:{}", address.getHostString(), address.getPort());
        return client;
    }

}
