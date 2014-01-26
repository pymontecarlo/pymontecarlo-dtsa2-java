package pymontecarlo.program.nistmonte.input.detector;

import static org.junit.Assert.assertEquals;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.NISTMonte.BasicMaterialModel;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MultiPlaneShape;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;
import gov.nist.microanalysis.Utility.Math2;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import pymontecarlo.util.TestCase;
import pymontecarlo.util.hdf5.HDF5FileWriter;
import pymontecarlo.util.hdf5.HDF5Group;

public class AbstractPhotonDetectorTest extends TestCase {

    private static class PhotonDetectorMock extends AbstractPhotonDetector {

        public PhotonDetectorMock(double takeOffAngle, double azimuthAngle) {
            super(takeOffAngle, azimuthAngle);
        }



        public PhotonDetectorMock(double[] pos) {
            super(pos);
        }



        @Override
        public void setup(MonteCarloSS mcss, XRayTransport3 charact,
                XRayTransport3 bremss, XRayTransport3 charactFluo,
                XRayTransport3 bremssFluo) {

        }



        @Override
        public boolean requiresBremmstrahlung() {
            return false;
        }



        @Override
        public String getPythonResultClass() {
            return "PhotonResultMock";
        }

    }

    private AbstractPhotonDetector det;

    private File resultsFile;



    @Before
    public void setUp() throws Exception {
        det = new PhotonDetectorMock(Math.toRadians(40.0), 0.0);
        resultsFile = createTempFile("h5");
    }



    @Test
    public void testAbstractPhotonDetectorDoubleArray() {
        AbstractPhotonDetector det =
                new PhotonDetectorMock(new double[] { 0.01, 0.02, 0.03 });
        double[] pos = det.getDetectorPosition();
        assertEquals(0.01, pos[0], 1e-4);
        assertEquals(0.02, pos[1], 1e-4);
        assertEquals(0.03, pos[2], 1e-4);
    }



    @Test
    public void testGetDetectorPosition() {
        double[] pos = det.getDetectorPosition();
        assertEquals(0.07652784, pos[0], 1e-4);
        assertEquals(0.0, pos[1], 1e-4);
        assertEquals(0.06421448, pos[2], 1e-4);
    }



    @Test
    public void testCreateLog() throws IOException {
        Properties props = new Properties();
        det.createLog(props);
        assertEquals(3, props.size());
    }



    @Test
    public void testSaveResults() throws Exception {
        HDF5Group root = HDF5Group.createRoot();
        det.saveResults(root, "det1");
        HDF5FileWriter.write(root, resultsFile, true);
    }



    @Test
    public void testFindAllXRayTransitions() throws Exception {
        MonteCarloSS mcss = getMonteCarloSS();
        new Region(mcss.getChamber(),
                new BasicMaterialModel(
                        MaterialFactory.createPureElement(Element.Au)),
                MultiPlaneShape.createSubstrate(Math2.Z_AXIS,
                        Math2.ORIGIN_3D));

        Set<XRayTransition> transitions =
                AbstractPhotonDetector.findAllXRayTransitions(mcss);
        assertEquals(62, transitions.size());
    }

}
