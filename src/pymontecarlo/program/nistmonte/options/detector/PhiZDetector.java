package pymontecarlo.program.nistmonte.options.detector;

import java.io.IOException;
import java.util.Properties;

import pymontecarlo.util.hdf5.HDF5Group;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.Gen3.PhiRhoZ3;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;

public class PhiZDetector extends AbstractPhotonDetector {

    /** Number of channels in the PRZ distribution. */
    private final int channels;

    /** Z values for all PRZ distributions. */
    private double[] zs;

    /** PRZ distribution of characteristic x-rays. */
    private PhiRhoZ3 przCharac = null;



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
    public void setup(MonteCarloSS mcss, XRayTransport3 charac,
            XRayTransport3 bremss, XRayTransport3 characFluo,
            XRayTransport3 bremssFluo) throws EPQException {
        // Calculate maximum depth
        double[] c0 = new double[3];
        double[] c1 = new double[3];
        mcss.estimateTrajectoryVolume(c0, c1);

        double zMin = c0[2];
        double zMax = c1[2];

        // Setup PRZs
        if (charac == null)
            throw new NullPointerException("charact == null");
        przCharac = new PhiRhoZ3(charac, zMin, zMax, channels);
        charac.addXRayListener(przCharac);

        // Setup zs
        zs = new double[channels];
        double width = przCharac.binWidth();
        for (int i = 0; i < channels; i++) {
            zs[i] = zMin + i * width + width / 2.0;
        }
    }



    @Override
    protected void createLog(Properties props) {
        super.createLog(props);
        props.setProperty("minz", Double.toString(zs[0]));
        props.setProperty("maxz", Double.toString(zs[zs.length - 1]));
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);

        HDF5Group group = root.requireSubgroup(key);

        String transitionName;
        double[][] gnf, gt, enf, et;
        HDF5Group transitionGroup;
        for (XRayTransition trans : przCharac.getTransitions()) {
            if (!trans.isWellKnown())
                continue;

            transitionName = trans.getIUPACName();
            transitionGroup = group.createSubgroup(transitionName);

            gnf = new double[2][channels];
            gt = new double[2][channels];
            enf = new double[2][channels];
            et = new double[2][channels];

            gnf[0] = zs;
            gnf[1] = przCharac.getGenerated(trans);

            gt[0] = zs;
            gt[1] = gnf[1].clone();

            enf[0] = zs;
            enf[1] = przCharac.getEmitted(trans);

            et[0] = zs;
            et[1] = enf[1].clone();

            transitionGroup.createDataset("gnf", transpose(gnf));
            transitionGroup.createDataset("gt", transpose(gt));
            transitionGroup.createDataset("enf", transpose(enf));
            transitionGroup.createDataset("et", transpose(et));
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
    public boolean requiresBremmstrahlung() {
        return false;
    }



    @Override
    public String getPythonResultClass() {
        return "PhiZResult";
    }

}
