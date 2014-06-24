package pymontecarlo.program._analytical.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

/**
 * Factory of geometry extractors.
 * 
 * @author ppinard
 */
public class GeometryExtractorFactory {

    protected abstract static class AbstractGeometryExtractor implements
            GeometryExtractor {

        /**
         * Extracts the composition from a material XML element.
         * 
         * @param materialElement
         *            material XML element
         * @return composition
         */
        protected Composition extractComposition(Element materialElement)
                throws IOException {
            Element compositionElement =
                    materialElement.getChild("composition");
            List<?> children = compositionElement.getChildren();

            gov.nist.microanalysis.EPQLibrary.Element[] elements =
                    new gov.nist.microanalysis.EPQLibrary.Element[children
                            .size()];
            double[] weightFractions = new double[children.size()];

            Element elementElement;
            int z;
            double weightFraction;
            for (int i = 0; i < children.size(); i++) {
                elementElement = (Element) children.get(i);

                try {
                    z = elementElement.getAttribute("z").getIntValue();
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }
                elements[i] =
                        gov.nist.microanalysis.EPQLibrary.Element
                                .byAtomicNumber(z);

                try {
                    weightFraction =
                            elementElement.getAttribute("weightFraction")
                                    .getDoubleValue();
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }
                weightFractions[i] = weightFraction;
            }

            return new Composition(elements, weightFractions);
        }



        /**
         * Extracts the materials from a geometry XML element and returns a map
         * where the keys are the index of each material and the values are the
         * material scatter model of each material. The material scatter model
         * interface is used as the absorption energy of electron is
         * automatically set from the one specified in the material XML element.
         * 
         * @param geometryImplElement
         *            geometry XML element
         * @return map of material index and material scatter model
         * @throws EPQException
         *             if an exception occurs while creating the material
         *             scatter model
         */
        protected Map<Integer, Material> extractMaterials(
                Element geometryElement) throws IOException, EPQException {
            Map<Integer, Material> materials = new HashMap<>();
            Element materialsElement = geometryElement.getChild("materials");

            int index;
            String name;
            double density;
            Composition composition;
            Material material;
            for (Element materialElement : materialsElement.getChildren()) {
                try {
                    index =
                            materialElement.getAttribute("_index")
                                    .getIntValue();
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }

                name = materialElement.getAttribute("name").getValue();

                try {
                    density =
                            materialElement.getAttribute("density")
                                    .getDoubleValue();
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }

                composition = extractComposition(materialElement);
                composition.setName(name);

                material = new Material(composition, density);
                material.setName(name);

                materials.put(index, material);
            }

            return materials;
        }

    }

    /** Substrate extractor. */
    protected static class SubstrateGeometryExtractor extends
            AbstractGeometryExtractor {

        @Override
        public SpectrumProperties extract(Element geometryElement)
                throws IOException, EPQException {
            Map<Integer, Material> materials =
                    extractMaterials(geometryElement);

            // Get material
            Element bodyElement = geometryElement.getChild("body");

            int materialIndex;
            try {
                materialIndex =
                        bodyElement.getAttribute("material").getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            Material material = materials.get(materialIndex);

            SpectrumProperties props = new SpectrumProperties();
            props.setCompositionProperty(
                    SpectrumProperties.MicroanalyticalComposition, material);
            props.setNumericProperty(SpectrumProperties.SpecimenDensity,
                    material.getDensity());

            return props;
        }
    }

    public static final GeometryExtractor SUBSTRATE =
            new SubstrateGeometryExtractor();

}
