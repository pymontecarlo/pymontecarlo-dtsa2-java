package gov.nist.microanalysis.NISTMonte;

/**
 * Utilities for radial distributions.
 * 
 * @author ppinard
 */
public class RadialDistributionUtil {

    /**
     * Returns the minimum values of an histogram.
     * 
     * @param rmax
     *            maximum radius of the distribution (in meters)
     * @param nBins
     *            number of bins
     * @param equalArea
     *            if <code>true</code> the values of the bins are calculated to
     *            have an equal area, if <code>false</code> the values of the
     *            bins have are radially equidistant.
     * @return minimum values of an histogram
     */
    public static double[] calculateBinMins(double rmax, int nBins,
            boolean equalArea) {
        double[] binMins = new double[nBins];
        if (equalArea) {
            double binArea = Math.pow(rmax, 2) / nBins;
            for (int i = 0; i < nBins; i++)
                binMins[i] = Math.sqrt(i * binArea);
        } else {
            double binWidth = rmax / nBins;
            for (int i = 0; i < nBins; i++)
                binMins[i] = i * binWidth;
        }

        return binMins;
    }
}
