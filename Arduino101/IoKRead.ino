/*
I use a sample sketch following this: 
 - * Sketch: SensortagButton.ino
 - *
 - * Description:
 - *   This Central sketch scan for a Peripheral called the SensorTag.
 - *   It looks for particular Service, discovers all its attributes,
 - *   and them on the serial monitor. 
*/

/*
 * Copyright (c) 2016 Intel Corporation.  All rights reserved.
 * See the bottom of this file for the license terms.
 */

#include <CurieBLE.h>//For Arudino101
#define Name "IoK" //Set name of a BLE device
#define ESP_PIN 13//Set a number of output pin for sending data to the Wifi module

void setup() {
  pinMode(ESP_PIN,OUTPUT);
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  // initialize the BLE hardware
  BLE.begin();

  Serial.println("BLE Central - IoK Read");
  Serial.println("Make sure to turn on the device.");

  // start scanning for peripheral
  BLE.scan();
}

void loop() {
  // check if a peripheral has been discovered
  BLEDevice peripheral = BLE.available();

  if (peripheral) {
    // discovered a peripheral, print out address, local name, and advertised service
    Serial.print("Found ");
    Serial.print(peripheral.address());
    Serial.print(" '");
    Serial.print(peripheral.localName());
    Serial.print("' ");
    Serial.print(peripheral.advertisedServiceUuid());
    Serial.println();

    /* see if peripheral is a SensorTag
     * The localName, CC2650 SensorTag, is in the Scan Response Data packet.
     * If this is not the expected name, please change the following
     * if-statement accordingly.
     */
    if (peripheral.localName() == "IoK") {
      // stop scanning
      BLE.stopScan();

      monitorSensorTagButtons(peripheral);

      // peripheral disconnected, start scanning again
      BLE.scan();
    }
  }
}

void monitorSensorTagButtons(BLEDevice peripheral)
{
  // connect to the peripheral
  Serial.println("Connecting ...");
  if (peripheral.connect()) {
    Serial.println("Connected");
  } else {
    Serial.println("Failed to connect!");
    return;
  }

  // discover peripheral attributes
  Serial.println("Discovering attributes of service 0xffe0 ...");
  if (peripheral.discoverAttributesByService("12345678-9012-3456-7890-1234567890ff")) {
    Serial.println("Attributes discovered");
  } else {
    Serial.println("Attribute discovery failed.");
    peripheral.disconnect();
    return;
  }

  // retrieve the simple key characteristic
  BLECharacteristic IoKCharacteristic = peripheral.characteristic("12345678-9012-3456-7890-123456789022");
  
  while (peripheral.connected()) {
    // while the peripheral is connected

    // check if the value of the simple key characteristic has been updated
    // yes, get the value, characteristic is 1 byte so use char value
    if(IoKCharacteristic.canRead()){
        IoKCharacteristic.read();
    }
     const byte* value = IoKCharacteristic.value();
     Serial.println(*value);
     if(*value==0x02){
       digitalWrite(ESP_PIN, HIGH);
     }else{
      digitalWrite(ESP_PIN, LOW);
     }
     delay(1000);
  }

  Serial.println("SensorTag disconnected!");
}



/*
  Arduino BLE Central SensorTag button example
  Copyright (c) 2016 Arduino LLC. All right reserved.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
*/


