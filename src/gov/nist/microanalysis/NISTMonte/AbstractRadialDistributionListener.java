package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.Utility.Math2;

import java.awt.event.ActionListener;

public abstract class AbstractRadialDistributionListener implements
        ActionListener {

    /** Centre of the radial distribution. Distance are evaluated to this point. */
    private final double[] center;

    /** Normal of the plane of the entering region. */
    private final double[] normal;



    /**
     * Creates a new <code>AbstractRadialDistributionListener</code>.
     * 
     * @param center
     *            centre of the radial distribution. Distance are evaluated to
     *            this point. Coordinates in meters.
     * @param normal
     *            normal to the entering region's surface
     */
    public AbstractRadialDistributionListener(double[] center, double[] normal) {
        if (center.length != 3)
            throw new IllegalArgumentException(
                    "The center must be an array of length 3.");
        this.center = center.clone();

        if (normal.length != 3)
            throw new IllegalArgumentException(
                    "The normal must be an array of length 3.");
        this.normal = normal.clone();
    }



    /**
     * Returns the distance between the intersection of the electron with the
     * plane and the centre. If the final position of the electron is below the
     * plane, <code>Double.NaN</code> is returned.
     * 
     * @param pos
     *            position
     * @return distance from the centre
     */
    protected double getRadius(Electron el) {
        double[] pos = el.getPosition();
        double[] prevPos = el.getPrevPosition();

        // From:
        // http://math.stackexchange.com/questions/7931/point-below-a-plane
        boolean above = Math2.dot(normal, Math2.minus(pos, center)) >= 0;
        if (!above)
            return Double.NaN;

        return Math2.magnitude(Math2.cross(normal,
                Math2.minus(prevPos, center)));
    }

}
