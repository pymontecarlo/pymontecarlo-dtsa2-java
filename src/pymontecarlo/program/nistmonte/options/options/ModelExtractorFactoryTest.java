package pymontecarlo.program.nistmonte.options.options;

import static org.junit.Assert.assertEquals;
import gov.nist.microanalysis.EPQLibrary.AbsoluteIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.AlgorithmClass;
import gov.nist.microanalysis.EPQLibrary.BetheElectronEnergyLoss;
import gov.nist.microanalysis.EPQLibrary.IonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.MassAbsorptionCoefficient;
import gov.nist.microanalysis.EPQLibrary.MeanIonizationPotential;
import gov.nist.microanalysis.EPQLibrary.NISTMottScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.RandomizedScatterFactory;
import gov.nist.microanalysis.EPQLibrary.Strategy;

import java.io.IOException;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import pymontecarlo.program.nistmonte.options.model.FluorescenceMC;

public class ModelExtractorFactoryTest {




    @Before
    public void setUp() throws Exception {
    }



    public static Element createElasticCrossSectionModelElement() {
        Element element = new Element("model");

        element.setAttribute("type", "elastic cross section");
        element.setAttribute("name", "ELSEPA");

        return element;
    }



    @Test
    public void testElasticCrossSection() throws IOException {
        // XML element
        Element element = createElasticCrossSectionModelElement();

        // Extrator
        Strategy strategy = ModelExtractorFactory.ALL.extract(element);

        // Test
        AlgorithmClass alg =
                strategy.getAlgorithm(RandomizedScatterFactory.class);
        assertEquals(NISTMottScatteringAngle.Factory, alg);
    }



    public static Element createIonizationCrossSectionModelElement() {
        Element element = new Element("model");

        element.setAttribute("type", "ionization cross section");
        element.setAttribute("name", "Bote and Salvat 2008");

        return element;
    }



    @Test
    public void testIonizationCrossSection() throws IOException {
        // XML element
        Element element = createIonizationCrossSectionModelElement();

        // Extrator
        Strategy strategy = ModelExtractorFactory.ALL.extract(element);

        // Test
        AlgorithmClass alg =
                strategy.getAlgorithm(IonizationCrossSection.class);
        assertEquals(AbsoluteIonizationCrossSection.BoteSalvat2008, alg);
    }



    public static Element createIonizationPotentialModelElement() {
        Element element = new Element("model");

        element.setAttribute("type", "ionization potential");
        element.setAttribute("name", "Sternheimer 1964");

        return element;
    }



    @Test
    public void testIonizationPotential() throws IOException {
        // XML element
        Element element = createIonizationPotentialModelElement();

        // Extrator
        Strategy strategy = ModelExtractorFactory.ALL.extract(element);

        // Test
        AlgorithmClass alg =
                strategy.getAlgorithm(MeanIonizationPotential.class);
        assertEquals(MeanIonizationPotential.Sternheimer64, alg);
    }



    public static Element createEnergyLossModelElement() {
        Element element = new Element("model");

        element.setAttribute("type", "energy loss");
        element.setAttribute("name", "Joy and Luo 1989");

        return element;
    }



    @Test
    public void testEnergyLoss() throws IOException {
        // XML element
        Element element = createEnergyLossModelElement();

        // Extrator
        Strategy strategy = ModelExtractorFactory.ALL.extract(element);

        // Test
        AlgorithmClass alg =
                strategy.getAlgorithm(BetheElectronEnergyLoss.class);
        assertEquals(BetheElectronEnergyLoss.JoyLuo1989, alg);
    }



    public static Element createMassAbsorptionCoefficientModelElement() {
        Element element = new Element("model");

        element.setAttribute("type", "mass absorption coefficient");
        element.setAttribute("name", "No MAC");

        return element;
    }



    @Test
    public void testMassAbsorptionCoefficient() throws IOException {
        // XML element
        Element element = createMassAbsorptionCoefficientModelElement();

        // Extrator
        Strategy strategy = ModelExtractorFactory.ALL.extract(element);

        // Test
        AlgorithmClass alg =
                strategy.getAlgorithm(MassAbsorptionCoefficient.class);
        assertEquals(MassAbsorptionCoefficient.Null, alg);
    }
    
    public static Element createFluorescenceModelElement() {
        Element element = new Element("model");

        element.setAttribute("type", "fluorescence");
        element.setAttribute("name", "fluorescence with Compton");

        return element;
    }



    @Test
    public void testFluorescence() throws IOException {
        // XML element
        Element element = createFluorescenceModelElement();

        // Extrator
        Strategy strategy = ModelExtractorFactory.ALL.extract(element);

        // Test
        AlgorithmClass alg =
                strategy.getAlgorithm(FluorescenceMC.class);
        assertEquals(FluorescenceMC.FluorescenceCompton, alg);
    }
}
