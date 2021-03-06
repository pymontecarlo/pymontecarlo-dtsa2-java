package pymontecarlo.program._analytical.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pymontecarlo.program._analytical.options.detector.Detector;
import pymontecarlo.program._analytical.options.detector.PhiZDetector;
import pymontecarlo.program._analytical.options.detector.PhotonIntensityDetector;

/**
 * Factory of detector extractors.
 * 
 * @author ppinard
 */
public class DetectorExtractorFactory {

    protected abstract static class AbstractDelimitedDetectorExtractor
            implements
            DetectorExtractor {

        /**
         * Extracts the take-off angle.
         * 
         * @param detectorElement
         *            XML element
         * @return take-off angle (elevation)
         */
        protected double extractTakeOffAngle(Element detectorElement)
                throws IOException {
            Element elevationElement = detectorElement.getChild("elevation");

            double elevationMin, elevationMax;
            try {
                elevationMin =
                        elevationElement.getAttribute("lower").getDoubleValue();
                elevationMax =
                        elevationElement.getAttribute("upper").getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            return (elevationMin + elevationMax) / 2.0;
        }



        /**
         * Extracts the azimuth angle.
         * 
         * @param detectorElement
         *            XML element
         * @return azimuth angle
         */
        protected double extractAzimuthAngle(Element detectorElement)
                throws IOException {
            Element azimuthElement = detectorElement.getChild("azimuth");

            double azimuthMin, azimuthMax;
            try {
                azimuthMin =
                        azimuthElement.getAttribute("lower").getDoubleValue();
                azimuthMax =
                        azimuthElement.getAttribute("upper").getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            return (azimuthMin + azimuthMax) / 2.0;
        }



        protected List<XRayTransition> extractTransitions(
                Element detectorElement) throws IOException {
            List<XRayTransition> transitions = new ArrayList<>();

            int z, src, dest;
            gov.nist.microanalysis.EPQLibrary.Element element;
            for (Element subelement : detectorElement.getChildren("transition")) {
                try {
                    z = subelement.getAttribute("z").getIntValue();
                    src = subelement.getAttribute("src").getIntValue();
                    dest = subelement.getAttribute("dest").getIntValue();
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }

                element =
                        gov.nist.microanalysis.EPQLibrary.Element
                                .byAtomicNumber(z);
                transitions.add(new XRayTransition(element, src - 1, dest - 1));
            }

            return transitions;
        }

    }

    protected static class PhotonIntensityDetectorExtractor extends
            AbstractDelimitedDetectorExtractor {

        @Override
        public Detector extract(Element detectorElement)
                throws IOException,
                EPQException {
            double takeOffAngle = extractTakeOffAngle(detectorElement);
            double azimuthAngle = extractAzimuthAngle(detectorElement);
            List<XRayTransition> transitions =
                    extractTransitions(detectorElement);

            return new PhotonIntensityDetector(takeOffAngle, azimuthAngle,
                    transitions);
        }

    }

    /** Photon intensity detector extractor. */
    public static final DetectorExtractor PHOTON_INTENSITY =
            new PhotonIntensityDetectorExtractor();

    protected static class PhiZDetectorExtractor extends
            AbstractDelimitedDetectorExtractor {

        /**
         * Extracts the number of channels.
         * 
         * @param detectorElement
         *            XML element
         * @return channels
         */
        protected int extractChannels(Element detectorElement)
                throws IOException {
            Element channelsElement = detectorElement.getChild("channels");
            return Integer.parseInt(channelsElement.getText());
        }



        @Override
        public Detector extract(Element detectorElement)
                throws IOException,
                EPQException {
            double takeOffAngle = extractTakeOffAngle(detectorElement);
            double azimuthAngle = extractAzimuthAngle(detectorElement);
            int channels = extractChannels(detectorElement);
            List<XRayTransition> transitions =
                    extractTransitions(detectorElement);

            return new PhiZDetector(takeOffAngle, azimuthAngle, channels,
                    transitions);
        }
    }

    /** Photon depth detector extractor. */
    public static final DetectorExtractor PHI_Z =
            new PhiZDetectorExtractor();

}