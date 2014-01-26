package gov.nist.microanalysis.EPQLibrary;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RelativeMassAbsorptionCoefficientTest {

    private MassAbsorptionCoefficient original;
    private MassAbsorptionCoefficient relative;
    
    @Before
    public void setUp() throws Exception {
        original = MassAbsorptionCoefficient.Chantler2005;
        relative = new RelativeMassAbsorptionCoefficient(original, 0.1);
    }



    @Test
    public void testComputeElementDouble() {
        double originalValue = original.compute(Element.Au, ToSI.keV(15.0));
        double relativeValue = relative.compute(Element.Au, ToSI.keV(15.0));
        assertEquals(relativeValue, originalValue * 1.1, 1e-4);
    }



    @Test
    public void testIsAvailableElementDouble() {
        assertTrue(original.isAvailable(Element.Au, ToSI.keV(15.0)));
        assertTrue(relative.isAvailable(Element.Au, ToSI.keV(15.0)));
    }

}
