package gov.nist.microanalysis.Utility;

import java.util.Arrays;

public abstract class AbstractHistogram2D extends AbstractHistogramND {

    /** Pixel x position (lower value). */
    protected final double[] xBinMins;

    /** Pixel y position (lower value). */
    protected final double[] yBinMins;



    /**
     * Creates a <code>AbstractHistogram2D</code> representing the specified
     * range with bins for over-range and under-range in two dimensions.
     * <p/>
     * Bins are numbered such that 0..(nBins-1) correspond to the full range.
     * Bin[0] -> [min,min+delta) where delta = (max-min)/nBins, Bin[nBins-1] ->
     * [max-delta,max). There are two bins for under and overrange values.
     * Bin[-1]->[-inf,min), Bin[nBins] -> [max,inf) where inf is infinity.
     * 
     * @param xMin
     *            minimum value in x
     * @param xMax
     *            maximum value in x
     * @param xBins
     *            number of bins in x
     * @param yMin
     *            minimum value in y
     * @param yMax
     *            maximum value in y
     * @param yBins
     *            number of bins in y
     */
    public AbstractHistogram2D(double xMin, double xMax, int xBins,
            double yMin, double yMax, int yBins) {
        this(HistogramUtil.createBins(xMin, xMax, xBins), xMax,
                HistogramUtil.createBins(yMin, yMax, yBins), yMax);
    }



    /**
     * Creates a new <code>Histogram2D</code>.
     * 
     * @param binMins
     *            minimum values of the bins
     * @param max
     *            maximum value of the top bin.
     */
    public AbstractHistogram2D(double[] xBinMins, double xMax,
            double[] yBinMins, double yMax) {
        this.xBinMins = Arrays.copyOf(xBinMins, xBinMins.length + 1);
        this.xBinMins[xBinMins.length] = xMax;
        Arrays.sort(this.xBinMins);
        if (this.xBinMins[this.xBinMins.length - 1] != xMax)
            throw new IllegalArgumentException(
                    "Max (" + xMax + ") is not larger than all binMins.");

        this.yBinMins = Arrays.copyOf(yBinMins, yBinMins.length + 1);
        this.yBinMins[yBinMins.length] = yMax;
        Arrays.sort(this.yBinMins);
        if (this.yBinMins[this.yBinMins.length - 1] != yMax)
            throw new IllegalArgumentException(
                    "Max (" + yMax + ") is not larger than all binMins.");
    }



    /**
     * Returns the bin along the x direction into which the value fits.
     * 
     * @param xval
     *            value
     * @return bin index
     */
    public int xBin(double xval) {
        return bin(xBinMins, xval);
    }



    /**
     * Returns the number of bins along the x direction (not counting over-range
     * and under-range bins).
     * 
     * @return number of bins along the x direction
     */
    public int xBinCount() {
        return binCount(xBinMins);
    }



    /**
     * Returns the upper limit for values stored in this bin along the x
     * direction. Actually this value is excluded from the bin and included in
     * the next larger bin.
     * 
     * @param bin
     *            bin index
     * @return upper limit
     */
    public double xMaxValue(int bin) {
        return maxValue(xBinMins, bin);
    }



    /**
     * Returns the middle value between the lower and upper limit of this bin
     * along the x direction.
     * 
     * @param bin
     *            bin index
     * @return middle value
     */
    public double xMidValue(int bin) {
        return midValue(xBinMins, bin);
    }



    /**
     * Returns the minimum value stored in the specified bin along the x
     * direction.
     * 
     * @param bin
     *            bin index
     * @return lower limit
     */
    public double xMinValue(int bin) {
        return minValue(xBinMins, bin);
    }



    /**
     * Returns the bin along the y direction into which the value fits.
     * 
     * @param yval
     *            value
     * @return bin index
     */
    public int yBin(double yval) {
        return bin(yBinMins, yval);
    }



    /**
     * Returns the number of bins along the y direction (not counting over-range
     * and under-range bins).
     * 
     * @return number of bins along the y direction
     */
    public int yBinCount() {
        return binCount(yBinMins);
    }



    /**
     * Returns the upper limit for values stored in this bin along the y
     * direction. Actually this value is excluded from the bin and included in
     * the next larger bin.
     * 
     * @param bin
     *            bin index
     * @return upper limit
     */
    public double yMaxValue(int bin) {
        return maxValue(yBinMins, bin);
    }



    /**
     * Returns the middle value between the lower and upper limit of this bin
     * along the y direction.
     * 
     * @param bin
     *            bin index
     * @return middle value
     */
    public double yMidValue(int bin) {
        return midValue(yBinMins, bin);
    }



    /**
     * Returns the minimum value stored in the specified bin along the y
     * direction.
     * 
     * @param bin
     *            bin index
     * @return lower limit
     */
    public double yMinValue(int bin) {
        return minValue(yBinMins, bin);
    }

}
