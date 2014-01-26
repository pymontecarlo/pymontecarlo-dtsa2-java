package pymontecarlo.program.nistmonte.input.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.EPQLibrary.XRayTransitionSet;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.RegionBase;
import gov.nist.microanalysis.Utility.Math2;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Abstract class of all photon detectors.
 * 
 * @author ppinard
 */
public abstract class AbstractPhotonDetector extends AbstractDetector implements
        PhotonDetector {

    /** Position of the detector in meters. */
    private final double[] detectorPosition;



    /**
     * Creates a new <code>PhotonDetector</code>.
     * 
     * @param takeOffAngle
     *            elevation from the x-y plane (in radians)
     * @param azimuthAngle
     *            counter-clockwise angle from the positive x-axis in the x-y
     *            plane (in radians)
     */
    public AbstractPhotonDetector(double takeOffAngle, double azimuthAngle) {
        double[] pos =
                new double[] { 1.0, Math.tan(azimuthAngle),
                        Math.tan(takeOffAngle) };
        detectorPosition =
                Math2.multiply(0.999 * MonteCarloSS.ChamberRadius,
                        Math2.normalize(pos));
    }



    /**
     * Creates a new <code>PhotonDetector</code>.
     * 
     * @param pos
     *            detector position in the chamber (in meters)
     */
    public AbstractPhotonDetector(double[] pos) {
        if (Math2.dot(pos, pos) >= Math.pow(MonteCarloSS.ChamberRadius, 2.0))
            throw new IllegalArgumentException(
                    "Detector position is outside the chamber. The chamber has a maximum radius of "
                            + MonteCarloSS.ChamberRadius + " m");
        detectorPosition = pos.clone();
    }



    @Override
    public void setup(MonteCarloSS mcss) throws EPQException {
        super.setup(mcss);
        setup(mcss, null, null, null, null);
    }



    @Override
    protected void createLog(Properties props) {
        super.createLog(props);

        props.setProperty("detectorPosition.x",
                Double.toString(detectorPosition[0]));
        props.setProperty("detectorPosition.y",
                Double.toString(detectorPosition[1]));
        props.setProperty("detectorPosition.z",
                Double.toString(detectorPosition[2]));
    }



    @Override
    public double[] getDetectorPosition() {
        return detectorPosition;
    }



    /**
     * Finds all x-ray transitions inside the geometry.
     * 
     * @param mcss
     *            Monte Carlo simulation
     * @return set of x-ray transitions
     */
    protected static Set<XRayTransition> findAllXRayTransitions(
            MonteCarloSS mcss) {
        double emax = mcss.getBeamEnergy();

        Set<XRayTransition> transitions = new HashSet<>();
        findAllXRayTransitions(mcss.getChamber(), emax, transitions);

        return transitions;
    }



    /**
     * Recursive to find all transitions for all elements in all regions. The
     * search considers the minimum energy value set for the scattering model.
     * 
     * @param region
     *            region
     * @param emax
     *            maximum energy (in Joules)
     * @param transitions
     *            set of transitions where the new transitions are added
     */
    private static void findAllXRayTransitions(RegionBase region, double emax,
            Set<XRayTransition> transitions) {
        double e0 = region.getScatterModel().getMinEforTracking();

        XRayTransitionSet transitionSet;
        for (Element element : region.getMaterial().getElementSet()) {
            transitionSet = new XRayTransitionSet(element, e0, emax);
            for (XRayTransition transition : transitionSet.getTransitions()) {
                if (transition.isWellKnown()) {
                    transitions.add(transition);
                }
            }
        }

        // Recursive
        for (RegionBase subRegion : region.getSubRegions())
            findAllXRayTransitions(subRegion, emax, transitions);
    }

}
