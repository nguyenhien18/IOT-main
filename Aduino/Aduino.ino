#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>
#include <BH1750.h>
#include <Wire.h>
#include <ArduinoJson.h>
#include "secrets.h"


// 1. KHAI BAO PHAN CUNG


#define DHT_PIN D4
#define DHT_TYPE DHT11

#define SDA_PIN D2
#define SCL_PIN D1

#define LED1 D5
#define LED2 D6
#define LED3 D7


// 2. WIFI VA MQTT
// Ten Wi-Fi, mat khau, dia chi MQTT va tai khoan MQTT
// duoc dat trong secrets.h de khong dua mat khau len Git.


// 3. TOPIC MQTT


#define SENSOR_TOPIC   "sensor_data"
#define CONTROL_TOPIC  "device_control"
#define FEEDBACK_TOPIC "device_feedback"


// 4. DOI TUONG


DHT dht(DHT_PIN, DHT_TYPE);
BH1750 lightMeter;

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

// Trang thai hien tai cua 3 LED.
bool led1On = false;
bool led2On = false;
bool led3On = false;

// Moc thoi gian gui du lieu cam bien gan nhat.
unsigned long lastSensorTime = 0;
const unsigned long SENSOR_INTERVAL_MS = 2000;


// CAP NHAT 3 LED NGOAI MACH THEO 3 BIEN TRANG THAI


void updatePhysicalLeds() {
  digitalWrite(LED1, led1On ? HIGH : LOW);
  digitalWrite(LED2, led2On ? HIGH : LOW);
  digitalWrite(LED3, led3On ? HIGH : LOW);
}


// DOI TRANG THAI MOT LED THEO ID TRONG DATABASE
// deviceId 1 -> LED1, 2 -> LED2, 3 -> LED3.


bool setLedByDeviceId(int deviceId, bool turnOn) {
  if (deviceId == 1) {
    led1On = turnOn;
  } else if (deviceId == 2) {
    led2On = turnOn;
  } else if (deviceId == 3) {
    led3On = turnOn;
  } else {
    return false;
  }

  updatePhysicalLeds();
  return true;
}


// GUI TRANG THAI CA 3 LED VE BACKEND
// Ham nay duoc dung sau khi ESP ket noi MQTT va khi nhan lenh kieu cu.


void publishDeviceSnapshot() {
  StaticJsonDocument<256> doc;
  JsonArray devices = doc.createNestedArray("devices");

  JsonObject led1 = devices.createNestedObject();
  led1["deviceId"] = 1;
  led1["status"] = led1On ? "ON" : "OFF";

  JsonObject led2 = devices.createNestedObject();
  led2["deviceId"] = 2;
  led2["status"] = led2On ? "ON" : "OFF";

  JsonObject led3 = devices.createNestedObject();
  led3["deviceId"] = 3;
  led3["status"] = led3On ? "ON" : "OFF";

  char feedback[256];
  serializeJson(doc, feedback, sizeof(feedback));
  mqttClient.publish(FEEDBACK_TOPIC, feedback);

  Serial.print("Feedback trang thai: ");
  Serial.println(feedback);
}


// GUI KET QUA CUA MOT LENH DO BACKEND GUI XUONG


void publishCommandFeedback(long actionId, int deviceId, bool success) {
  StaticJsonDocument<192> doc;
  doc["actionId"] = actionId;
  doc["deviceId"] = deviceId;
  doc["status"] = success ? "SUCCESS" : "FAILED";

  if (deviceId == 1) {
    doc["deviceStatus"] = led1On ? "ON" : "OFF";
  } else if (deviceId == 2) {
    doc["deviceStatus"] = led2On ? "ON" : "OFF";
  } else if (deviceId == 3) {
    doc["deviceStatus"] = led3On ? "ON" : "OFF";
  }

  char feedback[192];
  serializeJson(doc, feedback, sizeof(feedback));
  mqttClient.publish(FEEDBACK_TOPIC, feedback);

  Serial.print("Feedback lenh: ");
  Serial.println(feedback);
}


// HAM NAY CHAY KHI ESP NHAN LENH TU TOPIC device_control


