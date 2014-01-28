package pymontecarlo.program.nistmonte.options.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.NISTMonte.BasicMaterialModel;
import gov.nist.microanalysis.NISTMonte.IMaterialScatterModel;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.RegionBase;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Shape;

import java.io.IOException;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

public class GeometryExtractorFactoryTest {

    private Region chamber;



    protected static Element createMaterialsElement() throws EPQException {
        Element element = new Element("materials");

        Material mat = MaterialFactory.createCompound("Si3N4", 3.44);
        IMaterialScatterModel model = new BasicMaterialModel(mat);
        model.setMinEforTracking(ToSI.eV(1234));
        element.addContent(createMaterialElement(model, 1));

        mat = MaterialFactory.createCompound("Al2O3", 4.53);
        model = new BasicMaterialModel(mat);
        model.setMinEforTracking(ToSI.eV(4321));
        element.addContent(createMaterialElement(model, 2));

        mat =
                MaterialFactory
                        .createPureElement(gov.nist.microanalysis.EPQLibrary.Element.Au);
        model = new BasicMaterialModel(mat);
        model.setMinEforTracking(ToSI.eV(50));
        element.addContent(createMaterialElement(model, 3));

        return element;
    }



    private static Element createMaterialElement(IMaterialScatterModel model,
            int index) {
        Material mat = model.getMaterial();

        Element element = new Element("material");
        element.setAttribute("_index", Integer.toString(index));
        element.setAttribute("name", mat.getName());
        element.setAttribute("density", Double.toString(mat.getDensity()));

        Element compositionElement = new Element("composition");
        Element elementElement;
        for (gov.nist.microanalysis.EPQLibrary.Element el : mat.getElementSet()) {
            elementElement = new Element("element");

            elementElement.setAttribute("z",
                    Integer.toString(el.getAtomicNumber()));
            elementElement.setAttribute("weightFraction",
                    Double.toString(mat.weightFraction(el, false)));

            compositionElement.addContent(elementElement);
        }
        element.addContent(compositionElement);

        Element absorptionEnergyElement = new Element("absorptionEnergy");
        absorptionEnergyElement.setAttribute("particle", "electron");
        absorptionEnergyElement.setText(Double.toString(FromSI.eV(model
                .getMinEforTracking())));
        element.addContent(absorptionEnergyElement);

        return element;
    }



    @Before
    public void setUp() throws Exception {
        chamber = new Region(null, null, null);
    }



    public static Element createSubstrateGeometryElement() throws EPQException {
        Element element =
                new Element("substrate");
        element.setAttribute("rotation", "0.0");
        element.setAttribute("tilt", Double.toString(Math.toRadians(30.0)));

        element.addContent(createMaterialsElement());

        Element bodyElement = new Element("body");
        bodyElement.setAttribute("material", "1");
        element.addContent(bodyElement);

        return element;
    }



    @Test
    public void testSUBSTRATE() throws IOException, EPQException {
        // Setup
        Element element = createSubstrateGeometryElement();

        // Extract
        GeometryExtractor extractor = GeometryExtractorFactory.SUBSTRATE;
        extractor.extract(element, chamber);

        // Test
        assertEquals(1, chamber.getSubRegions().size());
        RegionBase region = chamber.getSubRegions().get(0);

        IMaterialScatterModel model = region.getScatterModel();
        assertEquals(1234, FromSI.eV(model.getMinEforTracking()), 1e-4);

        Material mat = region.getMaterial();
        assertEquals("Si3N4", mat.getName());
        assertEquals(3.44, mat.getDensity(), 1e-2);
        assertEquals(2, mat.getElementSet().size());
    }



    public static Element createInclusionGeometryElement() throws EPQException {
        Element element =
                new Element("inclusion");
        element.setAttribute("rotation", "0.0");
        element.setAttribute("tilt", Double.toString(Math.toRadians(30.0)));

        element.addContent(createMaterialsElement());

        Element substrateElement = new Element("substrate");
        substrateElement.setAttribute("material", "1");
        element.addContent(substrateElement);

        Element inclusionElement = new Element("inclusion");
        inclusionElement.setAttribute("material", "2");
        inclusionElement.setAttribute("diameter", "1e-6");
        element.addContent(inclusionElement);

        return element;
    }



