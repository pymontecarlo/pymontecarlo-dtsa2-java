package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.NISTMonte.Gen3.BaseXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;
import gov.nist.microanalysis.Utility.HistogramDouble3D;
import gov.nist.microanalysis.Utility.HistogramUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

public class PhotonEmissionMapListener implements ActionListener {

    /** Event listener for x-rays. */
    private final XRayTransport3 xrayEventListener;

    private final double[] xBinMins, yBinMins, zBinMins;

    private final double xMax, yMax, zMax;

    private final TreeMap<XRayTransition, HistogramDouble3D> emittedDistributions;

    private final TreeMap<XRayTransition, HistogramDouble3D> generatedDistributions;



    public PhotonEmissionMapListener(XRayTransport3 xrel,
            double xMin, double xMax, int xBins,
            double yMin, double yMax, int yBins,
            double zMin, double zMax, int zBins) {
        if (xrel == null)
            throw new NullPointerException("xrel == null");
        xrayEventListener = xrel;

        xBinMins = HistogramUtil.createBins(xMin, xMax, xBins);
        yBinMins = HistogramUtil.createBins(yMin, yMax, yBins);
        zBinMins = HistogramUtil.createBins(zMin, zMax, zBins);

        this.xMax = xMax;
        this.yMax = yMax;
        this.zMax = zMax;

        emittedDistributions = new TreeMap<>();
        generatedDistributions = new TreeMap<>();
    }



    @Override
    public void actionPerformed(ActionEvent ae) {
        assert ae.getSource() == xrayEventListener;

        switch (ae.getID()) {
        case BaseXRayGeneration3.XRayGeneration: {
            for (int i = xrayEventListener.getEventCount() - 1; i >= 0; i--) {
                XRayTransport3.XRayTr xrtransport =
                        (XRayTransport3.XRayTr) xrayEventListener.getXRay(i);
                XRayTransition xrt = xrtransport.getTransition();

                if (xrt != null) {
                    double[] pos = xrtransport.getInitialPos();

                    HistogramDouble3D emittedDistribution =
                            emittedDistributions.get(xrt);
                    if (emittedDistribution == null) {
                        emittedDistribution = createEmptyDistribution();
                        emittedDistributions.put(xrt, emittedDistribution);
                    }
                    emittedDistribution.add(pos[0], pos[1], pos[2],
                            xrtransport.getIntensity());

                    HistogramDouble3D generatedDistribution =
                            generatedDistributions.get(xrt);
                    if (generatedDistribution == null) {
                        generatedDistribution = createEmptyDistribution();
                        generatedDistributions.put(xrt, generatedDistribution);
                    }
                    generatedDistribution.add(pos[0], pos[1], pos[2],
                            xrtransport.getGenerated());
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
    public HistogramDouble3D getEmittedDistribution(XRayTransition xrt) {
        HistogramDouble3D distribution = emittedDistributions.get(xrt);
        if (distribution == null)
            distribution = createEmptyDistribution();
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
    public HistogramDouble3D getGeneratedDistribution(XRayTransition xrt) {
        HistogramDouble3D distribution = generatedDistributions.get(xrt);
        if (distribution == null)
            distribution = createEmptyDistribution();
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



    protected HistogramDouble3D createEmptyDistribution() {
        return new HistogramDouble3D(xBinMins, xMax,
                yBinMins, yMax,
                zBinMins, zMax);
    }

}
