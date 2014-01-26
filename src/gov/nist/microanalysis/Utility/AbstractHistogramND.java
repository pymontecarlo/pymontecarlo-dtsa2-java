package gov.nist.microanalysis.Utility;

import java.io.OutputStream;
import java.util.Arrays;

public abstract class AbstractHistogramND {

    /**
     * Returns the bin into which the value fits.
     * 
     * @param binMin
     *            position array
     * @param val
     *            a value
     * @return bin index
     */
    protected int bin(double[] binMins, double val) {
        int i = Arrays.binarySearch(binMins, val);
        i = (i >= 0 ? i : -i - 2);
        assert i >= -1 : "index is " + Integer.toString(i) + " for "
                + Double.toString(val);
        return i;
    }



    /**
     * Returns the number of bins in the specified array.
     * 
     * @param binMin
     *            position array
     * @return number of bins
     */
    protected int binCount(double[] binMins) {
        return binMins.length - 1;
    }



    /**
     * Reset the counts to 0.
     */
    public abstract void clear();



    @Override
    public abstract AbstractHistogramND clone();



    /**
     * Returns the upper limit for values stored in this bin. Actually this
     * value is excluded from the bin and included in the next larger bin.
     * 
     * @param binMin
     *            position array
     * @param bin
     *            bin index
     * @return upper limit
     */
    protected double maxValue(double[] binMins, int bin) {
        return bin + 1 < binMins.length ? binMins[bin + 1]
                : Double.POSITIVE_INFINITY;
    }



    /**
     * Returns the middle value between the lower and upper limit of this bin.
     * 
     * @param binMin
     *            position array
     * @param bin
     *            bin index
     * @return middle value
     */
    protected double midValue(double[] binMins, int bin) {
        return (minValue(binMins, bin) + maxValue(binMins, bin)) / 2.0;
    }



    /**
     * Returns the minimum value stored in the specified bin.
     * 
     * @param binMin
     *            position array
     * @param bin
     *            bin index
     * @return lower limit
     */
    protected double minValue(double[] binMins, int bin) {
        return bin > -1 ? binMins[bin] : Double.NEGATIVE_INFINITY;
    }



    /**
     * dump - Output the histogram as a comma separated value table.
     * 
     * @param out
     *            output stream
     */
    public abstract void dump(OutputStream out);

}