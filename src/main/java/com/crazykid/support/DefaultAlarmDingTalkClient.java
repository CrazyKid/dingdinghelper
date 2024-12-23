package com.crazykid.support;

import com.dingtalk.api.DefaultDingTalkClient;

/**
 * @author arthur
 * @date 2024/12/23 17:51
 */
public class DefaultAlarmDingTalkClient extends DefaultDingTalkClient {

    public DefaultAlarmDingTalkClient() {
        super(null);
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}