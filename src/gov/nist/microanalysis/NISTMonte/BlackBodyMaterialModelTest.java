package gov.nist.microanalysis.NISTMonte;

import static org.junit.Assert.*;
import gov.nist.microanalysis.EPQLibrary.ToSI;

import org.junit.Before;
import org.junit.Test;

public class BlackBodyMaterialModelTest {

    private Electron dummyElectron;

    private IMaterialScatterModel materialModel;



    @Before
    public void setUp() throws Exception {
        dummyElectron =
                new Electron(new double[] { 0.0, 0.0, 1.0 }, ToSI.keV(15.0));
        materialModel = new BlackBodyMaterialModel();
    }



    @Test
    public void testGetMaterial() {
        assertTrue(Double.isInfinite(materialModel.getMaterial().getDensity()));
    }



    @Test
    public void testRandomMeanPathLength() {
        assertEquals(0.0, materialModel.randomMeanPathLength(dummyElectron),
                1e-3);
    }



    @Test
    public void testScatter() {
        assertNull(materialModel.scatter(dummyElectron));
    }



    @Test
    public void testCalculateEnergyLoss() {
        assertTrue(Double.isInfinite(materialModel.calculateEnergyLoss(1.0,
                dummyElectron)));
    }

}
