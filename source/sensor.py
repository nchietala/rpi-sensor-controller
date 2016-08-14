#!usr/bin/env python
"""Detects and reords sensor input on a Raspberry Pi on 20 indepent channels"""

import RPi.GPIO as gpio
import time
gpio.setmode(gpio.BCM)

__author__     = "Nickholas Hietala"
__copywrite__  = "Copywrite 2016, Nickholas Hietala"
__license__    = "MIT"
__version__    = "1.0"
__maintainer__ = "Nickholas Hietala"
__status__     = "Production"

global delimiter
delimiter = ';'
global starttime
starttime = time.time()
global record
record = "/mnt/sda/channel"
global prevtime
prevtime = {
  2:0, 3:0, 4:0, 7:0, 8:0, 9:0,usr
  10:0, 11:0, 12:0, 13:0, 17:0,
  18:0, 19:0, 20:0, 22:0, 23:0,
  24:0, 25:0, 26:0, 27:0
  }
global timer
timer = {
  2:0, 3:0, 4:0, 7:0, 8:0, 9:0,
  10:0, 11:0, 12:0, 13:0, 17:0,
  18:0, 19:0, 20:0, 22:0, 23:0,
  24:0, 25:0, 26:0, 27:0
  }


def sensor(channel):
  """record input and print output"""
  if time.time() > starttime + 5:
    # declare variables
    global timer
    global prevtime
    global delimiter

    # starting from the second time the sensor is tripped check how long it has
    # been since the first time the sensor was tripped, if it's been more than
    # a minute enter a line break for every minute it has been and subtract a
    # minute for each line break
    if prevtime[channel]:
      timer[channel] += time.time()-prevtime[channel]
      while timer[channel] >= 60.0:
        timer[channel] -= 60
        with open("%s%s.txt" % (record, channel), "a") as file:
          file.write('\n')
	#print an output on the off chance someone is watching on a terminal
        print("New line on channel %s" % (channel))
    print("Channel %s at %s" % (channel, time.time()))

    # write the delimiter followed by the appropriate time stamp
    with open("%s%s.txt" % (record, channel), "a") as file:
      file.write("%s%s" % (delimiter, int((time.time() - prevtime[channel])*100)/100.0))
    prevtime[channel] = time.time()

def main():
  """set up channels, start endge detection to sensor function as a callback and wait for someone to press break"""
  # set up input channels, 2 and 3 have hardware pull-up resistors
  # 5, 6, 14, 15, 16 and 21 throw errors when used
  gpio.setup([2, 3],  gpio.IN)
  gpio.setup([
  4, 7, 8, 9, 10,
  11, 12, 13, 17,
  18, 19, 20, 22,
  23, 24, 25, 26, 27
  ], gpio.IN, pull_up_down=gpio.PUD_UP)

  # get all the input pins listening for rising edge detect, falling edge
  # was considered but gave erratic results
  for i in range (2,28):
    if i!=5 and i!=6 and i!=14 and i!=15 and i!=16 and i!=21:
      gpio.add_event_detect(i, gpio.RISING, callback=sensor)

  # allow ^C (AKA break, Ctrl-C, or KeybaorkInterrupt) to stop everything and
  # clean up the gpio settings upon exit.  this will mostly be used in testing
  try:
    while 1:
      time.sleep(1)
  except KeyboardInterrupt:
    gpio.cleanup()

if __name__ == "__main__":
  main()
