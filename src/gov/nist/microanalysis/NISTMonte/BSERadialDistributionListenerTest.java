package gov.nist.microanalysis.NISTMonte;

import static org.junit.Assert.*;
import gov.nist.microanalysis.Utility.Histogram;
import gov.nist.microanalysis.Utility.Math2;

import org.junit.Test;

public class BSERadialDistributionListenerTest {

    @Test
    public void testRadialDistributionListenerEqualRadii() {
        BSERadialDistributionListener listener;
        Histogram h;

        listener = new BSERadialDistributionListener(Math2.ORIGIN_3D,
                Math2.Z_AXIS, 100.0, 10, false);
        h = listener.getDistribution();
        assertEquals(10, h.binCount());
        assertEquals(0.0, h.minValue(0), 1e-4);
        assertEquals(10.0, h.maxValue(0), 1e-4);
        assertEquals(10.0, h.minValue(1), 1e-4);
        assertEquals(20.0, h.maxValue(1), 1e-4);

        listener =
                new BSERadialDistributionListener(Math2.ORIGIN_3D,
                        Math2.Z_AXIS, 100.0, 10, true);
        h = listener.getDistribution();
        assertEquals(10, h.binCount());
        assertEquals(0.0, h.minValue(0), 1e-4);
        assertEquals(31.62278, h.maxValue(0), 1e-4);
        assertEquals(31.62278, h.minValue(1), 1e-4);
        assertEquals(44.72136, h.maxValue(1), 1e-4);
        assertEquals(Math.pow(h.maxValue(0), 2) - Math.pow(h.minValue(0), 2),
                Math.pow(h.maxValue(1), 2) - Math.pow(h.minValue(1), 2), 1e-4);
    }

}
