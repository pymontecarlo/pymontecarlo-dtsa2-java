package gov.nist.microanalysis.NISTMonte;

import static org.junit.Assert.*;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Shape;
import gov.nist.microanalysis.Utility.UncertainValue2;

import org.junit.Before;
import org.junit.Test;

public class TimeListenerTest {
    
    private MonteCarloSS mcss;
    
    private TimeListener listener; 
    
    @Before
    public void setUp() throws Exception {
        // Setup
        mcss = new MonteCarloSS();
        mcss.setBeamEnergy(ToSI.keV(20.0));
        mcss.setElectronGun(new GaussianBeam(10e-9));

        Material mat = MaterialFactory.createPureElement(Element.Si);
        Shape shape =
                MultiPlaneShape.createSubstrate(new double[] { 0, 0, -1 },
                        new double[] { 0, 0, 0 });
        mcss.addSubRegion(mcss.getChamber(), mat, shape);

        listener = new TimeListener(mcss);
        mcss.addActionListener(listener);
        
        // Run
        mcss.runMultipleTrajectories(5);
    }



    @Test
    public void testGetSimulationTime() {
        double simTime = listener.getSimulationTime();
        assertTrue(simTime > 0);
    }



    @Test
    public void testGetMeanTrajectoryTime() {
        UncertainValue2 trajTime = listener.getMeanTrajectoryTime();
        assertTrue(trajTime.doubleValue() > 0);
    }

}
