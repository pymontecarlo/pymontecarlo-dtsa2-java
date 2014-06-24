package pymontecarlo.program._analytical.options.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;

import java.io.IOException;
import java.util.Properties;

import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Abstract detector.
 * 
 * @author ppinard
 */
public abstract class AbstractDetector implements Detector {

    private SpectrumProperties props = new SpectrumProperties();



    @Override
    public void reset() {
        props = new SpectrumProperties();
    }



    @Override
    public void setup(SpectrumProperties props) throws EPQException {
        this.props = props.clone();
    }



    protected void createLog(Properties props) {
        // Do nothing
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        HDF5Group group = root.requireSubgroup(key);

        // Set Python class
        group.setAttribute("_class", getPythonResultClass());

        // Save log
        Properties props = new Properties();
        createLog(props);
        group.setAttribute("log", props.toString());
    }



    protected SpectrumProperties getSpectrumProperties() {
        return props;
    }
}
