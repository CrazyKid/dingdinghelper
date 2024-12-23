package com.crazykid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author arthur
 * @date 2024/12/23 18:01
 */
@Configuration
@ConfigurationProperties(prefix = "ding")
@ConditionalOnProperty(prefix = "ding", name = "webhook")
public class DingProperties {
    private final Logger log = LoggerFactory.getLogger(DingProperties.class);

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

    @Override
    public String toString() {
        return "DingProperties{" +
                "webhook='" + webhook + '\'' +
                ", prefix='" + prefix + '\'' +
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
}
