package com.sphereon.ms.mshealthmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsHealthMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsHealthMonitorApplication.class, args);
    }

}
