package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.Utility.Histogram;

import java.awt.event.ActionEvent;

/**
 * Listener to get a radial distribution of the number of backscattered
 * electrons crossing a plane (typically the sample's surface).
 * 
 * @author ppinard
 */
public class BSERadialDistributionListener extends
        AbstractRadialDistributionListener {

    /** Histogram to store distribution. */
    private final Histogram distribution;



    /**
     * Creates a new <code>RadialDistributionListener</code>.
     * 
     * @param center
     *            centre of the radial distribution. Distance are evaluated to
     *            this point. Coordinates in meters.
     * @param normal
     *            normal to the entering region's surface
     * @param rmax
     *            maximum radius of the distribution (in meters)
     * @param nBins
     *            number of bins
     * @param equalArea
     *            if <code>true</code> the values of the bins are calculated to
     *            have an equal area, if <code>false</code> the values of the
     *            bins have are radially equidistant.
     */
    public BSERadialDistributionListener(double[] center, double[] normal,
            double rmax, int nBins, boolean equalArea) {
        super(center, normal);

        double[] binMins =
                RadialDistributionUtil.calculateBinMins(rmax, nBins, equalArea);
        try {
            distribution = new Histogram(binMins, rmax);
        } catch (EPQException e) {
            throw new IllegalArgumentException(e);
        }
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        MonteCarloSS mcss = (MonteCarloSS) e.getSource();

        switch (e.getID()) {
        case MonteCarloSS.FirstTrajectoryEvent:
            distribution.clear();
            break;
        case MonteCarloSS.BackscatterEvent:
            double r = getRadius(mcss.getElectron());
            distribution.add(r);
            break;
        default:
            break;
        }
    }



    /**
     * Return the radial distribution.
     * 
     * @return radial distribution
     */
    public Histogram getDistribution() {
        return new Histogram(distribution);
    }

}
