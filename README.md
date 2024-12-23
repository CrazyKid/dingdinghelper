
# version 1.0.0
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

# version 2.0.0
该版本基于1.0.0的基础上, 支持多报警机器人配置, 对于不同的报警对象, 使用不同的报警机器人;
对于同一个业务类型的重复报警, 短时间内做了一个次数的限制,避免造成骚扰
对于短时间内的业务重复次数计数, 通过让业务方实现`AlarmCacheDriver`自行提供缓存的工具
要开启忽略重复报警功能, 请在配置文件中设置 **ignoreDuplicated=true** 以及实现 **AlarmCacheDriver**该接口

## API功能列表
- 发送钉钉报警 : DingAlarmService 

## 核心依赖组件列表

- alibaba-dingtalk-service-sdk
- spring-boot-starter
## Spring boot 项目中的配置

- 配置类 : DingProperties, DingMultiProperties(二者只会同时生效一个, 都配置默认生效的是后者, 且当前者生效的时候, 相当于1.0.0 版本)
  DingMultiProperties 示例
```
ding-multi:
  # 必填. 至少一组报警对象的配置
  alarmList:
    # 必填. 钉钉机器人地址
    - webhook: "https://oapi.dingtalk.com/robot/send?access_token=123"
      # 钉钉机器人配置时,填写的自定义关键词.如果配置机器人时填写了,这里必填
      prefix: "客服通知"
      # 必填. 报警对象
      actionObject: 2
    - webhook: "https://oapi.dingtalk.com/robot/send?access_token=456"
      prefix: "支付"
      actionObject: 1
  # 必填, 开启多报警对象配置
  enable: true
  # 非必填. 如果希望用自己写的类 去解析报警对象, 需要把类的全路径名,填写到这个属性里
  actionObjectClassName:
  # 非必填. 默认艾特的手机号列表
  defaultAtPhoneList:
  #非必填. 正向代理的ip 和端口, 逗号分隔. 例如 proxyIpList: "host1:888888"
  proxyIpList:
  # 非必填. 需要走代理的环境信息, 逗号分隔. 例如 proxyEnvList: "prod"
  proxyEnvList:
  # 是否开启忽略短期内的相同报警信息
  ignore-duplicated: true
  # 相同的信息 在一定的时间内重复 会被忽略
  ignore-duplicated-time-seconds: 300
  # 相同的信息重复几次后会被忽略, 举个例子, 当值为2时,第三次报警相同信息会被忽略
  ignore-duplicated-repeat-times: 1
```