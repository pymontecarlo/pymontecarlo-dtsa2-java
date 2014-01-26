package pymontecarlo.program.nistmonte.options.detector;

import static org.junit.Assert.assertEquals;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import pymontecarlo.util.TestCase;
import pymontecarlo.util.hdf5.HDF5FileWriter;
import pymontecarlo.util.hdf5.HDF5Group;

public class PhotonIntensityDetectorTest extends TestCase {

    private PhotonIntensityDetector det;

    private File resultsFile;



    @Before
    public void setUp() throws Exception {
        det = new PhotonIntensityDetector(getDetectorPosition());
        resultsFile = createTempFile("zip");

        MonteCarloSS mcss = getMonteCarloSS();
        XRayTransport3 charac = getCharacteristicTransport(mcss);
        XRayTransport3 bremss = getBremmstrahlungTransport(mcss);
        XRayTransport3 characFluo =
                getCharacteristicFluoTransport(mcss, charac);
        XRayTransport3 bremssFluo =
                getBremmstrahlungFluoTransport(mcss, bremss);
        det.setup(mcss, charac, bremss, characFluo, bremssFluo);

        mcss.runTrajectory();
    }



    @Test
    public void testSaveResults() throws Exception {
        HDF5Group root = HDF5Group.createRoot();
        det.saveResults(root, "det1");
        HDF5FileWriter.write(root, resultsFile, true);
    }



    @Test
    public void testCreateLog() throws IOException {
        Properties props = new Properties();
        det.createLog(props);
        assertEquals(4, props.size());
    }
}
