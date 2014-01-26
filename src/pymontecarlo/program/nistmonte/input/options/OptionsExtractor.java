package pymontecarlo.program.nistmonte.input.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;

import pymontecarlo.program.nistmonte.input.detector.Detector;
import pymontecarlo.program.nistmonte.input.limit.Limit;

/**
 * Main extractor that extract all options from a options XML file.
 * 
 * @author ppinard
 */
public class OptionsExtractor implements Extractor {

    static {
        // Beam
        ExtractorManager.register("pencilBeam", BeamExtractorFactory.PENCIL);
        ExtractorManager.register("gaussianBeam",
                BeamExtractorFactory.GAUSSIAN_FWHM);

        // Geometry
        ExtractorManager.register("substrate",
                GeometryExtractorFactory.SUBSTRATE);
        ExtractorManager.register("inclusion",
                GeometryExtractorFactory.INCLUSION);
        ExtractorManager.register("multiLayers",
                GeometryExtractorFactory.MULTI_LAYERS);
        ExtractorManager.register("grainBoundaries",
                GeometryExtractorFactory.GRAIN_BOUNDARIES);
        ExtractorManager.register("thinGrainBoundaries",
                GeometryExtractorFactory.THIN_GRAIN_BOUNDARIES);

        // Detector
        ExtractorManager.register("photonIntensityDetector",
                DetectorExtractorFactory.PHOTON_INTENSITY);
        ExtractorManager
                .register("timeDetector", DetectorExtractorFactory.TIME);
        ExtractorManager.register("photonDepthDetector",
                DetectorExtractorFactory.PHOTON_DEPTH);
        ExtractorManager.register("photonRadialDetector",
                DetectorExtractorFactory.PHOTON_RADIAL);
        ExtractorManager.register("photonEmissionMapDetector",
                DetectorExtractorFactory.PHOTON_EMISSION_MAP);
        ExtractorManager.register("photonSpectrumDetector",
                DetectorExtractorFactory.PHOTON_SPECTRUM);
        ExtractorManager.register("trajectoryDetector",
                DetectorExtractorFactory.TRAJECTORY);
        ExtractorManager.register("backscatteredElectronRadialDetector",
                DetectorExtractorFactory.BACKSCATTERED_ELECTRON_RADIAL);

        // Limit
        ExtractorManager
                .register("showersLimit", LimitExtractorFactory.SHOWERS);

        // Model
        ExtractorManager.register("model", ModelExtractorFactory.ALL);
    }

    /** Name of the simulation. */
    private String name = null;

    /** Result <code>MonteCarloSS</code>. */
    private MonteCarloSS mcss = null;

    /** Extracted detectors. */
    private Map<String, Detector> detectors = null;

    /** Extracted limits. */
    private Set<Limit> limits = null;

    /** Extracted model strategy. */
    private Strategy strategy = null;



    /**
     * Creates a new <code>OptionsExtractor</code>.
     */
    public OptionsExtractor() {
    }



    /**
     * Creates a new <code>MonteCarloSS</code>.
     * 
     * @return a new <code>MonteCarloSS</code>
     */
    protected MonteCarloSS createMonteCarloSS() {
        return new MonteCarloSS();
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
        mcss = createMonteCarloSS();

        // Name
        name = root.getAttributeValue("name");

        // Beam
        ElectronGun beam = extractBeam(root);
        mcss.setBeamEnergy(beam.getBeamEnergy());
        mcss.setElectronGun(beam);

        // Geometry
        extractGeometry(root, mcss.getChamber());

        // Detectors
        detectors = extractDetectors(root);

        // Limits
        limits = extractLimits(root);

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
    protected ElectronGun extractBeam(Element root) throws IOException,
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
     * @param chamber
     *            region of the chamber as defined in <code>MonteCarloSS</code>
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the geometry
     * @return surface plane normal
     */
    protected void extractGeometry(Element root, Region chamber)
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
        extractor.extract(geometryElement, chamber);
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
     * Parses the XML options and returns the limits.
     * 
     * @param root
     *            XML options
     * @return limits
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the limits
     */
    protected Set<Limit> extractLimits(Element root)
            throws IOException, EPQException {
        Element limitsRoot = root.getChild("limits");

        Set<Limit> limits = new HashSet<Limit>();

        LimitExtractor extractor;
        Limit limit;
        for (Element limitElement : limitsRoot.getChildren()) {
            extractor =
                    (LimitExtractor) ExtractorManager.getExtractor(limitElement
                            .getName());
            limit = extractor.extract(limitElement);
            limits.add(limit);
        }

        return limits;
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



    /**
     * Returns the <code>MonteCarloSS</code> after {@link #extract(Element)} was
     * run.
     * 
     * @return <code>MonteCarloSS</code> read from options XML file
     * @throws RuntimeException
     *             if {@link #extract(Element)} was not run.
     */
    public MonteCarloSS getMonteCarloSS() {
        if (mcss == null)
            throw new RuntimeException("Call extract(Element) method first");
        return mcss;
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
     * Returns the <code>Limit</code>'s after {@link #extract(Element)} was run.
     * 
     * @return limits read from options XML file
     * @throws RuntimeException
     *             if {@link #extract(Element)} was not run.
     */
    public Set<Limit> getLimits() {
        if (limits == null)
            throw new RuntimeException("Call extract(Element) method first");
        return Collections.unmodifiableSet(limits);
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
