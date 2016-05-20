package pymontecarlo.program.nistmonte.options.beam;

import gov.nist.microanalysis.NISTMonte.Electron;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;
import gov.nist.microanalysis.Utility.Math2;

import java.util.Random;

/**
 * Electron beam where the diameter is specified at the FWHM.
 * 
 * @author ppinard
 */
public class GaussianFWHMExpTailBeam extends PencilBeam implements ElectronGun {

    /** Random number generator. */
    private final transient Random random = Math2.rgen;

    /** Beam diameter. */
    private double diameter;

    /** Skirt threshold. */
    private double skirtThreshold;

    /** Skirt factor. */
    private double skirtFactor;

    /** Conversion to Gaussian (1 sigma) to FWHM. */
    private static final double GAUSSIAN_TO_FWHM = 2.0 * Math.sqrt(2.0 * Math
            .log(2.0));



    /**
     * Creates a instance of the ElectronGun interface modeling a beam where the
     * diameter is specified at the FWHM.
     * 
     * @param diameter
     *            beam diameter at FWHM (in meters)
     */
    public GaussianFWHMExpTailBeam(double diameter, double skirtThreshold,
            double skirtFactor) {
        setDiameter(diameter);
        setSkirtThreshold(skirtThreshold);
        setSkirtFactor(skirtFactor);
    }



    @Override
    public Electron createElectron() {
        final double[] initialPos = getCenter();
        final double sigma = diameter / GAUSSIAN_TO_FWHM;
        double x = random.nextGaussian();

        final double r;
        if (Math.exp(-Math.pow(x, 2)) < getSkirtThreshold())
            r =
                    Math.signum(x) * sigma * getSkirtFactor()
                            * Math.log(random.nextDouble());
        else
            r = x * sigma;

        final double th = 2.0 * Math.PI * random.nextDouble();
        initialPos[0] += r * Math.cos(th);
        initialPos[1] += r * Math.sin(th);
        return new Electron(initialPos, getTheta(), getPhi(), getBeamEnergy());
    }



    /**
     * Returns the beam diameter at FWHM (in meters).
     * 
     * @return beam diameter at FWHM (in meters).
     */
    public double getDiameter() {
        return diameter;
    }



    /**
     * Sets the beam diameter at FWHM.
     * 
     * @param diameter
     *            beam diameter at FWHM (in meters)
     * @throws IllegalArgumentException
     *             if the diameter is less than 0.0
     */
    public void setDiameter(double diameter) {
        if (diameter < 0.0)
            throw new IllegalArgumentException(
                    "Diameter must be greater or equal to 0.0");
        this.diameter = diameter;
    }



    /**
     * Returns the skirt threshold, switching point between Gaussian and
     * exponential beam.
     * 
     * @return skirt threshold.
     */
    public double getSkirtThreshold() {
        return skirtThreshold;
    }



    /**
     * Sets the skirt threshold, switching point between Gaussian and
     * exponential beam.
     * 
     * @param skirtThreshold
     *            skirt threshold
     * @throws IllegalArgumentException
     *             if the threshold is not in between 0.0 and 1.0
     */
    public void setSkirtThreshold(double skirtThreshold) {
        if (skirtThreshold < 0.0 || skirtThreshold > 1.0)
            throw new IllegalArgumentException(
                    "Skirt factor must be greater than 0.0");
        this.skirtThreshold = skirtThreshold;
    }



    /**
     * Returns the skirt factor, a multiplier of the beam diameter 1sigma.
     * 
     * @return skirt factor.
     */
    public double getSkirtFactor() {
        return skirtFactor;
    }



    /**
     * Sets the skirt factor, a multiplier of the beam diameter 1sigma.
     * 
     * @param skirtFactor
     *            skirt factor
     * @throws IllegalArgumentException
     *             if the factor is less or equal to 0.0
     */
    public void setSkirtFactor(double skirtFactor) {
        if (skirtFactor <= 0.0)
            throw new IllegalArgumentException(
                    "Skirt factor must be greater than 0.0");
        this.skirtFactor = skirtFactor;
    }

}
