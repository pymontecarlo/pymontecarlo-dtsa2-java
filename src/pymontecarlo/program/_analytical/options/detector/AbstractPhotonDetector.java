package pymontecarlo.program._analytical.options.detector;

import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.EPQLibrary.XRayTransitionSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class of all photon detectors.
 * 
 * @author ppinard
 */
public abstract class AbstractPhotonDetector extends AbstractDetector implements
        PhotonDetector {

    /** Elevation angle (in radians). */
    private final double elevationAngle;

    /** Azimuth angle (in radians). */
    private final double azimuthAngle;



    @Override
    public double getElevationAngle() {
        return elevationAngle;
    }



    @Override
    public double getAzimuthAngle() {
        return azimuthAngle;
    }



    /**
     * Creates a new <code>PhotonDetector</code>.
     * 
     * @param elevationAngle
     *            elevation from the x-y plane (in radians)
     * @param azimuthAngle
     *            counter-clockwise angle from the positive x-axis in the x-y
     *            plane (in radians)
     */
    public AbstractPhotonDetector(double elevationAngle, double azimuthAngle) {
        this.elevationAngle = elevationAngle;
        this.azimuthAngle = azimuthAngle;
    }



    @Override
    public void setup(SpectrumProperties props) throws EPQException {
        super.setup(props);
        getSpectrumProperties().setDetectorPosition(getElevationAngle(),
                getAzimuthAngle(), 0.001, 0.011);
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
        double emax =
                ToSI.keV(props
                        .getNumericProperty(SpectrumProperties.BeamEnergy));

        Set<XRayTransition> transitions = new HashSet<>();

        XRayTransitionSet transitionSet;
        for (Element element : comp.getElementSet()) {
            transitionSet = new XRayTransitionSet(element, 0.0, emax);
            for (XRayTransition transition : transitionSet.getTransitions()) {
                if (transition.isWellKnown()
                        && transition.getEdgeEnergy() < emax) {
                    transitions.add(transition);
                }
            }
        }

        return transitions;
    }

}
