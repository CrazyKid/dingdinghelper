##### 接入步骤：
1. 引入依赖   
     ```xml
         <dependency>
            <groupId>com.aliyun</groupId>
            <artifactId>alibaba-dingtalk-service-sdk</artifactId>
            <version>1.0.1</version>
        </dependency>
     ```
2. 配置webhook 和 正向代理（非必填，不填时使用默认发送）   
    ```xml
    ding:
        webhook: 钉钉机器人复制的webhook
        proxyIpList: "host1:port1,host2:port2"
        #proxyIpList: "host1:port1"
    ``` 
3. 更改DingNotifyManager#DINGDING_PREFIX, 把前缀改成钉钉机器人配置的自定义关键词
    ![钉钉机器人的配置](https://img-blog.csdnimg.cn/7f15ac859e3d4e8897f214870098b3ef.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAYWx3YXlzX25vb2I=,size_20,color_FFFFFF,t_70,g_se,x_16)



##### 使用：
使用方法封装了三个较常用的
1. 发送纯文本 DingNotifyManager#sendCleanText
2. 发送带连接的通知 DingNotifyManager#sendLink
3. 发送markdown格式通知 DingNotifyManager#sendMarkdown