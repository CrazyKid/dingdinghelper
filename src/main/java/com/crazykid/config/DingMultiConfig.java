package com.crazykid.config;

import com.crazykid.dto.AlarmActionObject;
import com.crazykid.support.DefaultAlarmDingTalkClient;
import com.crazykid.support.DefaultProxyDingTalkClient;
import com.crazykid.utils.AlarmProxyAddressUtils;
import com.crazykid.utils.AlarmSpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;

/**
 * @author arthur
 * @date 2024/12/23 18:00
 */
@Configuration
@Import({ DingMultiConfig.RegisterBeanUtils.class })
@ConditionalOnBean({ DingMultiProperties.class })
public class DingMultiConfig {
    private static final Logger log = LoggerFactory.getLogger(DingMultiConfig.class);

    @PostConstruct
    private void register() {
        log.info("DingMultiConfig register ======>");
    }

    public static class RegisterBeanUtils implements ImportBeanDefinitionRegistrar, EnvironmentAware {
        private DingMultiProperties dingMultiProperties;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            if (dingMultiProperties == null) {
                log.info("bindDingMultiPropertiesFailed =============>");
                return;
            }
            List<DingMultiProperties.EachAlarm> alarmList = dingMultiProperties.getAlarmList();
            if (alarmList == null || alarmList.isEmpty()) {
                log.info("initDingTalkClientMultiFailed ======> alarmList is empty.[{}]", alarmList);
                return;
            }

            List<InetSocketAddress> proxyAddress =
                    AlarmProxyAddressUtils.getProxyAddress(dingMultiProperties.getProxyIpList());
            log.info("ding-alarm-proxy-address:{}", proxyAddress);
            Proxy proxy = null;
            if (!proxyAddress.isEmpty()) {
                proxy = new Proxy(Proxy.Type.HTTP, proxyAddress.get(0));
            }

            for (DingMultiProperties.EachAlarm eachAlarm : alarmList) {
                AlarmActionObject actionObject = dingMultiProperties.check(eachAlarm);
                // 创建默认的报警机器人
                {
                    RootBeanDefinition beanDefinition = new RootBeanDefinition();
                    beanDefinition.setBeanClass(DefaultAlarmDingTalkClient.class);
                    MutablePropertyValues values = new MutablePropertyValues();
                    values.addPropertyValue("serverUrl", eachAlarm.getWebhook());
                    beanDefinition.setPropertyValues(values);
                    String beanName = AlarmSpringUtils.getDingTalkClientBeanName(actionObject);
                    registry.registerBeanDefinition(beanName, beanDefinition);
                    log.info("DefaultAlarmDingTalkClient register beanName =====>{}", beanName);
                }

                // 创建走代理的报警机器人
                {
                    if (proxy != null) {
                        RootBeanDefinition beanDefinition = new RootBeanDefinition();
                        beanDefinition.setBeanClass(DefaultProxyDingTalkClient.class);
                        MutablePropertyValues values = new MutablePropertyValues();
                        values.addPropertyValue("serverUrl", eachAlarm.getWebhook());
                        values.addPropertyValue("proxy", proxy);
                        beanDefinition.setPropertyValues(values);
                        String beanName = AlarmSpringUtils.getProxyDingTalkClientBeanName(actionObject);
                        registry.registerBeanDefinition(beanName, beanDefinition);
                        log.info("DefaultProxyDingTalkClient register beanName =====>{}", beanName);
                    }
                }
            }
        }

        @Override
        public void setEnvironment(Environment environment) {
            String prefix = Objects.requireNonNull(
                    AnnotationUtils.getAnnotation(DingMultiProperties.class, ConfigurationProperties.class)).prefix();
            BindResult<DingMultiProperties> bindResult =
                    Binder.get(environment).bind(prefix, DingMultiProperties.class);
            if (bindResult.isBound()) {
                dingMultiProperties = bindResult.get();
            } else {
                log.info("bindDingMultiPropertiesFailed =============>");
            }
        }
    }
}