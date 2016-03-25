package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.NISTMonte.Gen3.BaseXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;
import gov.nist.microanalysis.NISTMonte.Gen3.BaseXRayGeneration3.CharacteristicXRay;
import gov.nist.microanalysis.NISTMonte.Gen3.BaseXRayGeneration3.XRay;
import gov.nist.microanalysis.Utility.HistogramDouble;
import gov.nist.microanalysis.Utility.Math2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

public class PhotonRadialDistributionListener implements ActionListener {

    /** Event listener for x-rays. */
    private final XRayTransport3 xrayEventListener;

    private final double[] center;

    private final double[] normal;

    private final HistogramDouble emptyDistribution;

    private final TreeMap<XRayTransition, HistogramDouble> emittedDistributions;

    private final TreeMap<XRayTransition, HistogramDouble> generatedDistributions;



    /**
     * Creates a new <code>PhotonRadialDistributionListener</code>.
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
    public PhotonRadialDistributionListener(XRayTransport3 xrel,
            double[] center, double[] normal,
            double rmax, int nBins, boolean equalArea) {
        if (xrel == null)
            throw new NullPointerException("xrel == null");
        xrayEventListener = xrel;

        if (center.length != 3)
            throw new IllegalArgumentException(
                    "The center must be an array of length 3.");
        this.center = center.clone();

        if (normal.length != 3)
            throw new IllegalArgumentException(
                    "The normal must be an array of length 3.");
        this.normal = Math2.normalize(normal);

        double[] binMins =
                RadialDistributionUtil.calculateBinMins(rmax, nBins, equalArea);
        emptyDistribution = new HistogramDouble(binMins, rmax);

        emittedDistributions = new TreeMap<>();
        generatedDistributions = new TreeMap<>();
    }



    @Override
    public void actionPerformed(ActionEvent ae) {
        assert ae.getSource() == xrayEventListener;

        switch (ae.getID()) {
        case BaseXRayGeneration3.XRayGeneration: {
            for (int i = xrayEventListener.getEventCount() - 1; i >= 0; i--) {
                XRay xray = xrayEventListener.getXRay(i);

                if (xray instanceof CharacteristicXRay) {
                    XRayTransition xrt =
                            ((CharacteristicXRay) xray).getTransition();
                    if (xrt != null) {

                        // From
                        // http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
                        double[] pos = xray.getGenerationPos();
                        double radius =
                                Math2.magnitude(Math2.cross(normal,
                                        Math2.minus(center, pos)));

                        HistogramDouble emittedDistribution =
                                emittedDistributions.get(xrt);
                        if (emittedDistribution == null) {
                            emittedDistribution = emptyDistribution.clone();
                            emittedDistributions.put(xrt, emittedDistribution);
                        }
                        emittedDistribution.add(radius, xray.getIntensity());

                        HistogramDouble generatedDistribution =
                                generatedDistributions.get(xrt);
                        if (generatedDistribution == null) {
                            generatedDistribution = emptyDistribution.clone();
                            generatedDistributions.put(xrt,
                                    generatedDistribution);
                        }
                        generatedDistribution.add(radius, xray.getGenerated());
                    }
                }
            }
        }
            break;
        case MonteCarloSS.FirstTrajectoryEvent:
            emittedDistributions.clear();
            generatedDistributions.clear();
            break;
        }
    }



    /**
     * Returns the emitted radial distribution of the specified x-ray
     * transition.
     * 
     * @param xrt
     *            x-ray transition
     * @return emitted radial distribution
     */
    public HistogramDouble getEmittedDistribution(XRayTransition xrt) {
        HistogramDouble distribution = emittedDistributions.get(xrt);
        if (distribution == null)
            distribution = emptyDistribution.clone();
        return distribution;
    }



    /**
     * Returns the generated radial distribution of the specified x-ray
     * transition.
     * 
     * @param xrt
     *            x-ray transition
     * @return generated radial distribution
     */
    public HistogramDouble getGeneratedDistribution(XRayTransition xrt) {
        HistogramDouble distribution = generatedDistributions.get(xrt);
        if (distribution == null)
            distribution = emptyDistribution.clone();
        return distribution;
    }



    /**
     * Returns the x-ray transitions for which a radial distribution exists.
     * 
     * @return x-ray transitions
     */
    public Set<XRayTransition> getTransitions() {
        return Collections.unmodifiableSet(generatedDistributions.keySet());
    }
}
