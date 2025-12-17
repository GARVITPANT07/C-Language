package com.jarvis.assistant;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    
    private static final int REQUEST_RECORD_AUDIO = 1;
    private static final int REQUEST_SPEECH_INPUT = 2;
    private static final String GEMINI_API_KEY = "AIzaSyA3gOwm2BtLrRON_Sfl8YVbQy0UiRcTdfM"; // Replace with your API key
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY;
    private static final String ESP32_IP = "192.168.1.14"; // Replace with your ESP32 IP
    
    private TextView tvOutput, tvStatus;
    private FloatingActionButton btnMic;
    private TextToSpeech tts;
    private OkHttpClient httpClient;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private boolean isListening = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvOutput = findViewById(R.id.tvOutput);
        tvStatus = findViewById(R.id.tvStatus);
        btnMic = findViewById(R.id.btnMic);
        
        tts = new TextToSpeech(this, this);
        httpClient = new OkHttpClient();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        btnMic.setOnClickListener(v -> {
            if (!isListening) {
                startVoiceInput();
            }
        });
        
        checkPermissions();
        updateStatus("Ready to assist, sir.");
    }
    
    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT
        };
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO);
                break;
            }
        }
    }
    
    private void updateStatus(String message) {
        runOnUiThread(() -> tvStatus.setText(message));
    }
    
    private void updateOutput(String message) {
        runOnUiThread(() -> tvOutput.setText(message));
    }
    
    private void startVoiceInput() {
        isListening = true;
        updateStatus("Listening...");
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        
        try {
            startActivityForResult(intent, REQUEST_SPEECH_INPUT);
        } catch (Exception e) {
            isListening = false;
            updateStatus("Ready to assist, sir.");
            Toast.makeText(this, getString(R.string.error_speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isListening = false;
        
        if (requestCode == REQUEST_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                updateOutput("You: " + spokenText);
                updateStatus("Processing...");
                processWithGemini(spokenText);
            } else {
                updateStatus("Ready to assist, sir.");
            }
        } else {
            updateStatus("Ready to assist, sir.");
        }
    }
    
    private void processWithGemini(String userInput) {
        updateStatus("Processing...");
        String input = userInput.toLowerCase();
        
        // Simple keyword detection
        boolean isLightCommand = input.contains("light") || input.contains("jalao") || input.contains("band") || 
                                input.contains("chalu") || input.contains("switch") || input.contains("turn");
        
        if (isLightCommand) {
            boolean isOn = input.contains("on") || input.contains("jalao") || input.contains("chalu");
            boolean isOff = input.contains("off") || input.contains("band") || input.contains("switch off");
            
            String action = isOn ? "on" : (isOff ? "off" : "on");
            final String finalSpeech;
            
            // Determine which light
            if (input.contains("light 2") || input.contains("second") || input.contains("dusri")) {
                finalSpeech = "Okay sir, light 2 is now " + action + ".";
                sendCommand("LIGHT2 " + action.toUpperCase());
            } else if (input.contains("both") || input.contains("all") || input.contains("sab")) {
                finalSpeech = "Okay sir, all lights are now " + action + ".";
                sendCommand("LIGHT1 " + action.toUpperCase());
                sendCommand("LIGHT2 " + action.toUpperCase());
            } else {
                finalSpeech = "Okay sir, the light is now " + action + ".";
                sendCommand("LIGHT1 " + action.toUpperCase());
            }
            
            runOnUiThread(() -> {
                updateStatus("Ready to assist, sir.");
                updateOutput("JARVIS: " + finalSpeech);
                speak(finalSpeech);
            });
        } else {
            // Simple chat responses
            String response = "I'm JARVIS, your smart home assistant. Try saying 'turn on light' or 'jalao light'.";
            runOnUiThread(() -> {
                updateStatus("Ready to assist, sir.");
                updateOutput("JARVIS: " + response);
                speak(response);
            });
        }
    }
    

    
    private void sendCommand(String command) {
        // Try Bluetooth first
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            sendBluetoothCommand(command);
        } else {
            // Fallback to HTTP
            sendHttpCommand(command);
        }
    }
    
    private void sendBluetoothCommand(String command) {
        try {
            if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
                connectBluetooth();
            }
            
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                outputStream.write((command + "\n").getBytes());
                outputStream.flush();
            }
        } catch (Exception e) {
            sendHttpCommand(command); // Fallback to HTTP
        }
    }
    
    private void connectBluetooth() {
        try {
            BluetoothDevice device = null;
            for (BluetoothDevice pairedDevice : bluetoothAdapter.getBondedDevices()) {
                if ("JARVIS".equals(pairedDevice.getName())) {
                    device = pairedDevice;
                    break;
                }
            }
            
            if (device != null) {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
            }
        } catch (Exception e) {
            // Bluetooth connection failed
        }
    }
    
    private void sendHttpCommand(String command) {
        String url = "http://" + ESP32_IP + "/command";
        
        JSONObject json = new JSONObject();
        try {
            json.put("command", command);
            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Command failed
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Command sent successfully
                }
            });
        } catch (Exception e) {
            // Error sending command
        }
    }
    
    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setPitch(0.7f);  // Deeper voice
            tts.setSpeechRate(0.8f);  // Slower, more authoritative
        }
    }
    
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        super.onDestroy();
    }
}