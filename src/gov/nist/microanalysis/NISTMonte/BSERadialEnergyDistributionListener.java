package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.Utility.Histogram2D;
import gov.nist.microanalysis.Utility.HistogramUtil;

import java.awt.event.ActionEvent;

/**
 * Listener to get an energy distribution at different radial distance from a
 * centre point.
 * 
 * @author ppinard
 */
public class BSERadialEnergyDistributionListener extends
        AbstractRadialDistributionListener {

    /** Histogram to store distribution. */
    private final Histogram2D distribution;



    /**
     * Creates a new <code>BSERadialEnergyDistributionListener</code>.
     * 
     * @param center
     *            centre of the radial distribution. Distance are evaluated to
     *            this point. Coordinates in meters.
     * @param normal
     *            normal to the entering region's surface
     * @param eMin
     *            lower limit for the energy distributions (in eV)
     * @param eMax
     *            upper limit for the energy distributions (in eV)
     * @param ebins
     *            number of bins for the energy distributions
     * @param rmax
     *            maximum radius of the distribution (in meters)
     * @param rBins
     *            number of bins for the radial distribution
     * @param equalArea
     *            if <code>true</code> the values of the bins are calculated to
     *            have an equal area, if <code>false</code> the values of the
     *            bins have are radially equidistant.
     */
    public BSERadialEnergyDistributionListener(double[] center,
            double[] normal, double eMin, double eMax,
            int eBins, double rMax, int rBins, boolean equalArea) {
        super(center, normal);

        double[] rBinMins =
                RadialDistributionUtil.calculateBinMins(rMax, rBins, equalArea);
        double[] eBinMins = HistogramUtil.createBins(eMin, eMax, eBins);
        distribution = new Histogram2D(rBinMins, rMax, eBinMins, eMax);
    }



    @Override
    public void actionPerformed(ActionEvent event) {
        MonteCarloSS mcss = (MonteCarloSS) event.getSource();

        switch (event.getID()) {
        case MonteCarloSS.FirstTrajectoryEvent:
            distribution.clear();
            break;
        case MonteCarloSS.BackscatterEvent:
            double r = getRadius(mcss.getElectron());
            if (!Double.isNaN(r)) {
                double e = FromSI.eV(mcss.getElectron().getEnergy());
                distribution.add(r, e);
            }
            break;
        default:
            break;
        }
    }



    /**
     * Return the energy radial distribution.
     * 
     * @return energy radial distribution
     */
    public Histogram2D getDistribution() {
        return distribution.clone();
    }

}
