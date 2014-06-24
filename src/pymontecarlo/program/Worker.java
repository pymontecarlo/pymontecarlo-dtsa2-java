package pymontecarlo.program;

import gov.nist.microanalysis.EPQLibrary.EPQException;

import java.io.File;
import java.io.IOException;

/**
 * Runner for NistMonte.
 * 
 * @author ppinard
 */
public interface Worker {

    /** Version of results file. */
    public static final String VERSION = "7";



    /**
     * Returns whether the worker operates in quite mode.
     * 
     * @return is in quite mode
     */
    public boolean isQuite();



    /**
     * Sets whether the worker operates in quite mode
     * 
     * @param state
     *            state of quite mode
     */
    public void setQuite(boolean state);



    /**
     * Runs a simulation and saves the results
     * 
     * @throws EPQException
     *             if an error occurs while setting up the simulation or running
     *             it
     * @throws IOException
     *             if an error occurs while reading the options
     */
    public void run(File optionsFile, File resultsDir) throws EPQException,
            IOException;

}
