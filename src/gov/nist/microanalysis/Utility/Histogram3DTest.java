package gov.nist.microanalysis.Utility;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

public class Histogram3DTest {

    private Histogram3D hist;



    @Before
    public void setUp() throws Exception {
        hist = new Histogram3D(0, 5, 5, 0, 6, 6, 0, 7, 7);

        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 6; j++)
                for (int k = 0; k < 7; k++)
                    hist.add(i, j, k);
    }



    @Test
    public void testClear() {
        hist.clear();
        assertEquals(0, hist.totalCounts());
    }



    @Test
    public void testDump() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        hist.dump(out);
        out.close();
    }



    @Test
    public void testClone() {
        Histogram3D other = hist.clone();
        other.add(1.0, 1.0, 1.0);
        assertEquals(1, hist.counts(1, 1, 1));
        assertEquals(2, other.counts(1, 1, 1));
    }



    @Test
    public void testAdd() {
        hist.add(1.0, 1.0, 1.0);
        assertEquals(2, hist.counts(1, 1, 1));
    }



    @Test
    public void testCounts() {
        assertEquals(1, hist.counts(1, 1, 0));
        assertEquals(1, hist.counts(1, 1, 1));
    }



    @Test
    public void testTotalSum() {
        assertEquals(210, hist.totalCounts());
    }



    @Test
    public void testGetArray() {
        // double[][][] arr = hist.getArray();
        // for (int k = 0; k < arr.length; k++) {
        // System.out.println("z = " + arr[k][0][0]);
        //
        // for (int j = 0; j < arr[0][0].length; j++) {
        // for (int i = 0; i < arr[0].length; i++) {
        // System.out.print(arr[k][i][j] + ", ");
        // }
        // System.out.println();
        // }
        // }
    }

}
