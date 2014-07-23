package pymontecarlo.program._analytical.options.detector;

import gov.nist.microanalysis.EPQLibrary.AbsoluteIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.AlgorithmUser;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.CorrectionAlgorithm.PhiRhoZAlgorithm;
import gov.nist.microanalysis.EPQLibrary.AtomicShell;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FluorescenceYield;
import gov.nist.microanalysis.EPQLibrary.IonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.PhysicalConstants;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pymontecarlo.util.hdf5.HDF5Dataset;
import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Detector to record intensity of photons.
 * 
 * @author ppinard
 */
public class PhotonIntensityDetector extends AbstractPhotonDetector {

    private final List<XRayTransition> transitions;

    private final Map<XRayTransition, Double> enfIntensities;

    private final Map<XRayTransition, Double> etIntensities;

    private final Map<XRayTransition, Double> gnfIntensities;



    /**
     * Creates a new <code>PhotonIntensityDetector</code>.
     * 
     * @param takeOffAngle
     *            elevation from the x-y plane (in radians)
     * @param azimuthAngle
     *            counter-clockwise angle from the positive x-axis in the x-y
     *            plane (in radians)
     */
    public PhotonIntensityDetector(double takeOffAngle, double azimuthAngle,
            List<XRayTransition> transitions) {
        super(takeOffAngle, azimuthAngle);

        this.transitions = new ArrayList<>();
        this.transitions.addAll(transitions);

        enfIntensities = new HashMap<>();
        etIntensities = new HashMap<>();
        gnfIntensities = new HashMap<>();
    }



    @Override
    public void reset() {
        super.reset();
        enfIntensities.clear();
        etIntensities.clear();
        gnfIntensities.clear();
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);

        HDF5Group group = root.requireSubgroup(key);

        String transitionName;
        HDF5Dataset ds;
        int[][] emptyData = new int[][] { { 0 } };
        for (XRayTransition transition : transitions) {
            transitionName = transition.getIUPACName();

            ds = group.createDataset(transitionName, emptyData);

            ds.setAttribute("gnf", gnfIntensities.get(transition), 0.0);
            ds.setAttribute("enf", enfIntensities.get(transition), 0.0);
            ds.setAttribute("et", etIntensities.get(transition), 0.0);
        }
    }



    @Override
    public String getPythonResultClass() {
        return "PhotonIntensityResult";
    }



    @Override
    public void setup(SpectrumProperties props) throws EPQException {
        super.setup(props);

        props = getSpectrumProperties();
        Composition comp =
                props.getCompositionProperty(SpectrumProperties.MicroanalyticalComposition);

        if (transitions.isEmpty())
            transitions.addAll(findAllXRayTransitions(comp, props));
    }



    @Override
    public void run() throws EPQException {
        SpectrumProperties props = getSpectrumProperties();
        Composition comp =
                props.getCompositionProperty(SpectrumProperties.MicroanalyticalComposition);
        double energy =
                ToSI.keV(props
                        .getNumericProperty(SpectrumProperties.BeamEnergy));

        // Create strategy
        Strategy strategy = AlgorithmUser.getGlobalStrategy();
        PhiRhoZAlgorithm corrAlg =
                (PhiRhoZAlgorithm) strategy
                        .getAlgorithm(PhiRhoZAlgorithm.class);
        if (corrAlg == null)
            throw new NullPointerException("No correction algorithm defined");
        IonizationCrossSection icx =
                (IonizationCrossSection) strategy
                        .getAlgorithm(IonizationCrossSection.class);
        if (icx == null)
            icx = AbsoluteIonizationCrossSection.BoteSalvat2008;
        FluorescenceYield fy =
                (FluorescenceYield) strategy
                        .getAlgorithm(FluorescenceYield.class);

        // Calculate intensities
        Element element;
        AtomicShell shell;
        double q, wf, yield, lineWeight, atomicWeight, factor;
        for (XRayTransition xrt : transitions) {
            element = xrt.getElement();

            if (comp.containsElement(element)) {
                shell = xrt.getDestination();
                corrAlg.initialize(comp, shell, props);
                q = icx.computeShell(shell, energy);
                wf = comp.weightFraction(element, false);
                yield = fy.compute(shell);
                atomicWeight = element.getAtomicWeight();
                lineWeight = xrt.getWeight(XRayTransition.NormalizeFamily);

                factor =
                        (q * yield * lineWeight)
                                * (wf / atomicWeight * PhysicalConstants.AvagadroNumber);

                etIntensities.put(xrt, corrAlg.computeZAFCorrection(xrt)
                        * factor);
                enfIntensities.put(xrt, corrAlg.computeZACorrection(xrt)
                        * factor);
                gnfIntensities.put(xrt, corrAlg.generated(xrt) * factor);
            } else {
                etIntensities.put(xrt, 0.0);
                enfIntensities.put(xrt, 0.0);
                gnfIntensities.put(xrt, 0.0);
            }
        }

    }
}
