package pymontecarlo.program.nistmonte.options.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;

public interface PhotonDetector extends Detector {

    /**
     * Returns the detector position in the chamber (in meters).
     * 
     * @return detector position (in meters)
     */
    public double[] getDetectorPosition();



    /**
     * Setups the simulation and other listeners. This typically involves adding
     * action listeners to collect data for this detector. Note that some x-ray
     * transports may be null if no Bremsstrahlung x-ray or fluorescence x-ray
     * are required.
     * 
     * @param mcss
     *            Monte Carlo simulation
     * @param charac
     *            x-ray transport object for characteristic x-rays
     * @param bremss
     *            x-ray transport object for Bremsstrahlung x-rays
     * @param characFluo
     *            x-ray transport object for characteristic fluorescence x-rays
     * @param bremssFluo
     *            x-ray transport object for Bremsstrahlung fluorescence x-rays
     */
    public void setup(MonteCarloSS mcss, XRayTransport3 charac,
            XRayTransport3 bremss, XRayTransport3 characFluo,
            XRayTransport3 bremssFluo) throws EPQException;



    /**
     * Returns whether the detector requires generation of Bremsstrahlung
     * x-rays.
     * 
     * @return <code>true</code> if Bremstrahlung x-rays should be generated,
     *         <code>false</code> otherwise
     */
    public boolean requiresBremmstrahlung();

}
