/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mothAnalyzer;

import java.util.Scanner;

/**
 *
 * @author Nickholas Hietala
 */
public class Channel {

    public final double[][] file;
    private final double[] speedPerMinute;
    private final double[] distanceProgression;
    public double circumference;
    public double cutOff;
    private final int rotations;
    private final double duration;
    private final double firstEntry;

    /**
     * Constructor
     *
     * @param newfile The complete path and filename of the file to be parsed
     * @param newCirc The circumference of the flight mill, doesn't matter what
     * unit, the math is the same
     * @param newCutOff The minimum amount of time between rotations before a
     * moth is considered "at rest"
     * @throws java.io.FileNotFoundException
     */
    Channel(String newfile, double newCirc, double newCutOff, int duration) throws java.io.FileNotFoundException, IllegalArgumentException {
        cutOff = newCutOff;
        circumference = newCirc;
        Scanner in = new Scanner(new java.io.File(newfile));
        String string = "";
        for (int i = 0; in.hasNext() && i < duration; i++) {
            string = string + in.nextLine() + "\n";
        }

        String[] linesArray = string.split("\n");
        double[][] buildFile = new double[linesArray.length][0];

        //generate jagged array
        for (int i = 0; i < buildFile.length; i++) {

            //declare the array to for this line
            String[] stringArray;

            //create the array of strings for this line
            if (!linesArray[i].equals("")) {
                stringArray = linesArray[i].substring(1).split(";");
            } else {
                stringArray = new String[0];
            }

            //declare file line array to appropriate length
            buildFile[i] = new double[stringArray.length];

            //assign values to file line array
            for (int j = 0; j < buildFile[i].length; j++) {
                buildFile[i][j] = Float.parseFloat(stringArray[j]);
            }
        }
        file = buildFile;

        firstEntry = findFirstEntry();
        if (illegal()) throw new IllegalArgumentException();
        this.duration = calcDuration(duration);
        rotations = calcDistance();
        speedPerMinute = buildSpeedPerMinute();
        distanceProgression = buildDistanceProgression();

    }

    private boolean illegal() {
        if (file.length == 1)
            return file[0].length == 1;
        return firstEntry == file[file.length-1][file[file.length-1].length-1];
    }
    
    private double findFirstEntry() {
        double ret = 0;
        for (double[] i : file) {
            if (i.length != 0) {
                ret = i[0];
                break;
            }
        }

        return ret;
    }

    private double[] buildSpeedPerMinute() {
        double[] newSpeedPerMinute = new double[file.length];

        for (int i = 0; i < file.length; i++) {
            newSpeedPerMinute[i] = file[i].length * circumference / 60;
        }
        return newSpeedPerMinute;
    }
    
    private double[] buildDistanceProgression(){
        double[] newDistanceProgression = new double[file.length];
        newDistanceProgression[0] = circumference * file[0].length;
        for (int i = 1; i < file.length; i++){
            newDistanceProgression[i] = newDistanceProgression[i-1] + (circumference * file[i].length);
        }
        return newDistanceProgression;
    }
    
    
    /**
     * Returns the total distance traveled over the total time of the experiment
     *
     * @return The average units per second traveled
     */
    public double getAvgSpeed() {
        return circumference * rotations / duration;
    }

    /**
     * Returns the totals distance traveled over the amount of total times less
     * than the maximum cutoff This is for measuring the average flight speed
     * while ignoring the time spent not in motion
     *
     * @return The average units per second of speed in flight
     */
    public double getAvgSpeedInFlight() {
        double time = 0;
        //this conditional is better here than in the loop so at to reduce processing time
        if (firstEntry < cutOff) {
            time -= firstEntry;
        }

        //add all rotation times less than the cutoff
        for (double[] file1 : file) {
            for (double file2 : file1) {
                if (file2 < cutOff) {
                    time += file2;
                }
            }
        }

        //the moth only traveled while flying, so distance didn't change
        return getDistance() / time;
    }

    /**
     * Calculate and return the sum of all entries but the first
     *
     * @return The total duration of the experiment
     */
    private double calcDuration(int duration) {
        if (Integer.MAX_VALUE == duration) {
            double time = 0 - firstEntry;
            for (double[] file1 : file) {
                for (double file2 : file1) {
                    time += file2;
                }
            }
            return time;
        }
        return duration * 60;
    }

    /**
     * Calculate and return the total duration of the experiment
     *
     * @param increment 60 for minutes, 3600 for hours, 86400 for days
     * @return The duration of the experiment in seconds
     */
    public double getDuration(double increment) {
        return duration / increment;
    }

