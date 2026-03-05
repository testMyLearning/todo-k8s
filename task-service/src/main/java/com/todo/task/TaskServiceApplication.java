package com.todo.task;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;


@SpringBootApplication(scanBasePackages = {
        "com.todo.task",  // сканирует твой код
        "com.todo.common"    // сканирует код из common модуля!
})
@EnableScheduling
public class TaskServiceApplication {
    public static void main(String[] args) {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        SpringApplication application = new SpringApplication(TaskServiceApplication.class);
        application.setLogStartupInfo(false);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }
}