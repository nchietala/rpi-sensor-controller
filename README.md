**INSTALLATION:** 
Install a new copy of raspbian to a raspberry pi, NOOBS is easy to use and can be found HERE:  
https://www.raspberrypi.org/downloads/noobs/  
Connect the pi to a network cable and power, determine it's IP address, connect to it using SSH and run the following commands:  
  
wget https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/install.sh  
sudo sh install.sh  

**USAGE:**  
This software is designed to record data to a flash drive insterted into the raspberry pi.  You will need a flash drive inserted into the raspberry pi for it to run.
There should be a single-pole single-throw switch between 5V and GPIO pin 14, and a 3.7V LED with a 330Ω (or similar) resistor between GPIO pin 15 and ground. GPIO pins 2, 3, 4, 7, 8, 9, 10, 11, 12, 13, 17, 18, 19, 20, 22, 23, 24, 25, 26, and 27 all detect changes in voltage and will write to the output file on the drive when they do so, they have pull-up resistors configured in software.  

If properly configured and installed:  
>With the switch in the ON position:  
>>The LED will turn on 5-10 seconds after the pi is given power  
If the LED is on, the pi is ready to record data to the flash drive  
The LED will blink once each time it records data  


>With the switch in the OFF position:  
>>The LED will turn off, after which the flash drive can be removed for data retrieval  

**DO NOT** remove the flash drive while the LED is on  
**DO NOT** unplug the raspberry pi while the LED is on  
If the LED is on, it is an indicator that the drive is mounted and it cannot lose power or be safely removed without risking data corruption.
