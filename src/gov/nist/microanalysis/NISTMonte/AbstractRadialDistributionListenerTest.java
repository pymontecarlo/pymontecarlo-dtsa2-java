package gov.nist.microanalysis.NISTMonte;

import static org.junit.Assert.*;

import gov.nist.microanalysis.Utility.Math2;

import java.awt.event.ActionEvent;

import org.junit.Before;
import org.junit.Test;

public class AbstractRadialDistributionListenerTest {

    private class MockRadialDistributionListener extends
            AbstractRadialDistributionListener {

        public MockRadialDistributionListener(double[] center, double[] normal) {
            super(center, normal);
        }



        @Override
        public void actionPerformed(ActionEvent e) {
        }

    }

    private MockRadialDistributionListener listener;



    @Before
    public void setUp() throws Exception {
        double[] center = Math2.ORIGIN_3D;
        double[] normal = Math2.Z_AXIS;
        listener = new MockRadialDistributionListener(center, normal);
    }



    @Test
    public void testGetRadius() {
        double[] pos;
        Electron el;

        pos = new double[] { 1.0, 0.0, 0.0 };
        el = new Electron(pos, 0.0, 0.0, 99.9); // phi & theta not used
        el.move(new double[] { 1.0, 0.0, 1.0 }, 99.9);
        assertEquals(1.0, listener.getRadius(el), 1e-4);

        pos = new double[] { 1.0, 0.0, -1.0 };
        el = new Electron(pos, 0.0, 0.0, 99.9); // phi & theta not used
        el.move(new double[] { 1.0, 0.0, -2.0 }, 99.9);
        assertTrue(Double.isNaN(listener.getRadius(el)));

        pos = new double[] { 0.0, 2.0, 0.0 };
        el = new Electron(pos, 0.0, 0.0, 99.9); // phi & theta not used
        el.move(new double[] { 0.0, 2.0, 1.0 }, 99.9);
        assertEquals(2.0, listener.getRadius(el), 1e-4);
    }

}
