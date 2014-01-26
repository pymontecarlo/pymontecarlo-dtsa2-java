package pymontecarlo.program.nistmonte.options.detector;

import static org.junit.Assert.*;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import pymontecarlo.util.TestCase;
import pymontecarlo.util.hdf5.HDF5FileWriter;
import pymontecarlo.util.hdf5.HDF5Group;

public class TrajectoryDetectorTest extends TestCase {

    private TrajectoryDetector det;

    private File resultsFile;



    @Before
    public void setUp() throws Exception {
        det = new TrajectoryDetector(false);
        resultsFile = createTempFile("h5");

        MonteCarloSS mcss = getMonteCarloSS();
        det.setup(mcss);

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
        assertEquals(1, props.size());
    }

}
