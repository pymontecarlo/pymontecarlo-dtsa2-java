package gov.nist.microanalysis.Utility;

import java.util.Arrays;

public class HistogramDouble3D extends AbstractHistogram3D {

    private final double[][][] sums;



    public HistogramDouble3D(double xMin, double xMax, int xBins, double yMin,
            double yMax, int yBins, double zMin, double zMax, int zBins) {
        super(xMin, xMax, xBins, yMin, yMax, yBins, zMin, zMax, zBins);
        sums = new double[xBinCount() + 2][yBinCount() + 2][zBinCount() + 2];
    }



    public HistogramDouble3D(double[] xBinMins, double xMax, double[] yBinMins,
            double yMax, double[] zBinMins, double zMax) {
        super(xBinMins, xMax, yBinMins, yMax, zBinMins, zMax);
        sums = new double[xBinCount() + 2][yBinCount() + 2][zBinCount() + 2];
    }



    @Override
    public void clear() {
        for (int i = 0; i < sums.length; i++)
            for (int j = 0; j < sums[i].length; j++)
                for (int k = 0; k < sums[i][j].length; k++)
                    sums[i][j][k] = 0.0;

    }



    @Override
    public HistogramDouble3D clone() {
        double[] xBinMins =
                Arrays.copyOf(this.xBinMins, this.xBinMins.length - 1);
        double xMax = this.xBinMins[this.xBinMins.length - 1];

        double[] yBinMins =
                Arrays.copyOf(this.yBinMins, this.yBinMins.length - 1);
        double yMax = this.yBinMins[this.yBinMins.length - 1];

        double[] zBinMins =
                Arrays.copyOf(this.zBinMins, this.zBinMins.length - 1);
        double zMax = this.zBinMins[this.zBinMins.length - 1];

        HistogramDouble3D other =
                new HistogramDouble3D(xBinMins, xMax, yBinMins, yMax,
                        zBinMins, zMax);
        for (int i = 0; i < sums.length; i++)
            for (int j = 0; j < sums[i].length; j++)
                System.arraycopy(sums[i][j], 0, other.sums[i][j], 0,
                        sums[i][j].length);

        return other;
    }



    /**
     * Add the specified value to a bin of this histogram.
     * 
     * @param xBinValue
     *            value of bin in x
     * @param yBinValue
     *            value of bin in y
     * @param zBinValue
     *            value of bin in z
     * @param value
     *            to add to the bin
     */
    public void add(double xBinVal, double yBinVal, double zBinVal, double value) {
        sums[xBin(xBinVal) + 1][yBin(yBinVal) + 1][zBin(zBinVal) + 1] += value;
    }



    /**
     * Returns the sum of the values in the specified bin.
     * 
     * @param xBin
     *            index of bin in x
     * @param yBin
     *            index of bin in y
     * @param zBin
     *            index of bin in z
     * @return sum of values inside bin
     */
    public double sum(int xBin, int yBin, int zBin) {
        return sums[xBin + 1][yBin + 1][zBin + 1];
    }



    /**
     * Returns the total number of counts in the histogram.
     * 
     * @return total number of counts
     */
    public double totalSum() {
        double total = 0;
        for (int i = 0; i < sums.length; i++)
            for (int j = 0; j < sums[i].length; j++)
                for (int k = 0; k < sums[i][j].length; k++)
                    total += sums[i][j][k];
        return total;
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
    public double[][][] getArray() {
        double[][][] array =
                new double[zBinMins.length - 1][xBinMins.length][yBinMins.length];
        for (int k = 0; k < array.length; k++) {
            array[k][0][0] = zBinMins[k]; // z value
            System.arraycopy(yBinMins, 0, array[k][0], 1, yBinMins.length - 1);

            for (int i = 1; i < array[0].length; i++) {
                array[k][i][0] = xBinMins[i - 1];

                for (int j = 1; j < array[0][0].length; j++) {
                    array[k][i][j] = sums[i][j][k + 1];
                }
            }
        }
        return array;
    }

}
