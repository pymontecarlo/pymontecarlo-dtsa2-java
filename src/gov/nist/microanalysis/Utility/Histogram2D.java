package gov.nist.microanalysis.Utility;

import gov.nist.microanalysis.EPQLibrary.EPQException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

public class Histogram2D extends AbstractHistogram2D {

    /** Values of the pixels. */
    private final int[][] counts;



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
     */
    public Histogram2D(double xMin, double xMax, int xBins,
            double yMin, double yMax, int yBins) {
        super(HistogramUtil.createBins(xMin, xMax, xBins), xMax,
                HistogramUtil.createBins(yMin, yMax, yBins), yMax);
        counts = new int[xBinCount() + 2][yBinCount() + 2];
    }



    /**
     * Creates a new <code>Histogram2D</code>.
     * 
     * @param binMins
     *            minimum values of the bins
     * @param max
     *            maximum value of the top bin.
     */
    public Histogram2D(double[] xBinMins, double xMax,
            double[] yBinMins, double yMax) {
        super(xBinMins, xMax, yBinMins, yMax);
        counts = new int[xBinCount() + 2][yBinCount() + 2];
    }



    /**
     * Add the specified value to the specified bin.
     * 
     * @param xBinVal
     *            x coordinate
     * @param yBinVal
     *            y coordinate
     */
    public void add(double xBinVal, double yBinVal) {
        ++counts[xBin(xBinVal) + 1][yBin(yBinVal) + 1];
    }



    /**
     * Reset the counts to 0.
     */
    public void clear() {
        for (int i = 0; i < counts.length; i++)
            for (int j = 0; j < counts[i].length; j++)
                counts[i][j] = 0;
    }



    @Override
    public Histogram2D clone() {
        double[] xBinMins =
                Arrays.copyOf(this.xBinMins, this.xBinMins.length - 1);
        double xMax = this.xBinMins[this.xBinMins.length - 1];
        double[] yBinMins =
                Arrays.copyOf(this.yBinMins, this.yBinMins.length - 1);
        double yMax = this.yBinMins[this.yBinMins.length - 1];

        Histogram2D other = new Histogram2D(xBinMins, xMax, yBinMins, yMax);
        for (int i = 0; i < counts.length; i++)
            System.arraycopy(counts[i], 0, other.counts[i], 0, counts[i].length);

        return other;
    }



    /**
     * Returns the counts in the specified bin.
     * 
     * @param xbin
     *            index of the bin in the x direction
     * @param ybin
     *            index of the bin in the y direction
     * @return value
     */
    public int counts(int xbin, int ybin) {
        return counts[xbin + 1][ybin + 1];
    }



    /**
     * Returns the accumulated values as a function of x.
     * 
     * @return histogram containing the x projection of the values
     */
    public Histogram getXProjection() {
        double[] binMins = Arrays.copyOf(xBinMins, xBinMins.length - 1);
        double max = xBinMins[xBinMins.length - 1];

        Histogram h;
        try {
            h = new Histogram(binMins, max);
        } catch (EPQException e) {
            throw new RuntimeException(e);
        }

        double binValue;
        for (int i = 0; i < counts.length; i++) {
            binValue = xMinValue(i - 1);

            for (int j = 0; j < counts[i].length; j++) {
                for (int k = 0; k < counts[i][j]; k++) {
                    h.add(binValue);
                }
            }
        }

        return h;
    }



    /**
     * Returns the accumulated values as a function of y.
     * 
     * @return histogram containing the y projection of the values
     */
    public Histogram getYProjection() {
        double[] binMins = Arrays.copyOf(yBinMins, yBinMins.length - 1);
        double max = yBinMins[yBinMins.length - 1];

        Histogram h;
        try {
            h = new Histogram(binMins, max);
        } catch (EPQException e) {
            throw new RuntimeException(e);
        }

        double binValue;
        for (int j = 0; j < counts[0].length; j++) {
            binValue = yMinValue(j - 1);

            for (int i = 0; i < counts.length; i++) {
                for (int k = 0; k < counts[i][j]; k++) {
                    h.add(binValue);
                }
            }
        }

        return h;
    }



    /**
     * Returns the total number of counts in the histogram.
     * 
     * @return total number of counts
     */
    public int totalCounts() {
        int total = 0;
        for (int i = 0; i < counts.length; i++)
            for (int j = 0; j < counts[i].length; j++)
                total += counts[i][j];
        return total;
    }



    /**
     * dump - Output the histogram as a 2D comma separated value table.
     * 
     * @param out
     *            output stream
     */
    public void dump(OutputStream out) {
        PrintWriter pw = new PrintWriter(out);

        // Header
        pw.print(",");
        for (int j = -1; j < yBinCount() + 1; j++) {
            pw.print(yMinValue(j) + ",");
        }
        pw.println();

        // Data
        for (int i = -1; i < xBinCount() + 1; i++) {
            pw.print(xMinValue(i) + ",");

            for (int j = -1; j < yBinCount() + 1; j++)
                pw.print(counts(i, j) + ",");

            pw.println();
        }

        pw.close();
    }
    
    public int[][] getCountsArray() {
        return counts.clone();
    }
    
    public double[] getXArray() {
        return xBinMins.clone();
    }
    
    public double[] getYArray() {
        return yBinMins.clone();
    }

}