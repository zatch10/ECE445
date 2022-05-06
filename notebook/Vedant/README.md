<h1>Vedant's Workflow</h1>

<h2>02/21/2022 : Design Document and App layout</h2>

After getting our project of Air Pollution Mapping Band approved, I began writing out all the requirements for our application. Our application should be able to connect and communicate with our microcontroller ESP32. There has to be a central server that collects and stores the data collected by the users. Our application should be able to fetch the data from the server. I also decided to show this data visually on a google maps overlay as I thought that this would be the best way to visualize all the critical data we were collecting. Lastly, I decided to use notification banners to alert the user about high gas contamination.

Here is a rough block diagram for all the communications that my application would be responsible for:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/datapath.png" width="200" hspace="20"/>

Laying out all the requirements gave me a better understanding to start designing the application UI. I spent a considerable time on this to make sure that the application is user friendly while also clearly displaying all the important data. 

Here is a preminilary app layout:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/app_layout.png" width="200" hspace="20"/>

<h2>02/28/2022 : Using Google Maps API and Integrating it in the app</h2>

To integrate Google Maps into our Android application, I followed the official documentation provided by the Android Developer Guide. The first step in the process was to create a Google Cloud Platform account to get an API key that links the usage by our Android app to my Google account.
I could then integrate this API key in the manifest file of the application and use the pre-built google maps layout to show the overlay on the application. Since there is no other fragment in the application, the overlay took up the entire window.
After displaying the overlay, I could start plotting markers and circles on the map which will be used to indicate contamination zones and a 100-meter radius around the center of the zone. This proved out to be a great proof of concept for being able to create a heat map once we have the user collected data.

Here is a code snippet for plotting markers and circles on google maps:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/google_maps_code.png" width="200" hspace="20"/>

Here is a screenshot of the resulting google maps window:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/google_maps_overlay.png" width="200" hspace="20"/>


<h2>03/21/2022 : Extracting User Location</h2>

It is important for us to show the current user location on the google maps overlay so that we can not only zoom to the user location automatically instead of having the user to zoom in themselves, but it would also makes it easy for the user to see the contamination zones around them. Upon my research, I found out that there were two ways of getting the user location: through the gps data and using the network data. Since the precise user location was extremely important to us, I decided to write code that extracts the user location using both the means and then choosing the more accurate one.
Extracting user location using both of these means proved out to be more difficult than I thought and required a week's worth of research. I went through a lot of GeeksForGeeks forums and TutorialPoint examples to finally get the code working. 

Here is the code responsible for extracting the user location:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/user_location_code.png" width="200" hspace="20"/>

Since I used an emulator to test out the application, I could set the GPS location for the emulated device. Next, in the code above, I logged the latitude and longitude coordinates extracted from the comparison between GPS and Network values keeping the most accurate one. This way, I could test out if the coordinate values that I set for my emulator matched the ones calculate by my code. 

Here is a screenshot of setting the location for the emulator:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/emulator_location.png" width="200" hspace="20"/>

<h2>03/28/2022 : Bluetooth Connection</h2>

We decided that the sensor data collected on the microcontroller will be sent over bluetooth in the form of a string where each of the three gas concentration values takes up 4 characters and two commas to separate them, resulting in the total payload size being 14 bytes. To receive and parse out this data on the application side, I began writing code to establish a bluetooth connection with the microcontroller. 
Unfortunately, this took up a lot of time due to the complexity of code and a myriad ways of doing this. I started out by experimenting with the built-in android library for bluetooth connections. I started with setting up a bluetooth adapter through which I could set up a socket using the function listenUsingRfcommWithServiceRecord() that takes in a UUID which can be consistent with the UUID of the microcontroller. In theory, this should allow us to use an input stream on the socket to listen to the continuous data broadcasted by the micro-controller. However, since the nature of broadcasting was not continous, this method did not work for us despite of hours of debugging and testing with a ESP32 devboard.

