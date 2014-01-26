package gov.nist.microanalysis.Utility;

import java.util.Arrays;

public class Histogram3D extends AbstractHistogram3D {

    private final int[][][] counts;



    public Histogram3D(double xMin, double xMax, int xBins, double yMin,
            double yMax, int yBins, double zMin, double zMax, int zBins) {
        super(xMin, xMax, xBins, yMin, yMax, yBins, zMin, zMax, zBins);
        counts = new int[xBinCount() + 2][yBinCount() + 2][zBinCount() + 2];
    }



    public Histogram3D(double[] xBinMins, double xMax, double[] yBinMins,
            double yMax, double[] zBinMins, double zMax) {
        super(xBinMins, xMax, yBinMins, yMax, zBinMins, zMax);
        counts = new int[xBinCount() + 2][yBinCount() + 2][zBinCount() + 2];
    }



    @Override
    public void clear() {
        for (int i = 0; i < counts.length; i++)
            for (int j = 0; j < counts[i].length; j++)
                for (int k = 0; k < counts[i][j].length; k++)
                    counts[i][j][k] = 0;

    }



    @Override
    public Histogram3D clone() {
        double[] xBinMins =
                Arrays.copyOf(this.xBinMins, this.xBinMins.length - 1);
        double xMax = this.xBinMins[this.xBinMins.length - 1];

        double[] yBinMins =
                Arrays.copyOf(this.yBinMins, this.yBinMins.length - 1);
        double yMax = this.yBinMins[this.yBinMins.length - 1];

        double[] zBinMins =
                Arrays.copyOf(this.zBinMins, this.zBinMins.length - 1);
        double zMax = this.zBinMins[this.zBinMins.length - 1];

        Histogram3D other =
                new Histogram3D(xBinMins, xMax, yBinMins, yMax,
                        zBinMins, zMax);
        for (int i = 0; i < counts.length; i++)
            for (int j = 0; j < counts[i].length; j++)
                System.arraycopy(counts[i][j], 0, other.counts[i][j], 0,
                        counts[i][j].length);

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
     */
    public void add(double xBinVal, double yBinVal, double zBinVal) {
        counts[xBin(xBinVal) + 1][yBin(yBinVal) + 1][zBin(zBinVal) + 1] += 1;
    }



    /**
     * Returns the number of counts in the specified bin.
     * 
     * @param xBin
     *            index of bin in x
     * @param yBin
     *            index of bin in y
     * @param zBin
     *            index of bin in z
     * @return sum of values inside bin
     */
    public int counts(int xBin, int yBin, int zBin) {
        return counts[xBin + 1][yBin + 1][zBin + 1];
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
                for (int k = 0; k < counts[i][j].length; k++)
                    total += counts[i][j][k];
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
    @Override
    public double[][][] getArray() {
        double[][][] array =
                new double[zBinMins.length - 1][xBinMins.length][yBinMins.length];
        for (int k = 0; k < array.length; k++) {
            array[k][0][0] = zBinMins[k]; // z value
            System.arraycopy(yBinMins, 0, array[k][0], 1, yBinMins.length - 1);

            for (int i = 1; i < array[0].length; i++) {
                array[k][i][0] = xBinMins[i - 1];

                for (int j = 1; j < array[0][0].length; j++) {
                    array[k][i][j] = counts[i][j][k + 1];
                }
            }
        }
        return array;
    }

}
