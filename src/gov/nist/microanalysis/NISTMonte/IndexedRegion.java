package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Shape;

/**
 * MonteCarloSS region with an added attribute to specify an index.
 * 
 * @author ppinard
 */
public class IndexedRegion extends Region {

    /** Index of region. */
    private final int index;



    public IndexedRegion(Region parent, IMaterialScatterModel msm, Shape shape,
            int index) {
        super(parent, msm, shape);

        if (index < 0)
            throw new IllegalArgumentException("Index must be >= 0");
        this.index = index;
    }



    /**
     * Returns index of this region.
     * 
     * @return index of this region
     */
    public int getIndex() {
        return index;
    }

}
