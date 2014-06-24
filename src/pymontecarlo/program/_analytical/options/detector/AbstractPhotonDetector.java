package pymontecarlo.program._analytical.options.detector;

import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.EPQLibrary.XRayTransitionSet;
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
        this(new double[] { 1.0, Math.tan(azimuthAngle),
                Math.tan(takeOffAngle) });
    }



    /**
     * Creates a new <code>PhotonDetector</code>.
     * 
     * @param pos
     *            detector position in the chamber (in meters)
     */
    public AbstractPhotonDetector(double[] pos) {
        detectorPosition = Math2.normalize(pos);
    }



    @Override
    public void setup(SpectrumProperties props) throws EPQException {
        super.setup(props);
        getSpectrumProperties().setDetectorPosition(getDetectorPosition(), 0.0);
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
     * @throws EPQException
     */
    protected static Set<XRayTransition> findAllXRayTransitions(
            Composition comp, SpectrumProperties props) throws EPQException {
        double emax = props.getNumericProperty(SpectrumProperties.BeamEnergy);

        Set<XRayTransition> transitions = new HashSet<>();

        XRayTransitionSet transitionSet;
        for (Element element : comp.getElementSet()) {
            transitionSet = new XRayTransitionSet(element, 0.0, emax);
            for (XRayTransition transition : transitionSet.getTransitions()) {
                if (transition.isWellKnown()) {
                    transitions.add(transition);
                }
            }
        }

        return transitions;
    }

}
