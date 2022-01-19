##### 接入步骤：
###### 代码模块:
如果选择引入jar包到自己项目:
1. install 或者 deploy 本项目
2. 引入依赖   
     ```xml
         <dependency>
            <groupId>org.crazykid</groupId>
            <artifactId>dingdinghelper</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
     ```
如果选择直接复制文件到自己项目:
1. 拷贝DefaultProxyDingTalkClient,DingConfig,DingAlarmService,DefaultDingAlarmServiceImpl四个文件即可

###### 配置模块:
1. 配置application配置项   
    ```xml
    ding:
      # 必填. 钉钉机器人地址
      webhook: https://oapi.dingtalk.com/robot/send?access_token=123
      # 钉钉机器人配置时,填写的自定义关键词.如果配置机器人时填写了,这里必填
      prefix: 支付网关
      # 非必填. 正向代理的ip 和端口, 逗号分隔. 例如 proxyIpList: "host1:888888"
      proxyIpList: "host1:123,host2:456"
      # 非必填. 需要走代理的环境信息, 逗号分隔. 例如 proxyEnvList: "prod"
      proxyEnvList: "pre,prod"
    ``` 
![钉钉机器人配置时填写的自定义关键词](https://oss.getjing.cn/document/20220119/ding_prefix.png)

##### 使用：
使用方法封装了三个较常用的
1. 发送纯文本 DingAlarmService#sendCleanText
2. 发送带连接的通知 DingAlarmService#sendLink
3. 发送markdown格式通知 DingAlarmService#sendMarkdown