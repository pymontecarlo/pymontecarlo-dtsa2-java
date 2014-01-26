package gov.nist.microanalysis.NISTMonte;

import static org.junit.Assert.*;
import gov.nist.microanalysis.Utility.Histogram2D;
import gov.nist.microanalysis.Utility.Math2;

import org.junit.Test;

public class BSERadialEnergyDistributionListenerTest {




    @Test
    public void testBSERadialEnergyDistributionListener() {
        BSERadialEnergyDistributionListener listener;
        Histogram2D h;

        listener = new BSERadialEnergyDistributionListener(Math2.ORIGIN_3D,
                Math2.Z_AXIS, 0.0, 50.0, 5, 100.0, 10, false);
        h = listener.getDistribution();
        assertEquals(10, h.xBinCount());
        assertEquals(5, h.yBinCount());
    }

}
