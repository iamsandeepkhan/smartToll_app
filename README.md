# smartToll_app
<h1>Smart Toll</h1>
<p>This project showcases a simple interconnectivity among a server, a mobile application and a hardware. The mobile application (android) majorly deals with user input and exchanges data with the server (PHP). The server inturn publishes data onto the mqtt channel. The hardware (raspberry pi 3) is connected to the same mqtt channel and depending on the message sent over the channel does specific operation.<p>

<h3>Proposed Working Principle:</h3>
<p>Consider a metro station with tollbooth. Commutators scan a QR Code on the tollbooth which unlocks the toll gate. The travel through the metro and once they reach the destination station, they scan the QR Code on the toll booth again to unlock the gate. The transaction not only gets recorded but the entire process requires no physical exchange of money. <p>
<ul>
  <li>A tollbooth consists a QR Code attached to it.</li>
  <li>The QR Code encodes the station and gate number of the tollbooth. This helps to note which tollbooth is the commutator using. The   information is sent to the server when the QR Code is scanned.</li>
  <li>Use the mobile application to scan the QR code on the tollbooth.</li>
  <li>Data associated with the tollbooth and user is spent over to the server.</li>
  <li>Server updates the database and spends message over MQTT Channel to open the connected toll gate.</li>
  <li>Simlarly the commutator can unlock the gate at the destination station.</li>
</ul>

<h3>Requirements:</h3>
<ol>
  <li>Raspberry Pi/Arduino - This handles the hardware part of the system. We have used Raspberry to avoid the trouble of setting up        ethernet for arduino.</li>
  <li>Ultrasonic Sensor (HC-SR04) - This will enable the auto closing of the gate after a commutator passes through the gate.</li>
  <li>Server</u> - This handles the centralized transaction and updation of user records. We have used XAMPP and coded our server with      PHP.</li>
  <li>Mobile Application - This handles the interaction with the user. The camera of the mobile scans the QR Code and use internet to     relay data to the server. We have used Android.</li>
  <li>MQTT Broker - (Message Queuing Telemetry Transport) In lucid terms it is just a channel where devices can <b>publish</b> or         <b>subscribe</b> to a <b>topic</b>. There are many free brokers available for testing!</li>
   <li>Database System - Store the user data for authentication, transaction and tour details. We have used MYSQL.</li>
  </ol>

<h3>Repositories:</h3>
<ul>
  <li><b>Android (Mobile Application)</b> - https://github.com/iamsandeepkhan/smartToll_app</li>
  <li><b>PHP (Server)</b> - https://github.com/iamsudiptasaha/smartToll_server</li>
  <li><b>Raspberry pi (Hardware)</b> - https://github.com/iamsudiptasaha/smartToll_hardware</li>
</ul>
    
 <h3>About this repository:</h3>

<p>This repository contains the mobile application functionalities to handle the android application. The application uses a QR code scanner to scan the QR codes on the tollbooth. The data is then sent over to the server using HTTP protocol. The data is processed and the gate is unlocked accordingly. User information associated with a tour is also updated. The application provides tour history and a demo bank system. In real time it is recommended to use third party bank api.</p>

<p>Link to JSON : https://github.com/bluerhinos/phpMQTT</p>
<p>Link to Vision API : https://developers.google.com/vision/android/barcodes-overview</p>

<h3>Miscellaneous informations : </h3>
<ul>
  <li>Return values : JSON Encoded data. Contains two mandatory fields to determine successful server operation:
    <p>"sucess" : "true"/"false" - If the operation was successful or not.</p>
    <p>"errorCode" : If sucess is false then we may get the following error codes. The associated error is mentioned alongside.<p>
    <p>
      <ul>
        <li>"1" = Invalid data or empty data.
        <li>"2"= Improper option.
        <li>"3"= Failed DatabaseConnectivity, try again.
        <li>"4"= Duplicate username, rejected.
        <li>"5"= MYSQL error.
        <li>"6"= no such user</li>
     </ul>
    </p>
  </li>
  
   <li>Tour status : Different status codes for tours and their associated status is mentioned along side:
     <p>
      <ul>
        <li>"0" = Default tour status value.
        <li>"1"= Travelling.
        <li>"2"= Completed.
     </ul>
    </p>
  </li>
</ul>
