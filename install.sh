#!/bin/sh
# /home/pi/install.sh
# install script for multi-channel input sensor

#update all the things
apt-get --yes update
apt-get --yes upgrade

# put all the files where they go
curl https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/source/sensor.py > /home/pi/sensor.py
curl https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/source/sensorstart > /etc/init.d/sensorstart
curl https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/LICENSE.md > /home/pi/LICENSE.md
curl https://raw.githubusercontent.com/xervir/rpi-sensor-controller/master/README.md > /home/pi/README.md

# make the necessary files executable, readable, and writeable
chmod 777 /etc/init.d/sensor.py
chmod 777 /etc/init.d/sensorstart

# make the start script actually work
update-rc.d sensorstart defaults

# install screen and python, python because the script runs on it, and screen because it makes it easier to
# to ensure that the script can be easily monitored without accidentally killing the session it runs in
apt-get --yes --force-yes install screen python

mkdir /mnt/sda

# reboot the machine
sudo shutdown -r 1 "The installation is complete, the controller will now reboot. Make sure sensor leads and switch are installed."
