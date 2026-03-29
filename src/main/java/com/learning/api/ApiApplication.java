package com.learning.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// 🔥 改用 excludeName！直接輸入字串，就不會報錯說找不到 package 了！
@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})

//更新過期預約狀態
@EnableScheduling
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

}