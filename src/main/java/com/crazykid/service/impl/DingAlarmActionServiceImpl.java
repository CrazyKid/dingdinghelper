package com.crazykid.service.impl;

import com.crazykid.config.DingMultiProperties;
import com.crazykid.dto.AlarmActionObject;
import com.crazykid.utils.AlarmSpringUtils;
import com.dingtalk.api.DingTalkClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author arthur
 * @date 2024/12/23 18:10
 */
@Service
@ConditionalOnBean({ DingMultiProperties.class })
@Primary
public class DingAlarmActionServiceImpl extends AbstractDingAlarmFactory {
    @Resource
    private DingMultiProperties dingMultiProperties;

    @PostConstruct
    private void init() {
        ignoreDuplicated = dingMultiProperties.getIgnoreDuplicated();
        ignoreDuplicatedRepeatTimes = dingMultiProperties.getIgnoreDuplicatedRepeatTimes();
        ignoreDuplicatedTimeSeconds = dingMultiProperties.getIgnoreDuplicatedTimeSeconds();
    }

    /**
     * 获取支付信息的前缀
     *
     * @param action 报警对象
     * @return
     */
    @Override
    public String getPrefix(AlarmActionObject action) {
        for (DingMultiProperties.EachAlarm eachAlarm : dingMultiProperties.getAlarmList()) {
            if (eachAlarm.getActionObject().equals(action.getActionObject())) {
                return eachAlarm.getPrefix() + "-" + getCurrentEnv() + "-";
            }
        }
        return "" + "-" + getCurrentEnv() + "-";
    }

    /**
     * 测试环境/本地,不需要正向代理;正式和预发,使用正向代理
     *
     * @return
     */
    @Override
    public DingTalkClient getClient(AlarmActionObject action) {
        String beanName = AlarmSpringUtils.getDingTalkClientBeanName(action);
        if (!getNeedProxyEnv().contains(getCurrentEnv())) {
            return AlarmSpringUtils.getBean(beanName, DingTalkClient.class);
        }
        String proxyBeanName = AlarmSpringUtils.getProxyDingTalkClientBeanName(action);
        return AlarmSpringUtils.getBean(proxyBeanName, DingTalkClient.class);
    }

    @Override
    public Boolean isAtActionIgnoredEnvByForce() {
        return dingMultiProperties.getAtActionIgnoredEnvByForce();
    }

    @Override
    public String getProxyEnvList() {
        return dingMultiProperties.getProxyEnvList();
    }
}

