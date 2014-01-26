package pymontecarlo.program.nistmonte.input.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayAccumulator3;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import pymontecarlo.util.hdf5.HDF5Dataset;
import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Detector to record intensity of photons.
 * 
 * @author ppinard
 */
public class PhotonIntensityDetector extends AbstractPhotonDetector {

    /** Stores the <code>XRayAccumulator</code> for characteristic x-rays. */
    private XRayAccumulator3 accumCharac = null;

    /**
     * Stores the <code>XRayAccumulator</code> for characteristic x-rays created
     * by fluorescence of characteristic x-rays.
     */
    private XRayAccumulator3 accumCharacFluo = null;

    /**
     * Stores the <code>XRayAccumulator</code> for characteristic x-rays created
     * by fluorescence of Bremsstrahlung x-rays.
     */
    private XRayAccumulator3 accumBremssFluo = null;

    /** Counter for the number of trajectories. */
    private int trajectoryCount;



    /**
     * Creates a new <code>PhotonIntensityDetector</code>.
     * 
     * @param takeOffAngle
     *            elevation from the x-y plane (in radians)
     * @param azimuthAngle
     *            counter-clockwise angle from the positive x-axis in the x-y
     *            plane (in radians)
     */
    public PhotonIntensityDetector(double takeOffAngle, double azimuthAngle) {
        super(takeOffAngle, azimuthAngle);
    }



    /**
     * Creates a new <code>PhotonIntensityDetector</code>.
     * 
     * @param position
     *            detector position in the chamber (in meters)
     */
    public PhotonIntensityDetector(double[] position) {
        super(position);
    }



    @Override
    protected void createLog(Properties props) {
        super.createLog(props);

        Set<Element> elements = new HashSet<>();
        for (XRayTransition xrayTransition : accumCharac.getTransitions()) {
            elements.add(xrayTransition.getElement());
        }
        props.setProperty("accumulators", elements.toString());
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);

        HDF5Group group = root.requireSubgroup(key);

        double normFactor = (double) trajectoryCount;

        String transitionName;
        HDF5Dataset ds;
        double gcf, gbf, gnf;
        double ecf, ebf, enf;
        int[][] emptyData = new int[][] { { 0 } };
        for (XRayTransition transition : accumCharac.getTransitions()) {
            transitionName =
                    transition.getElement().toAbbrev() + " "
                            + transition.getIUPACName();

            ds = group.createDataset(transitionName, emptyData);

            gnf = accumCharac.getGenerated(transition) / normFactor;
            enf = accumCharac.getEmitted(transition) / normFactor;

            gcf = ecf = 0.0;
            if (accumCharacFluo != null) {
                gcf = accumCharacFluo.getGenerated(transition) / normFactor;
                ecf = accumCharacFluo.getEmitted(transition) / normFactor;
            }

            gbf = ebf = 0.0;
            if (accumBremssFluo != null) {
                gbf = accumBremssFluo.getGenerated(transition) / normFactor;
                ebf = accumBremssFluo.getEmitted(transition) / normFactor;
            }

            ds.setAttribute("gcf", gcf, 0.0);
            ds.setAttribute("gbf", gbf, 0.0);
            ds.setAttribute("gnf", gnf, 0.0);
            ds.setAttribute("gt", gnf + gcf + gbf, 0.0);

            ds.setAttribute("ecf", ecf, 0.0);
            ds.setAttribute("ebf", ebf, 0.0);
            ds.setAttribute("enf", enf, 0.0);
            ds.setAttribute("et", enf + ecf + ebf, 0.0);
        }
    }



    @Override
    public boolean requiresBremmstrahlung() {
        return false;
    }



    @Override
    public void reset() {
        super.reset();
        accumCharac.clear();
        if (accumCharacFluo != null)
            accumCharacFluo.clear();
        if (accumBremssFluo != null)
            accumBremssFluo.clear();
        trajectoryCount = 0;
    }



    @Override
    public void setup(MonteCarloSS mcss, XRayTransport3 charac,
            XRayTransport3 bremss, XRayTransport3 characFluo,
            XRayTransport3 bremssFluo) throws EPQException {
        Collection<XRayTransition> transitions = findAllXRayTransitions(mcss);

        if (charac == null)
            throw new NullPointerException("charact == null");
        accumCharac = new XRayAccumulator3(transitions, "characteristic");
        charac.addXRayListener(accumCharac);

        if (characFluo != null) {
            accumCharacFluo =
                    new XRayAccumulator3(transitions,
                            "characteristic fluorescence");
            characFluo.addXRayListener(accumCharacFluo);
        }

        if (bremssFluo != null) {
            accumBremssFluo =
                    new XRayAccumulator3(transitions,
                            "Bremmstrahlung fluorescence");
            bremssFluo.addXRayListener(accumBremssFluo);
        }
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        switch (e.getID()) {
        case MonteCarloSS.TrajectoryStartEvent:
            trajectoryCount += 1;
            break;
        default:
            break;
        }
    }



    @Override
    public String getPythonResultClass() {
        return "PhotonIntensityResult";
    }

}
