# IoT Room Monitoring Backend

Spring Boot backend cho giao diện IoT Room Monitoring. Cấu trúc và style code được tổ chức theo dự án mẫu `D:\iot_be\QLNT1`:

```text
com.iotroom
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── enums
├── exception
├── mapper
├── repository
├── service
└── util
```

## Công nghệ

- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Spring Security + JWT
- MySQL
- Eclipse Paho MQTT client
- Lombok
- Maven

## Chuẩn bị MySQL

Ứng dụng mặc định kết nối MySQL ở `localhost:3306` bằng tài khoản `root` và tự tạo database `iot_smart_room1` nếu tài khoản có quyền.

Script MySQL hoàn chỉnh nằm tại `src/main/resources/sql/iot_smart_room1.sql`.

Nếu MySQL có mật khẩu, copy file:

```text
secrets-local.example.properties -> secrets-local.properties
```

Sau đó sửa `DB_USERNAME`, `DB_PASSWORD` và `JWT_SECRET` trong `secrets-local.properties`.

## Chạy backend

```bash
./mvnw spring-boot:run
```

Trên Windows dùng `mvnw.cmd spring-boot:run`.

Backend chạy tại `http://localhost:8080`.

Tài khoản được tạo tự động ở lần chạy đầu:

```text
username: admin
password: 123456
```

Ba cảm biến và ba LED được khởi tạo tự động. Dữ liệu cảm biến và lịch sử thao tác mẫu chỉ được khởi tạo khi `DEMO_DATA_ENABLED=true`; mặc định là `false` để chờ dữ liệu từ ESP8266. Các bản ghi đã có sẵn trong database không bị xóa khi tắt chế độ demo.

## Kết nối MQTT

Backend subscribe `sensor_data` và `device_feedback`, đồng thời publish lệnh xuống `device_control`.

Thêm vào `secrets-local.properties`:

```properties
MQTT_ENABLED=true
MQTT_BROKER_URL=tcp://localhost:1885
MQTT_CLIENT_ID=iot-room-backend
MQTT_USERNAME=NguyenVanHien
MQTT_PASSWORD=your_mqtt_password
MQTT_SENSOR_TOPIC=sensor_data
MQTT_CONTROL_TOPIC=device_control
MQTT_FEEDBACK_TOPIC=device_feedback
MQTT_QOS=1
MQTT_COMMAND_TIMEOUT_MS=5000
```

Mỗi payload `{temp, humid, light}` được chuyển thành ba bản ghi trong `data_sensors`. Lệnh điều khiển chỉ được đánh dấu `SUCCESS` sau khi ESP8266 gửi ACK; mất kết nối hoặc quá 5 giây sẽ thành `FAILED`.

## Kết nối frontend

Tạo `.env` ở thư mục frontend:

```env
VITE_API_URL=http://localhost:8080
VITE_USE_MOCK=false
```

Sau đó chạy frontend bằng `npm run dev`.

## API

Swagger UI được mở tại:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON: `http://localhost:8080/v3/api-docs`.

Để test API có bảo vệ: chạy `POST /api/auth/login`, copy `result.token`, bấm **Authorize** và dán token vào ô `bearerAuth` (không cần tự thêm chữ `Bearer`). Swagger sẽ giữ token khi tải lại trang.

- `POST /api/auth/login`
- `GET /api/sensors`
- `GET /api/sensors/latest`
- `GET /api/data-sensors`
- `POST /api/data-sensors`
- `GET /api/data-sensors/chart`
- `GET /api/devices`
- `POST /api/devices/{id}/control`
- `GET /api/actions`
- `GET /api/profile`
- `PUT /api/profile`

Các API ngoài đăng nhập yêu cầu header:

```text
Authorization: Bearer <token>
```

## Bộ lọc ngày

Hai endpoint lịch sử hỗ trợ các tham số `from` và `to` theo ISO DateTime:

```text
GET /api/data-sensors?from=2026-08-26T00:00:00&to=2026-08-26T23:59:59.999
GET /api/actions?from=2026-08-26T00:00:00&to=2026-08-26T23:59:59.999
```

## Dữ liệu cảm biến mô phỏng

Mặc định bộ mô phỏng tự động đang tắt. Có thể bật trong `secrets-local.properties`:

```properties
SENSOR_SIMULATION_ENABLED=true
SENSOR_SIMULATION_INTERVAL_MS=5000
```
