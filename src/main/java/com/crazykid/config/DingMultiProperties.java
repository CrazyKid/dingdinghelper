package com.crazykid.config;

import com.crazykid.dto.AlarmActionObject;
import com.crazykid.dto.AlarmActionObjectHolder;
import com.crazykid.enums.AlarmActionObjectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

/**
 * 配置多个报警机器人, 面向不同的报警群体, 使用不同的报警机器人
 *
 * @author arthur
 * @date 2024/12/23 18:01
 */
@Configuration
@ConfigurationProperties(prefix = "ding-multi")
@ConditionalOnProperty(prefix = "ding-multi", name = "enable", havingValue = "true")
public class DingMultiProperties {

    private final Logger log = LoggerFactory.getLogger(DingMultiProperties.class);

    /**
     * 每个报警对象的配置
     */
    private List<EachAlarm> alarmList;

    /**
     * 是否开启报警机器人 目前是必须要开启的,不开启启动报错
     */
    private Boolean enable;

    /**
     * 使用这个类名, 来解析报警机器人作用对象
     */
    private String actionObjectClassName;

    /**
     * 默认艾特的手机号列表
     */
    private List<String> defaultAtPhoneList;

    /**
     * 正向代理 地址加端口,逗号分隔 例如 "192.168.100.100:11328,192.168.100.101:11328"
     */
    private String proxyIpList;

    /**
     * 正向代理 使用的环境信息,逗号分隔, 例如 "prod,pre"
     */
    private String proxyEnvList;

    /**
     * 是否强制开启at功能而不区分环境
     * 如果null 或者 false,除预发和正式环境外不会有at功能.
     * 如果true, 根据at all的入参 或者 at phone list决定是否有at功能
     */
    private Boolean atActionIgnoredEnvByForce;

    /**
     * 是否开启忽略短期内的相同报警信息
     */
    private Boolean ignoreDuplicated = false;

    /**
     * 相同的信息 在一定的时间内重复 会被忽略
     */
    private Long ignoreDuplicatedTimeSeconds = 300L;

    /**
     * 相同的信息重复几次后会被忽略, 举个例子, 当值为2时,第三次报警相同信息会被忽略
     */
    private Integer ignoreDuplicatedRepeatTimes = 1;

    /**
     * 内部使用的, 不需要配置,
     * 用于存放解析出来的 {@link #actionObjectClassName} 类信息
     */
    private transient AlarmActionObjectHolder alarmActionObjectHolder;

    @Override
    public String toString() {
        return "DingMultiProperties{" +
                "alarmList=" + alarmList +
                ", enable=" + enable +
                ", actionObjectClassName='" + actionObjectClassName + '\'' +
                ", defaultAtPhoneList=" + defaultAtPhoneList +
                ", proxyIpList='" + proxyIpList + '\'' +
                ", proxyEnvList='" + proxyEnvList + '\'' +
                ", atActionIgnoredEnvByForce=" + atActionIgnoredEnvByForce +
                ", ignoreDuplicated=" + ignoreDuplicated +
                ", ignoreDuplicatedTimeSeconds=" + ignoreDuplicatedTimeSeconds +
                ", ignoreDuplicatedRepeatTimes=" + ignoreDuplicatedRepeatTimes +
                '}';
    }

    @PostConstruct
    private void register() {
        log.info(toString());
    }

    public AlarmActionObject check(EachAlarm alarm) {
        if (alarm == null) {
            throw new RuntimeException("报警机器人作用对象配置不可为空");
        }
        if (StringUtils.isEmpty(alarm.getWebhook())) {
            throw new RuntimeException("报警机器人webhook[链接地址]不可为空");
        }
        if (StringUtils.isEmpty(alarm.getPrefix())) {
            throw new RuntimeException("报警机器人prefix[关键词]不可为空");
        }

        if (alarmActionObjectHolder == null) {
            createAlarmActionObjectHolder();
        }

        return alarmActionObjectHolder.getInstance().getByActionObject(alarm.getActionObject());
    }

