package pymontecarlo.program.nistmonte.input.options;

import gov.nist.microanalysis.EPQLibrary.AbsoluteIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.AlgorithmClass;
import gov.nist.microanalysis.EPQLibrary.BetheElectronEnergyLoss;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.IonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.MassAbsorptionCoefficient;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MeanIonizationPotential;
import gov.nist.microanalysis.EPQLibrary.NISTMottScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.RandomizedScatterFactory;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.NISTMonte.IMaterialScatterModel;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.RegionBase;

import java.util.Map;
import java.util.Set;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import pymontecarlo.program.nistmonte.input.beam.PencilBeam;
import pymontecarlo.program.nistmonte.input.detector.Detector;
import pymontecarlo.program.nistmonte.input.detector.PhotonIntensityDetector;
import pymontecarlo.program.nistmonte.input.limit.Limit;

import static org.junit.Assert.assertEquals;

public class OptionsExtractorTest {

    private OptionsExtractor extractor;



    @Before
    public void setUp() throws Exception {
        Element root = createOptionsElement();

        extractor = new OptionsExtractor();
        extractor.extract(root);
    }



    public static Element createOptionsElement() throws EPQException {
        Element element = new Element("options");
        element.setAttribute("name", "test");

        Element beamElement = new Element("beam");
        beamElement.addContent(BeamExtractorFactoryTest
                .createPencilBeamElement());
        element.addContent(beamElement);

        Element geometryElement = new Element("geometry");
        geometryElement.addContent(GeometryExtractorFactoryTest
                .createSubstrateGeometryElement());
        element.addContent(geometryElement);

        Element detectorsElement = new Element("detectors");
        detectorsElement.addContent(DetectorExtractorFactoryTest
                .createPhotonIntensityDetectorElement("det1"));
        // detectorsElement.addContent(DetectorExtractorFactoryTest
        // .createPhotonSpectrumDetector("det2"));
        // detectorsElement.addContent(DetectorExtractorFactoryTest
        // .createBackscatteredElectronEnergyDetectorElement("det3"));
        element.addContent(detectorsElement);

        Element limitsElement = new Element("limits");
        limitsElement.addContent(LimitExtractorFactoryTest
                .createShowersLimitElement());
        element.addContent(limitsElement);

        Element modelsElement = new Element("models");
        modelsElement.addContent(ModelExtractorFactoryTest
                .createElasticCrossSectionModelElement());
        modelsElement.addContent(ModelExtractorFactoryTest
                .createIonizationCrossSectionModelElement());
        modelsElement.addContent(ModelExtractorFactoryTest
                .createIonizationPotentialModelElement());
        modelsElement.addContent(ModelExtractorFactoryTest
                .createEnergyLossModelElement());
        modelsElement.addContent(ModelExtractorFactoryTest
                .createMassAbsorptionCoefficientModelElement());
        element.addContent(modelsElement);

        return element;
    }



    @Test
    public void testGetName() {
        assertEquals("test", extractor.getName());
    }



    @Test
    public void testGetMonteCarloSS() {
        MonteCarloSS mcss = extractor.getMonteCarloSS();

        // Beam
        PencilBeam beam = (PencilBeam) mcss.getElectronGun();

        assertEquals(1234, FromSI.eV(beam.getBeamEnergy()), 1e-4);
        assertEquals(0.01, beam.getCenter()[0], 1e-4);
        assertEquals(0.02, beam.getCenter()[1], 1e-4);
        assertEquals(0.03, beam.getCenter()[2], 1e-4);
        assertEquals(4, beam.getDirection()[0], 1e-4);
        assertEquals(5, beam.getDirection()[1], 1e-4);
        assertEquals(6, beam.getDirection()[2], 1e-4);

        // Geometry
        Region chamber = mcss.getChamber();

        assertEquals(1, chamber.getSubRegions().size());
        RegionBase region = chamber.getSubRegions().get(0);

        IMaterialScatterModel model = region.getScatterModel();
        assertEquals(1234, FromSI.eV(model.getMinEforTracking()), 1e-4);

        Material mat = region.getMaterial();
        assertEquals("Si3N4", mat.getName());
        assertEquals(3.44, mat.getDensity(), 1e-2);
        assertEquals(2, mat.getElementSet().size());
    }



    @Test
    public void testGetDetectors() {
        Map<String, Detector> detectors = extractor.getDetectors();
        assertEquals(1, detectors.size());

        // Detector 1 (photon intensity detector)
        Detector det = detectors.get("det1");

        PhotonIntensityDetector phDet = (PhotonIntensityDetector) det;
        double[] pos = phDet.getDetectorPosition();
        assertEquals(Math.toRadians(40.0), Math.atan2(pos[2], pos[0]), 1e-4);
        assertEquals(Math.toRadians(90.0), Math.atan2(pos[1], pos[0]), 1e-4);

        // // Detector 2 (photon spectrum)
        // det = detectors.get("det2");
        //
        // ChannelDetector chDet = (ChannelDetector) det;
        // assertEquals(0.0, chDet.getMinimumLimit(), 1e-4);
        // assertEquals(5.0, chDet.getMaximumLimit(), 1e-4);
        // assertEquals(100, chDet.getChannels());
        // assertEquals(0.05, chDet.getChannelWidth(), 1e-4);
        //
        // phDet = (PhotonDetector) det;
        // assertEquals(Math.toRadians(40.0), phDet.getTakeOffAngle(), 1e-4);
        // assertEquals(Math.toRadians(90.0), phDet.getAzimuthAngle(), 1e-4);
        //
        // // Detector 3 (backscattered electron energy)
        // det = detectors.get("det3");
        //
        // chDet = (ChannelDetector) det;
        // assertEquals(0.0, chDet.getMinimumLimit(), 1e-4);
        // assertEquals(5.0, chDet.getMaximumLimit(), 1e-4);
        // assertEquals(100, chDet.getChannels());
        // assertEquals(0.05, chDet.getChannelWidth(), 1e-4);
    }



    @Test
    public void testGetLimits() {
        Set<Limit> limits = extractor.getLimits();
        assertEquals(1, limits.size());
    }



    @Test
    public void testGetStrategy() {
        Strategy strategy = extractor.getStrategy();

        AlgorithmClass alg =
                strategy.getAlgorithm(RandomizedScatterFactory.class);
        assertEquals(NISTMottScatteringAngle.Factory, alg);

        alg = strategy.getAlgorithm(IonizationCrossSection.class);
        assertEquals(AbsoluteIonizationCrossSection.BoteSalvat2008, alg);

        alg = strategy.getAlgorithm(MeanIonizationPotential.class);
        assertEquals(MeanIonizationPotential.Sternheimer64, alg);

        alg = strategy.getAlgorithm(BetheElectronEnergyLoss.class);
        assertEquals(BetheElectronEnergyLoss.JoyLuo1989, alg);

        alg = strategy.getAlgorithm(MassAbsorptionCoefficient.class);
        assertEquals(MassAbsorptionCoefficient.Null, alg);
    }

}
