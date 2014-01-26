package pymontecarlo.program.nistmonte.input.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.NISTMonte.BSERadialDistributionListener;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;
import gov.nist.microanalysis.Utility.Histogram;
import gov.nist.microanalysis.Utility.Math2;

import java.io.IOException;

import pymontecarlo.program.nistmonte.input.beam.GaussianFWHMBeam;
import pymontecarlo.util.hdf5.HDF5Group;

public class BackscatteredElectronRadialDetector extends AbstractDetector {

    /** Number of channels in the distribution. */
    private final int channels;

    /** Listener for the radial distribution. */
    private BSERadialDistributionListener radialDist;



    public BackscatteredElectronRadialDetector(int channels) {
        if (channels < 1)
            throw new IllegalArgumentException("Channels < 1");
        this.channels = channels;
    }



    @Override
    public void setup(MonteCarloSS mcss) throws EPQException {
        super.setup(mcss);

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

        radialDist =
                new BSERadialDistributionListener(center, normal, rmax,
                        channels, true);
        mcss.addActionListener(radialDist);
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);
        
        HDF5Group group = root.requireSubgroup(key);
        
        // Create array
        Histogram hist = radialDist.getDistribution();
        double[][] data = new double[channels][2];
        
        for (int i = 0; i < channels; i++) {
            data[i][0] = (hist.minValue(i) + hist.maxValue(i)) / 2.0;
            data[i][1] = hist.counts(i);
        }
        
        // Save dataset
        group.createDataset("data", data);
    }



    @Override
    public String getPythonResultClass() {
        return "BackscatteredElectronRadialResult";
    }

}