    @Test
    public void testINCLUSION() throws IOException, EPQException {
        // Setup
        Element element = createInclusionGeometryElement();

        // Extract
        GeometryExtractor extractor = GeometryExtractorFactory.INCLUSION;
        extractor.extract(element, chamber);

        // Test
        assertEquals(2, chamber.getSubRegions().size());

        RegionBase region = chamber.getSubRegions().get(0);

        IMaterialScatterModel model = region.getScatterModel();
        assertEquals(1234, FromSI.eV(model.getMinEforTracking()), 1e-4);

        Material mat = region.getMaterial();
        assertEquals("Si3N4", mat.getName());
        assertEquals(3.44, mat.getDensity(), 1e-2);
        assertEquals(2, mat.getElementSet().size());

        region = chamber.getSubRegions().get(1);

        model = region.getScatterModel();
        assertEquals(4321, FromSI.eV(model.getMinEforTracking()), 1e-4);

        mat = region.getMaterial();
        assertEquals("Al2O3", mat.getName());
        assertEquals(4.53, mat.getDensity(), 1e-2);
        assertEquals(2, mat.getElementSet().size());
    }



    public static Element createHorizontalLayersGeometryElement()
            throws EPQException {
        Element element =
                new Element("horizontalLayers");
        element.setAttribute("tilt", Double.toString(Math.toRadians(30.0)));
        element.setAttribute("rotation", Double.toString(Math.toRadians(180)));

        element.addContent(createMaterialsElement());

        Element substrateElement = new Element("substrate");
        substrateElement.setAttribute("material", "3");
        element.addContent(substrateElement);

        Element layersElement = new Element("layers");

        Element layerElement = new Element("layer");
        layerElement.setAttribute("material", "1");
        layerElement.setAttribute("thickness", "50e-9");
        layersElement.addContent(layerElement);

        layerElement = new Element("layer");
        layerElement.setAttribute("material", "2");
        layerElement.setAttribute("thickness", "150e-9");
        layersElement.addContent(layerElement);

        element.addContent(layersElement);

        return element;
    }



    @Test
    public void testMULTI_LAYERS() throws IOException, EPQException {
        // Setup
        Element element = createHorizontalLayersGeometryElement();

        // Extract
        GeometryExtractor extractor =
                GeometryExtractorFactory.HORIZONTAL_LAYERS;
        extractor.extract(element, chamber);

        // Tests
        assertEquals(3, chamber.getSubRegions().size());

        // Test layer 1
        RegionBase region = chamber.getSubRegions().get(0);

        IMaterialScatterModel model = region.getScatterModel();
        assertEquals(1234, FromSI.eV(model.getMinEforTracking()), 1e-4);

        Material mat = region.getMaterial();
        assertEquals("Si3N4", mat.getName());
        assertEquals(3.44, mat.getDensity(), 1e-2);
        assertEquals(2, mat.getElementSet().size());

        Shape shape = region.getShape();
        assertFalse(shape.contains(new double[] { 0.0, 0.0, 0.01 })); // above
        assertTrue(shape.contains(new double[] { 0.0, 0.0, -25e-9 })); // in
        assertFalse(shape.contains(new double[] { 0.0, 0.0, -0.01 })); // below

        // Test layer 2
        region = chamber.getSubRegions().get(1);

        model = region.getScatterModel();
        assertEquals(4321, FromSI.eV(model.getMinEforTracking()), 1e-4);

        mat = region.getMaterial();
        assertEquals("Al2O3", mat.getName());
        assertEquals(4.53, mat.getDensity(), 1e-2);
        assertEquals(2, mat.getElementSet().size());

        shape = region.getShape();
        assertFalse(shape.contains(new double[] { 0.0, 0.0, -25e-9 })); // above
        assertTrue(shape.contains(new double[] { 0.0, 0.0, -125e-9 })); // in
        assertFalse(shape.contains(new double[] { 0.0, 0.0, -0.01 })); // below

        // Test substrate
        region = chamber.getSubRegions().get(2);

        model = region.getScatterModel();
        assertEquals(50.0, FromSI.eV(model.getMinEforTracking()), 1e-4);

        mat = region.getMaterial();
        assertEquals("Pure gold", mat.getName());
        assertEquals(19300, mat.getDensity(), 1e-2);
        assertEquals(1, mat.getElementSet().size());
    }



