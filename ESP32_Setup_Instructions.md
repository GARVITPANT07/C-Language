# ESP32 Setup Instructions for JARVIS

## Hardware Requirements
- ESP32 Development Board
- Relay Module (for controlling lights)
- Jumper wires
- Light bulb/LED for testing

## Wiring
```
ESP32 Pin 26 → Relay IN1 (Light 1)
ESP32 Pin 27 → Relay IN2 (Light 2)
ESP32 GND → Relay GND
ESP32 5V → Relay VCC
Relay1 COM → Light1 Positive
Relay1 NO → Power Supply Positive
Relay2 COM → Light2 Positive
Relay2 NO → Power Supply Positive
```

## ESP32 Code (Arduino IDE)

```cpp
#include <WiFi.h>
#include <WebServer.h>
#include "BluetoothSerial.h"

BluetoothSerial SerialBT;
WebServer server(80);

// WiFi credentials
const char* ssid = "J.A.R.V.I.S.";
const char* password = "12345678";

// Relay pins (SAFE)
#define LIGHT1_PIN 26
#define LIGHT2_PIN 27

void processCommand(String cmd);

void setup() {
  Serial.begin(115200);

  pinMode(LIGHT1_PIN, OUTPUT);
  pinMode(LIGHT2_PIN, OUTPUT);

  // Relays OFF (LOW-trigger)
  digitalWrite(LIGHT1_PIN, HIGH);
  digitalWrite(LIGHT2_PIN, HIGH);

  // Bluetooth
  SerialBT.begin("JARVIS");
  Serial.println("Bluetooth: JARVIS ready");

  // WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi connected");
  Serial.println(WiFi.localIP());

  // HTTP endpoint
  server.on("/command", HTTP_POST, []() {
    String body = server.arg("plain");

    int i = body.indexOf("LIGHT");
    if (i >= 0) {
      processCommand(body.substring(i));
    }

    server.send(200, "application/json", "{\"status\":\"ok\"}");
  });

  server.begin();
}

void loop() {
  server.handleClient();

  // Bluetooth commands
  if (SerialBT.available()) {
    String cmd = SerialBT.readStringUntil('\n');
    cmd.trim();
    processCommand(cmd);
  }
}

void processCommand(String cmd) {
  cmd.toUpperCase();
  Serial.println("CMD: " + cmd);

  if (cmd.indexOf("LIGHT1 ON") >= 0) {
    digitalWrite(LIGHT1_PIN, LOW);
  } 
  else if (cmd.indexOf("LIGHT1 OFF") >= 0) {
    digitalWrite(LIGHT1_PIN, HIGH);
  } 
  else if (cmd.indexOf("LIGHT2 ON") >= 0) {
    digitalWrite(LIGHT2_PIN, LOW);
  } 
  else if (cmd.indexOf("LIGHT2 OFF") >= 0) {
    digitalWrite(LIGHT2_PIN, HIGH);
  }
}
```

## Setup Steps

1. **Install Arduino IDE** and ESP32 board package
2. **Upload the code** to your ESP32
3. **Update WiFi credentials** in the code
4. **Note the IP address** printed in Serial Monitor
5. **Update Android app** with the ESP32 IP address in `sendHttpCommand()` method
6. **Pair ESP32** with your phone via Bluetooth (device name: "JARVIS")

## Testing

1. Open Serial Monitor in Arduino IDE
2. Run the Android app
3. Say "turn on the light" or "light jalao"
4. Check Serial Monitor for received commands
5. Verify relay clicks and light turns on/off

## Troubleshooting

- **Bluetooth not connecting**: Check if ESP32 is discoverable and not paired with another device
- **WiFi not connecting**: Verify SSID and password
- **Commands not working**: Check Serial Monitor for received commands
- **Relay not clicking**: Check wiring and power supply