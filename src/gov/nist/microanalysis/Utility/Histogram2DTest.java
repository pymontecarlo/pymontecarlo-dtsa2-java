package gov.nist.microanalysis.Utility;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

public class Histogram2DTest {

    private Histogram2D h;



    @Before
    public void setUp() throws Exception {
        h = new Histogram2D(0.0, 5.0, 5, 100.0, 150.0, 10);
    }



    @Test
    public void testHistogram2DDoubleDoubleIntDoubleDoubleInt() {
        assertEquals(5, h.xBinCount());
        assertEquals(0.0, h.xMinValue(0), 1e-4);
        assertEquals(0.5, h.xMidValue(0), 1e-4);
        assertEquals(1.0, h.xMaxValue(0), 1e-4);
        assertEquals(1.0, h.xMinValue(1), 1e-4);
        assertEquals(1.5, h.xMidValue(1), 1e-4);
        assertEquals(2.0, h.xMaxValue(1), 1e-4);

        assertEquals(10, h.yBinCount());
        assertEquals(100.0, h.yMinValue(0), 1e-4);
        assertEquals(102.5, h.yMidValue(0), 1e-4);
        assertEquals(105.0, h.yMaxValue(0), 1e-4);
        assertEquals(105.0, h.yMinValue(1), 1e-4);
        assertEquals(107.5, h.yMidValue(1), 1e-4);
        assertEquals(110.0, h.yMaxValue(1), 1e-4);
    }



    @Test
    public void testHistogram2DDoubleArrayDoubleDoubleArrayDouble() {
        double[] xBinMins = new double[] { 0.0, 1.0, 2.0, 3.0, 4.0 };
        double[] yBinMins =
                new double[] { 100.0, 105.0, 110.0, 115.0, 120.0, 125.0, 130.0, 135.0,
                        140.0, 145.0 };
        h = new Histogram2D(xBinMins, 5.0, yBinMins, 150.0);
        
        assertEquals(5, h.xBinCount());
        assertEquals(0.0, h.xMinValue(0), 1e-4);
        assertEquals(0.5, h.xMidValue(0), 1e-4);
        assertEquals(1.0, h.xMaxValue(0), 1e-4);
        assertEquals(1.0, h.xMinValue(1), 1e-4);
        assertEquals(1.5, h.xMidValue(1), 1e-4);
        assertEquals(2.0, h.xMaxValue(1), 1e-4);

        assertEquals(10, h.yBinCount());
        assertEquals(100.0, h.yMinValue(0), 1e-4);
        assertEquals(102.5, h.yMidValue(0), 1e-4);
        assertEquals(105.0, h.yMaxValue(0), 1e-4);
        assertEquals(105.0, h.yMinValue(1), 1e-4);
        assertEquals(107.5, h.yMidValue(1), 1e-4);
        assertEquals(110.0, h.yMaxValue(1), 1e-4);
    }



    @Test
    public void testClear() {
        h.add(1.0, 125.0);
        assertEquals(1, h.counts(1, 5));
        
        h.clear();
        assertEquals(0, h.counts(1, 5));
    }



    @Test
    public void testClone() {
        h.add(1.0, 125.0);
        
        Histogram2D other = h.clone();
        assertEquals(1, other.counts(1, 5));
        
        other.add(1.0, 125.0);
        assertEquals(2, other.counts(1, 5));
        assertEquals(1, h.counts(1, 5));
    }



    @Test
    public void testXBin() {
        assertEquals(0, h.xBin(0.0));
        assertEquals(1, h.xBin(1.0));
        assertEquals(-1, h.xBin(-1.0));
        assertEquals(5, h.xBin(5.0));
        assertEquals(5, h.xBin(6.0));
    }



    @Test
    public void testYBin() {
        assertEquals(0, h.yBin(100.0));
        assertEquals(1, h.yBin(105.0));
        assertEquals(-1, h.yBin(95.0));
        assertEquals(10, h.yBin(150.0));
        assertEquals(10, h.yBin(160.0));
    }



    @Test
    public void testXMinValue() {
        assertEquals(2.0, h.xMinValue(2), 1e-4);
    }



    @Test
    public void testYMinValue() {
        assertEquals(110.0, h.yMinValue(2), 1e-4);
    }



    @Test
    public void testXMaxValue() {
        assertEquals(3.0, h.xMaxValue(2), 1e-4);
    }



    @Test
    public void testYMaxValue() {
        assertEquals(115.0, h.yMaxValue(2), 1e-4);
    }



    @Test
    public void testXMidValue() {
        assertEquals(2.5, h.xMidValue(2), 1e-4);
    }



    @Test
    public void testYMidValue() {
        assertEquals(112.5, h.yMidValue(2), 1e-4);
    }



    @Test
    public void testAdd() {
        h.add(1.0, 125.0);
        assertEquals(1, h.counts(1, 5));
    }



    @Test
    public void testXBinCount() {
        assertEquals(5, h.xBinCount());
    }



    @Test
    public void testYBinCount() {
        assertEquals(10, h.yBinCount());
    }



    @Test
    public void testCounts() {
        h.add(1.0, 125.0);
        assertEquals(1, h.counts(1, 5));
    }



    @Test
    public void testTotalCounts() {
        assertEquals(0, h.totalCounts());
        
        h.add(1.0, 125.0);
        assertEquals(1, h.totalCounts());
    }



    @Test
    public void testGetXProjection() {
        for (int i = -1; i <= 5; i++) {
            h.add(i, 125.0);
            h.add(i, 145.0);
        }
        
        Histogram x = h.getXProjection();
        
        assertEquals(0.0, x.minValue(0), 1e-4);
        assertEquals(1.0, x.maxValue(0), 1e-4);
        assertEquals(1.0, x.minValue(1), 1e-4);
        assertEquals(2.0, x.maxValue(1), 1e-4);
        
        for (int i = -1; i <= 5; i++)
            assertEquals(2, x.counts(i));
    }



    @Test
    public void testGetYProjection() {
        for (int i = -1; i <= 10; i++) {
            h.add(2.0, 100.0 + i * 5);
            h.add(2.0, 100.0 + i * 5);
        }
        
        Histogram y = h.getYProjection();
        
        assertEquals(100.0, y.minValue(0), 1e-4);
        assertEquals(105.0, y.maxValue(0), 1e-4);
        assertEquals(105.0, y.minValue(1), 1e-4);
        assertEquals(110.0, y.maxValue(1), 1e-4);
        
        for (int i = -1; i <= 10; i++)
            assertEquals(2, y.counts(i));
    }
    
    @Test
    public void testDump() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        h.dump(out);
        out.close();
    }

}
