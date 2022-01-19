package com.crazykid.controller;

import com.crazykid.service.DingAlarmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author arthur
 * @date 2022/1/18 11:11 下午
 */
@RestController
public class TestController {

    @Resource
    private DingAlarmService dingAlarmService;

    @GetMapping("/health")
    public String healthy() {
        return "successful";
    }

    @GetMapping("/notify")
    public String config() {
        // 请查看readme.md, 完成配置后在进行调用
        dingAlarmService.sendCleanText("alarm", true);
        return "invoked";
    }

}