    /**
     * Calculate and return the total duration of the experiment
     *
     * @return The duration of the experiment in seconds
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Calculate and return the total number of rotations
     *
     * @return The total number of rotations
     */
    private int calcDistance() {
        int count = 0;
        for (double[] file1 : file) {
            count += file1.length;
        }
        return count;
    }

    /**
     * Calculate and return the total distance traveled
     *
     * @return The total distance traveled in the unit of the distance parameter
     */
    public double getDistance() {
        return circumference * rotations;
    }

    /**
     * Generates and returns an array of rotation time frequencies
     *
     * @param classes The number of classes to be made
     * @param classWidth The width of the classes
     * @return The completed array
     */
    public int[] getFreqDist(int classes, double classWidth) {
        int[] freqDist = new int[classes];
        for (int i = 0; i < freqDist.length; i++) {
            freqDist[i] = 0;
        }
        for (double[] file1 : file) {
            for (double file2 : file1) {
                if (file2 / classWidth > freqDist.length - 1) {
                    freqDist[freqDist.length - 1]++;
                } else {
                    freqDist[(int) (file2 / classWidth + .5)]++;
                }
            }
        }

        return freqDist;
    }

    /**
     * Calculate the amount of time that the moth spent in flight
     *
     * @return The amount of time spent in flight in seconds
     */
    public double getFlightTime() {
        double time = 0;
        for (int i = 0; i < file.length; i++) {
            for (int j = 0; j < file[i].length; j++) {
                if (i + j != 0 && file[i][j] < cutOff) {
                    time += file[i][j];
                }
            }
        }
        return time;
    }

    /**
     * Calculate the amount of time spent at rest
     *
     * @return The total time spent at rest
     */
    public double getRestTime() {
        return duration - getFlightTime();
    }

    /**
     * Calculate the longest time spent in flight
     *
     * @return Longest time in flight in seconds
     */
    public double getLongFlight() {
        double time = 0;
        double candidate = 0;
        for (int i = 0; i < file.length; i++) {
            for (int j = 0; j < file[i].length; j++) {
                if (i + j != 0 && file[i][j] < cutOff) {
                    candidate += file[i][j];
                } else if (time < candidate) {
                    time = candidate;
                    candidate = 0;
                }
            }
        }
        if (time < candidate) {
            time = candidate;
        }
        return time;
    }

    /**
     * Calculate the greatest distance traveled in a single hop
     *
     * @return The distance traveled in the unit of the circumference parameter
     * set in the constructor
     */
    public double getLongDist() {
        double count = 0;
        double candidate = 0;
        for (int i = 0; i < file.length; i++) {
            for (int j = 0; j < file[i].length; j++) {
                if (i + j != 0 && file[i][j] < cutOff) {
                    candidate++;
                } else if (count < candidate) {
                    count = candidate;
                    candidate = 0;
                }
            }
        }
        if (count < candidate) {
            count = candidate;
        }
        return count * circumference;
    }

    /**
     * Returns the greatest time spent at rest
     *
     * @return
     */
    public double getLongRest() {
        double rest = 0, time = 0 - firstEntry;
        for (int i = 0; i < file.length; i++) {
            for (int j = 0; j < file[i].length; j++) {
                if (i + j != 0) {
                    rest = Math.max(rest, file[i][j]);
                }
                time += file[i][j];
            }
        }
        return Math.max(rest, (duration - time));
    }

    /**
     * Calculate the standard deviation of the flight speed incremented over
     * distances of the circumference
     *
     * @return the standard deviation
     */
    public double getStdDev() {
        double mean = getAvgSpeedInFlight();
        double stdDev = 0;
        int n = -1;

        for (int i = 0; i < file.length; i++) {
            for (int j = 0; j < file[i].length; j++) {
                if (i + j != 0 && file[i][j] < cutOff) {
                    stdDev += Math.pow(circumference / file[i][j] - mean, 2);
                    n++;
                }
            }
        }

        stdDev = stdDev / n;
        stdDev = Math.sqrt(stdDev);

        return stdDev;
    }

    /**
     * Calculate and construct an array of speeds at each minute
     *
     * @return An array of speeds in the unit of the circumference parameter per
     * second
     */
    public double[] getSpeedPerMinute() {
        return speedPerMinute;
    }
    
    public double[] getDistanceProgression() {
        return distanceProgression;
    }

    public double getFirstEntry() {
        return firstEntry;
    }

    public Aggregate getAgg() {
        return new Aggregate(this);
    }
}
