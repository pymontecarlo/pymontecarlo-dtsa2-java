package pymontecarlo.program._analytical.options.detector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import pymontecarlo.util.hdf5.HDF5Group;
import gov.nist.microanalysis.EPQLibrary.AlgorithmUser;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.CorrectionAlgorithm;
import gov.nist.microanalysis.EPQLibrary.CorrectionAlgorithm.PhiRhoZAlgorithm;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.ElectronRange;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.Utility.HistogramUtil;

public class PhiZDetector extends AbstractPhotonDetector {

    /** Number of channels in the PRZ distribution. */
    private final int channels;

    /** Z values for all phi-z distributions. */
    private Map<XRayTransition, double[]> zPZS = new HashMap<>();

    private Map<XRayTransition, double[]> gnfPZs = new HashMap<>();

    private Map<XRayTransition, double[]> enfPZs = new HashMap<>();



    public PhiZDetector(double takeOffAngle, double azimuthAngle,
            int channels) {
        super(takeOffAngle, azimuthAngle);

        if (channels < 1)
            throw new IllegalArgumentException("Channels < 1");
        this.channels = channels;
    }



    public PhiZDetector(double[] pos, int channels) {
        super(pos);

        if (channels < 1)
            throw new IllegalArgumentException("Channels < 1");
        this.channels = channels;
    }



    @Override
    public void reset() {
        super.reset();
        zPZS.clear();
        gnfPZs.clear();
        enfPZs.clear();
    }



    @Override
    public void setup(SpectrumProperties props) throws EPQException {
        super.setup(props);

        // Calculate maximum depth
        Strategy strategy = AlgorithmUser.getGlobalStrategy();
        ElectronRange electronRange =
                (ElectronRange) strategy.getAlgorithm(ElectronRange.class);
        if (electronRange == null)
            electronRange = ElectronRange.Pouchou1991;

        props = getSpectrumProperties();
        Composition comp =
                props.getCompositionProperty(SpectrumProperties.MicroanalyticalComposition);
        double energy =
                ToSI.keV(props
                        .getNumericProperty(SpectrumProperties.BeamEnergy));
        double density =
                ToSI.gPerCC(props
                        .getNumericProperty(SpectrumProperties.SpecimenDensity));

        double rMax;
        for (XRayTransition xrt : findAllXRayTransitions(comp, props)) {
            rMax =
                    electronRange.compute(comp, xrt.getDestination(), energy)
                            / density;
            zPZS.put(xrt, HistogramUtil.createBins(-rMax, 0.0, channels));
            gnfPZs.put(xrt, new double[channels]);
            enfPZs.put(xrt, new double[channels]);
        }
    }



    @Override
    protected void createLog(Properties props) {
        super.createLog(props);
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);

        HDF5Group group = root.requireSubgroup(key);

        String transitionName;
        double[][] gnf, enf;
        HDF5Group transitionGroup;
        for (XRayTransition trans : zPZS.keySet()) {
            transitionName = trans.getIUPACName();
            transitionGroup = group.createSubgroup(transitionName);

            gnf = new double[2][channels];
            enf = new double[2][channels];

            gnf[0] = zPZS.get(trans);
            gnf[1] = gnfPZs.get(trans);

            enf[0] = zPZS.get(trans);
            enf[1] = enfPZs.get(trans);

            transitionGroup.createDataset("gnf", transpose(gnf));
            transitionGroup.createDataset("enf", transpose(enf));
        }
    }



    private double[][] transpose(double[][] a) {
        double[][] b = new double[a[0].length][a.length];
        for (int i = 0; i < a[0].length; i++) {
            for (int j = 0; j < a.length; j++) {
                b[i][j] = a[j][i];
            }
        }
        return b;
    }



    @Override
    public String getPythonResultClass() {
        return "PhiZResult";
    }



    @Override
    public void run() throws EPQException {
        SpectrumProperties props = getSpectrumProperties();
        Composition comp =
                props.getCompositionProperty(SpectrumProperties.MicroanalyticalComposition);
        double density =
                ToSI.gPerCC(props
                        .getNumericProperty(SpectrumProperties.SpecimenDensity));

        Strategy strategy = AlgorithmUser.getGlobalStrategy();
        PhiRhoZAlgorithm corrAlg =
                (PhiRhoZAlgorithm) strategy
                        .getAlgorithm(CorrectionAlgorithm.class);
        if (corrAlg == null)
            throw new NullPointerException("No correction algorithm defined");
        System.out.println(corrAlg);

        double[] zs;
        double rz;
        for (XRayTransition xrt : gnfPZs.keySet()) {
            corrAlg.initialize(comp, xrt.getDestination(), props);

            zs = zPZS.get(xrt);

            for (int i = 0; i < zs.length; i++) {
                rz = Math.abs(zs[i]) * density;
                gnfPZs.get(xrt)[i] = corrAlg.computeCurve(rz);
                enfPZs.get(xrt)[i] = corrAlg.computeAbsorbedCurve(xrt, rz);
            }
        }
    }

}