void mqttCallback(char* topic, byte* payload, unsigned int length) {
  if (strcmp(topic, CONTROL_TOPIC) != 0) {
    return;
  }

  // Chuyen noi dung MQTT thanh String de de quan sat tren Serial Monitor.
  String message = "";
  for (unsigned int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  message.trim();

  Serial.print("Nhan lenh: ");
  Serial.println(message);

  // Ho tro 2 lenh cu: "on all" va "off all".
  String simpleCommand = message;
  simpleCommand.toLowerCase();

  if (simpleCommand == "on all") {
    led1On = true;
    led2On = true;
    led3On = true;
    updatePhysicalLeds();
    publishDeviceSnapshot();
    return;
  }

  if (simpleCommand == "off all") {
    led1On = false;
    led2On = false;
    led3On = false;
    updatePhysicalLeds();
    publishDeviceSnapshot();
    return;
  }

  // Cac lenh con lai phai la JSON.
  StaticJsonDocument<256> doc;
  DeserializationError error = deserializeJson(doc, message);
  if (error) {
    Serial.print("JSON loi: ");
    Serial.println(error.c_str());
    return;
  }

  // Dinh dang hien tai cua backend:
  // {"actionId":123,"deviceId":2,"action":"ON"}
  if (doc.containsKey("actionId") &&
      doc.containsKey("deviceId") &&
      doc.containsKey("action")) {
    long actionId = doc["actionId"] | 0L;
    int deviceId = doc["deviceId"] | 0;
    String action = doc["action"] | "";
    action.toUpperCase();

    bool validAction = action == "ON" || action == "OFF";
    bool success = false;

    if (actionId > 0 && validAction) {
      success = setLedByDeviceId(deviceId, action == "ON");
    }

    publishCommandFeedback(actionId, deviceId, success);
    return;
  }

  // Van ho tro dinh dang cua code cu:
  // {"led1":"on"}, {"led2":"off"}, ...
  bool hasLegacyCommand = false;

  if (doc.containsKey("led1")) {
    String state = doc["led1"] | "";
    state.toLowerCase();
    if (state == "on" || state == "off") {
      led1On = state == "on";
      hasLegacyCommand = true;
    }
  }

  if (doc.containsKey("led2")) {
    String state = doc["led2"] | "";
    state.toLowerCase();
    if (state == "on" || state == "off") {
      led2On = state == "on";
      hasLegacyCommand = true;
    }
  }

  if (doc.containsKey("led3")) {
    String state = doc["led3"] | "";
    state.toLowerCase();
    if (state == "on" || state == "off") {
      led3On = state == "on";
      hasLegacyCommand = true;
    }
  }

  if (hasLegacyCommand) {
    updatePhysicalLeds();
    publishDeviceSnapshot();
  } else {
    Serial.println("Lenh khong dung dinh dang");
  }
}


// KET NOI WIFI


void connectWifi() {
  if (WiFi.status() == WL_CONNECTED) {
    return;
  }

  Serial.print("Dang ket noi WiFi");
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println();
  Serial.println("WiFi OK");
  Serial.print("IP ESP: ");
  Serial.println(WiFi.localIP());
}


// KET NOI MQTT VA DANG KY NHAN LENH DIEU KHIEN


bool connectMqtt() {
  if (mqttClient.connected()) {
    return true;
  }

  Serial.println("Dang ket noi MQTT...");
  String clientId = "ESP8266-" + String(ESP.getChipId(), HEX);

  if (!mqttClient.connect(clientId.c_str(), MQTT_USERNAME, MQTT_PASSWORD)) {
    Serial.print("MQTT loi: ");
    Serial.println(mqttClient.state());
    return false;
  }

  Serial.println("MQTT OK");

  if (!mqttClient.subscribe(CONTROL_TOPIC)) {
    Serial.println("Subscribe device_control that bai");
    mqttClient.disconnect();
    return false;
  }

  Serial.println("Subscribe device_control");
  publishDeviceSnapshot();
  return true;
}


// DOC VA GUI DU LIEU CAM BIEN


void publishSensorData() {
  float temp = dht.readTemperature();
  float humid = dht.readHumidity();
  float light = lightMeter.readLightLevel();

  if (isnan(temp) || isnan(humid) || light < 0) {
    Serial.println("Loi doc sensor");
    return;
  }

  // Du lieu gui len co dang:
  // {"temp":29.5,"humid":68.0,"light":520.0}
  String data = "{";
  data += "\"temp\":" + String(temp, 1);
  data += ",\"humid\":" + String(humid, 1);
  data += ",\"light\":" + String(light, 1);
  data += "}";

  if (mqttClient.publish(SENSOR_TOPIC, data.c_str())) {
    Serial.print("Sensor: ");
    Serial.println(data);
  } else {
    Serial.println("Gui sensor_data that bai");
  }
}


// SETUP - CHAY MOT LAN KHI ESP KHOI DONG


void setup() {
  Serial.begin(115200);

  pinMode(LED1, OUTPUT);
  pinMode(LED2, OUTPUT);
  pinMode(LED3, OUTPUT);
  updatePhysicalLeds();

  dht.begin();
  Wire.begin(SDA_PIN, SCL_PIN);
  lightMeter.begin(BH1750::CONTINUOUS_HIGH_RES_MODE);

  connectWifi();

  mqttClient.setServer(MQTT_HOST, MQTT_PORT);
  mqttClient.setBufferSize(256);
  mqttClient.setCallback(mqttCallback);
}


// LOOP - CHAY LAP LIEN TUC


void loop() {
  connectWifi();

  if (!connectMqtt()) {
    delay(1000);
    return;
  }

  // Phai goi lien tuc de ESP nhan lenh MQTT.
  mqttClient.loop();

  // Cu 2 giay doc DHT11 + BH1750 va gui len sensor_data.
  if (millis() - lastSensorTime >= SENSOR_INTERVAL_MS) {
    lastSensorTime = millis();
    publishSensorData();
  }
}
