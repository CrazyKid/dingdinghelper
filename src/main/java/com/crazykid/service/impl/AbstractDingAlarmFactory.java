package com.crazykid.service.impl;

import com.crazykid.dto.AlarmActionObject;
import com.crazykid.service.DingAlarmService;
import com.crazykid.support.AlarmCacheDriver;
import com.crazykid.utils.AlarmSpringUtils;
import com.crazykid.utils.Md5Utils;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 提供一些基础的功能实现,并定义一些方法由子类实现
 *
 * @author arthur
 * @date 2024/12/23 18:07
 */
public abstract class AbstractDingAlarmFactory implements DingAlarmService {

    /**
     * 重复报警的缓存key
     */
    private static final String CACHE_KEY_PREFIX = "dingAlarmIgnoreDupKeyPrefix";
    private static final String DUPLICATED_IGNORED_PREFIX = "[触发屏蔽规则, 相同文案报警将会在未来%s内被屏蔽,请尽快进行处理]";
    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 是否开启忽略短期内的相同报警信息
     */
    protected boolean ignoreDuplicated = false;
    /**
     * 相同的信息 在一定的时间内重复 会被忽略
     */
    protected Long ignoreDuplicatedTimeSeconds;
    /**
     * 相同的信息重复几次后会被忽略, 举个例子, 当值为2时,第三次报警相同信息会被忽略
     */
    protected Integer ignoreDuplicatedRepeatTimes;

    @Resource
    private Environment environment;

    private String getDuplicatedIgnoredPrefix() {
        return String.format(DUPLICATED_IGNORED_PREFIX,
                ignoreDuplicatedTimeSeconds < 60
                        ? (ignoreDuplicatedTimeSeconds + "秒")
                        : (ignoreDuplicatedTimeSeconds / 60 + "分钟"));
    }

    /**
     * 获取报警机器人
     * 测试环境/本地,不需要正向代理;正式和预发,使用正向代理
     *
     * @return
     */
    abstract DingTalkClient getClient(AlarmActionObject action);

    /**
     * 获取支付信息的前缀
     *
     * @return
     */
    abstract String getPrefix(AlarmActionObject action);

    /**
     * 是否强制开启at功能而不区分环境
     * 如果null 或者 false,除预发和正式环境外不会有at功能.
     * 如果true, 根据at all的入参 或者 at phone list决定是否有at功能
     *
     * @return
     */
    abstract Boolean isAtActionIgnoredEnvByForce();

    /**
     * 获取配置文件中的正向代理配置
     * 例如prod,pre, 逗号分隔
     *
     * @return
     */
    abstract String getProxyEnvList();

    @PostConstruct
    private void register() {
        log.info(this.getClass().getName() + " register==========>");
    }

    /**
     * 获取重复报警的缓存key
     *
     * @param actionObject
     * @param contentMd5
     * @return
     */
    private String getCacheKey(AlarmActionObject actionObject, String contentMd5) {
        return String.format("%s%s_%s",
                CACHE_KEY_PREFIX,
                Optional.ofNullable(actionObject).map(AlarmActionObject::getActionObjectName).orElse("null"),
                contentMd5);
    }

    /**
     * 是否忽略本次报警
     *
     * @param content
     * @param actionObject
     * @return
     */
    private IgnoreResult checkIgnoreResult(String content, AlarmActionObject actionObject) {
        if (!ignoreDuplicated) {
            return IgnoreResult.NO_IGNORE;
        }

        AlarmCacheDriver driver = null;
        try {
            driver = AlarmSpringUtils.getBean(AlarmCacheDriver.class);
        } catch (Exception ignored) {
        }
        if (driver == null) {
            return IgnoreResult.NO_IGNORE;
        }

        String md5String = Md5Utils.getMD5String(content);
        String cacheKey = getCacheKey(actionObject, md5String);
        Object cache = null;
        try {
            cache = driver.getCache(cacheKey);
        } catch (Exception ignored) {
        }
        if (cache == null) {
            return new IgnoreResult(false, false, 0, driver, cacheKey);
        }
        int times = Integer.parseInt(cache.toString());
        if (ignoreDuplicatedRepeatTimes < (times + 1)) {
            return new IgnoreResult(true, false, times, driver, cacheKey);
        }
        return new IgnoreResult(false, ignoreDuplicatedRepeatTimes == (times + 1), times, driver, cacheKey);
    }

