package pymontecarlo.program.nistmonte.input.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Properties;

import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Abstract detector.
 * 
 * @author ppinard
 */
public abstract class AbstractDetector implements Detector {

    @Override
    public void actionPerformed(ActionEvent e) {
    }



    @Override
    public void setup(MonteCarloSS mcss) throws EPQException {
    }



    @Override
    public void reset() {
        // Do nothing
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
}
