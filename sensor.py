import RPi.GPIO as gpio
from threading import Thread
import time
gpio.setmode(gpio.BCM)

global record
record = "/mnt/sda/channel"
global prevtime
prevtime = {
  2:0, 3:0, 4:0, 7:0, 8:0, 9:0,
  10:0, 11:0, 12:0, 13:0, 17:0,
  18:0, 19:0, 20:0, 22:0, 23:0,
  24:0, 25:0, 26:0, 27:0
  }

# timer function runs in its own thread
# each minute it appends a new line to each record
def newline():
  print "Minute:"
  for i in range (2,28):
    if i!=5 and i!=6 and i!=14 and i!=15 and i!=16 and i!=21:
      with open ("%s%s.txt" % (record, i), "a") as file:
        file.write('\n')

# sensor function prints a tab and the exact current time to it's channel's
# record each time it is called
def sensor(channel):
  print("Channel %s at %s" % (channel, time.time()))
  with open("%s%s.txt" % (record, channel), "a") as file:
    file.write("        %s" % (time.time() - prevtime[channel]))
  prevtime[channel] = time.time()

def main():
  # set up input channels, 2 and 3 have hardware pull-up resistors
  # 5, 6, 14, 15, 16 and 21 throw errors when used
  gpio.setup(2,  gpio.IN)
  gpio.setup(3,  gpio.IN)
  gpio.setup([4, 7, 8, 9, 10, 11, 12, 13, 17, 18, 19, 20, 22, 23, 24, 25, 26, 27], gpio.IN, pull_up_down=gpio.PUD_UP)

  for i in range (2,28):
    if i!=5 and i!=6 and i!=14 and i!=15 and i!=16 and i!=21:
      gpio.add_event_detect(i, gpio.RISING, callback=sensor)

  try:
    while 1:
      time.sleep(60)
      Thread(target = newline).start()
  except KeyboardInterrupt:
    gpio.cleanup()

if __name__ == "__main__":
  try:
    main()
  except KeyboardInterrupt:
    gpio.cleanup()
