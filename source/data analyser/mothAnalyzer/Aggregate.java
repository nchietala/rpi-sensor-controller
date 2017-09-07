/*
Aggregator class, this class produces and holds agregate data from Channel
objects, and houses methods for gather statistical data from them
 */
package mothAnalyzer;

public class Aggregate {

    public final double startTime, duration, distance, avgSpeed, flySpeed,
            flyTime, restTime, longestLeg, classWidth, longestFlight, longestRest;
    public final int[] hist;
    public final double[] prog;

    Aggregate(Channel g) {
        this(g, 32, .25);
    }

    Aggregate(Channel g, int classes, double classWidth) {
        startTime = g.getFirstEntry();
        duration = g.getDuration();
        distance = g.getDistance();
        avgSpeed = g.getAvgSpeed();
        flySpeed = g.getAvgSpeedInFlight();
        flyTime = g.getFlightTime();
        restTime = g.getRestTime();
        longestLeg = g.getLongDist();
        longestFlight = g.getLongFlight();
        longestRest = g.getLongRest();
        this.classWidth = classWidth;
        hist = g.getFreqDist(classes, classWidth);
        prog = g.getDistanceProgression();
    }

    private Aggregate(double avgSpeed, double classWidth,
            double distance, double duration, double flySpeed,
            double flyTime, double longestFlight, double longestLeg,
            double longestRest, double restTime, double startTime,
            int[] hist, double[] prog) {
        this.avgSpeed = avgSpeed;
        this.classWidth = classWidth;
        this.distance = distance;
        this.duration = duration;
        this.flySpeed = flySpeed;
        this.flyTime = flyTime;
        this.hist = hist;
        this.longestFlight = longestFlight;
        this.longestLeg = longestLeg;
        this.longestRest = longestRest;
        this.restTime = restTime;
        this.startTime = startTime;
        this.prog = prog;
    }

    public String serialize() {
        String serial
                = hist.length + " "
                + avgSpeed + " "
                + classWidth + " "
                + distance + " "
                + duration + " "
                + flySpeed + " "
                + flyTime + " "
                + longestFlight + " "
                + longestLeg + " "
                + longestRest + " "
                + restTime + " "
                + startTime + " ";
        for (int i = 0; i < hist.length; i++) {
            serial = serial + hist[i] + " ";
        }
        serial = serial + ";";
        for (int i = 0; i < prog.length; i++) {
            serial = serial + prog[i] + " ";
        }
        return serial.trim();
    }

    public static Aggregate deserialize(String serial) throws IllegalArgumentException {
        int[] hist;
        double[] progression;
        String[] progStr = new String[0];
        String[] pair;

        pair = serial.split(";");
        String[] arr = pair[0].split(" ");
        if (pair.length == 2) {
            progStr = pair[1].split(" ");
        }
        for (int i = 0; i < arr.length; i++) {
            if (i > 0 && i < 12) {
                try {
                    Double.parseDouble(arr[i]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("\"" + arr[i] + "\" is not a valid number in this position.");
                }
            } else {
                try {
                    Integer.parseInt(arr[i]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("\"" + arr[i] + "\" is not a valid number in this position.");
                }
            }

            /**
             * } else { try { Integer.parseInt(arr[i]); } catch
             * (NumberFormatException e) { throw new
             * IllegalArgumentException("\"" + arr[i] + "\" is not a valid
             * number in this position."); } }*
             */
        }
        if (arr.length != 12 + Integer.parseInt(arr[0])) {
            throw new IllegalArgumentException("The string proveded is not deserializable to aggregator data.");
        }
        hist = new int[Integer.parseInt(arr[0])];

        for (int i = 0; i < Integer.parseInt(arr[0]); i++) {
            hist[i] = Integer.parseInt(arr[12 + i]);
        }

        progression = new double[progStr.length];
        for (int i = 0; i < progStr.length; i++) {
            progression[i] = Double.parseDouble(progStr[i]);
        }

        for (int i = 1; i < progression.length; i++) {
            if (progression[i - 1] > progression[i]) {
                System.out.println("Invalid data detected: omitting...");
                progression = new double[0];
                break;
            }
        }

        return new Aggregate(Double.parseDouble(arr[1]),
                Double.parseDouble(arr[2]),
                Double.parseDouble(arr[3]),
                Double.parseDouble(arr[4]),
                Double.parseDouble(arr[5]),
                Double.parseDouble(arr[6]),
                Double.parseDouble(arr[7]),
                Double.parseDouble(arr[8]),
                Double.parseDouble(arr[9]),
                Double.parseDouble(arr[10]),
                Double.parseDouble(arr[11]),
                hist, progression);
    }
}
