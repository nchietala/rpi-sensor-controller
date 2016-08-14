INSTALLATION:  
Install a new copy of raspbian to a raspberry pi, NOOBS is easy to use and can be found HERE:  
https://www.raspberrypi.org/downloads/noobs/  
Connect the pi to a network cable and power, determine it's IP address, connect to it using SSH and run the following commands:  
cd  
wget https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/install.sh  
sudo sh install.sh  

USAGE:  
You must have a flash drive formatted to FAT32 insterted into the pi BEFORE you boot it. Keep the flash drive in the pi for the entire duration of your data collecting experiment. When finished turn off the pi BEFORE removing the flash drive to prevent data corruption from pulling a mounted drive.
