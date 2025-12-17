# JARVIS - AI Voice Assistant for Smart Home

A minimal Android app that uses Google's Gemini AI to control smart home devices through voice commands in multiple languages.

## Features

- **Voice Input**: Accepts speech in Hindi, English, Hinglish, Urdu, and mixed languages
- **AI Processing**: Uses Gemini AI to understand commands vs questions
- **Smart Home Control**: Controls lights via ESP32 (Bluetooth/WiFi)
- **Natural Speech**: Text-to-Speech responses with assistant tone
- **Dark Theme**: JARVIS-style UI

## Quick Start

1. **Setup Gemini API**
   - Get API key from [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Replace `YOUR_GEMINI_API_KEY_HERE` in `MainActivity.java`

2. **Setup ESP32**
   - Follow instructions in `ESP32_Setup_Instructions.md`
   - Update WiFi credentials and note IP address

3. **Build & Install**
   - Open project in Android Studio
   - Update ESP32 IP in `sendHttpCommand()` method
   - Build and install on Android device

4. **Test**
   - Pair with ESP32 Bluetooth device "JARVIS"
   - Tap microphone button and say commands
   - Try: "turn on light", "light jalao", "what's 2+2?"

## Voice Commands

### Light Control
- **ON**: "light on", "turn on light", "jalao", "chalu karo"
- **OFF**: "light off", "band karo", "switch off"

### General Chat
- Any other questions go to Gemini AI for normal conversation

## Architecture

```
Voice Input → Speech-to-Text → Gemini AI → JSON Response → Action
                                    ↓
                            Command/Chat Decision
                                    ↓
                        ESP32 (Bluetooth/WiFi) + TTS
```

## Files Structure

```
JARVIS/
├── app/
│   ├── src/main/
│   │   ├── java/com/jarvis/assistant/
│   │   │   └── MainActivity.java          # Main app logic
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml   # UI layout
│   │   │   ├── values/strings.xml         # App strings
│   │   │   └── values/themes.xml          # Dark theme
│   │   └── AndroidManifest.xml            # Permissions
│   └── build.gradle                       # Dependencies
├── ESP32_Setup_Instructions.md            # Hardware setup
├── Gemini_Prompt_Example.md              # AI prompt details
└── README.md                             # This file
```

## Security Notes

- Store Gemini API key securely (consider backend proxy for production)
- ESP32 uses basic HTTP/Bluetooth (add authentication for production)
- App requires microphone and Bluetooth permissions

## Troubleshooting

- **No voice recognition**: Check microphone permissions
- **Gemini errors**: Verify API key and internet connection  
- **ESP32 not responding**: Check Bluetooth pairing and WiFi connection
- **Commands not working**: Check Serial Monitor on ESP32

## Extending

- Add more devices (fans, AC, etc.)
- Implement MQTT for better IoT communication
- Add user authentication
- Support more languages
- Add scheduling features