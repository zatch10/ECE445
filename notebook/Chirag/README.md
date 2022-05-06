# Chirag Worklog

## 2022-02-15: Research Sensors
After completing our project proposal, the next step was to research and find sensors that we would use in our project. 
Since the sensors would be the backbone of our project we decided to allocate close to half of our budget for our sensor relay. The sensors I shortlisted are shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/MQ2.jpg" width="200" hspace="20"/>      <img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/MQ9.jpg" width="200"  hspace="20"/>      <img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/Sensiron.png" width="200"  hspace="20"/>

From left to right: MQ2 (Flamable gas sensor), MQ9 (Carbon Monoxide Sensor), SCD40 (Carbon Dioxide Sensor). 

The MQ9 and MQ2 are cost effective and can measure Propane and Carbon Monoxide upto the thresholds we specified in our high level requirements. 
The SCD40 also meets our thresholds, however it costs almost $60. Hence, if we were to use this sensor, we would not be able to order a second sensor as a backup. 

## 2022-02-20: Complete Circuit Schematic for 1st PCB order
After talking to our TA, we realized that spending $60 on a single sensor and not ordering back ups could be a point of failure for our project. 
As a result we decided to use the Adafruit's SGP30 sensor. While it is not as accurate as the SCD40, it measures Carbon Dioxide upto our required 
thresholds and costs around $20. An image of the SGP30 sensor is shown below:

<img src="https://cdn-learn.adafruit.com/guides/images/000/001/892/medium640/SGP30_top_angle.jpg" width="200"/>

Having finalized all our parts, I started working on our Circuit Schematic for the first PCB order. This schematic is shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/Buck%20Convertor%20Ciruit%20Schematic.png" width="600"/> 

## 2022-03-21: Order passive parts and figure out casing
During this session, I first worked on researching and ordering our passive parts for our project. The passive parts include capacitors, inductors, and resistors. 
These parts and their costs are listed in the table below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/passive%20parts.PNG" width="400"/>

