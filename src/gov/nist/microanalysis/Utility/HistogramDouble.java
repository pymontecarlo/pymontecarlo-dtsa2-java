package gov.nist.microanalysis.Utility;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

public class HistogramDouble extends AbstractHistogramND {

    private final double[] binMins;

    private final double[] sums;



    public HistogramDouble(double min, double max, int nBins) {
        this(HistogramUtil.createBins(min, max, nBins), max);
    }



    @Override
    public HistogramDouble clone() {
        double[] binMins =
                Arrays.copyOf(this.binMins, this.binMins.length - 1);
        double max = this.binMins[this.binMins.length - 1];

        HistogramDouble other = new HistogramDouble(binMins, max);
        System.arraycopy(sums, 0, other.sums, 0, sums.length);

        return other;
    }



    public HistogramDouble(double[] binMins, double max) {
        this.binMins = Arrays.copyOf(binMins, binMins.length + 1);
        this.binMins[binMins.length] = max;
        Arrays.sort(this.binMins);
        if (this.binMins[this.binMins.length - 1] != max)
            throw new IllegalArgumentException(
                    "Max (" + max + ") is not larger than all binMins.");

        sums = new double[this.binMins.length + 1];
    }



    /**
     * Returns the bin into which the value fits.
     * 
     * @param val
     *            value
     * @return index of the bin
     */
    public int bin(double val) {
        return bin(binMins, val);
    }



    /**
     * Returns the minimum value stored in the specified bin.
     * 
     * @param bin
     *            index of bin
     * @return minimum value
     */
    public double minValue(int bin) {
        return minValue(binMins, bin);
    }



    /**
     * Returns the upper limit for values stored in this bin. Actually this
     * value is excluded from the bin and included in the next larger bin.
     * 
     * @param bin
     *            index of bin
     * @return maximum value
     */
    public double maxValue(int bin) {
        return maxValue(binMins, bin);
    }



    /**
     * Returns the middle value between the lower and upper limit of this bin.
     * 
     * @param bin
     *            index of bin
     * @return middle value
     */
    public double midValue(int bin) {
        return midValue(binMins, bin);
    }



    /**
     * Add the specified value to a bin of this histogram.
     * 
     * @param binValue
     *            value of bin
     * @param value
     *            to add to the bin
     */
    public void add(double binValue, double value) {
        sums[bin(binValue) + 1] += value;
    }



    /**
     * Returns the number of bins (not counting over-range and under-range
     * bins).
     * 
     * @return number of bins
     */
    public int binCount() {
        return binCount(binMins);
    }



    /**
     * Returns the sum of the values in the specified bin.
     * 
     * @param bin
     *            index of bin
     * @return sum of values inside bin
     */
    public double sum(int bin) {
        return sums[bin + 1];
    }



    /**
     * Reset the counts to 0.
     */
    public void clear() {
        for (int i = 0; i < sums.length; i++)
            sums[i] = 0;
    }



    /**
     * Returns the sum of the values that fell into the overrange bin (larger
     * than the max argument of the constructor).
     * 
     * @return sum of the values that fell into the overrange bin
     */
    public double overrange() {
        return sums[sums.length - 1];
    }



    /**
     * Returns the sum of the values that fell into the underrange bin (less
     * than the min argument of the constructor).
     * 
     * @return sum of the values that fell into the underrange bin
     */
    public double underrange() {
        return sums[0];
    }



    /**
     * Returns the total sum of all values recorded in the histogram.
     * 
     * @return total sum
     */
    public double totalSum() {
        double res = 0;
        for (final double s : sums)
            res += s;
        return res;
    }



    /**
     * Returns a 2D array of the minimum bin values and sum of all values
     * recorded in each bin. The under and over range are not returned.
     * 
     * @return
     */
    public double[][] getArray() {
        double[][] array = new double[binMins.length - 1][2];
        for (int i = 0; i < array.length; i++) {
            array[i][0] = binMins[i];
            array[i][1] = sums[i + 1];
        }
        return array;
    }



    /**
     * dump - Output the histogram as a comma separated value table.
     * 
     * @param out
     *            output stream
     */
    public void dump(OutputStream out) {
        PrintWriter pw = new PrintWriter(out);

        for (int i = -1; i < binCount() + 1; i++)
            pw.println(minValue(i) + "," + sum(i));

        pw.close();
    }

}