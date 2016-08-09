The python script needs to be run on a raspberry pi, written for debian, though
that may not matter.  In order to write to a file you will have to create the
directory /mnt/sda which will have to be writable by the user that calls the script

The output files:
These are designed to be imported to a spreadsheet using tabs as delimiters, each
line represents one minute of runtime.  It is done this way rather than simply
one long row or column of data so that a spreadsheet program is not overwhelmed
by potentially a few million lines.

Example:
	1470717793.45	23.2	8	0.05	54.26	55
	91.4	312.55	27

Each number represents the number of seconds since the last time the input pin
triggered, the first number subtracts zero, so it's just unix time code for the
first input.