After ordering the parts we started brainstorming about the casing. The machine shop had already told us that they will not help with building a simple box. 
Fortunately we could use a 3D printer to design a case. We ultimately had two options. The first was to use [UIUC's 3D printing Service](https://makerlab.illinois.edu/). The second option was to use the 3D printers at the openlab. 
The second option would be free since my roommate had extra filament that he was willing to give me for the project. We ultimately decided to move forward with the second option

## 2022-03-24: Test first PCB
Our first PCB order finally arrived and a picture of it is shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/Initial%20PCB.jpg" width="400"/>

We immediately found two issues with the PCB:
1. Firstly, the inductors that we had ordered for the power subsystem circuit had a different footprint. 
So they could not be soldered on the board. I tried searching for inductors with a matching footprint size (0805), however, I realized that there were no such inductors 
that could both tolerate the current flowing on the board and that would ship within the span of this course. 

2. Secondly, our board was a bit too big. It would be impossible to create a wearable device using this circuit unless we made a major change to our design.

Hence we have to rethink our ciruit design for our second PCB order. 

## 2022-03-27: Revise Power Subsystem Circuit Schematic
We quickly realized that our power subsystem was taking up too much space as it used too many capacitors, resistors, and inductors. We ultimately decided to use 
simpler low-dropout (LDO) regulators. The updated the power subsystem circuit schematic is shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/LDO%20Power%20Schematic.png" width="400"/> 

We used this schematic for our second PCB order. After arranging the components we were able to get the dimensions of our PCB down to 8.9cm x 5.6cm.

## 2021-03-29: Work on Arduino Code and Bluetooth
Now that the ESP 32 devolopment board had finally arrived, I could finally start working on the arduino code and test bluetooth. I first created a datapath to get an
overview of how the sensor subsystem would look. This is shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/Microcontroller%20datapath.png" width="500"/> 

I then researched about how bluetooth works on the esp32. Since the data transfer was supposed to be periodic, I also research on how to use the programming
interrupt timer inside the esp32. My bluetooth test is shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/Bluetooth%20Test.PNG" width="500"/> 

The test essentially confirms that data can be transferred periodically every 2 minutes. Also, if values were to exceed our thresholds, it was possible to asynchronously
send a message to phone.

## 2022-04-05: Order new passive parts and test sensors
Having finalized our new power subsystem circuit schematic, I had to research and order the new regulators and passive components. These parts are summarized in the 
table shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/LDO%20order.PNG" width="400"/>

Additionally having finally recieved our sensors, it was time to test them on a bread board. The MQ9 and MQ2 required to be heated overnight to calibrate them. This
heating process would burn any impurities on the heating coil which would release a really foul odour. Nevertheless, all the sensors were working perfectly. I used 
the [MQunifiedSensor library](https://www.arduino.cc/reference/en/libraries/mqunifiedsensor/) to verify the readings in parts per million (ppm).
Additionally, I was able to read the SGP30 using the [Wire library](https://www.arduino.cc/en/reference/wire) to read data over I2C.

## 2022-04-18: Boot test the ESP32 chip on PCB
After Vatsin had soldered the ESP32 using the reflow oven, I worked on trying to load the ESP32 with a test program. To set the ESP32 to boot mode
I had to first set the boot pin to low. To do so, I essentially connected our boot circuit together. This circuit drives the boot pin to ground.
I then connected the board to my laptop using a UART programmer and a micro usb cable. An image of the
programmer I used is shown below:

<img src="https://media.digikey.com/photos/Sparkfun%20Elec%20%20Photos/MFG_BOB-13830.jpg" width="200"/>

I had already tested the program on the devolopment board so I knew it would work. However, I quickly realized that ESP32 was not booting. I tested 
every single pin on the ESP32 and the boot circuit using the multimeter and all the connections seemed fine. Nevertheless, upon referring to the 
datasheet I found out that EN pin on the ESP32 needs to be set high. Upon referring back to our PCB design I found a single floating wire that I had
forgotten to connect as shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/Open%20wire.PNG" width="400"/>

We quickly soldered the EN pin to high and the ESP32 started working!

## 2022-04-20: Test sensors on PCB over Bluetooth
As the ESP32 was able to load programs, it was time to test the sensors that were soldered into the board. I combined the arduino code that I written to collect sensor data with the bluetooth code I had written. Using the following setup I then tested our board

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/Lab%20testing%20the%20board%20and%20sensors.jpg" width="400"/>

The following readings were read on my bluetooth debugger app on the phone

## 2022-04-22: Design Casing and start 3D print
With the help of a friend who was an expert in designing CAD models, I created the following case (top and bottom photos shown):

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/top.PNG" width="200" hspace="20"/>      <img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/bottom.PNG" width="200"  hspace="20"/> 

I then went to the ECE open lab and started a 3D print job. It took almost 7 hours to print during which time I had left to work on something else. Unfortunately, the print job failed mid way as shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/failed%20print.jpg" width="400"/>

Nevertheless, I started one more print at night before leaving the ECE building.

## 2022-04-23: Connect Board system to App system
This time the print task was a resounding success. Now that both the board and app were working by themselves, it was time to test the entire device. 
The pipeline below highlights how we tested the device:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Chirag/Complete%20structure.png" width="800"/>

The purple light on the left image indicates that the device is not connected to the app. Once connected, the purple light changes to blue as the 
ESP32 is able to communicate with the app. Finally, all three gas concentrations are successfully recieved by the app as shown in the image on the 
right.

## 2022-05-24: Field test final product
We finally tested our final product outside. We tested it with a lighter to detect propane and then we walked around campus to test the mapping capability.
We had test with the lighter multiple times to figure out the threshold we should use for detecting propane since the sensor was very noise and would
randomly return very high concentrations. Video links are provided in our repository. 
