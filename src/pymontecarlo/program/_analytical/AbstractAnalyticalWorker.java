package pymontecarlo.program._analytical;

import gov.nist.microanalysis.EPQLibrary.AlgorithmUser;
import gov.nist.microanalysis.EPQLibrary.CorrectionAlgorithm.PhiRhoZAlgorithm;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.Strategy;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import pymontecarlo.program.AbstractWorker;
import pymontecarlo.program._analytical.fileformat.options.OptionsExtractor;
import pymontecarlo.program._analytical.options.detector.Detector;
import pymontecarlo.util.hdf5.HDF5FileWriter;
import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Worker for analytical correction models.
 * 
 * @author ppinard
 */
public abstract class AbstractAnalyticalWorker extends AbstractWorker {

    protected abstract PhiRhoZAlgorithm getCorrectionAlgorithm();



    @Override
    public void run(File optionsFile, File resultsDir) throws EPQException,
            IOException {
        if (optionsFile == null)
            throw new NullPointerException("options file == null");
        if (resultsDir == null)
            throw new NullPointerException("results dir == null");
        if (!resultsDir.isDirectory())
            throw new IllegalArgumentException("resultsDir must be a directory");

        // Extract from options XML file
        report(0.0, "Loading options file");

        SAXBuilder builder = new SAXBuilder();
        Element rootElement;
        try {
            rootElement = builder.build(optionsFile).getRootElement();
        } catch (JDOMException e) {
            throw new IOException(e);
        }

        OptionsExtractor extractor = new OptionsExtractor();
        extractor.extract(rootElement);

        // Create simulation
        String name = extractor.getName();
        SpectrumProperties props = extractor.getSpectrumProperties();
        Map<String, Detector> detectors = extractor.getDetectors();
        Strategy strategy = extractor.getStrategy();

        // Apply models' strategy
        report(0.0, "Setup models");
        strategy.addAlgorithm(PhiRhoZAlgorithm.class, getCorrectionAlgorithm());
        AlgorithmUser.applyGlobalOverride(strategy);

        // Setup and run detectors
        int i = 0;
        double length = detectors.size();
        for (Detector detector : detectors.values()) {
            i++;
            report(i / length, "Running");
            
            detector.reset();
            detector.setup(props);
            detector.run();
        }

        // Save results
        report(1.0, "Saving results");
        HDF5Group rootGroup =
                createHDF5Group(props, detectors, rootElement, name);
        File resultsH5 = new File(resultsDir, name + ".h5");
        HDF5FileWriter.write(rootGroup, resultsH5, true);
        report(1.0, "Complete");
    }



    private HDF5Group createHDF5Group(SpectrumProperties props,
            Map<String, Detector> detectors,
            Element rootElement, String name) throws IOException {
        HDF5Group rootGroup = HDF5Group.createRoot();

        // Save version, class
        rootGroup.setAttribute("version", VERSION);
        rootGroup.setAttribute("_class", "Results");

        // Create results group
        String identifier = rootElement.getAttributeValue("uuid");
        rootGroup.setAttribute("identifiers", new String[] { identifier });

        HDF5Group resultsGroup =
                rootGroup.createSubgroup("result-" + identifier);

        // Save results from detectors
        String key;
        Detector detector;
        for (Entry<String, Detector> entry : detectors.entrySet()) {
            key = entry.getKey();
            detector = entry.getValue();
            detector.saveResults(resultsGroup, key);
        }

        // Save overall log
        Properties logProps = new Properties();
        logProps.putAll(props.getPropertyMap());
        createLog(logProps);
        resultsGroup.setAttribute("log", props.toString());

        // Save options
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        String optionsStr = outputter.outputString(rootElement);
        resultsGroup.setAttribute("options", optionsStr);
        rootGroup.setAttribute("options", optionsStr);

        return rootGroup;
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
    private void createLog(Properties props)
            throws IOException {
        // Model
        Strategy strategy = AlgorithmUser.getGlobalStrategy();
        for (String algClass : strategy.listAlgorithmClasses()) {
            props.setProperty("model." + algClass,
                    strategy.getAlgorithm(algClass).toString());
        }
    }

}
