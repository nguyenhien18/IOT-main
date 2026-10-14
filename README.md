# IoT Room Monitoring - End-to-End

Hệ thống giám sát phòng bằng ESP8266, DHT11, BH1750 và điều khiển 3 LED từ giao diện React.

## Luồng hoạt động

```text
DHT11/BH1750 -> ESP8266 -> MQTT sensor_data -> Spring Boot -> MySQL
                                                        -> React (poll REST API)

React -> POST /api/devices/{id}/control -> Spring Boot
      -> MQTT device_control -> ESP8266 -> GPIO LED
      -> MQTT device_feedback -> Spring Boot -> MySQL -> React
```

Backend chỉ cập nhật lệnh thành `SUCCESS` sau khi nhận đúng `actionId` và `deviceId` từ ESP8266. Nếu không có phản hồi trong 5 giây, API trả lỗi timeout và lịch sử được lưu là `FAILED`.

## Thành phần

- `Aduino/`: firmware NodeMCU ESP8266.
- `Backend/`: backend chính, Java 21 + Spring Boot + MySQL + MQTT.
- `iot-fe-ui-frame/`: React/Vite; cấu hình mẫu dùng backend thật.

## 1. MQTT Mosquitto

Tạo tài khoản MQTT trong file mật khẩu nằm ngoài repository. Lệnh `-c` dùng khi tạo file mới; khi thêm tài khoản vào file đã tồn tại, bỏ `-c` để tránh ghi đè:

```powershell
& "C:\Program Files\mosquitto\mosquitto_passwd.exe" -c "C:\Users\Public\mosquitto_passwd.txt" "your_mqtt_username"
```

`mosquitto/mosquitto.conf` trỏ tới file mật khẩu trên. Nếu chọn đường dẫn khác, sửa dòng `password_file` cho khớp. Sau đó chạy broker từ thư mục gốc dự án:

```powershell
& "C:\Program Files\mosquitto\mosquitto.exe" -v -c ".\mosquitto\mosquitto.conf"
```

Nếu ESP8266 không kết nối được, cho phép TCP port `1885` trong Windows Firewall và kiểm tra ESP8266/laptop đang cùng mạng Wi-Fi.

## 2. Backend

Copy `Backend/secrets-local.example.properties` thành `Backend/secrets-local.properties`, sau đó điền MySQL và MQTT. File chứa thông tin thật này được Git bỏ qua:

```properties
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=change-this-secret-to-at-least-32-characters-long

MQTT_ENABLED=true
MQTT_BROKER_URL=tcp://localhost:1885
MQTT_USERNAME=your_mqtt_username
MQTT_PASSWORD=the_same_mqtt_password
```

Chạy backend:

```powershell
cd Backend
.\mvnw.cmd spring-boot:run
```

Backend chạy tại `http://localhost:8080`. Lần đầu sẽ tạo database, tài khoản quản trị, 3 sensor và 3 LED. Dữ liệu số đo và lịch sử thao tác mẫu không còn được tạo mặc định; muốn bật cho demo thì đặt `DEMO_DATA_ENABLED=true`. Dữ liệu mẫu đã tồn tại trong MySQL từ lần chạy cũ sẽ không tự bị xóa.

Swagger UI để test API: `http://localhost:8080/swagger-ui.html`. Đăng nhập bằng endpoint `/api/auth/login`, copy `result.token`, sau đó bấm **Authorize** và dán token (không cần thêm chữ `Bearer`). OpenAPI JSON nằm tại `http://localhost:8080/v3/api-docs`.

## 3. Frontend

Copy `iot-fe-ui-frame/.env.example` thành `iot-fe-ui-frame/.env` để dùng API thật. File `.env` được Git bỏ qua:

```env
VITE_API_URL=http://localhost:8080
VITE_USE_MOCK=false
```

Chạy:

```powershell
cd iot-fe-ui-frame
npm install
npm run dev
```

Mở `http://localhost:5173` và đăng nhập bằng tài khoản backend (`admin` / `123456` khi database mới khởi tạo). Form frontend có thể đang điền sẵn tài khoản mock; hãy thay bằng `admin`. Đổi mật khẩu mặc định trước khi dùng ngoài môi trường demo.

## 4. ESP8266

Copy `Aduino/secrets.example.h` thành `Aduino/secrets.h`; file chứa thông tin thật này được Git bỏ qua. Sau đó sửa:

- `WIFI_SSID`, `WIFI_PASSWORD`.
- `MQTT_HOST`: IPv4 LAN của laptop, không dùng `localhost`.
- `MQTT_PORT`: `1885`, giống `mosquitto/mosquitto.conf` và backend.
- `MQTT_USERNAME`, `MQTT_PASSWORD`: giống Mosquitto và backend.

Sau đó mở `Aduino/Aduino.ino` và upload với board `NodeMCU 1.0 (ESP-12E Module)`. File sketch đã được đổi tên từ `1sketch_sep23a.ino` để khớp tên thư mục Arduino IDE. Sketch dùng thư viện ESP8266WiFi, PubSubClient, DHT, BH1750 và ArduinoJson. DHT11 nối D4; BH1750 dùng SDA D2, SCL D1; LED 1/2/3 lần lượt ở D5/D6/D7. Chân LED hiện được điều khiển mức HIGH để bật.

Trước khi upload, bật hotspot/Wi-Fi sẽ dùng thật rồi kiểm tra IPv4 máy chạy Mosquitto bằng `ipconfig`; đặt `MQTT_HOST` trong `Aduino/secrets.h` theo địa chỉ đó.

## MQTT contract

Telemetry từ ESP8266:

```json
{"temp":28.0,"humid":70.0,"light":850.0}
```

Lệnh từ backend:

```json
{"actionId":123,"deviceId":2,"action":"ON"}
```

Phản hồi từ ESP8266:

```json
{"actionId":123,"deviceId":2,"status":"SUCCESS","deviceStatus":"ON"}
```

Khi ESP8266 reconnect, nó gửi snapshot của cả 3 LED để database đồng bộ lại với trạng thái vật lý. ESP8266 gửi số đo lên `sensor_data` mỗi 2 giây; frontend hỏi API số đo mới nhất mỗi 2 giây và biểu đồ mỗi 5 giây.

## Kiểm thử

```powershell
cd Backend
.\mvnw.cmd test

cd iot-fe-ui-frame
npm run build
```

Chọn board `NodeMCU 1.0 (ESP-12E Module)`, cổng COM và upload lên ESP8266 bằng Arduino IDE. Sau khi upload, kiểm tra Serial Monitor 115200 thấy Wi-Fi, MQTT và các gói `Sensor data`. Nếu giao diện vẫn hiện số đo cũ, hãy kiểm tra thời gian của bản ghi mới nhất trong API; frontend không tự tạo dữ liệu mẫu khi `VITE_USE_MOCK=false`.
Dashboard hiện dấu `--` và cảnh báo khi số đo mới nhất đã quá 15 giây. Dữ liệu cũ vẫn xem được trong biểu đồ và bảng lịch sử.