    /**
     * 忽略本次报警的时候 打出报警的文案 便于后续跟踪
     *
     * @param ignoreResult
     * @param content
     */
    private void logIgnoreMsg(IgnoreResult ignoreResult, String content) {
        String msg = String.format("报警信息%s分钟内重复了%s次, 忽略本次报警.本次报警文案:%s",
                ignoreDuplicatedTimeSeconds / 60,
                ignoreResult.getRepeatTimes(),
                content);
        log.info(msg);
    }

    @Override
    public boolean sendCleanText(String content, boolean atAll, AlarmActionObject actionObject) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        IgnoreResult ignoreResult = checkIgnoreResult(content, actionObject);
        if (ignoreResult.isIgnore()) {
            logIgnoreMsg(ignoreResult, content);
            return false;
        }

        return handlerAlarm(getClient(actionObject), ignoreResult, (OapiRobotSendRequest request) -> {
            request.setMsgtype("text");
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            if (ignoreResult.isNextTimeIgnore()) {
                text.setContent(getPrefix(actionObject) + getDuplicatedIgnoredPrefix() + content);
            } else {
                text.setContent(getPrefix(actionObject) + content);
            }
            request.setText(text);
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            if (Boolean.TRUE.equals(isAtActionIgnoredEnvByForce())) {
                at.setIsAtAll(atAll);
            } else {
                at.setIsAtAll(isPreOrProdEnv() && atAll);
            }
            request.setAt(at);
        });
    }

    @Override
    public boolean sendCleanText(String content, List<String> phoneList, AlarmActionObject actionObject) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        IgnoreResult ignoreResult = checkIgnoreResult(content, actionObject);
        if (ignoreResult.isIgnore()) {
            logIgnoreMsg(ignoreResult, content);
            return false;
        }
        return handlerAlarm(getClient(actionObject), ignoreResult, (OapiRobotSendRequest request) -> {
            request.setMsgtype("text");
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            if (ignoreResult.isNextTimeIgnore()) {
                text.setContent(getPrefix(actionObject) + getDuplicatedIgnoredPrefix() + content);
            } else {
                text.setContent(getPrefix(actionObject) + content);
            }
            request.setText(text);

            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setIsAtAll(false);
            if (!CollectionUtils.isEmpty(phoneList)) {
                if (Boolean.TRUE.equals(isAtActionIgnoredEnvByForce()) || isPreOrProdEnv()) {
                    at.setAtMobiles(phoneList);
                }
            }
            request.setAt(at);
        });
    }

    @Override
    public boolean sendLink(String title, String content, String picUrl, String linkUrl,
            AlarmActionObject actionObject) {
        IgnoreResult ignoreResult = checkIgnoreResult(content, actionObject);
        if (ignoreResult.isIgnore()) {
            logIgnoreMsg(ignoreResult, content);
            return false;
        }
        return handlerAlarm(getClient(actionObject), ignoreResult, (OapiRobotSendRequest request) -> {
            request.setMsgtype("link");
            OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();
            link.setMessageUrl(linkUrl);
            link.setPicUrl(picUrl);
            link.setTitle(getPrefix(actionObject) + title);
            if (ignoreResult.isNextTimeIgnore()) {
                link.setText(getDuplicatedIgnoredPrefix() + content);
            } else {
                link.setText(content);
            }
            request.setLink(link);
        });
    }

    @Override
    public boolean sendMarkdown(String title, String markdownText, AlarmActionObject actionObject) {
        IgnoreResult ignoreResult = checkIgnoreResult(markdownText, actionObject);
        if (ignoreResult.isIgnore()) {
            logIgnoreMsg(ignoreResult, markdownText);
            return false;
        }
        return handlerAlarm(getClient(actionObject), ignoreResult, (OapiRobotSendRequest request) -> {
            request.setMsgtype("markdown");
            OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
            markdown.setTitle(getPrefix(actionObject) + title);
            if (ignoreResult.isNextTimeIgnore()) {
                markdown.setText(getDuplicatedIgnoredPrefix() + markdownText);
            } else {
                markdown.setText(markdownText);
            }
            request.setMarkdown(markdown);
        });
    }

    /**
     * 获取配置中需要正向代理的环境信息
     *
     * @return
     */
    protected List<String> getNeedProxyEnv() {
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
     * 获取当前的profile
     *
     * @return
     */
    protected String getCurrentEnv() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Optional.of(activeProfiles).flatMap(a -> Arrays.stream(a).findFirst()).orElse(null);
    }

    /**
     * 是否是正式/预发环境
     *
     * @return
     */
    protected boolean isPreOrProdEnv() {
        String currentEnv = getCurrentEnv();
        if (currentEnv == null) {
            return false;
        }
        String env = currentEnv.toLowerCase();
        return "pre".equals(env) || "prod".equals(env);
    }

    /**
     * 处理通知发送
     *
     * @param client
     * @param ignoreResult
     * @param consumer
     * @return
     */
    private boolean handlerAlarm(DingTalkClient client, IgnoreResult ignoreResult,
            Consumer<OapiRobotSendRequest> consumer) {
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        consumer.accept(request);
        return sendAlarm(client, ignoreResult, request);
    }

    /**
     * 发送组装好的通知
     *
     * @param client
     * @param ignoreResult
     * @param request
     * @return
     */
    private boolean sendAlarm(DingTalkClient client, IgnoreResult ignoreResult, OapiRobotSendRequest request) {
        boolean success;
        try {
            OapiRobotSendResponse response = client.execute(request);
            success = response.isSuccess();
            if (!success) {
                log.info("sendDingAlarmFailed request:{}, response:{}", request, response);
            }
            // 这个是否成功 不影响success. 忽略报警功能失败就失败了
            if (ignoreDuplicated && ignoreResult != null && ignoreResult.getAlarmCacheDriver() != null) {
                ignoreResult.getAlarmCacheDriver()
                        .cacheIncrementTimes(ignoreResult.getCacheKey(), ignoreDuplicatedTimeSeconds, TimeUnit.SECONDS);
            }
        } catch (ApiException e) {
            log.error("sendDingAlarmError request:{}, error:{}", request, e);
            success = false;
        }
        return success;
    }

    private static class IgnoreResult {

        final static IgnoreResult NO_IGNORE = new IgnoreResult(false, false, 0, null, null);

        /**
         * 是否忽略本次报警
         */
        private final boolean ignore;
        /**
         * 是否下一次报警将会触发屏蔽
         */
        private final boolean nextTimeIgnore;
        /**
         * 指定时间范围内, 已经重复了多少次
         */
        private final int repeatTimes;

        /**
         * 用于缓存
         */
        private final AlarmCacheDriver alarmCacheDriver;

        /**
         * 缓存的key
         */
        private final String cacheKey;

        public IgnoreResult(boolean ignore, boolean nextTimeIgnore, int repeatTimes, AlarmCacheDriver alarmCacheDriver,
                String cacheKey) {
            this.ignore = ignore;
            this.nextTimeIgnore = nextTimeIgnore;
            this.repeatTimes = repeatTimes;
            this.alarmCacheDriver = alarmCacheDriver;
            this.cacheKey = cacheKey;
        }

        public boolean isIgnore() {
            return ignore;
        }

        public boolean isNextTimeIgnore() {
            return nextTimeIgnore;
        }

        public int getRepeatTimes() {
            return repeatTimes;
        }

        public AlarmCacheDriver getAlarmCacheDriver() {
            return alarmCacheDriver;
        }

        public String getCacheKey() {
            return cacheKey;
        }
    }
}