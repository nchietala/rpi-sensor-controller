import RPi.GPIO as gpio
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
global timer
timer = {
  2:0, 3:0, 4:0, 7:0, 8:0, 9:0,
  10:0, 11:0, 12:0, 13:0, 17:0,
  18:0, 19:0, 20:0, 22:0, 23:0,
  24:0, 25:0, 26:0, 27:0
  }

# sensor function prints a tab and the exact current time to it's channel's
# record each time it is called
def sensor(channel):
  # declare variables
  global timer
  global prevtime

  # putting the timer incrementer in an if statement prevents the output
  # of 24 million blank lines to the output file
  if prevtime[channel]:
    timer[channel] += time.time()-prevtime[channel]

  # print an output on the off chance someone is watching on a terminal
  print("Channel %s at %s" % (channel, time.time()))

  # everything that has to do with the file inside a with statement so
  # that the file closes when writing is done, this minimizes the chance
  # of file corruption if the removable disk is pulled without stopping
  # the program (in theory, I haven't done much testing on that)
  with open("%s%s.txt" % (record, channel), "a") as file:

    # check whether a minute has passed since last time the input pin
    # tripped, if subtract a minute and output a line break to the record
    # file until the timer reads less than a minute
    while timer[channel] >= 60.0:
      timer[channel] -= 60
      file.write('\n')
      # again, maybe someone has a reason to watch it on a terminal
      print("New line on channel %s" % (channel))

    # output a tab and the time since the last time the pin was tripped
    # rounded to the nearest 1/100th of a second
    file.write("        %s" % (int((time.time() - prevtime[channel])*100)/100.0))
  prevtime[channel] = time.time()

def main():

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

  # allow ^C (AKA break, Ctrl-C, or KeybaorkInterrupt to stop everything and
  # clean up the gpio settings upon exit.  this will mostly be used in testing
  try:
    while 1:
      time.sleep(1)
  except KeyboardInterrupt:
    gpio.cleanup()

if __name__ == "__main__":
  main()
