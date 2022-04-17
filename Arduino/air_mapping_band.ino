#include "BluetoothSerial.h"
#include <Wire.h>
#include "Adafruit_SGP30.h"
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif
#define CON 30
#define RDY 31
#define PERIOD 30 //testing with period of 2 minutes
String TEST = "[5000,200,1]";
volatile int interruptCounter;
int totalInterruptCounter;
bool CO2sensor = 1;
float CO2Value = 0;
int counter = 0;
 
hw_timer_t * timer = NULL;
portMUX_TYPE timerMux = portMUX_INITIALIZER_UNLOCKED;
Adafruit_SGP30 sgp;
BluetoothSerial SerialBT;
 
void IRAM_ATTR onTimer() {
  portENTER_CRITICAL_ISR(&timerMux);
  interruptCounter++;
  portEXIT_CRITICAL_ISR(&timerMux);
 
}

void resetSGP() {
  counter = 0;
  uint16_t TVOC_base, eCO2_base;
  if (! sgp.getIAQBaseline(&eCO2_base, &TVOC_base)) {
    Serial.println("Failed to get baseline readings");
    return;
  }
  //Serial.print("****Baseline values: eCO2: 0x"); Serial.print(eCO2_base, HEX);
  //Serial.print(" & TVOC: 0x"); Serial.println(TVOC_base, HEX); 
}

void setup() {
  /* setup LED */
  pinMode(CON, OUTPUT); 
  pinMode(RDY, OUTPUT); 
  Serial.begin(115200);
  /* setup Bluetooth */
  if(!SerialBT.begin("AIR_MAP_BAND")){
    Serial.println("Error in initiallizing Bluetooth");
  }else{
    Serial.println("Bluetooth initialized");
  }
  digitalWrite(RDY, HIGH); 

  /*setup peridic timer */
  timer = timerBegin(0, 80, true);
  timerAttachInterrupt(timer, &onTimer, true);
  timerAlarmWrite(timer, 1000000*PERIOD, true);

  /*setup sgp30*/
  if (! sgp.begin()) {
    Serial.println("CO2 Sensor not found :(");
    CO2sensor = 0;
  }
}

void loop() {
  if(interruptCounter > 0){
      portENTER_CRITICAL(&timerMux);
      interruptCounter--;
      portEXIT_CRITICAL(&timerMux);
        if(!SerialBT.println(TEST)) {
           digitalWrite(CON, LOW);
           digitalWrite(RDY, HIGH);
             Serial.write("Disconnected");
        } 
  }
  
  if (SerialBT.available())
  {
    Serial.write(SerialBT.read());
    digitalWrite(CON, HIGH);
    digitalWrite(RDY, LOW);  
    timerAlarmEnable(timer);
  }
  
  /* Test Async emergency reading */
  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }

  /* SGP30 */
    if (CO2sensor && sgp.IAQmeasure()) {
      CO2Value = sgp.eCO2;
    }
    else {
      Serial.println("CO2 Measurement failed");
    } 
    counter++;
    if (counter == 30) {
      counter = 0;
      resetSGP();
    }
    Serial.print("CO2 value: ");
    Serial.println(CO2Value);
}
