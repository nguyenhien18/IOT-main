package com.iotroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.iotroom.repository.ActionHistoryRepository;
import com.iotroom.repository.SensorDataRepository;
import com.iotroom.service.SensorService;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:iot_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "jwt.secret=unit-test-signing-key-must-be-at-least-thirty-two-bytes",
        "app.sensor-simulation.enabled=false",
        "app.demo-data.enabled=false",
        "app.mqtt.enabled=false"
})
class IoTRoomMonitoringApplicationTests {
    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private ActionHistoryRepository actionHistoryRepository;

    @Autowired
    private SensorService sensorService;

    @Test
    void contextLoads() {
        assertEquals(0, sensorDataRepository.count());
        assertEquals(0, actionHistoryRepository.count());
        assertNull(sensorService.getLatest().temperature());
        assertNull(sensorService.getLatest().time());
    }
}
