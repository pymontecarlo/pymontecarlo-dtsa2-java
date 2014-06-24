package pymontecarlo.program._analytical.options.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;

import java.io.IOException;

import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Interface for all detectors.
 * 
 * @author ppinard
 */
public interface Detector {

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
     * Setups the simulation.
     * 
     * @param props
     *            spectrum properties
     * @throws EPQException
     *             if an error occurs
     */
    public void setup(SpectrumProperties props) throws EPQException;



    /**
     * Runs the simulation.
     * 
     * @throws EPQException
     *             if an error occurs
     */
    public void run() throws EPQException;



    /**
     * Returns the name of the Python result class associated to this detector.
     * 
     * @return Python result class
     */
    public String getPythonResultClass();

}
