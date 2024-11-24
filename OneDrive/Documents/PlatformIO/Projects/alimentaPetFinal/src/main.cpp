#include <Arduino.h>
#include <WiFiManager.h>
#include <Firebase_ESP_Client.h>
#include <ESP32Servo.h>
#include "HX711.h"
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// firebase key
#define API_KEY "AIzaSyCtiF74Zka36MT2N3_VEefxJyCxiFXW0tU"
#define DATABASE_URL "https://alimenta-pet-539e4-default-rtdb.asia-southeast1.firebasedatabase.app/"

// GPIO
#define DT 23
#define SCK 22
#define servoPin 21
#define Led_Red 19
#define Led_Green 18
#define Led_BLue 17

// esp32 unique chip id
uint64_t chipID = ESP.getEfuseMac();

FirebaseData fbdo;
FirebaseData fbdo2;
FirebaseData stream;

FirebaseConfig config;
FirebaseAuth auth;

FirebaseJson json;           
FirebaseJsonArray arr; 

Servo servo;
HX711 scale;
WiFiManager wifimanager;

String chipIDString = String((uint16_t)(chipID >> 32), HEX) + String((uint32_t)chipID, HEX);
String parentPath = "/feeder/" + chipIDString;
String childPath = "/feeds";
String updatedPath = parentPath + childPath; 


bool signupOK = false;
float calibration_factor = 2050;

unsigned long sendDataPrevMillis = 0;
int ldrData = 0;
unsigned long count = 0;
float weight = 0;

class Color {
public:
    // Set RGB color
    void setColor(int red, int green, int blue) {
        analogWrite(Led_Red, red);
        analogWrite(Led_Red, green);
        analogWrite(Led_Red, blue);
    }

    // Predefined colors
    void red() {
        setColor(255, 0, 0);
    }

    void yellow() {
        setColor(255, 255, 0);
    }

    void green() {
        setColor(0, 0, 255);
    }
};
class ServoState {
public: 
    void rotate(){
      servo.write(180);
    }

    void stop(){
      servo.write(90);
    }
};

Color color;
ServoState servoState;

void initializePath(){
  if(Firebase.RTDB.getJSON(&fbdo, parentPath)){
    
  }else{
    json.add("name", "PetFeeder");

    // Send the JSON object with the embedded array to Firebase
    if (Firebase.RTDB.setJSON(&fbdo, parentPath, &json)) {
        Serial.println("Set object with array... ok");
    } else {
        Serial_Printf("Set object with array... ", fbdo.errorReason());
    }
  }
}

void streamCallbackAlarm(FirebaseStream data) {
  if (data.dataTypeEnum() == fb_esp_rtdb_data_type_json) {
    FirebaseJson json = data.to<FirebaseJson>();
    FirebaseJsonData jsonData;
    String state;

    String alarmPath = data.dataPath();
    String updatePath = parentPath + "/" + alarmPath + "/state"; 

    if (json.get(jsonData, "state")) {
      state = jsonData.stringValue;  // Extract the value as a string
      if (state == "start") {
        Serial.println("Alarm triggered: Rotating servo...");

        servoState.rotate();
        delay(3000);
        servoState.stop();

        if (Firebase.RTDB.setString(&fbdo, updatePath, "done")) {
          Serial.println("State updated to 'done'.");
        } else {
          Serial.printf("Failed to update state: %s\n", fbdo.errorReason().c_str());
        }
      }
    }
  }
}

void streamCallbackFeed(FirebaseStream data) {
  if (data.dataTypeEnum() == fb_esp_rtdb_data_type_boolean) {
    
    bool isEnabled = data.boolData();

    if (isEnabled){
      Serial.println("rotating");
      Serial.println("isEnabled : true.");
      servoState.rotate();
      
      while (true) {
        weight = scale.get_units(10);  // Fetch the latest weight
        Serial.print("Current weight: ");
        Serial.println(weight);

        if (weight >= 500) {
            Serial.println("Weight threshold reached.");
            break;  // Exit the loop when the condition is met
        }
        delay(500);  // Add a small delay to prevent overloading the system
    }

      if (weight >= 500) {
        if(Firebase.RTDB.setBool(&fbdo2, updatedPath, false)){
          Serial.println("isEnabled : false.");
        }else{
          Serial.println("Failed to set isEnabled to false: ");
          Serial.println(fbdo2.errorReason());
        }
      }

    }else{
      servoState.stop();
      Serial.println("Stop");
    }

  }
}

void streamTimeoutCallback(bool timeout) {
    if (timeout) {
        Serial.println("Stream timeout! Reconnecting...");
    }

    Firebase.RTDB.readStream(&stream);
}

void setup(){
  WiFi.mode(WIFI_STA);
  Serial.begin(115200);

  // hx711 and loadcell
  scale.begin(DT, SCK);
  scale.set_scale(calibration_factor);
  scale.tare();

  // servo motor
  servo.attach(servoPin, 500, 2500);

  // red led if not connected to wifi
  color.red();

  // rgb
  pinMode(Led_Red, OUTPUT);
  pinMode(Led_Green, OUTPUT);
  pinMode(Led_BLue, OUTPUT);

  // wifiManager
  if(!wifimanager.autoConnect("AlimentaPet")){
    Serial.println("Failed to connect");
  }else{
    Serial.println("WiFi Connected");

    // if connected to wifi
    color.green();

    // firebase configuration
    config.api_key = API_KEY;
    config.database_url = DATABASE_URL;
    config.token_status_callback  = tokenStatusCallback;

    // firebase authentication
    if(Firebase.signUp(&config, &auth,"", "")){
      Serial.println("Firebase Connected");
      signupOK = true;
    }else{
      Serial.println(config.signer.signupError.message.c_str());
    }

    Firebase.begin(&config, &auth);
    Firebase.reconnectWiFi(true);

    initializePath();

    if (Firebase.RTDB.beginStream(&fbdo, parentPath)) {
      Serial.println("Stream 1 started.");
      Firebase.RTDB.setStreamCallback(&fbdo, streamCallbackAlarm, streamTimeoutCallback);
    }else{
      Serial.printf("Failed to start stream: %s\n", fbdo.errorReason().c_str());
    }

    if (Firebase.RTDB.beginStream(&fbdo2, updatedPath)) {
      Serial.println("Stream 2 started.");
      Firebase.RTDB.setStreamCallback(&fbdo2, streamCallbackFeed, streamTimeoutCallback);
    }else{
      Serial.printf("Failed to start stream: %s\n", fbdo2.errorReason().c_str());
    }
  }
}

void loop(){
}