package com.crazykid.service.impl;

import com.crazykid.support.DefaultProxyDingTalkClient;
import com.crazykid.config.DingProperties;
import com.crazykid.dto.AlarmActionObject;
import com.dingtalk.api.DingTalkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.Proxy;
import java.util.Optional;

/**
 * @author arthur
 * @date 2022/1/19 3:26 下午
 */
@Service
@ConditionalOnBean({ DingProperties.class })
public class DefaultDingAlarmServiceImpl extends AbstractDingAlarmFactory {

    Logger log = LoggerFactory.getLogger(DefaultDingAlarmServiceImpl.class);

    @Resource(name = "proxy-ding-client")
    private DingTalkClient proxyClient;
    @Resource(name = "default-ding-client")
    private DingTalkClient defaultClient;
    @Resource
    private DingProperties dingProperties;

    @PostConstruct
    private void init() {
        ignoreDuplicated = dingProperties.getIgnoreDuplicated();
        ignoreDuplicatedRepeatTimes = dingProperties.getIgnoreDuplicatedRepeatTimes();
        ignoreDuplicatedTimeSeconds = dingProperties.getIgnoreDuplicatedTimeSeconds();
    }

    /**
     * 获取支付信息的前缀
     *
     * @return
     */
    @Override
    public String getPrefix(AlarmActionObject action) {
        return Optional.ofNullable(dingProperties.getPrefix()).orElse("") + "-" + getCurrentEnv() + "-";
    }

    /**
     * 测试环境/本地,不需要正向代理;正式和预发,使用正向代理
     *
     * @return
     */
    @Override
    public DingTalkClient getClient(AlarmActionObject action) {
        if (!getNeedProxyEnv().contains(getCurrentEnv())) {
            return defaultClient;
        }
        if (proxyClient instanceof DefaultProxyDingTalkClient) {
            log.info("usingProxyClient:{}",
                    Optional.ofNullable(((DefaultProxyDingTalkClient) proxyClient).getProxy()).map(
                            Proxy::address).orElse(null));
        }
        return proxyClient;
    }

    @Override
    public Boolean isAtActionIgnoredEnvByForce() {
        return dingProperties.getAtActionIgnoredEnvByForce();
    }

    @Override
    String getProxyEnvList() {
        return dingProperties.getProxyEnvList();
    }
}
