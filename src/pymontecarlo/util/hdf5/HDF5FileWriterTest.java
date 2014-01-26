package pymontecarlo.util.hdf5;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import pymontecarlo.util.TestCase;

public class HDF5FileWriterTest extends TestCase {

    HDF5Group root;

    File path;



    @Before
    public void setUp() throws Exception {
        path = createTempFile("h5");
        root = HDF5Group.createRoot();

        HDF5Group g1 = root.createSubgroup("group1");
        HDF5Group subg1 = g1.createSubgroup("subgroup1");
        HDF5Group subg2 = g1.createSubgroup("subgroup2");

        subg1.setAttribute("a1", "abc");
        subg1.setAttribute("a2", "def");

        int[][] data = new int[][] { { 1, 2, 3 }, { 4, 5, 6 } };
        HDF5Dataset d1 = subg2.createDataset("b1", data);
        d1.setAttribute("b1a", 5, 6, 9);
        d1.setAttribute("b1b", 1);

        double[][] data2 =
                new double[][] { { 1.1, 2.2, 3.3 }, { 4.4, 5.5, 6.6 } };
        HDF5Dataset d2 = subg2.createDataset("b2", data2);
        d2.setAttribute("b2a", 5.5, 6.6, 9.9);
        d2.setAttribute("b2b", 1.1);

        double[][][] data3 =
                new double[][][] { { { 1, 2 }, { 3, 4 } },
                        { { 5, 6 }, { 7, 8 } }, { { 9, 10 }, { 11, 12 } } };
        subg2.createDataset("b3", data3);

        String[][] data4 =
                new String[][] { { "abc", "def" }, { "hij", "klm" } };
        subg2.createDataset("b4", data4);
    }



    @Test
    public void testWrite() throws IOException {
        HDF5FileWriter.write(root, path, true);
    }

}
