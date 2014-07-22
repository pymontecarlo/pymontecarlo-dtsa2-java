package pymontecarlo.fileformat.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.nist.microanalysis.EPQLibrary.AbsoluteIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.AlgorithmClass;
import gov.nist.microanalysis.EPQLibrary.BetheElectronEnergyLoss;
import gov.nist.microanalysis.EPQLibrary.IonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.MassAbsorptionCoefficient;
import gov.nist.microanalysis.EPQLibrary.MeanIonizationPotential;
import gov.nist.microanalysis.EPQLibrary.NISTMottScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.RandomizedScatterFactory;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.EPQLibrary.ToSI;

import java.io.IOException;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import pymontecarlo.fileformat.ExtractorManager;
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
        Strategy strategy = ModelExtractorFactory.REGISTERED.extract(element);

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
        Strategy strategy = ModelExtractorFactory.REGISTERED.extract(element);

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
        Strategy strategy = ModelExtractorFactory.REGISTERED.extract(element);

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
        Strategy strategy = ModelExtractorFactory.REGISTERED.extract(element);

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
        Strategy strategy = ModelExtractorFactory.REGISTERED.extract(element);

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
        Strategy strategy = ModelExtractorFactory.REGISTERED.extract(element);

        // Test
        AlgorithmClass alg =
                strategy.getAlgorithm(FluorescenceMC.class);
        assertEquals(FluorescenceMC.FluorescenceCompton, alg);
    }



    public static Element createUserDefinedMassAbsorptionCoefficientModelElement() {
        Element element =
                new Element("userDefinedMassAbsorptionCoefficientModel");

        element.setAttribute("type", "mass absorption coefficient");
        element.setAttribute("name", "user defined mass absorption coefficient");

        Element subelement = new Element("model");
        subelement.setAttribute("type", "mass absorption coefficient");
        subelement.setAttribute("name", "Henke 1993");
        element.addContent(subelement);

        subelement = new Element("mac");
        subelement.setAttribute("absorber", "29");
        subelement.setAttribute("energy", "8904.0");
        subelement.setText("200");
        element.addContent(subelement);

        return element;
    }



    @Test
    public void testUserDefinedMassAbsorptionCoefficientModel()
            throws IOException {
        // XML element
        Element element =
                createUserDefinedMassAbsorptionCoefficientModelElement();

        // Extrator
        ExtractorManager.register("model", ModelExtractorFactory.REGISTERED);
        Strategy strategy =
                ModelExtractorFactory.USER_DEFINED_MASS_ABSORPTION_COEFFICIENT
                        .extract(element);

        // Test
        MassAbsorptionCoefficient mac =
                (MassAbsorptionCoefficient) strategy
                        .getAlgorithm(MassAbsorptionCoefficient.class);
        assertTrue(mac instanceof MassAbsorptionCoefficient.UserSpecifiedCoefficient);
        assertEquals(200.0,
                mac.compute(gov.nist.microanalysis.EPQLibrary.Element.Cu,
                        ToSI.eV(8904.0)), 1e-4);
    }
}
