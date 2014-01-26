package pymontecarlo.program.nistmonte.options.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;

import java.io.IOException;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pymontecarlo.program.nistmonte.options.detector.BackscatteredElectronRadialDetector;
import pymontecarlo.program.nistmonte.options.detector.Detector;
import pymontecarlo.program.nistmonte.options.detector.PhotonDepthDetector;
import pymontecarlo.program.nistmonte.options.detector.PhotonEmissionMapDetector;
import pymontecarlo.program.nistmonte.options.detector.PhotonIntensityDetector;
import pymontecarlo.program.nistmonte.options.detector.PhotonRadialDetector;
import pymontecarlo.program.nistmonte.options.detector.PhotonSpectrumDetector;
import pymontecarlo.program.nistmonte.options.detector.TimeDetector;
import pymontecarlo.program.nistmonte.options.detector.TrajectoryDetector;

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

    }

    protected static class PhotonIntensityDetectorExtractor extends
            AbstractDelimitedDetectorExtractor {

        @Override
        public Detector extract(Element detectorElement) throws IOException,
                EPQException {
            double takeOffAngle = extractTakeOffAngle(detectorElement);
            double azimuthAngle = extractAzimuthAngle(detectorElement);

            return new PhotonIntensityDetector(takeOffAngle, azimuthAngle);
        }

    }

    /** Photon intensity detector extractor. */
    public static final DetectorExtractor PHOTON_INTENSITY =
            new PhotonIntensityDetectorExtractor();

    protected static class TimeDetectorExtractor implements DetectorExtractor {

        @Override
        public Detector extract(Element detectorElement) throws IOException,
                EPQException {
            return new TimeDetector();
        }

    }

    /** Time detector extractor. */
    public static final DetectorExtractor TIME = new TimeDetectorExtractor();

    protected static class PhotonDepthDetectorExtractor extends
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
            return Integer.getInteger(channelsElement.getText());
        }



        @Override
        public Detector extract(Element detectorElement) throws IOException,
                EPQException {
            double takeOffAngle = extractTakeOffAngle(detectorElement);
            double azimuthAngle = extractAzimuthAngle(detectorElement);
            int channels = extractChannels(detectorElement);

            return new PhotonDepthDetector(takeOffAngle, azimuthAngle, channels);
        }

    }

    /** Photon depth detector extractor. */
    public static final DetectorExtractor PHOTON_DEPTH =
            new PhotonDepthDetectorExtractor();

    protected static class PhotonRadialDetectorExtractor extends
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
            return Integer.getInteger(channelsElement.getText());
        }



        @Override
        public Detector extract(Element detectorElement) throws IOException,
                EPQException {
            double takeOffAngle = extractTakeOffAngle(detectorElement);
            double azimuthAngle = extractAzimuthAngle(detectorElement);
            int channels = extractChannels(detectorElement);

            return new PhotonRadialDetector(takeOffAngle, azimuthAngle,
                    channels);
        }

    }

    /** Photon radial detector extractor. */
    public static final DetectorExtractor PHOTON_RADIAL =
            new PhotonRadialDetectorExtractor();

    protected static class PhotonEmissionMapDetectorExtractor extends
            AbstractDelimitedDetectorExtractor {

        @Override
        public Detector extract(Element detectorElement) throws IOException,
                EPQException {
            double takeOffAngle = extractTakeOffAngle(detectorElement);
            double azimuthAngle = extractAzimuthAngle(detectorElement);
            int xBins =
                    Integer.getInteger(detectorElement.getChild("xbins")
                            .getText());
            int yBins =
                    Integer.getInteger(detectorElement.getChild("ybins")
                            .getText());
            int zBins =
                    Integer.getInteger(detectorElement.getChild("zbins")
                            .getText());

            return new PhotonEmissionMapDetector(takeOffAngle, azimuthAngle,
                    xBins, yBins, zBins);
        }

    }

    /** Photon emission map detector extractor. */
    public static final DetectorExtractor PHOTON_EMISSION_MAP =
            new PhotonEmissionMapDetectorExtractor();

    protected static class PhotonSpectrumDetectorExtractor extends
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
            return Integer.getInteger(channelsElement.getText());
        }



        /**
         * Extracts the channel width.
         * 
         * @param detectorElement
         *            XML element
         * @return channels
         */
        protected double extractChannelWidth(Element detectorElement)
                throws IOException {
            int channels = extractChannels(detectorElement);

            Element limitElement = detectorElement.getChild("limits");

            double limitMin, limitMax;
            try {
                limitMin = limitElement.getAttribute("lower").getDoubleValue();
                limitMax = limitElement.getAttribute("upper").getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            return (limitMax - limitMin) / channels;
        }



        @Override
        public Detector extract(Element detectorElement) throws IOException,
                EPQException {
            double takeOffAngle = extractTakeOffAngle(detectorElement);
            double azimuthAngle = extractAzimuthAngle(detectorElement);
            double channelWidth = extractChannelWidth(detectorElement);
            int channels = extractChannels(detectorElement);

            return new PhotonSpectrumDetector(takeOffAngle, azimuthAngle,
                    channelWidth, channels);
        }

    }

    /** Photon spectrum detector extractor. */
    public static final DetectorExtractor PHOTON_SPECTRUM =
            new PhotonSpectrumDetectorExtractor();

    protected static class BackscatteredElectronRadialDetectorExtractor
            implements DetectorExtractor {

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
            return Integer.getInteger(channelsElement.getText());
        }



        @Override
        public Detector extract(Element detectorElement) throws IOException,
                EPQException {
            int channels = extractChannels(detectorElement);

            return new BackscatteredElectronRadialDetector(channels);
        }

    }

    /** Backscattered electron radial detector extractor. */
    public static final DetectorExtractor BACKSCATTERED_ELECTRON_RADIAL =
            new BackscatteredElectronRadialDetectorExtractor();

    protected static class TrajectoryDetectorExtractor implements
            DetectorExtractor {

        @Override
        public Detector extract(Element detectorElement) throws IOException,
                EPQException {
            Element secondaryElement = detectorElement.getChild("secondary");
            boolean secondary = Boolean.getBoolean(secondaryElement.getText());

            return new TrajectoryDetector(secondary);
        }

    }

    /** Trajectory detector extractor. */
    public static final DetectorExtractor TRAJECTORY =
            new TrajectoryDetectorExtractor();
}
