package com.iotroom.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.iotroom.entity.ActionHistory;
import com.iotroom.entity.Device;
import com.iotroom.entity.Sensor;
import com.iotroom.entity.SensorData;
import com.iotroom.entity.UserAccount;
import com.iotroom.repository.ActionHistoryRepository;
import com.iotroom.repository.DeviceRepository;
import com.iotroom.repository.SensorDataRepository;
import com.iotroom.repository.SensorRepository;
import com.iotroom.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserAccountRepository userAccountRepository;
    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final DeviceRepository deviceRepository;
    private final ActionHistoryRepository actionHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo-data.enabled:false}")
    private boolean demoDataEnabled;

    @Override
    public void run(String... args) {
        UserAccount admin = initializeAdmin();
        List<Sensor> sensors = initializeSensors();
        List<Device> devices = initializeDevices();
        if (demoDataEnabled) {
            initializeSensorData(sensors);
            initializeActionHistory(devices, admin);
        }
    }

    private UserAccount initializeAdmin() {
        return userAccountRepository.findByUsername("admin").orElseGet(() -> {
            UserAccount admin = new UserAccount();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(UserAccount.Role.ADMIN);
            admin.setFullName("Nguyễn Văn A");
            admin.setStudentCode("B23DCCN001");
            admin.setClassName("D23CQCN01-B");
            admin.setEmail("student@example.com");
            admin.setGithubUrl("https://github.com/");
            admin.setFigmaUrl("https://www.figma.com/");
            admin.setPostmanUrl("https://www.postman.com/");
            admin.setReportUrl("");
            admin.setAvatarUrl("");
            return userAccountRepository.save(admin);
        });
    }

    private List<Sensor> initializeSensors() {
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(getOrCreateSensor("TEMPERATURE"));
        sensors.add(getOrCreateSensor("HUMIDITY"));
        sensors.add(getOrCreateSensor("LIGHT"));
        return sensors;
    }

    private Sensor getOrCreateSensor(String name) {
        return sensorRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Sensor sensor = new Sensor();
            sensor.setName(name);
            sensor.setCreatedAt(LocalDateTime.now());
            return sensorRepository.save(sensor);
        });
    }

    private void initializeSensorData(List<Sensor> sensors) {
        if (sensorDataRepository.count() > 0 || sensors.size() < 3) {
            return;
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<SensorData> rows = new ArrayList<>();
        for (int index = 0; index < 24; index++) {
            LocalDateTime createdAt = now.minusMinutes((23L - index) * 2);
            rows.add(sensorData(sensors.get(0), 29.5 + (index % 5) * 0.2, createdAt));
            rows.add(sensorData(sensors.get(1), 68.0 + (index % 6), createdAt));
            rows.add(sensorData(sensors.get(2), 520.0 + (index % 8) * 6, createdAt));
        }
        sensorDataRepository.saveAll(rows);
    }

    private SensorData sensorData(Sensor sensor, double value, LocalDateTime createdAt) {
        SensorData sensorData = new SensorData();
        sensorData.setSensor(sensor);
        sensorData.setValue(Math.round(value * 10.0) / 10.0);
        sensorData.setCreatedAt(createdAt);
        return sensorData;
    }

    private List<Device> initializeDevices() {
        List<Device> devices = new ArrayList<>();
        devices.add(getOrCreateDevice("LED 1", Device.Status.OFF));
        devices.add(getOrCreateDevice("LED 2", Device.Status.OFF));
        devices.add(getOrCreateDevice("LED 3", Device.Status.OFF));
        return devices;
    }

    private Device getOrCreateDevice(String name, Device.Status status) {
        return deviceRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Device device = new Device();
            device.setName(name);
            device.setStatus(status);
            device.setCreatedAt(LocalDateTime.now());
            return deviceRepository.save(device);
        });
    }

    private void initializeActionHistory(List<Device> devices, UserAccount user) {
        if (actionHistoryRepository.count() > 0 || devices.size() < 2) {
            return;
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<ActionHistory> rows = List.of(
                actionHistory(devices.get(0), user, ActionHistory.Action.ON, ActionHistory.Status.SUCCESS, now.minusMinutes(20)),
                actionHistory(devices.get(0), user, ActionHistory.Action.OFF, ActionHistory.Status.SUCCESS, now.minusMinutes(16)),
                actionHistory(devices.get(1), user, ActionHistory.Action.ON, ActionHistory.Status.SUCCESS, now.minusMinutes(12)),
                actionHistory(devices.get(1), user, ActionHistory.Action.OFF, ActionHistory.Status.FAILED, now.minusMinutes(8)),
                actionHistory(devices.get(1), user, ActionHistory.Action.ON, ActionHistory.Status.SUCCESS, now.minusMinutes(4))
        );
        actionHistoryRepository.saveAll(rows);
    }

    private ActionHistory actionHistory(
            Device device,
            UserAccount user,
            ActionHistory.Action action,
            ActionHistory.Status status,
            LocalDateTime createdAt
    ) {
        ActionHistory history = new ActionHistory();
        history.setDevice(device);
        history.setUser(user);
        history.setAction(action);
        history.setStatus(status);
        history.setCreatedAt(createdAt);
        return history;
    }
}
