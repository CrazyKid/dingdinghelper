package com.crazykid.utils;

/**
 * @author arthur
 * @date 2024/12/23 17:55
 */
public class AlarmConstants {
    /**
     * 报警机器人在spring容器中,bean对象前缀
     */
    public static final String DING_TALK_CLIENT_BEAN_PREFIX = "ding_alarm_";

    /**
     * 报警机器人在spring容器中, 使用正向代理的bean对象后缀
     */
    public static final String DING_TALK_CLIENT_BEAN_USER_PROXY_SUFFIX = "_proxy";

    private AlarmConstants() {
    }
}
