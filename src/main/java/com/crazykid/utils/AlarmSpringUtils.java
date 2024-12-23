package com.crazykid.utils;

import com.crazykid.dto.AlarmActionObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author arthur
 * @date 2024/12/23 17:55
 */
@Component
public class AlarmSpringUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    /**
     * 获取指定类型的报警机器人bean
     *
     * @param actionObject 报警机器人报警对象类型
     * @return
     */
    public static String getDingTalkClientBeanName(AlarmActionObject actionObject) {
        if (actionObject == null) {
            return null;
        }
        return AlarmConstants.DING_TALK_CLIENT_BEAN_PREFIX + actionObject.getActionObjectName();
    }

    public static String getProxyDingTalkClientBeanName(AlarmActionObject actionObject) {
        if (actionObject == null) {
            return null;
        }
        return AlarmConstants.DING_TALK_CLIENT_BEAN_PREFIX + actionObject.getActionObjectName() + AlarmConstants.DING_TALK_CLIENT_BEAN_USER_PROXY_SUFFIX;
    }

    /**
     * 获取applicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext1) throws BeansException {
        if (applicationContext == null) {
            applicationContext = applicationContext1;
        }
    }

    /**
     * acquire spring bean.
     *
     * @param <T>  class
     * @param type type
     * @return bean bean
     */
    public static <T> T getBean(final Class<T> type) {
        return applicationContext.getBean(type);
    }

    /**
     * @param beanName
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName, Class<T> type) {
        return applicationContext.getBean(beanName, type);
    }
}
