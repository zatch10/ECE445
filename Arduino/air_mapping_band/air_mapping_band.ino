#include "BluetoothSerial.h"
#include <Wire.h>
#include "Adafruit_SGP30.h"
#include <MQUnifiedsensor.h>
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif
#define RED 17
#define GREEN 18
#define BLUE 19
#define PERIOD 30 //testing with period of 2 minutes
#define COPORT 39 // VN
#define PROPORT 36 // VP
#define CO2_THRESH 500
#define CO_THRESH 200
/************************Hardware Related Macros************************************/
#define         Board                   ("ESP 32")
#define         Voltage_Resolution      (5)
#define         ADC_Bit_Resolution      (12)

/* MQ9 Definitions */
/***********************Software Related Macros************************************/
#define         COType                    ("MQ-9") //MQ9
#define         RatioMQ9CleanAir        (9.6) //RS / R0 = 60 ppm 
#define         PreaheatControlPin5     (3) // Preaheat pin to control with 5 volts
#define         PreaheatControlPin14    (4)
/*****************************Globals***********************************************/
MQUnifiedsensor MQ9(Board, Voltage_Resolution, ADC_Bit_Resolution, COPORT, COType);

/* MQ2 Definitions */
/***********************Software Related Macros************************************/
#define         PROType                ("MQ-2") //MQ2
#define         RatioMQ2CleanAir        (9.83) //RS / R0 = 9.83 ppm 

/*****************************Globals***********************************************/
MQUnifiedsensor MQ2(Board, Voltage_Resolution, ADC_Bit_Resolution, PROPORT, PROType);


String TEST = "[5000,200,1]";
volatile int interruptCounter;
int totalInterruptCounter;
bool CO2sensor = 1;
float CO2Value = 0;
float COValue = 0;
float PropaneValue = 0;
int counter = 0;
float data[] = {400.0,5.0,0.0};
float temp = 0;
 
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

int SendMsg() {

  String toSend = "";
  for(int i = 0; i < 3; i++) {
    toSend.concat(data[i]);
    toSend.concat("+");
  }
  
  if(!SerialBT.println(toSend)) {
     Serial.println("Disconnected");
     digitalWrite(RED, HIGH);
     return 0;
  }
  else {
        digitalWrite(RED, LOW);
  }
  return 1;
}

void setup() {
  /* setup LED */
  pinMode(RED, OUTPUT);
  pinMode(BLUE, OUTPUT);  

  Serial.begin(115200);
  float calcR0;
  
  /* setup Bluetooth */
  if(!SerialBT.begin("AIR_MAP_BAND")){
    Serial.println("Error in initiallizing Bluetooth");
  }else{
    Serial.println("Bluetooth initialized");
  }

  /*setup peridic timer */
  timer = timerBegin(0, 80, true);
  timerAttachInterrupt(timer, &onTimer, true);
  timerAlarmWrite(timer, 1000000*PERIOD, true);

  /*setup sgp30*/
  if (! sgp.begin()) {
    Serial.println("CO2 Sensor not found :(");
    CO2sensor = 0;
  }

  /*setup MQ9*/
  //Set math model to calculate the PPM concentration and the value of constants
  MQ9.setRegressionMethod(1); //_PPM =  a*ratio^b
  MQ9.setA(599.65); MQ9.setB(-2.244); // Configure the equation to to calculate CO concentration
  MQ9.init();
  calcR0 = 0;
  Serial.print("Calibrating CO-MQ9 please wait.");
  for(int i = 1; i<=10; i ++)
  {
    MQ9.update(); // Update data, the arduino will read the voltage from the analog pin
    calcR0 += MQ9.calibrate(RatioMQ9CleanAir);
    Serial.print(".");
  }
  MQ9.setR0(calcR0/10);
  Serial.println("  done!.");

  if(isinf(calcR0)) {Serial.println("Warning: Conection issue, CO R0 is infinite (Open circuit detected) please check your wiring and supply"); while(1);}
  if(calcR0 == 0){Serial.println("Warning: Conection issue found, CO R0 is zero (Analog pin shorts to ground) please check your wiring and supply"); while(1);}

  /*setup MQ2*/
  //Set math model to calculate the PPM concentration and the value of constants
  MQ2.setRegressionMethod(1); //_PPM =  a*ratio^b
  MQ2.setA(658.71); MQ2.setB(-2.168); // Configure the equation to to calculate Propane
  MQ2.init();
  Serial.print("Calibrating Propane-MQ2 please wait.");
  calcR0 = 0;
  for(int i = 1; i<=10; i ++)
  {
    MQ2.update(); // Update data, the arduino will read the voltage from the analog pin
    calcR0 += MQ2.calibrate(RatioMQ2CleanAir);
    Serial.print(".");
  }
  MQ2.setR0(calcR0/10);
  Serial.println("  done!.");
  
  if(isinf(calcR0)) {Serial.println("Warning: Conection issue, PROPANE R0 is infinite (Open circuit detected) please check your wiring and supply");}
  if(calcR0 == 0){Serial.println("Warning: Conection issue found, PROPANE R0 is zero (Analog pin shorts to ground) please check your wiring and supply");}

  /* start LED */
  digitalWrite(BLUE, HIGH);
  digitalWrite(RED, HIGH);
}

void loop() {
  if(interruptCounter > 0){
      portENTER_CRITICAL(&timerMux);
      interruptCounter--;
      portEXIT_CRITICAL(&timerMux);
      String toSend = "";
      for(int i = 0; i < 3; i++) {
        toSend.concat(data[i]);
        toSend.concat("+");
      }
      
      if(!SerialBT.println(toSend)) {
         Serial.println("Disconnected");    
         digitalWrite(RED, HIGH);
      } 
      else {
        digitalWrite(RED, LOW);
      }
  }
  
  if (SerialBT.available())
  {
    Serial.write(SerialBT.read());
    digitalWrite(RED, LOW); 
    timerAlarmEnable(timer);
  }
  
  /* Test Async emergency reading */
  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }

  /* SGP30 */
  if (CO2sensor && sgp.IAQmeasure()) {
    CO2Value = sgp.eCO2;
    data[0] = CO2Value;
  }
  else {
    Serial.println("CO2 Measurement failed");
  } 
  counter++;
  if (counter == 30) {
    counter = 0;
    resetSGP();
  }
  /* MQ9 */
  MQ9.update();
  COValue = MQ9.readSensor();
  data[1] = COValue;

  /* MQ2 */
  MQ2.update();
  temp = MQ2.readSensor();
  if(abs(temp - PropaneValue) >= 1000) {
    data[2] = 1.0;
  }
  else {
    data[2] = 0.0;
  }
  PropaneValue = temp;

  if(data[2] == 1.0 || (data[2] == 0.0 && (data[0] >= CO2_THRESH || data[1] >= CO_THRESH))) {
    if(!SendMsg()) {
      Serial.println("Disconnected");
    }
  }
}
