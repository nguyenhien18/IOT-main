package com.iotroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IoTRoomMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(IoTRoomMonitoringApplication.class, args);
    }
}

