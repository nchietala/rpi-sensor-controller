#!usr/bin/env python
"""Detects and records sensor input on a Raspberry Pi on 20 indepent channels"""

import RPi.GPIO as gpio
import time
import thread
from os import system
from threading import Thread
gpio.setmode(gpio.BCM)

__author__     = "Nickholas Hietala"
__copywrite__  = "Copywrite 2016, Nickholas Hietala"
__license__    = "MIT"
__version__    = "1.0"
__maintainer__ = "Nickholas Hietala"
__status__     = "Production"

# declare global variables
global linetime
global running
global ready
global delimiter
global starttime
global directory
global drive
global record
global prevtime
global timer

# define global variables that remain constant, could be moved to a "sensor settings" file
linetime = 60
delimiter = ';'
record = "channel"
drive = "/dev/sda1"
directory = "/mnt/sda"

# define global variables that are used by software
running = 1
ready = 0
starttime = int(time.time()+5)
prevtime = {
  2:0, 3:0, 4:0, 7:0, 8:0, 9:0,
  10:0, 11:0, 12:0, 13:0, 17:0,
  18:0, 19:0, 20:0, 22:0, 23:0,
  24:0, 25:0, 26:0, 27:0
  }
timer = {
  2:0, 3:0, 4:0, 7:0, 8:0, 9:0,
  10:0, 11:0, 12:0, 13:0, 17:0,
  18:0, 19:0, 20:0, 22:0, 23:0,
  24:0, 25:0, 26:0, 27:0
  }

def sensor(channel):
  """record input and print output"""
  global ready
  global timer
  global prevtime
  global delimiter
  if ready:
    gpio.output(15, 0)

    # check whether this is the first time this channel has been called
    if prevtime[channel]:
	  # if not increment the minute timer
      timer[channel] += time.time()-prevtime[channel]
	  
	  # check whether it has been long enough to one of more new lines
      while timer[channel] >= linetime:
        timer[channel] -= linetime
		
		# write a new line to the output file
        with open("%s/%s%s.txt" % (directory, record, channel), "a") as file:
          file.write('\n')
        #print an output on the off chance someone is watching on a terminal
        print("New line on channel %s" % (channel))
    print("Channel %s at %s" % (channel, time.time()))
	
    # write the delimiter followed by the appropriate time stamp
    with open("%s/%s%s.txt" % (directory, record, channel), "a") as file:
      file.write("%s%s" % (delimiter, int((time.time() - prevtime[channel])*100)/100.0))
    prevtime[channel] = time.time()
	
	# wait long enough that a person can see the LED blink
    time.sleep(0.05)
	# turn the LED on
    gpio.output(15, ready)

def switch():
  """keep ready state in order, and mount/unmount
  the flash drive based on whether the switch is on:
  ON: the flash drive is mounted and the sensors are writing to it, the LED is ON
  OFF: the flash drive is unmounted and the sensors are not writing, the LED is OFF"""
  global running
  global ready
  gpio.setmode(gpio.BCM)
  
  while running:
    # check whether the program has just started, or if the switch is off
    if not gpio.input(14) or time.time() < starttime:
	  # this wait timer allows noisier channels to fluctuate (which they do) without
	  # writing false positives to the output
      while time.time() < starttime:
        time.sleep(0.5)
		
	  # stop writing to the drive
      ready = 0
	  
	  # repeatedly attempt to unmount the drive until it succeeds
      while system("umount %s" % (drive)):
        system("mount %s %s" % (drive, directory))
        print "Unmounting error, make sure the drive is inserted."
        time.sleep(1)
      print "Unmounted"
	  
	  # turn off the LED to show the drive is safe to remove
      gpio.output(15, 0)
	  
	  # wait for the switch to flip
      while not gpio.input(14):
        time.sleep(0.1)
		
	  # turn on the LED to show the drive is not safe to remove
      gpio.output(15, 1)
	  
	  # repeatedly attempt to mount the drive until it succeeds
      while system("mount %s %s" % (drive, directory)):
        system("umount %s" % (drive))
        print "Mounting error, make sure the dirve is inserted"
        time.sleep(1)
      print "Mounted"
	  
	  # allow the sensors to write to the output files again
      ready = 1
	
    time.sleep(1)

def main():
  """set up channels, start edge detection to sensor function as a callback and wait for someone to press break"""
  global ready
  global running
  
  # set up input channels, 2 and 3 have hardware pull-up resistors
  # 5, 6, 14, 15, 16 and 21 throw errors when used
  gpio.setup([2, 3],  gpio.IN)
  gpio.setup([
  4, 7, 8, 9, 10,
  11, 12, 13, 17,
  18, 19, 20, 22,
  23, 24, 25, 26, 27
  ], gpio.IN, pull_up_down=gpio.PUD_UP)
  gpio.setup(14, gpio.IN, pull_up_down=gpio.PUD_DOWN)
  gpio.setup(15, gpio.OUT)
  
  # get all the input pins listening for rising edge detect, falling edge
  # was considered but gave erratic results
  for i in range (2,28):
    if i!=5 and i!=6 and i!=14 and i!=15 and i!=16 and i!=21:
      gpio.add_event_detect(i, gpio.RISING, callback=sensor)

  # start the switch function in a new thread
  Thread(target = switch).start()

  # allow ^C (AKA break, Ctrl-C, or KeybaorkInterrupt) to stop everything and
  # clean up the gpio settings upon exit.  this will mostly be used in testing
  try:
    while 1:
      time.sleep(1)
  except KeyboardInterrupt:
    gpio.cleanup()
	# set this to 0 to kill the "switch" thread
    running = 0

if __name__ == "__main__":
  main()
