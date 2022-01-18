package com.crazykid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author arthur
 * @date 2022/1/18 11:18 下午
 */
@SpringBootApplication
public class Dingdinghelper {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Dingdinghelper.class);
        application.run(args);
    }

}
