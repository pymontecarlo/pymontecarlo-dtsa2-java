package pymontecarlo.program.nistmonte.runner;

import gov.nist.microanalysis.EPQLibrary.AlgorithmClass;
import gov.nist.microanalysis.EPQLibrary.AlgorithmUser;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.NISTMonte.IMaterialScatterModel;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.RegionBase;
import gov.nist.microanalysis.NISTMonte.Gen3.BremsstrahlungXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.CharacteristicXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.FluorescenceXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import pymontecarlo.program.nistmonte.options.beam.PencilBeam;
import pymontecarlo.program.nistmonte.options.detector.Detector;
import pymontecarlo.program.nistmonte.options.detector.PhotonDetector;
import pymontecarlo.program.nistmonte.options.limit.Limit;
import pymontecarlo.program.nistmonte.options.limit.ShowersLimit;
import pymontecarlo.program.nistmonte.options.model.FluorescenceMC;
import pymontecarlo.program.nistmonte.options.options.OptionsExtractor;
import pymontecarlo.util.hdf5.HDF5FileWriter;
import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Runner for NistMonte.
 * 
 * @author ppinard
 */
public class Worker {

    /** Version of results file. */
    private static final String VERSION = "7";

    /** XML file of the options. */
    private final File optionsFile;

    /** Results directory. */
    private final File resultsDir;

    /** Whether to run in quite mode. */
    private final boolean quite;



    /**
     * Creates a new runner process.
     * 
     * @param optionsFile
     *            XML file of the options
     * @param resultsDir
     *            results directory
     */
    public Worker(File optionsFile, File resultsDir, boolean quite) {
        if (optionsFile == null)
            throw new NullPointerException("options file == null");
        this.optionsFile = optionsFile;

        if (resultsDir == null)
            throw new NullPointerException("results dir == null");
        if (!resultsDir.isDirectory())
            throw new IllegalArgumentException("resultsDir must be a directory");
        this.resultsDir = resultsDir;

        this.quite = quite;
    }



    /**
     * Returns the results directory.
     * 
     * @return results directory
     */
    public File getResultsDir() {
        return resultsDir;
    }



    /**
     * Returns the options extractor to use to read the options XML file.
     * 
     * @return options extractor
     */
    protected OptionsExtractor getOptionsExtractor() {
        return new OptionsExtractor();
    }



    /**
     * Runs a simulation.
     * 
     * @return number of electron simulated
     * @throws EPQException
     *             if an error occurs while setting up the simulation or running
     *             it
     * @throws IOException
     *             if an error occurs while reading the options
     */
    public int run() throws EPQException, IOException {
        // Extract from options XML file
        report(0.0, "Loading options file");

        SAXBuilder builder = new SAXBuilder();
        Element rootElement;
        try {
            rootElement = builder.build(optionsFile).getRootElement();
        } catch (JDOMException e) {
            throw new IOException(e);
        }

        OptionsExtractor extractor = getOptionsExtractor();
        extractor.extract(rootElement);

        // Create simulation
        String name = extractor.getName();
        MonteCarloSS mcss = extractor.getMonteCarloSS();
        Map<String, Detector> detectors = extractor.getDetectors();
        Set<Limit> limits = extractor.getLimits();
        Strategy strategy = extractor.getStrategy();

        // Setup detectors
        report(0.0, "Setup detectors");
        setupDetectors(mcss, detectors.values(), strategy);

        // Register limits and get the number of showers
        report(0.0, "Setup limits");
        int showers = setupLimits(mcss, limits);

        // Apply models' strategy
        report(0.0, "Setup models");
        AlgorithmUser.applyGlobalOverride(strategy);

        // Run
        report(0.0, "Running showers");

        for (Detector det : detectors.values())
            det.reset();

        for (int n = 0; n < showers; n++) {
            report((double) n / showers, "Running showers");
            mcss.runTrajectory();
        }

        // Save results
        report(1.0, "Saving results");
        save(mcss, detectors, rootElement, name);
        report(1.0, "Complete");

        return showers;
    }



