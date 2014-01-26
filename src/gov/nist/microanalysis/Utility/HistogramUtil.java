package gov.nist.microanalysis.Utility;

/**
 * Utilities for histogram.
 * 
 * @author ppinard
 *
 */
public class HistogramUtil {

    /**
     * Returns the minimum values of the bins.
     * 
     * @param min
     *            minimum value
     * @param max
     *            maximum value
     * @param nBins
     *            number of bins (excluding over- and under-range)
     * @return bins
     */
    public static final double[] createBins(double min, double max, int nBins) {
        if (min >= max)
            throw new IllegalArgumentException("min <= max: " + min + " <= "
                    + max);
        if (nBins < 1)
            throw new IllegalArgumentException("bins < 1: " + nBins);

        double[] binMins = new double[nBins];
        double delta = (max - min) / nBins;
        for (int i = 0; i < binMins.length; i++)
            binMins[i] = min + i * delta;

        return binMins;
    }
}
