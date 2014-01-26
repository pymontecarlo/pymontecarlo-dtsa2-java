package gov.nist.microanalysis.Utility;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

public abstract class AbstractHistogram3D extends AbstractHistogramND {

    /** Pixel x position (lower value). */
    protected final double[] xBinMins;

    /** Pixel y position (lower value). */
    protected final double[] yBinMins;

    /** Pixel z position (lower value). */
    protected final double[] zBinMins;



    /**
     * Creates a <code>Histogram2D</code> representing the specified range with
     * bins for over-range and under-range in two dimensions.
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
     * @param zMin
     *            minimum value in y
     * @param zMax
     *            maximum value in y
     * @param zBins
     *            number of bins in y
     */
    public AbstractHistogram3D(double xMin, double xMax, int xBins,
            double yMin, double yMax, int yBins,
            double zMin, double zMax, int zBins) {
        this(HistogramUtil.createBins(xMin, xMax, xBins), xMax,
                HistogramUtil.createBins(yMin, yMax, yBins), yMax,
                HistogramUtil.createBins(zMin, zMax, zBins), zMax);
    }



    /**
     * Creates a new <code>Histogram2D</code>.
     * 
     * @param xBinMins
     *            minimum values of the bins in x
     * @param xMax
     *            maximum value of the top bin in x
     * @param yBinMins
     *            minimum values of the bins in y
     * @param yMax
     *            maximum value of the top bin in y
     * @param zBinMins
     *            minimum values of the bins in z
     * @param zMax
     *            maximum value of the top bin in z
     */
    public AbstractHistogram3D(double[] xBinMins, double xMax,
            double[] yBinMins, double yMax,
            double[] zBinMins, double zMax) {
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

        this.zBinMins = Arrays.copyOf(zBinMins, zBinMins.length + 1);
        this.zBinMins[zBinMins.length] = zMax;
        Arrays.sort(this.zBinMins);
        if (this.zBinMins[this.zBinMins.length - 1] != zMax)
            throw new IllegalArgumentException(
                    "Max (" + zMax + ") is not larger than all binMins.");
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



    /**
     * Returns the bin along the z direction into which the value fits.
     * 
     * @param yval
     *            value
     * @return bin index
     */
    public int zBin(double yval) {
        return bin(zBinMins, yval);
    }



    /**
     * Returns the number of bins along the z direction (not counting over-range
     * and under-range bins).
     * 
     * @return number of bins along the z direction
     */
    public int zBinCount() {
        return binCount(zBinMins);
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
    public double zMaxValue(int bin) {
        return maxValue(zBinMins, bin);
    }



    /**
     * Returns the middle value between the lower and upper limit of this bin
     * along the z direction.
     * 
     * @param bin
     *            bin index
     * @return middle value
     */
    public double zMidValue(int bin) {
        return midValue(zBinMins, bin);
    }



    /**
     * Returns the minimum value stored in the specified bin along the z
     * direction.
     * 
     * @param bin
     *            bin index
     * @return lower limit
     */
    public double zMinValue(int bin) {
        return minValue(zBinMins, bin);
    }



    /**
     * Returns a 3D array made of XY slices. The first index of the array
     * corresponds to the z index of the slice. The z value is stored in the
     * first cell of the slice (x=0, y=0). The first row of each slice is the
     * minimum bin values in the x direction and the first column is the minimum
     * bin values in the y direction. The sums of all values in each bin are
     * stored from <code>x=[1, xBins]</code> and <code>y=[1, yBins]</code>.
     * 
     * @return 3D array made of XY slices
     */
    public abstract double[][][] getArray();



    @Override
    public void dump(OutputStream out) {
        PrintWriter pw = new PrintWriter(out);

        double[][][] array = getArray();

        for (int k = 0; k < array.length; k++) {
            for (int i = 0; i < array[0].length; i++) {
                for (int j = 0; j < array[0][0].length; j++) {
                    pw.write(array[k][i][j] + ",");
                }

                pw.println();
            }

            pw.println(); // two empty lines between slices
            pw.println();
        }

        pw.close();
    }

}