    private void setupDetectors(MonteCarloSS mcss,
            Collection<Detector> detectors, Strategy strategy)
            throws EPQException {
        // Register detector as action listener
        for (Detector det : detectors)
            mcss.addActionListener(det);

        // Split photon detectors from detectors
        Collection<Detector> dets = new ArrayList<>(detectors); // copy
        Collection<PhotonDetector> phDets = findPhotonDetectors(dets);
        dets.removeAll(phDets); // remove photon detectors

        // Setup other detectors
        for (Detector det : dets)
            det.setup(mcss);

        if (phDets.isEmpty())
            return;

        // Generation
        AlgorithmClass classFluo = strategy.getAlgorithm(FluorescenceMC.class);
        boolean hasFluo =
                classFluo == FluorescenceMC.Fluorescence ||
                        classFluo == FluorescenceMC.FluorescenceCompton;

        CharacteristicXRayGeneration3 characGen =
                CharacteristicXRayGeneration3.create(mcss);
        characGen.initialize(); // Bug fix

        BremsstrahlungXRayGeneration3 bremmsGen = null;
        if (hasFluo)
            bremmsGen = BremsstrahlungXRayGeneration3.create(mcss);
        else {
            for (PhotonDetector det : phDets) {
                if (det.requiresBremmstrahlung()) {
                    bremmsGen = BremsstrahlungXRayGeneration3.create(mcss);
                    break;
                }
            }
        }

        FluorescenceXRayGeneration3 characFluoGen = null;
        if (hasFluo) {
            characFluoGen = FluorescenceXRayGeneration3.create(mcss, characGen);
            characFluoGen
                    .setIncludeCompton(classFluo == FluorescenceMC.FluorescenceCompton);
        }

        FluorescenceXRayGeneration3 bremssFluoGen = null;
        if (hasFluo) {
            bremssFluoGen = FluorescenceXRayGeneration3.create(mcss, bremmsGen);
        }

        // Transport and setup
        Map<Integer, XRayTransport3> characMap = new HashMap<>();
        Map<Integer, XRayTransport3> bremssMap = new HashMap<>();
        Map<Integer, XRayTransport3> characFluoMap = new HashMap<>();
        Map<Integer, XRayTransport3> bremssFluoMap = new HashMap<>();

        Integer hashCode;
        double[] detPosition;
        XRayTransport3 charac, bremss, characFluo, bremssFluo;
        for (PhotonDetector det : phDets) {
            detPosition = det.getDetectorPosition();
            hashCode = Arrays.hashCode(detPosition);

            charac = characMap.get(hashCode);
            if (charac == null) {
                charac = XRayTransport3.create(mcss, detPosition, characGen);
                characMap.put(hashCode, charac);
            }

            bremss = bremssMap.get(hashCode);
            if (bremss == null && bremmsGen != null) {
                bremss = XRayTransport3.create(mcss, detPosition, bremmsGen);
                bremssMap.put(hashCode, bremss);
            }

            characFluo = characFluoMap.get(hashCode);
            if (characFluo == null && characFluoGen != null) {
                characFluo =
                        XRayTransport3.create(mcss, detPosition, characFluoGen);
                characFluoMap.put(hashCode, characFluo);
            }

            bremssFluo = bremssFluoMap.get(hashCode);
            if (bremssFluo == null && bremssFluoGen != null) {
                bremssFluo =
                        XRayTransport3.create(mcss, detPosition, bremssFluoGen);
                bremssFluoMap.put(hashCode, bremssFluo);
            }

            det.setup(mcss, charac, bremss, characFluo, bremssFluo);
        }
    }



    private int setupLimits(MonteCarloSS mcss, Set<Limit> limits)
            throws EPQException {
        int showers = 0;
        for (Limit limit : limits) {
            limit.setup(mcss);

            if (limit instanceof ShowersLimit)
                showers = ((ShowersLimit) limit).getMaximumShowers();
        }

        if (showers == 0)
            throw new EPQException("No ShowersLimit specified.");

        return showers;
    }



