package pymontecarlo.program.nistmonte.options.detector;

import java.io.IOException;

import pymontecarlo.program.nistmonte.options.beam.GaussianFWHMBeam;
import pymontecarlo.util.hdf5.HDF5Group;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.PhotonRadialDistributionListener;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;
import gov.nist.microanalysis.Utility.Math2;

public class PhotonRadialDetector extends AbstractPhotonDetector {

    /** Number of channels in the distribution. */
    private final int channels;

    /** Listener for radial distribution (no fluorescence). */
    private PhotonRadialDistributionListener radialCharac;

    /** Listener for radial distribution (with fluorescence). */
    private PhotonRadialDistributionListener radialTotal;



    public PhotonRadialDetector(double takeOffAngle, double azimuthAngle,
            int channels) {
        super(takeOffAngle, azimuthAngle);

        if (channels < 1)
            throw new IllegalArgumentException("Channels < 1");
        this.channels = channels;
    }



    public PhotonRadialDetector(double[] pos, int channels) {
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

        ElectronGun gun = mcss.getElectronGun();
        double[] center = gun.getCenter();

        // FIXME: Should find a way to know the normal
        double[] normal = Math2.Z_AXIS;

        double rmax = Math2.max(new double[] { c0[0], c0[1], c1[0], c1[1] });
        if (gun instanceof GaussianFWHMBeam) {
            rmax += ((GaussianFWHMBeam) gun).getDiameter() / 2.0;
        }

        // Setup PRZs
        if (charac == null)
            throw new NullPointerException("charact == null");
        radialCharac =
                new PhotonRadialDistributionListener(charac, center, normal,
                        rmax, channels, true);
        radialTotal =
                new PhotonRadialDistributionListener(charac, center, normal,
                        rmax, channels, true);
        charac.addXRayListener(radialCharac);
        charac.addXRayListener(radialTotal);

        if (characFluo != null)
            characFluo.addXRayListener(radialTotal);

        if (bremssFluo != null)
            bremssFluo.addXRayListener(radialTotal);
    }



    @Override
    public boolean requiresBremmstrahlung() {
        return false;
    }



    @Override
    public String getPythonResultClass() {
        return "PhotonRadialResult";
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);

        HDF5Group group = root.requireSubgroup(key);

        String transitionName;
        double[][] gnf, gt, enf, et;
        HDF5Group transitionGroup;
        for (XRayTransition trans : radialTotal.getTransitions()) {
            if (!trans.isWellKnown())
                continue;

            transitionName = trans.getIUPACName();
            transitionGroup = group.createSubgroup(transitionName);

            gnf = radialCharac.getGeneratedDistribution(trans).getArray();
            gt = radialTotal.getGeneratedDistribution(trans).getArray();
            enf = radialCharac.getEmittedDistribution(trans).getArray();
            et = radialTotal.getEmittedDistribution(trans).getArray();

            transitionGroup.createDataset("gnf", gnf);
            transitionGroup.createDataset("gt", gt);
            transitionGroup.createDataset("enf", enf);
            transitionGroup.createDataset("et", et);
        }
    }

}
