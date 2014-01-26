package pymontecarlo.program.nistmonte.options.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;

import java.awt.event.ActionListener;
import java.io.IOException;

import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Interface for all detectors.
 * 
 * @author ppinard
 */
public interface Detector extends ActionListener {

    /**
     * Saves the data collected by the detector inside a ZIP.
     * 
     * @param root
     *            HDF5 root group
     * @param key
     *            key of the detector
     * @throws IOException
     *             if an error occurs while saving the results
     */
    public void saveResults(HDF5Group root, String key)
            throws IOException;



    /**
     * Method called at the start of a new simulation.
     */
    public void reset();



    /**
     * Setups the simulation and other listeners. This typically involves adding
     * action listeners to collect data for this detector.
     * 
     * @param mcss
     *            Monte Carlo simulation
     */
    public void setup(MonteCarloSS mcss) throws EPQException;



    /**
     * Returns the name of the Python result class associated to this detector.
     * 
     * @return Python result class
     */
    public String getPythonResultClass();

}
