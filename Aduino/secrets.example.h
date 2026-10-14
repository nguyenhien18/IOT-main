#pragma once

// Copy this file to secrets.h. Use the Wi-Fi available to the ESP8266.
#define WIFI_SSID "your-wifi-name"
#define WIFI_PASSWORD "your-wifi-password"

// IPv4 address of the computer running Mosquitto, not localhost.
#define MQTT_HOST "192.168.1.100"
#define MQTT_PORT 1885

// Must match the Mosquitto account configured in Backend/secrets-local.properties.
#define MQTT_USERNAME "your-mqtt-user"
#define MQTT_PASSWORD "your-mqtt-password"
