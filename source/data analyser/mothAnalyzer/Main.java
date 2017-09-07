package mothAnalyzer;

/**
 * Analyzes data from flight mills and produces and report to be viewed Also
 * saves that report as a PDF
 *
 * @author Nickholas Hietala
 */
//import com.sun.prism.paint.Color;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;

import java.io.File;
import java.io.PrintWriter;
import javafx.geometry.Pos;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Application {

    double circ = 0.957557441;
    double cutOff = 10;
    double clWidth = 100;
    int classes = 800;
    String dir;

    @Override
    public void start(Stage primstage) {
        try {
            dir = Main.class.
                    getProtectionDomain().
                    getCodeSource().
                    getLocation().
                    toURI().
                    getPath();
            dir = dir.substring(0, dir.lastIndexOf("/") + 1);

            Button disc, aggr;
            aggr = new Button("Aggregate");
            disc = new Button("Discrete");

            TextField timeSet;
            timeSet = new TextField("Hours, Discrete run only");

            timeSet.textProperty().addListener((ob, ov, nv) -> {
                try {
                    Integer.parseInt(nv);
                } catch (NumberFormatException ex) {
                    if (!nv.equals("")) {
                        timeSet.setText(ov);
                    }
                }
            });

            aggr.setOnAction((ev) -> {
                primstage.close();
                aggregate();
            });
            disc.setOnAction((ev) -> {
                primstage.close();
                try {
                    discrete(Integer.parseInt(timeSet.getText()) * 60);
                } catch (NumberFormatException ex) {
                    discrete(Integer.MAX_VALUE);
                }
            });

            disc.setPrefWidth(100);
            aggr.setPrefWidth(100);
            VBox box = new VBox(timeSet, disc, aggr);
            box.setPrefSize(275, 100);
            box.setSpacing(10);
            box.setAlignment(Pos.CENTER);
            primstage.setScene(new Scene(box));
            primstage.setResizable(false);
            primstage.show();

        } catch (java.net.URISyntaxException e) {
            TextArea t = new TextArea("Error!  Cannot parse folder name!");
            t.setFont(new Font("Courier New", 18));
            t.setEditable(false);
            t.setPrefSize(800, 70);
            Scene s = new Scene(t);
            Stage st = new Stage();
            st.setScene(s);
            st.setTitle("Error!");
            st.show();
        }

    }

    public void discrete(int maxDuration) {

        //count the number of files that are appropriately named
        int count = 0;
        for (int i = 0; i < 20; i++) {
            java.io.File f = new java.io.File(dir + "channel" + getChanNum(i) + ".txt");
            if (f.exists()) {
                count++;
                System.out.println("Found file: channel" + getChanNum(i) + ".txt");
            }
        }
        if (count == 0) {
            System.out.println("No valid files found.");
            System.exit(0);
        }
        Channel[] chan = new Channel[count];

        //start looping through the files
        for (int i = 0, g = 0; i < 20; i++) {
            System.gc();
            try {

                //construct the Channel object
                chan[g] = new Channel(dir + "channel" + getChanNum(i) + ".txt", circ, cutOff, maxDuration);

                Aggregate agg = new Aggregate(chan[g], classes, 1.0 / clWidth);
                //this giant block of spaghetti contructs a string reasonably formatted
                //and made up of calculated data from the Channel object
                String statistics
                        = "          Total Distance Flown     "
                        + String.valueOf(.01 * ((int) (100 * agg.distance))) + " m\n"
                        + "                Total Duration     "
                        + String.valueOf((int) (agg.duration / 60)) + " min\n"
                        + "\n"
                        + "                 Average Speed     "
                        + String.valueOf(((int) (100 * agg.avgSpeed)) / 100.0) + " m/s\n"
                        + "       Average Speed in Flight     "
                        + String.valueOf(((int) (100 * agg.flySpeed)) / 100.0) + " m/s\n"
                        + "\n"
                        + "          Total Time in Flight     "
                        + String.valueOf((int) (agg.flyTime / 60)) + " min\n"
                        + "            Total Time at Rest     "
                        + String.valueOf((int) (agg.restTime / 60)) + " min\n"
                        + "\n"
                        + "        Longest Time in Flight     "
                        + String.valueOf((int) (agg.longestFlight / 60)) + " min\n"
                        + "          Longest Time at Rest     "
                        + String.valueOf((int) (agg.longestRest / 60)) + " min\n"
                        + "Greatest Distance w/o Stopping     "
                        + String.valueOf((int) (100 * agg.longestLeg) / 100.0) + " m\n";

                //textarea object so that the user can copy and paste the statistics
                //at their leisure
                TextArea text = new TextArea(statistics);
                text.setEditable(false);

                //declare an areachart
                NumberAxis x = new NumberAxis();
                NumberAxis y = new NumberAxis();
                x.setLabel("Time (minutes)");
                y.setLabel("Speed (m/s)");
                AreaChart<Number, Number> overTime
                        = new AreaChart<>(x, y);
                overTime.setLegendVisible(false);
                overTime.setCreateSymbols(false);

                //create series for the areachart
                XYChart.Series series = new XYChart.Series();
                double[] chartData = chan[g].getSpeedPerMinute();

                //populate area chart
                for (int q = 0; q < chartData.length; q++) {
                    series.getData().add(new XYChart.Data(q, chartData[q]));
                }
                series.setName("Data");
                //place series in area chart
                overTime.getData().add(series);

                //declare barchart for rotation time distribution
                CategoryAxis histx = new CategoryAxis();
                NumberAxis histy = new NumberAxis();
                BarChart<String, Number> histogram
                        = new BarChart<>(histx, histy);
                histogram.setLegendVisible(false);

                //create and populate series for the areachart
                XYChart.Series histSeries = new XYChart.Series();
                int[] histchartData = agg.hist;

                //populate bar chart
                for (int q = 0; q < histchartData.length; q++) {
                    histSeries.getData().add(new XYChart.Data(String.valueOf((q + 1) / clWidth), histchartData[q]));
                }
                histSeries.setName("Data");
                //place series in bar chart
                histogram.getData().addAll(histSeries);

                //declare an areachart
                NumberAxis progX = new NumberAxis();
                NumberAxis progY = new NumberAxis();
                //progX.setLabel("Time (minutes)");
                progY.setLabel("Distance Traveled (meters)");
                AreaChart<Number, Number> progression
                        = new AreaChart<>(progX, progY);
                progression.setLegendVisible(false);
                progression.setCreateSymbols(false);

                //create series for the areachart
                XYChart.Series progSeries = new XYChart.Series();
                double[] progChartData = chan[g].getDistanceProgression();

                //populate area chart
                for (int q = 0; q < progChartData.length; q++) {
                    progSeries.getData().add(new XYChart.Data(q, progChartData[q]));
                }
                progSeries.setName("Data");
                //place series in area chart
                progression.getData().add(progSeries);

                exportAggregateData(agg, dir, getChanNum(i));
                //increment channel name
                g++;

                //create and populate boxes
                HBox Hdisplay = new HBox(text, histogram);
                VBox Vdisplay = new VBox(Hdisplay, progression, overTime);

                //create, populate, name, and display the window
                Scene scene = new Scene(Vdisplay);

                //set object sizes and formats
                overTime.getStylesheets().add("chart.css");
                text.setFont(new Font("Courier New", 18));
                overTime.setPrefWidth(1100);
                overTime.setPrefHeight(350);
                progression.getStylesheets().add("chart.css");
                text.setFont(new Font("Courier New", 18));
                progression.setPrefWidth(1100);
                progression.setPrefHeight(350);
                text.setPrefHeight(150);
                text.setPrefWidth(575);
                histogram.setPrefHeight(150);
                histogram.setPrefWidth(525);
                histogram.setAnimated(false);
                overTime.setAnimated(false);
                progression.setAnimated(false);

                //show the stuff
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Channel " + getChanNum(i));
                stage.show();

                //create the PDF file
                PDFWriter.export(progression, overTime, histogram, dir + "Channel " + getChanNum(i) + ".pdf", "Channel " + getChanNum(i), statistics.split("\n"));

            } //ignore missing files
            catch (java.io.FileNotFoundException ex) {
            } //report misformatted files
            catch (NumberFormatException ex) {
                TextArea t = new TextArea("File " + getChanNum(i) + " contains text.\n"
                        + "Remove the offending file, or find and remove the offending text.");
                t.setFont(new Font("Courier New", 18));
                t.setEditable(false);
                t.setPrefSize(800, 70);
                Scene s = new Scene(t);
                Stage st = new Stage();
                st.setScene(s);
                st.setTitle("Error!");
                st.show();
            } catch (IllegalArgumentException ex) {
                TextArea t = new TextArea("File " + getChanNum(i) + " appears to be invalid, skipping.\n");
                t.setFont(new Font("Courier New", 18));
                t.setEditable(false);
                t.setPrefSize(800, 70);
                Scene s = new Scene(t);
                Stage st = new Stage();
                st.setScene(s);
                st.setTitle("Error!");
                st.show();
            } //All files must be processed, so catch all other exceptions and report something useful
            catch (Exception ex) {
                TextArea t = new TextArea("File " + getChanNum(i) + " threw an error.\n" + ex);
                t.setFont(new Font("Courier New", 18));
                t.setEditable(false);
                t.setPrefSize(800, 70);
                Scene s = new Scene(t);
                Stage st = new Stage();
                st.setScene(s);
                st.setTitle("Error!");
                st.show();
            }
        }
    }

    /**
     * Launches GUI application
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Get the name of the output text file
     *
     * @param i integer from which to parse the filename
     * @return The complete filename and extension
     */
    private static String getChanNum(int i) {
        switch (i) {
            case 0:
                i = 2;
                break;
            case 1:
                i = 3;
                break;
            case 2:
                i = 4;
                break;
            case 3:
                i = 7;
                break;
            case 4:
                i = 8;
                break;
            case 5:
                i = 9;
                break;
            case 6:
                i = 10;
                break;
            case 7:
                i = 11;
                break;
            case 8:
                i = 12;
                break;
            case 9:
                i = 13;
                break;
            case 10:
                i = 17;
                break;
            case 11:
                i = 18;
                break;
            case 12:
                i = 19;
                break;
            case 13:
                i = 20;
                break;
            case 14:
                i = 22;
                break;
            case 15:
                i = 23;
                break;
            case 16:
                i = 24;
                break;
            case 17:
                i = 25;
                break;
            case 18:
                i = 26;
                break;
            case 19:
                i = 27;
                break;
            default:
                i = 0;
        }

        return String.valueOf(i);
    }

    /**
     * Create and save a file containing basic data about this channel with a
     * reasonably unique filename for later data aggregation
     *
     * @param g
     * @param dir
     * @param chan
     */
    public static void exportAggregateData(Aggregate g, String dir, String chan) {

        try {

            //create a new file
            File file = new File(dir + g.startTime + chan + ".aggr");
            if (file.exists()) {
                file.delete();
                System.out.println("Deleting existing .aggr file: " + file.getName());
            }
            file.createNewFile();

            try ( //write to that file
                    PrintWriter writer = new PrintWriter(file)) {
                writer.write(g.serialize());
                System.out.println("Discrete data file " + file.getName() + " successfully generated.");
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        } catch (IOException e) {
            System.out.println("Failed to write data file " + g.startTime + ".aggr " + e.getMessage());
            TextArea t = new TextArea("Unable to compile aggregator data on channel " + chan + ".");
            t.setFont(new Font("Courier New", 18));
            t.setEditable(false);
            t.setPrefSize(800, 70);
            Scene s = new Scene(t);
            Stage st = new Stage();
            st.setScene(s);
            st.setTitle("Error!");
            st.show();
        }

    }

    public void aggregate() {

        ArrayList<Double> avgSpeedList, distanceList, durationList, flySpeedList, flightPercentList,
                restPercentList, longestFlightList, longestLegList, longestRestList, flyTimeList, startTimeList;
        ArrayList<int[]> histList;
        ArrayList<double[]> progList;
        double[] avgSpeed, distance, duration, flySpeed, flightPercent,
                longestFlight, longestLeg, longestRest, flyTime, startTime;
        double classWidth = 0;
        int[][] hist;
        double[][] prog;
        int classCount = 0;
        boolean matchingClasses = true;

        File directory = new File(dir);
        File[] file = directory.listFiles((File directory1, String filename)
                -> filename.endsWith(".aggr"));

        if (file.length != 0) {

            avgSpeedList = new ArrayList<>();
            distanceList = new ArrayList<>();
            durationList = new ArrayList<>();
            flySpeedList = new ArrayList<>();
            flightPercentList = new ArrayList<>();
            longestFlightList = new ArrayList<>();
            longestLegList = new ArrayList<>();
            longestRestList = new ArrayList<>();
            flyTimeList = new ArrayList<>();
            restPercentList = new ArrayList<>();
            startTimeList = new ArrayList<>();
            histList = new ArrayList<>();
            progList = new ArrayList<>();

            Aggregate[] agg = new Aggregate[file.length];
            for (int i = 0; i < file.length; i++) {
                try {

                    Scanner s = new Scanner(file[i]);
                    String in = s.next();
                    while (s.hasNext()) {
                        in = in + " " + s.next();
                    }

                    agg[i] = Aggregate.deserialize(in);

                    //ensure that the histograms have matching parameters
                    if (matchingClasses) {
                        if (i == 0) {
                            classWidth = agg[i].classWidth;
                            classCount = agg[i].hist.length;
                        } else if (classWidth != agg[i].classWidth
                                || classCount != agg[i].hist.length) {
                            matchingClasses = false;
                        }
                    }

                    avgSpeedList.add(agg[i].avgSpeed);
                    distanceList.add(agg[i].distance);
                    durationList.add(agg[i].duration);
                    flySpeedList.add(agg[i].flySpeed);
                    flightPercentList.add((double) agg[i].flyTime / (double) agg[i].duration);
                    longestFlightList.add(agg[i].longestFlight);
                    longestLegList.add(agg[i].longestLeg);
                    longestRestList.add(agg[i].longestRest);
                    flyTimeList.add(agg[i].flyTime);
                    restPercentList.add(agg[i].restTime / agg[i].duration);
                    startTimeList.add(agg[i].startTime);
                    histList.add(agg[i].hist);
                    progList.add(agg[i].prog);

                    System.out.println("Compiling..."
                            + "\n" + "Added file: " + file[i].getName());
                } catch (java.io.FileNotFoundException e) {
                    System.out.println("Could not find file " + file[i].getName());
                } catch (IllegalArgumentException e) {
                    System.out.println("File " + file[i].getName() + " is invalid: " + e.getMessage());
                }
            }

            avgSpeed = Data.getPrimativeDoubleArray(avgSpeedList
                    .toArray(new Double[avgSpeedList.size()]));
            distance = Data.getPrimativeDoubleArray(distanceList
                    .toArray(new Double[distanceList.size()]));
            duration = Data.getPrimativeDoubleArray(durationList
                    .toArray(new Double[durationList.size()]));
            flySpeed = Data.getPrimativeDoubleArray(flySpeedList
                    .toArray(new Double[flySpeedList.size()]));
            flightPercent = Data.getPrimativeDoubleArray(flightPercentList
                    .toArray(new Double[flightPercentList.size()]));
            longestFlight = Data.getPrimativeDoubleArray(longestFlightList
                    .toArray(new Double[longestFlightList.size()]));
            longestLeg = Data.getPrimativeDoubleArray(longestLegList
                    .toArray(new Double[longestLegList.size()]));
            longestRest = Data.getPrimativeDoubleArray(longestRestList
                    .toArray(new Double[longestRestList.size()]));
            flyTime = Data.getPrimativeDoubleArray(flyTimeList
                    .toArray(new Double[flyTimeList.size()]));
            startTime = Data.getPrimativeDoubleArray(startTimeList
                    .toArray(new Double[startTimeList.size()]));

            hist = histList.toArray(new int[histList.size()][]);
            prog = progList.toArray(new double[progList.size()][]);

            NumberAxis x = new NumberAxis();
            NumberAxis y = new NumberAxis();
            x.setLabel("Time (minutes)");
            y.setLabel("Distance (meters)");
            AreaChart<Number, Number> distOverTime
                    = new AreaChart<>(x, y);

            int length = 0;
            for (double[] prog1 : prog) {
                length = Math.max(length, prog1.length);
            }
            double[] aggProg = new double[length];

            XYChart.Series<Number, Number>[] multiGraph = new XYChart.Series[prog.length + 1];
            multiGraph[0] = new XYChart.Series();
            for (int i = 0; i < length; i++) {
                double value = 0;
                for (double[] prog1 : prog) {
                    if (prog1.length > i) {
                        value += prog1[i];
                    } else {
                        value += prog1[prog1.length - 1];
                    }
                }
                multiGraph[0].getData().add(new XYChart.Data(i, value / prog.length));
            }
            distOverTime.getData().add(multiGraph[0]);
            multiGraph[0].getNode().lookup(".chart-series-area-fill").setStyle("-fx-stroke: #ff0000; -fx-stroke-width: 3px;");
            for (int i = 1; i < multiGraph.length; i++) {
                System.out.println("Rendering channel " + i + " of " + (multiGraph.length - 1));
                multiGraph[i] = new XYChart.Series();
                distOverTime.getData().add(multiGraph[i]);
                javafx.scene.Node fill = multiGraph[i].getNode().lookup(".chart-series-area-fill");
                javafx.scene.Node line = multiGraph[i].getNode().lookup(".chart-series-area-line");
                for (int j = 0; j < prog[i - 1].length; j++) {
                    multiGraph[i].getData().add(new XYChart.Data(j, prog[i - 1][j]));
                }
                fill.setStyle("-fx-fill: #ffff0000;");
                line.setStyle("-fx-stroke: #0000ff; -fx-stroke-width: 1px;");
            }

            //distOverTime.getData().add(multiGraph[0]);
            distOverTime.setAnimated(false);
            distOverTime.setCreateSymbols(false);
            //distOverTime.getStylesheets().add("chart.css");
            distOverTime.setPrefWidth(1100);
            distOverTime.setPrefHeight(700);

            int[] aggregateHist = new int[hist[0].length];

            for (int i = 0; i < hist[0].length; i++) {
                aggregateHist[i] = 0;
                for (int[] hist1 : hist) {
                    aggregateHist[i] += hist1[i];
                }
            }

            CategoryAxis catx = new CategoryAxis();
            y = new NumberAxis();
            BarChart<String, Number> histogram
                    = new BarChart<>(catx, y);
            XYChart.Series<String, Number> series = new XYChart.Series();
            for (int i = 0; i < aggregateHist.length - 1; i++) {
                series.getData().add(new XYChart.Data(String.valueOf(((int) (100 * ((i + 1) * classWidth))) / 100.0), aggregateHist[i]));
            }
            histogram.getData().add(series);
            histogram.setCategoryGap(-1);
            histogram.setCategoryGap(-1);
            histogram.setVerticalGridLinesVisible(false);
            histogram.setAnimated(false);
            histogram.setPrefSize(525, 150);

            double[] values = new double[classCount];
            double[] counts = new double[classCount];
            double modeRotationTime = 0;

            for (int i = 0; i < classCount; i++) {
                values[i] = classWidth * i;
                counts[i] = aggregateHist[i];
                if (counts[i] > counts[(int) modeRotationTime]) {
                    modeRotationTime = i;
                }
            }
            modeRotationTime = values[(int) modeRotationTime];

            //average speed weighted at the square root of the data run's duration
            double weightedAverageSpeed = (int) (1000 * Data.average(
                    avgSpeed)) / 1000.0;

            //average speed in flight weighted at the square root of the data run's
            //duration
            double weightedAverageFlySpeed = (int) (1000 * Data.average(
                    flySpeed)) / 1000.0;

            //percent of time in flight weighted by the square root of the data
            //run's duration
            double weightedAverageFlightPercent = (int) (10000 * Data.average(
                    flightPercent)) / 100.0;

            //the average of the histogram
            double averageRotationTime = (int) (100 * Data.average(
                    values)) / 100.0;

            //the a average duration multiplied by the weighted average speed
            double weightedAverageDistance = (int) ((Data.average(
                    distance)) / 10) / 100.0;

            //the average duration multipled by the weighted average percent of time in flight
            double averageFlyTime = (int) ((Data.average(
                    duration) * weightedAverageFlightPercent)) / 100.0;

            //the total duration of all the data runs
            double totalDuration = (int) (100 * (Data.average(duration) / 60)) / 100.0;

            //the average longest single flight weighted by the square root of duration
            double weightedAverageLongestFlight = (int) (100 * (Data.average(
                    longestFlight)) / 60) / 100.0;

            //the average longest single time at rest weighted by the square root of duration
            double weightedAverageLongestRest = (int) (100 * (Data.average(
                    longestRest)) / 60) / 100.0;

            //the weighted average longest distance in a single hop weighted by the suqare root of duration
            double weightedAverageLongestLeg = (int) (Data.average(
                    longestLeg) / 10) / 100.0;

            //double[] restTime = Data.getPrimativeDoubleArray(restPercentList.toArray(new Double[file.length]));
            System.out.println(weightedAverageSpeed + "\n"
                    + weightedAverageFlySpeed + "\n"
                    + "%" + weightedAverageFlightPercent);
            //System.exit(0);

            String statistics
                    = "              Number of data runs     " + file.length + "\n"
                    + "              Experiment Duration     " + totalDuration + " minutes\n"
                    + "                 Average Distance     " + weightedAverageDistance + " km\n"
                    + "\n"
                    + "                        Avg Speed     " + weightedAverageSpeed + " m/s\n"
                    + "              Avg Speed in Flight     " + weightedAverageFlySpeed + " m/s\n"
                    + "          Avg % of time in Flight     " + weightedAverageFlightPercent + "%\n"
                    + "       Avg Longest Time in Flight     " + weightedAverageLongestFlight + " minutes\n"
                    + "        Avg  Longest Time at Rest     " + weightedAverageLongestRest + " minutes\n"
                    + "                  Avg Longest Leg     " + weightedAverageLongestLeg + " km\n"
                    + "\n"
                    + "            Average Rotation Time     " + averageRotationTime + " seconds\n"
                    + "               Mode Rotation Time     " + modeRotationTime + " seconds";

            TextArea text = new TextArea(statistics);

            text.setPrefHeight(150);
            text.setPrefWidth(575);

            VBox box = new VBox(new HBox(text, histogram), distOverTime);
            Scene scene = new Scene(box);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();

            PDFWriter.export(new VBox(), distOverTime, histogram, dir + "Aggregate.pdf", "Aggregate data on " + file.length + " channels", statistics.split("\n"));

        } else {
            System.out.println("No data");
            TextArea t = new TextArea("No data, run the discrete analyzer before aggregating.");
            t.setFont(new Font("Courier New", 18));
            t.setEditable(false);
            t.setPrefSize(800, 70);
            Scene s = new Scene(t);
            Stage st = new Stage();
            st.setScene(s);
            st.setTitle("Error!");
            st.show();
        }

    }
}
