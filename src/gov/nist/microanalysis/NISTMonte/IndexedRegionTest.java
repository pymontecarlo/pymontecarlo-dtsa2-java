package gov.nist.microanalysis.NISTMonte;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class IndexedRegionTest {
    
    private IndexedRegion region;

    @Before
    public void setUp() throws Exception {
        region = new IndexedRegion(null, null, null, 9);
    }



    @Test
    public void testGetIndex() {
        assertEquals(9, region.getIndex());
    }

}
