# JARVIS Setup Instructions

Complete setup guide for the JARVIS AI Voice Assistant Android app.

## Prerequisites

1. **Android Studio** (latest version)
2. **Android device** (API 21+)
3. **ESP32** with 2-channel relay module
4. **Google Gemini API key**

## Step 1: Get Gemini API Key

1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with Google account
3. Create new API key
4. Copy the API key

## Step 2: Configure Android App

1. Open `MainActivity.java`
2. Replace `GEMINI_API_KEY` with your actual API key:
   ```java
   private static final String GEMINI_API_KEY = "AIzaSyDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
   ```
3. Update ESP32 IP address:
   ```java
   private static final String ESP32_IP = "192.168.1.100"; // Your ESP32 IP
   ```

## Step 3: Setup ESP32

1. **Hardware Wiring:**
   ```
   ESP32 Pin 26 → Relay IN1 (Light 1)
   ESP32 Pin 27 → Relay IN2 (Light 2)
   ESP32 GND → Relay GND
   ESP32 5V → Relay VCC
   ```

2. **Upload ESP32 Code** (see `ESP32_Setup_Instructions.md`)

3. **Note ESP32 IP** from Serial Monitor

## Step 4: Build Android App

1. Open project in Android Studio
2. Sync Gradle files
3. Build → Make Project
4. Run on Android device

## Step 5: Permissions

Grant these permissions when prompted:
- **Microphone** - for voice input
- **Bluetooth** - for ESP32 communication

## Step 6: Connect to ESP32

### Bluetooth Method:
1. Pair phone with ESP32 device "JARVIS"
2. App will auto-connect when sending commands

### WiFi Method:
1. Ensure phone and ESP32 on same network
2. Update ESP32_IP in MainActivity.java
3. App will use HTTP commands

## Step 7: Test Voice Commands

Try these commands:
- **"Turn on the light"** → Controls Light 1
- **"Light 2 on"** → Controls Light 2  
- **"All lights off"** → Controls both lights
- **"Jalao light"** → Hindi command for Light 1
- **"What's 2+2?"** → General AI chat

## Troubleshooting

### App Issues:
- **No voice recognition**: Check microphone permission
- **Gemini errors**: Verify API key and internet
- **Build errors**: Sync Gradle, clean project

### ESP32 Issues:
- **Not responding**: Check Bluetooth pairing
- **WiFi problems**: Verify network and IP address
- **Commands not working**: Check Serial Monitor

### Voice Recognition:
- Speak clearly and wait for "Listening..." status
- Try different phrasings if not understood
- Check internet connection for Gemini AI

## Voice Command Examples

### Light Control:
- "Turn on light" / "Light jalao" → LIGHT1 ON
- "Switch off light" / "Band karo" → LIGHT1 OFF
- "Light 2 on" / "Dusri light jalao" → LIGHT2 ON
- "All lights off" / "Sab lights band karo" → Both OFF

### General Chat:
- "What's the time?"
- "Tell me a joke"
- "What's 5 times 7?"
- "How are you?"

## Security Notes

- **API Key**: Store securely, consider backend proxy for production
- **Network**: ESP32 uses basic HTTP, add authentication for production
- **Permissions**: App only requests necessary permissions

## Extending the App

### Add More Devices:
1. Update ESP32 code with new pins
2. Add device types in Gemini prompt
3. Handle new commands in `parseGeminiResponse()`

### Add More Languages:
1. Update Gemini prompt with new keywords
2. Test voice recognition with target language
3. Add localized responses

### Improve Security:
1. Use HTTPS for ESP32 communication
2. Add device authentication
3. Implement user access control