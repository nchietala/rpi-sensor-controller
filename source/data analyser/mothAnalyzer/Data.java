/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mothAnalyzer;

/**
 *
 * @author Nick
 */
public abstract class Data {

    public static double average(double... args) {
        return sum(args) / args.length;
    }

    public static double sum(double... args) {
        double sum = 0;
        for (double args1 : args) {
            sum += args1;
        }
        return sum;
    }

    public static double[] getPrimativeDoubleArray(Double[] args) {
        double[] newArray = new double[args.length];
        for (int i = 0; i < args.length; i++) {
            newArray[i] = args[i];
        }
        return newArray;
    }

    public static double weightedAverage(double[] args, double[] weight, int root) {
        if (args.length - weight.length != 0) {
            throw new IndexOutOfBoundsException("Weights array must be equal in length to arguments array");
        }

        double dividend = 0, divisor = 0;
        for (int i = 0; i < args.length; i++) {
            double hold = Math.pow(weight[i], (1.0 / root));
            dividend += args[i] * hold;
            divisor += hold;
        }
        return dividend / divisor;
    }

    public static int maxLength(double[][] compare) {
        int length = compare[0].length;
        if (compare.length > 0) {
            for (int i = 1; i < compare.length; i++) {
                length = Math.max(compare[i - 1].length, compare[i].length);
            }
        }
        return length;
    }
    
    public static double inverseAvg(double[][] data, int index) throws ArrayIndexOutOfBoundsException {
        double avg = 0, count = 0;
        for (double[] sub : data) {
            if (sub.length < index) {
                count++;
                avg += sub[index];
            }
        }
        //if (count == 0) throw new ArrayIndexOutOfBoundsException();
        return avg / count;
    }
}
