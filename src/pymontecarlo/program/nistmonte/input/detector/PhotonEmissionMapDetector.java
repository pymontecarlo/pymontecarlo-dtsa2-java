package pymontecarlo.program.nistmonte.input.detector;

import java.io.IOException;

import pymontecarlo.program.nistmonte.input.beam.GaussianFWHMBeam;
import pymontecarlo.util.hdf5.HDF5Group;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.PhotonEmissionMapListener;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;

public class PhotonEmissionMapDetector extends AbstractPhotonDetector {

    private final int xBins, yBins, zBins;

    private PhotonEmissionMapListener mapCharac;
    
    private PhotonEmissionMapListener mapTotal;



    public PhotonEmissionMapDetector(double takeOffAngle, double azimuthAngle,
            int xBins, int yBins, int zBins) {
        super(takeOffAngle, azimuthAngle);

        if (xBins < 1)
            throw new IllegalArgumentException("bins < 1: " + xBins);
        this.xBins = xBins;

        if (yBins < 1)
            throw new IllegalArgumentException("bins < 1: " + yBins);
        this.yBins = yBins;

        if (zBins < 1)
            throw new IllegalArgumentException("bins < 1: " + zBins);
        this.zBins = zBins;
    }



    public PhotonEmissionMapDetector(double[] pos, int xBins, int yBins,
            int zBins) {
        super(pos);

        if (xBins < 1)
            throw new IllegalArgumentException("bins < 1: " + xBins);
        this.xBins = xBins;

        if (yBins < 1)
            throw new IllegalArgumentException("bins < 1: " + yBins);
        this.yBins = yBins;

        if (zBins < 1)
            throw new IllegalArgumentException("bins < 1: " + zBins);
        this.zBins = zBins;
    }



    @Override
    public void setup(MonteCarloSS mcss, XRayTransport3 charac,
            XRayTransport3 bremss, XRayTransport3 characFluo,
            XRayTransport3 bremssFluo) throws EPQException {
        if (charac == null)
            throw new NullPointerException("charact == null");
        
        // Calculate maximum dimensions
        double[] c0 = new double[3];
        double[] c1 = new double[3];
        mcss.estimateTrajectoryVolume(c0, c1);

        ElectronGun gun = mcss.getElectronGun();
        double offset = 0.0;
        if (gun instanceof GaussianFWHMBeam)
            offset = ((GaussianFWHMBeam) gun).getDiameter() / 2.0;

        double xMin = Math.min(c0[0], c1[0]) - offset;
        double xMax = Math.max(c0[0], c1[0]) + offset;
        double yMin = Math.min(c0[1], c1[1]) - offset;
        double yMax = Math.max(c0[1], c1[1]) + offset;
        double zMin = Math.min(c0[2], c1[2]);
        double zMax = Math.max(c0[2], c1[2]);

        // Create and register listener
        mapCharac =
                new PhotonEmissionMapListener(charac, xMin, xMax, xBins,
                        yMin, yMax, yBins, zMin, zMax, zBins);
        mapTotal =
                new PhotonEmissionMapListener(charac, xMin, xMax, xBins,
                        yMin, yMax, yBins, zMin, zMax, zBins);
        charac.addXRayListener(mapCharac);
        charac.addXRayListener(mapTotal);
        
        if (characFluo != null)
            characFluo.addXRayListener(mapTotal);

        if (bremssFluo != null)
            bremssFluo.addXRayListener(mapTotal);
    }



    @Override
    public boolean requiresBremmstrahlung() {
        return false;
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);
        
        HDF5Group group = root.requireSubgroup(key);

        String transitionName;
        double[][][] gnf, gt, enf, et;
        HDF5Group transitionGroup;
        for (XRayTransition trans : mapTotal.getTransitions()) {
            if (!trans.isWellKnown())
                continue;

            transitionName =
                    trans.getElement().toAbbrev() + " " + trans.getIUPACName();
            transitionGroup = group.createSubgroup(transitionName);

            gnf = mapCharac.getGeneratedDistribution(trans).getArray();
            gt = mapTotal.getGeneratedDistribution(trans).getArray();
            enf = mapCharac.getEmittedDistribution(trans).getArray();
            et = mapTotal.getGeneratedDistribution(trans).getArray();

            transitionGroup.createDataset("gnf", gnf);
            transitionGroup.createDataset("gt", gt);
            transitionGroup.createDataset("enf", enf);
            transitionGroup.createDataset("et", et);
        }
    }



    @Override
    public String getPythonResultClass() {
        return "PhotonEmissionMapResult";
    }

}