Here is the code snippet for the process described above:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/bluetooth_code_not_working.png" width="200" hspace="20"/>

<h2>04/04/2022 : Bluetooth Connection Continuation</h2>

After hours of debugging, I realized that listenUsingRfcommWithServiceRecord() would not work for our purposes. I decided to use a different method which involved establishing a socket directly with the paired ESP32 microcontroller. For this, I required the microcontroller to be already paired with the phone the application was running on. Once this was done, I could programmatically find the paired device and directly set up a socket with it using the function createRfcommSocketToServiceRecord(uuid). Once the socket was established, I could successfully send and receive data using it. I wrote out the code for sending out a connection message to the microcontroller once the bluetooth connection was established and then establish an input stream where we could collect the incoming data and parse it. This proved out to be a success. I could testing out the code by using the test script written by Chirag on the ESP32 devboard and display the collected data on the application. 

Once I could verify that the code was working, I transferred the bluetooth code to run on a separate thread in the application. This way, we would continuously fetch the data over bluetooth and parse it without affecting the UI tasks running on the main thread.

Here is the code snippet for the working version of Bluetooth code:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/bluetooth_code_working_1.png" width="200" hspace="20"/>

I could log the received data on the application side and confirm that the pipeline is working:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/bluetooth_log.png" width="200" hspace="20"/>]

<h2>04/11/2022 : AWS Server</h2>

For creating the server, I decided to use AWS as it offers an intuitive interface to make endpoints that can invoke user define functions. This proves to be a very useful tool to make highly available servers that can scale according to the number of incoming requests. I used AWS API Gateway to make POST and GET endpoints that invoke specific Lambda functions which are in turn connected to a DynamoDB database also hosted on AWS to store and return the data. 
I used the following schema for our NoSQL DynamoDB table where the primary key is gps coordinates:
{
    "gps": "123, 123",
    "CO2" : 123,
    "CO" : 456,
    "Propane": 789
}

I tested this pipeline from end to end by making a test POST request to the API endpoint using Postman as shown on the left of the figure below. I verified that we get a status of 200 in the response of the POST request and that the entry was successfully made in the DynamoDB table as shown on the right in the figure below: 

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/post_request.png" width="200" hspace="20"/>

Similarly, to test the GET request pipeline, I made a test GET request to the API endpoint using Postman and verified that the entire DynamoDB table was returned in the response object of the request as shown below:

<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/get_request.png" width="200" hspace="20"/>

<h2>04/18/2022: Incorporating Server Code in the Application</h2>

Last step remaining for the application was to incorporate the http calls to the server in the application code. Since we wanted to periodically send the data and periodically fetch the data from the server, I decided to write this communication code on a separate thread so that main thread operations won't be interfered with. I used the OkHTTP library to make the REST calls to the API endpointsof our server. To parse out the retrived data, I had to use the FasterXML library. Using these two libraries, I wrote our routines to send the user collected data and fetch and parse the server data on the application. On this thread, all I had to do was call the Post routine, clear out the map, call the Get routine and plot out the data returned by the Get routine. 

Here is the code snippet programmatically performing the tasks described above:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/server_code.png" width="200" hspace="20"/>


<h2>Week of 04/25/2022</h2>

After the entire app had been written out and modularly tested, the only task left was to test out the application in the real world. To fully test our application all the way from testing the Bluetooth connection and server communication to plotting out the contamination zones on the Google Maps overlay, we cleared the DynamoDB table and conducted a walk around the campus while wearing the band on ourselves.
We conducted this walk from the ECEB to Green Street through the Bardeen Quad. The figure below shows all the contamination zones we plotted on this walk along with a notification banner indicating that carbon-dioxide is detected since it exceeded our threshold. 

Here is a screenshot of the application after our test walk:
<img src="https://github.com/zatch10/ECE445/blob/master/notebook/Vedant/app_screenshot.png" width="200" hspace="20"/>
