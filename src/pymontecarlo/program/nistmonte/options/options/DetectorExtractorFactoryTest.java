package pymontecarlo.program.nistmonte.options.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;

import java.io.IOException;

import org.jdom2.Element;
import org.junit.Test;

import pymontecarlo.program.nistmonte.options.detector.PhotonIntensityDetector;
import static org.junit.Assert.assertEquals;

public class DetectorExtractorFactoryTest {

    public static Element createPhotonIntensityDetectorElement(String key) {
        Element element =
                new Element(
                        "photonIntensityDetector");
        element.setAttribute("_key", key);

        element.setAttribute("elevation_min",
                Double.toString(Math.toRadians(30)));
        element.setAttribute("elevation_max",
                Double.toString(Math.toRadians(50)));
        element.setAttribute("azimuth_min", Double.toString(Math.toRadians(0)));
        element.setAttribute("azimuth_max",
                Double.toString(Math.toRadians(180)));

        return element;
    }



    @Test
    public void testPHOTON_INTENSITY() throws IOException, EPQException {
        Element element = createPhotonIntensityDetectorElement("det1");

        DetectorExtractor extractor = DetectorExtractorFactory.PHOTON_INTENSITY;

        PhotonIntensityDetector det =
                (PhotonIntensityDetector) extractor.extract(element);

        double[] pos = det.getDetectorPosition();
        assertEquals(Math.toRadians(40.0), Math.atan2(pos[2], pos[0]), 1e-4);
        assertEquals(Math.toRadians(90.0), Math.atan2(pos[1], pos[0]), 1e-4);
    }

}
