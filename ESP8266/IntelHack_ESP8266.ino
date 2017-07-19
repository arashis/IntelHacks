#include <WiFiManager.h>

//
// Copyright 2015 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

#include <ESP8266WiFi.h>//https://github.com/esp8266/Arduino
#include <ESP8266WebServer.h>
#include <WiFiManager.h>         //https://github.com/tzapu/WiFiManager

#include <FirebaseArduino.h>//For using firebase on arduino
#include <DNSServer.h>
#define FIREBASE_HOST "******.firebaseio.com"
//Please set FIREBASE_HOST by the firebase URL like a https://console.firebase.google.com/project/******/overview

String payload;//Status of a key
const bool  SW= 0;//ESP8266's I/O pin number
bool  SW_state=0,old_state=0;//Variables for checking door state "Open" or "Lock"
  
void setup() {
  Serial.begin(9600);
  pinMode(SW,INPUT);
  // connect to wifi by WiFiManager
    //Local intialization. Once its business is done, there is no need to keep it around
    WiFiManager wifiManager;
    //reset saved settings
    wifiManager.resetSettings();
    
    //set custom ip for portal
    //wifiManager.setAPStaticIPConfig(IPAddress(10,0,1,1), IPAddress(10,0,1,1), IPAddress(255,255,255,0));

    //fetches ssid and pass from eeprom and tries to connect
    //if it does not connect it starts an access point with the specified name
    //here  "AutoConnectAP"
    //and goes into a blocking loop awaiting configuration
    wifiManager.autoConnect("AutoConnectAP");
    //or use this for auto generated name ESP + ChipID
    //wifiManager.autoConnect();

    
    //if you get here you have connected to the WiFi
    Serial.println("connected...yeey :)");
  Firebase.begin(FIREBASE_HOST);
}

void loop() {
  SW_state=digitalRead(SW);//Get I/O pin voltage state for checking door state "Open" or "Lock"
 //-------- Only executed when cheanged door state --------
 if (old_state!=SW_state) {
    if(SW_state==HIGH){
      Serial.println("Lock");
      payload ="Lock";
    }else{
      Serial.println("Open");
      payload ="Open";
    }
    //-------- Indicate "payload"
    Serial.print("Sending payload: ");
    Serial.println(payload);
    // set value
    Firebase.setString("Status", payload);
    // handle error
    if (Firebase.failed()) {
        Serial.print("setting State:");
        Serial.println(Firebase.error());  
        return;
    }
    delay(1000);
 }
 old_state=SW_state;//Update "old_state"
}
