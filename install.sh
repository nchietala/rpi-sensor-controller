#!/bin/sh
# /home/pi/install.sh
# install script for multi-channel input sensor

# put all the files where they go
wget -O /home/pi/sensor.py https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/source/sensor.py
wget -O /etc/init.d/startsensor https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/source/sensorstart
wget -O /home/pi/LICENSE.md https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/LICENSE.md
wget -O /home/pi/README.md https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/README.md

# make the necessary files executable, readable, and writeable
chmod 777 /etc/init.d/sensor.py
chmod 777 /etc/init.d/sensorstart

# make the start script actually work
update-rc.d sensorstart defaults

# mount a flash drive at boot
echo "/dev/sda1       /mnt/sda        vfat    defaults                0       0" >> /etc/fstab

#clean up
rm install.sh
