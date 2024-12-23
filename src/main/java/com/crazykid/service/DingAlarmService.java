package com.crazykid.service;

import java.util.List;

/**
 * 钉钉消息发送封装
 * <a href="https://ding-doc.dingtalk.com/doc#/serverapi2/qf2nxq/d535db33">自定义机器人webhook</a>
 *
 * @author arthur
 * @date 2022/1/19 3:24 下午
 */
public interface DingAlarmService {

    /**
     * 发送纯文本,
     *
     * @param content 纯文本内容
     * @param atAll   是否@所有人
     * @return 是否发送成功
     */
    boolean sendCleanText(String content, boolean atAll);

    /**
     * 按照手机号去艾特人 通知纯文本信息
     *
     * @param content 纯文本内容
     * @param phoneList 要通知的人的手机号, 如果是null 或者empty, 只会有通知,不会at人
     * @return
     */
    boolean sendCleanText(String content, List<String> phoneList);

    /**
     * 发送带连接的通知
     *
     * @param title   通知标题
     * @param content 介绍文案
     * @param picUrl  图片跳转
     * @param linkUrl 卡片跳转
     * @return 是否发送成功
     */
    boolean sendLink(String title, String content, String picUrl, String linkUrl);

    /**
     * 发送markdown格式通知
     *
     * @param title        显示在对话列表,但不显示在具体窗口中的文案
     * @param markdownText 显示在具体窗口中的文案,markdown语法支持 {@code #}, {@code >}, {@code ****}, {@code **},<br/>
     *                     {@code [text](http://www.baidu.com)}, {@code ![image](http://name.com/pic.jpg)}, {@code -}, {@code 1.}
     * @return 是否发送成功
     */
    boolean sendMarkdown(String title, String markdownText);
}

