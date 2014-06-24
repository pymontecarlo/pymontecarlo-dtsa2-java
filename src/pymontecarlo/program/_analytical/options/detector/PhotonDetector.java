package pymontecarlo.program._analytical.options.detector;

public interface PhotonDetector extends Detector {

    /**
     * Returns the detector position in the chamber (in meters).
     * 
     * @return detector position (in meters)
     */
    public double[] getDetectorPosition();

}
