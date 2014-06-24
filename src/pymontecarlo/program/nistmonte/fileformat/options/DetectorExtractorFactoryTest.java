package pymontecarlo.program.nistmonte.fileformat.options;

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

        Element elevationElement = new Element("elevation");
        elevationElement.setAttribute("lower",
                Double.toString(Math.toRadians(30)));
        elevationElement.setAttribute("upper",
                Double.toString(Math.toRadians(50)));
        element.addContent(elevationElement);
        
        Element azimuthElement = new Element("azimuth");
        azimuthElement.setAttribute("lower", Double.toString(Math.toRadians(0)));
        azimuthElement.setAttribute("upper", Double.toString(Math.toRadians(180)));
        element.addContent(azimuthElement);

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