    private Collection<PhotonDetector> findPhotonDetectors(
            Collection<Detector> detectors) {
        List<PhotonDetector> photonDetectors = new ArrayList<>();
        for (Detector detector : detectors) {
            if (detector instanceof PhotonDetector) {
                photonDetectors.add((PhotonDetector) detector);
            }
        }
        return photonDetectors;
    }



    private void save(MonteCarloSS mcss, Map<String, Detector> detectors,
            Element rootElement, String name) throws IOException {
        HDF5Group rootGroup = HDF5Group.createRoot();

        // Save version, class
        rootGroup.setAttribute("version", VERSION);
        rootGroup.setAttribute("_class", "Results");

        // Create results group
        String identifier = "i" + UUID.randomUUID().toString().replace("-", "");
        rootGroup.setAttribute("identifiers", new String[] { identifier });

        HDF5Group resultsGroup = rootGroup.createSubgroup(identifier);

        // Save results from detectors
        String key;
        Detector detector;
        for (Entry<String, Detector> entry : detectors.entrySet()) {
            key = entry.getKey();
            detector = entry.getValue();

            detector.saveResults(resultsGroup, key);
        }

        // Save overall log
        Properties props = new Properties();
        createLog(props, mcss);
        resultsGroup.setAttribute("log", props.toString());

        // Save options
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        String optionsStr = outputter.outputString(rootElement);
        resultsGroup.setAttribute("options", optionsStr);
        rootGroup.setAttribute("options", optionsStr);

        File resultsH5 = new File(resultsDir, name + ".h5");
        HDF5FileWriter.write(rootGroup, resultsH5, true);
    }



    /**
     * Saves the parameters of the simulation.
     * 
     * @param resultsDir
     *            results directory
     * @param baseName
     *            name of the simulation
     * @param mcss
     *            MonteCarloSS
     * @throws IOException
     *             if an error occurs while saving the log file
     */
    protected void createLog(Properties props, MonteCarloSS mcss)
            throws IOException {
        // Beam
        ElectronGun beam = mcss.getElectronGun();
        props.setProperty("beam.energy",
                Double.toString(FromSI.eV(beam.getBeamEnergy())));
        props.setProperty("beam.center.x", Double.toString(beam.getCenter()[0]));
        props.setProperty("beam.center.y", Double.toString(beam.getCenter()[1]));
        props.setProperty("beam.center.z", Double.toString(beam.getCenter()[2]));

        if (beam instanceof PencilBeam) {
            props.setProperty("beam.direction.x",
                    Double.toString(((PencilBeam) beam).getDirection()[0]));
            props.setProperty("beam.direction.y",
                    Double.toString(((PencilBeam) beam).getDirection()[1]));
            props.setProperty("beam.direction.z",
                    Double.toString(((PencilBeam) beam).getDirection()[2]));
        }

        // Geometry
        List<RegionBase> regions = mcss.getChamber().getSubRegions();
        RegionBase region;
        Material material;
        IMaterialScatterModel model;
        String key;
        for (int i = 0; i < regions.size(); i++) {
            key = "geometry.region." + i + ".";
            region = regions.get(i);
            material = region.getMaterial();
            model = region.getScatterModel();

            props.setProperty(key + "material.name", material.getName());
            props.setProperty(key + "material.density",
                    Double.toString(material.getDensity()));
            props.setProperty(key + "model.absorptionEnergy",
                    Double.toString(FromSI.eV(model.getMinEforTracking())));
        }

        // Model
        Strategy strategy = AlgorithmUser.getGlobalStrategy();
        for (String algClass : strategy.listAlgorithmClasses()) {
            props.setProperty("model." + algClass,
                    strategy.getAlgorithm(algClass).toString());
        }
    }



    /**
     * Report progress and status.
     * 
     * @param progress
     *            current progress (between 0.0 and 1.0)
     * @param status
     *            current status
     */
    protected void report(double progress, String status) {
        if (!quite) {
            System.out.println(progress + "\t" + status);
            System.out.flush();
        }
    }
}