    public static Element createVerticalLayersGeometryElement()
            throws EPQException {
        Element element =
                new Element("verticalLayers");
        element.setAttribute("rotation", "0.0");
        element.setAttribute("tilt", Double.toString(Math.toRadians(30.0)));

        element.addContent(createMaterialsElement());
        
        Element leftSubstrateElement = new Element("leftSubstrate");
        leftSubstrateElement.setAttribute("material", "1");
        leftSubstrateElement.setAttribute("depth", "-INF");
        element.addContent(leftSubstrateElement);

        Element layersElement = new Element("layers");
        Element layerElement = new Element("layer");
        layerElement.setAttribute("material", "2");
        layerElement.setAttribute("thickness", "150e-9");
        layerElement.setAttribute("depth", "-INF");
        layersElement.addContent(layerElement);
        element.addContent(layersElement);
        
        Element rightSubstrateElement = new Element("rightSubstrate");
        rightSubstrateElement.setAttribute("material", "3");
        rightSubstrateElement.setAttribute("depth", "-INF");
        element.addContent(rightSubstrateElement);

        return element;
    }



    @Test
    public void testVERTICAL_LAYERS() throws IOException, EPQException {
        // Setup
        Element element = createVerticalLayersGeometryElement();

        // Extract
        GeometryExtractor extractor = GeometryExtractorFactory.VERTICAL_LAYERS;
        extractor.extract(element, chamber);

        // Test
        assertEquals(3, chamber.getSubRegions().size());

        // Test left substrate
        RegionBase region = chamber.getSubRegions().get(0);

        IMaterialScatterModel model = region.getScatterModel();
        assertEquals(1234, FromSI.eV(model.getMinEforTracking()), 1e-4);

        Material mat = region.getMaterial();
        assertEquals("Si3N4", mat.getName());
        assertEquals(3.44, mat.getDensity(), 1e-2);
        assertEquals(2, mat.getElementSet().size());

        Shape shape = region.getShape();
        assertFalse(shape.contains(new double[] { 0.0, 0.0, 0.01 })); // above
        assertTrue(shape.contains(new double[] { -80e-9, 0.0, -50e-8 })); // in
        assertFalse(shape.contains(new double[] { 0.0, 0.0, -50e-8 })); // right

        // Test layer
        region = chamber.getSubRegions().get(1);

        model = region.getScatterModel();
        assertEquals(4321, FromSI.eV(model.getMinEforTracking()), 1e-4);

        mat = region.getMaterial();
        assertEquals("Al2O3", mat.getName());
        assertEquals(4.53, mat.getDensity(), 1e-2);
        assertEquals(2, mat.getElementSet().size());

        shape = region.getShape();
        assertFalse(shape.contains(new double[] { 0.0, 0.0, 0.01 })); // above
        assertFalse(shape.contains(new double[] { -80e-9, 0.0, -50e-8 })); // left
        assertTrue(shape.contains(new double[] { 0.0, 0.0, -50e-8 })); // in
        assertFalse(shape.contains(new double[] { 80e-9, 0.0, -50e-8 })); // right

        // Test right substrate
        region = chamber.getSubRegions().get(2);

        model = region.getScatterModel();
        assertEquals(50.0, FromSI.eV(model.getMinEforTracking()), 1e-4);

        mat = region.getMaterial();
        assertEquals("Pure gold", mat.getName());
        assertEquals(19300, mat.getDensity(), 1e-2);
        assertEquals(1, mat.getElementSet().size());

        shape = region.getShape();
        assertFalse(shape.contains(new double[] { 0.0, 0.0, 0.01 })); // above
        assertTrue(shape.contains(new double[] { 80e-9, 0.0, -50e-8 })); // in
        assertFalse(shape.contains(new double[] { 0.0, 0.0, -50e-8 })); // left
    }

}
