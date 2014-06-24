package pymontecarlo.program._analytical.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.Strategy;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import pymontecarlo.fileformat.Extractor;
import pymontecarlo.fileformat.ExtractorManager;
import pymontecarlo.fileformat.options.ModelExtractor;
import pymontecarlo.fileformat.options.ModelExtractorFactory;
import pymontecarlo.program._analytical.options.detector.Detector;

/**
 * Main extractor that extract all options from a options XML file.
 * 
 * @author ppinard
 */
public class OptionsExtractor implements Extractor {

    static {
        // Beam
        ExtractorManager.register("pencilBeam", BeamExtractorFactory.PENCIL);

        // Geometry
        ExtractorManager.register("substrate",
                GeometryExtractorFactory.SUBSTRATE);

        // Detector
        ExtractorManager.register("photonIntensityDetector",
                DetectorExtractorFactory.PHOTON_INTENSITY);
        ExtractorManager.register("phiRhoZDetector",
                DetectorExtractorFactory.PHIRHOZ);

        // Limit
        // No limit

        // Model
        ExtractorManager.register("model", ModelExtractorFactory.ALL);
    }

    public static final String VERSION = "6";

    /** Name of the simulation. */
    private String name = null;

    /** Spectrum properties. */
    private SpectrumProperties props = null;

    /** Extracted detectors. */
    private Map<String, Detector> detectors = null;

    /** Extracted model strategy. */
    private Strategy strategy = null;



    /**
     * Creates a new <code>OptionsExtractor</code>.
     */
    public OptionsExtractor() {
    }



    /**
     * Extracts options from a options XML element.
     * 
     * @param root
     *            options XML element
     * @throws IOException
     *             if an error occurs while reading the options XML element
     * @throws EPQException
     *             if an error occurs while setting up the options
     */
    public void extract(Element root) throws IOException, EPQException {
        props = new SpectrumProperties();

        // Header
        String version = root.getAttributeValue("version");
        if (!version.equalsIgnoreCase(VERSION))
            throw new IOException("Incompatible version: " + version + " != "
                    + VERSION);

        // Name
        name = root.getAttributeValue("name");

        // Beam
        props.addAll(extractBeam(root));

        // Geometry
        props.addAll(extractGeometry(root));

        // Detectors
        detectors = extractDetectors(root);

        // Models
        strategy = extractModels(root);
    }



    /**
     * Parses the XML options and returns the beam.
     * 
     * @param root
     *            XML options
     * @return electron beam
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the beam
     */
    protected SpectrumProperties extractBeam(Element root) throws IOException,
            EPQException {
        Element beamRoot = root.getChild("beam");

        // Get beam implementation
        List<Element> children = beamRoot.getChildren();
        if (children.isEmpty())
            throw new IOException("No beam implementation found");

        Element beamElement = children.get(0);

        BeamExtractor extractor =
                (BeamExtractor) ExtractorManager.getExtractor(beamElement
                        .getName());
        return extractor.extract(beamElement);
    }



    /**
     * Parses the XML options and sets the geometry inside the chamber.
     * 
     * @param root
     *            XML options
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the geometry
     * @return surface plane normal
     */
    protected SpectrumProperties extractGeometry(Element root)
            throws IOException, EPQException {
        Element geometryRoot = root.getChild("geometry");

        // Get geometry implementation
        List<Element> children = geometryRoot.getChildren();
        if (children.isEmpty())
            throw new IOException("No geometry implementation found");

        Element geometryElement = children.get(0);

        GeometryExtractor extractor =
                (GeometryExtractor) ExtractorManager
                        .getExtractor(geometryElement.getName());
        return extractor.extract(geometryElement);
    }



    /**
     * Parses the XML options and returns the detectors.
     * 
     * @param root
     *            XML options
     * @return map of detector and their key name
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the detectors
     */
    protected Map<String, Detector> extractDetectors(Element root)
            throws IOException, EPQException {
        Element detectorsRoot = root.getChild("detectors");

        Map<String, Detector> detectors = new HashMap<String, Detector>();

        DetectorExtractor extractor;
        String key;
        Detector detector;
        for (Element detectorElement : detectorsRoot.getChildren()) {
            key = detectorElement.getAttributeValue("_key");

            extractor =
                    (DetectorExtractor) ExtractorManager
                            .getExtractor(detectorElement.getName());
            detector = extractor.extract(detectorElement);

            detectors.put(key, detector);
        }

        return detectors;
    }



    /**
     * Parses the XML options and sets the models inside the strategy.
     * 
     * @param root
     *            XML options
     * @return strategy of algorithms
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the strategy
     */
    protected Strategy extractModels(Element root) throws IOException,
            EPQException {
        Element modelsRoot = root.getChild("models");

        Strategy strategy = new Strategy();

        ModelExtractor extractor;
        for (Element modelElement : modelsRoot.getChildren()) {
            extractor =
                    (ModelExtractor) ExtractorManager.getExtractor(modelElement
                            .getName());
            strategy.addAll(extractor.extract(modelElement));
        }

        return strategy;
    }



    /**
     * Returns the name of the simulation.
     * 
     * @return name of the simulation
     * @throws RuntimeException
     *             if {@link #extract(Element)} was not run.
     */
    public String getName() {
        if (name == null)
            throw new RuntimeException("Call extract(Element) method first");
        return name;
    }



    public SpectrumProperties getSpectrumProperties() {
        if (props == null)
            throw new RuntimeException("Call extract(Element) method first");
        return props;
    }



    /**
     * Returns the <code>Detector</code>'s after {@link #extract(Element)} was
     * run.
     * 
     * @return detectors read from options XML file
     * @throws RuntimeException
     *             if {@link #extract(Element)} was not run.
     */
    public Map<String, Detector> getDetectors() {
        if (detectors == null)
            throw new RuntimeException("Call extract(Element) method first");
        return Collections.unmodifiableMap(detectors);
    }



    /**
     * Returns the <code>Strategy</code> after {@link #extract(Element)} was
     * run.
     * 
     * @return strategy read from options XML file
     * @throws RuntimeException
     *             if {@link #extract(Element)} was not run.
     */
    public Strategy getStrategy() {
        if (strategy == null)
            throw new RuntimeException("Call extract(Element) method first");
        return (Strategy) strategy.clone();
    }

}