    private synchronized void createAlarmActionObjectHolder() {
        if (StringUtils.isEmpty(actionObjectClassName)) {
            alarmActionObjectHolder = new AlarmActionObjectHolder();
            alarmActionObjectHolder.setClazz(AlarmActionObjectTemplate.class);
            alarmActionObjectHolder.setInstance(AlarmActionObjectTemplate.COMMON);
        } else {
            try {
                Class<?> clazz = Class.forName(actionObjectClassName);
                Object anyone;
                if (!clazz.isEnum()) {
                    anyone = clazz.newInstance();
                } else {
                    Object[] enumConstants = clazz.getEnumConstants();
                    if (enumConstants == null || enumConstants.length == 0) {
                        throw new RuntimeException("配置的报警作用对象解析类枚举值为空:" + actionObjectClassName);
                    }
                    anyone = enumConstants[0];
                }
                if (!(anyone instanceof AlarmActionObject)) {
                    throw new RuntimeException("配置的报警作用对象解析类并非[AlarmActionObject]的实现类:" + actionObjectClassName);
                }
                Class<? extends AlarmActionObject> aClass = (Class<? extends AlarmActionObject>) clazz;
                alarmActionObjectHolder = new AlarmActionObjectHolder();
                alarmActionObjectHolder.setClazz(aClass);
                alarmActionObjectHolder.setInstance(((AlarmActionObject) anyone));
            } catch (ClassNotFoundException classNotFoundException) {
                log.error("配置的报警作用对象解析类不存在,{}", actionObjectClassName, classNotFoundException);
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("配置的报警作用对象解析类无法初始化,{}", actionObjectClassName, e);
            }
            if (alarmActionObjectHolder == null) {
                throw new RuntimeException("配置的报警作用对象解析类不能正常生效:" + actionObjectClassName);
            }
        }
    }

    public List<EachAlarm> getAlarmList() {
        return alarmList;
    }

    public void setAlarmList(List<EachAlarm> alarmList) {
        this.alarmList = alarmList;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getActionObjectClassName() {
        return actionObjectClassName;
    }

    public void setActionObjectClassName(String actionObjectClassName) {
        this.actionObjectClassName = actionObjectClassName;
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

    public Boolean getIgnoreDuplicated() {
        return ignoreDuplicated;
    }

    public void setIgnoreDuplicated(Boolean ignoreDuplicated) {
        this.ignoreDuplicated = ignoreDuplicated;
    }

    public Long getIgnoreDuplicatedTimeSeconds() {
        return ignoreDuplicatedTimeSeconds;
    }

    public void setIgnoreDuplicatedTimeSeconds(Long ignoreDuplicatedTimeSeconds) {
        this.ignoreDuplicatedTimeSeconds = ignoreDuplicatedTimeSeconds;
    }

    public Integer getIgnoreDuplicatedRepeatTimes() {
        return ignoreDuplicatedRepeatTimes;
    }

    public void setIgnoreDuplicatedRepeatTimes(Integer ignoreDuplicatedRepeatTimes) {
        this.ignoreDuplicatedRepeatTimes = ignoreDuplicatedRepeatTimes;
    }

    public static class EachAlarm implements Serializable {
        /**
         * 机器人的链接地址
         */
        private String webhook;

        /**
         * 消息中必须包含这个关键词,因为在创建机器人的时候配置了该关键词
         */
        private String prefix;

        /**
         * 报警接受的对象类型, 可以使用{@link AlarmActionObjectTemplate}
         * 也可以自行定义, 但必须要实现 {@link AlarmActionObject}
         * 要求每个接受报警的对象, 只能有一个对应的webhook
         */
        private Integer actionObject;

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

        public Integer getActionObject() {
            return actionObject;
        }

        public void setActionObject(Integer actionObject) {
            this.actionObject = actionObject;
        }

        @Override
        public String toString() {
            return "EachAlarm{" +
                    "webhook='" + webhook + '\'' +
                    ", prefix='" + prefix + '\'' +
                    ", actionObject=" + actionObject +
                    '}';
        }
    }
}

