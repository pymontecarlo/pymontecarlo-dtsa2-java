package pymontecarlo.program.nistmonte.options.options;

import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.NISTMonte.BasicMaterialModel;
import gov.nist.microanalysis.NISTMonte.IMaterialScatterModel;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.RegionBase;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Shape;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.TransformableRegion;
import gov.nist.microanalysis.NISTMonte.IndexedRegion;
import gov.nist.microanalysis.NISTMonte.MultiPlaneShape;
import gov.nist.microanalysis.NISTMonte.ShapeDifference;
import gov.nist.microanalysis.NISTMonte.Sphere;
import gov.nist.microanalysis.Utility.Math2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

/**
 * Factory of geometry extractors.
 * 
 * @author ppinard
 */
public class GeometryExtractorFactory {

    /**
     * Stores body information.
     * 
     * @author ppinard
     */
    protected static class Body {

        /** Index. */
        public final int index;

        /** Material. */
        public final IMaterialScatterModel material;



        /**
         * Creates a <code>Body</code>.
         * 
         * @param material
         *            material
         */
        public Body(int index, IMaterialScatterModel material) {
            if (material == null)
                throw new NullPointerException("material == null");
            this.material = material;
            if (index < 0)
                throw new IllegalArgumentException("index < 0");
            this.index = index;
        }
    }

    /**
     * Stores layer information.
     * 
     * @author ppinard
     */
    protected static class Layer extends Body {

        /** Thickness of the layer (in meters). */
        public final double thickness;



        /**
         * Creates a <code>Layer</code>.
         * 
         * @param material
         *            material
         * @param thickness
         *            thickness (in meters)
         */
        public Layer(int index, IMaterialScatterModel material, double thickness) {
            super(index, material);
            this.thickness = thickness;
        }
    }

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
        protected Map<Integer, IMaterialScatterModel> extractMaterials(
                Element geometryElement) throws IOException, EPQException {
            Map<Integer, IMaterialScatterModel> materials =
                    new HashMap<Integer, IMaterialScatterModel>();
            materials.put(0, new BasicMaterialModel(Material.Null));

            Element materialsElement = geometryElement.getChild("materials");

            Element materialElement;
            int index;
            String name;
            double density, absorptionEnergyElectron;
            Composition composition;
            Material material;
            IMaterialScatterModel scatterModel;
            for (Object obj : materialsElement.getChildren()) {
                materialElement = (Element) obj;

                try {
                    index = materialElement.getAttribute("index").getIntValue();
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

                try {
                    absorptionEnergyElectron =
                            ToSI.eV(materialElement.getAttribute(
                                    "absorptionEnergyElectron")
                                    .getDoubleValue());
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }

                composition = extractComposition(materialElement);

                material = new Material(composition, density);
                material.setName(name);

                scatterModel = new BasicMaterialModel(material);
                scatterModel.setMinEforTracking(absorptionEnergyElectron);

                materials.put(index, scatterModel);
            }

            return materials;
        }



        /**
         * Extracts bodies and layers from a geometry XML element.
         * 
         * @param geometryImplElement
         *            XML element
         * @param materials
         *            materials in the geometry. See
         *            {@link #extractMaterials(Element)}.
         * @return map of body index and {@link Body}
         * @throws IOException
         *             if an error occurs while reading the bodies
         */
        protected Map<Integer, Body> extractBodies(Element geometryElement,
                Map<Integer, IMaterialScatterModel> materials)
                throws IOException {
            Map<Integer, Body> bodies = new HashMap<Integer, Body>();

            Element bodiesElement = geometryElement.getChild("bodies");

            Element bodyElement;
            int bodyIndex, materialIndex;
            double thickness;
            IMaterialScatterModel material;
            Body body;
            for (Object obj : bodiesElement.getChildren()) {
                bodyElement = (Element) obj;

                try {
                    bodyIndex = bodyElement.getAttribute("index").getIntValue();
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }

                try {
                    materialIndex =
                            bodyElement.getAttribute("material").getIntValue();
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }
                material = materials.get(materialIndex);

                switch (bodyElement.getName()) {
                case "body":
                    body = new Body(bodyIndex, material);
                    break;
                case "layer":
                    try {
                        thickness =
                                bodyElement.getAttribute("thickness")
                                        .getDoubleValue();
                    } catch (DataConversionException e) {
                        throw new IOException(e);
                    }
                    body = new Layer(bodyIndex, material, thickness);
                    break;
                default:
                    throw new IOException("Unknown body implementation: "
                            + bodyElement.getName());
                }

                bodies.put(bodyIndex, body);
            }

            return bodies;
        }



        /**
         * Extracts and applies the rotation and tilt to the chamber.
         * 
         * @param geometryImplElement
         *            XML element
         * @param chamber
         *            region of the chamber as defined in
         *            <code>MonteCarloSS</code>
         * @return surface plane normal
         */
        protected void applyRotationTilt(Element geometryElement,
                Region chamber) throws IOException {
            double[] pivot = Math2.ORIGIN_3D;
            double rotation, tilt;

            try {
                rotation =
                        geometryElement.getAttribute("rotation")
                                .getDoubleValue();
                tilt = geometryElement.getAttribute("tilt").getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            double phi = (rotation - Math.PI / 2.0) % (2.0 * Math.PI);
            double theta = tilt;
            double psi = Math.PI / 2.0;

            for (final RegionBase r : chamber.getSubRegions())
                if (r instanceof TransformableRegion)
                    ((TransformableRegion) r).rotate(pivot, phi, theta, psi);
        }
    }

    /** Substrate extractor. */
    protected static class SubstrateGeometryExtractor extends
            AbstractGeometryExtractor {

        @Override
        public void extract(Element geometryElement, Region chamber)
                throws IOException, EPQException {
            Map<Integer, IMaterialScatterModel> materials =
                    extractMaterials(geometryElement);
            Map<Integer, Body> bodies =
                    extractBodies(geometryElement, materials);

            // Get material
            int substrate;
            try {
                substrate =
                        geometryElement.getAttribute("substrate")
                                .getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            IMaterialScatterModel material = bodies.get(substrate).material;

            // Create shape
            double[] normal = Math2.Z_AXIS;
            double[] pt = Math2.ORIGIN_3D;
            MultiPlaneShape shape =
                    MultiPlaneShape.createSubstrate(normal, pt);

            // Add shape to chamber
            new IndexedRegion(chamber, material, shape, substrate);

            applyRotationTilt(geometryElement, chamber);
        }

    }

    public static final GeometryExtractor SUBSTRATE =
            new SubstrateGeometryExtractor();

    /** Inclusion extractor. */
    protected static class InclusionGeometryExtractor extends
            AbstractGeometryExtractor {

        @Override
        public void extract(Element geometryElement, Region chamber)
                throws IOException, EPQException {
            Map<Integer, IMaterialScatterModel> materials =
                    extractMaterials(geometryElement);
            Map<Integer, Body> bodies =
                    extractBodies(geometryElement, materials);

            // Get materials
            int substrate;
            try {
                substrate =
                        geometryElement.getAttribute("substrate")
                                .getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            IMaterialScatterModel substrateMaterial =
                    bodies.get(substrate).material;

            int inclusion;
            try {
                inclusion =
                        geometryElement.getAttribute("inclusion")
                                .getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            IMaterialScatterModel inclusionMaterial =
                    bodies.get(inclusion).material;

            // Get radius
            double radius;
            try {
                radius =
                        geometryElement.getAttribute("diameter")
                                .getDoubleValue() / 2.0;
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            // Create shapes
            double[] normal = Math2.MINUS_Z_AXIS;
            double[] center = Math2.ORIGIN_3D;
            Shape sphere = new Sphere(center, radius);
            Shape upPlane = MultiPlaneShape.createSubstrate(normal, center);
            Shape inclusionShape = new ShapeDifference(sphere, upPlane);

            normal = Math2.Z_AXIS;
            Shape downPlane = MultiPlaneShape.createSubstrate(normal, center);
            Shape substrateShape =
                    new ShapeDifference(downPlane, sphere);

            // Add shape to chamber
            new IndexedRegion(chamber, substrateMaterial, substrateShape,
                    substrate);
            new IndexedRegion(chamber, inclusionMaterial, inclusionShape,
                    inclusion);

            applyRotationTilt(geometryElement, chamber);

        }
    }

    public static final GeometryExtractor INCLUSION =
            new InclusionGeometryExtractor();

    /** Multi-layers extractor. */
    protected static class MultiLayersGeometryExtractor extends
            AbstractGeometryExtractor {

        @Override
        public void extract(Element geometryElement, Region chamber)
                throws IOException, EPQException {
            Map<Integer, IMaterialScatterModel> materials =
                    extractMaterials(geometryElement);
            Map<Integer, Body> bodies =
                    extractBodies(geometryElement, materials);

            // Layers
            String[] tmpLayers =
                    geometryElement.getAttributeValue("layers").split(",");

            MultiPlaneShape shape;
            double[] normal = Math2.Z_AXIS;
            double[] point = Math2.ORIGIN_3D;

            Layer layer;
            IMaterialScatterModel material;
            double thickness;
            for (String tmpLayer : tmpLayers) {
                layer = (Layer) bodies.get(Integer.parseInt(tmpLayer));
                thickness = layer.thickness;
                material = layer.material;

                shape = MultiPlaneShape.createFilm(normal, point, thickness);
                new IndexedRegion(chamber, material, shape, layer.index);

                // Calculate next point
                point =
                        Math2.minus(point,
                                Math2.multiply(thickness, Math2.Z_AXIS));
            }

            // Substrate
            int substrate;

            Attribute substrateAttribute =
                    geometryElement.getAttribute("substrate");
            if (substrateAttribute == null) {
                substrate = 0;
            } else {
                try {
                    substrate = substrateAttribute.getIntValue();
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }
            }

            if (substrate > 0) {
                material = bodies.get(substrate).material;
                thickness = 0.1; // 10 cm

                shape = MultiPlaneShape.createFilm(normal, point, thickness);
                new IndexedRegion(chamber, material, shape, substrate);
            }

            applyRotationTilt(geometryElement, chamber);
        }

    }

    public static final GeometryExtractor MULTI_LAYERS =
            new MultiLayersGeometryExtractor();

    /** Grain boundaries extractor. */
    protected static class GrainBoundariesExtractor extends
            AbstractGeometryExtractor {

        @Override
        public void extract(Element geometryElement, Region chamber)
                throws IOException, EPQException {
            Map<Integer, IMaterialScatterModel> materials =
                    extractMaterials(geometryElement);
            Map<Integer, Body> bodies =
                    extractBodies(geometryElement, materials);

            // Setup layers
            List<Layer> layers = new ArrayList<Layer>();
            double totalThickness = 0.0;

            int leftSubstrate;
            try {
                leftSubstrate =
                        geometryElement.getAttribute("left_substrate")
                                .getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            layers.add(new Layer(leftSubstrate,
                    bodies.get(leftSubstrate).material, 0.1));
            totalThickness += 0.1;

            String[] indexStrs =
                    geometryElement.getAttributeValue("layers").split(",");
            Layer tmpLayer;
            for (String indexStr : indexStrs) {
                tmpLayer =
                        (Layer) bodies.get(Integer.parseInt(indexStr));
                layers.add(tmpLayer);
                totalThickness += tmpLayer.thickness;
            }

            int rightSubstrate;
            try {
                rightSubstrate =
                        geometryElement.getAttribute("right_substrate")
                                .getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            layers.add(new Layer(rightSubstrate,
                    bodies.get(rightSubstrate).material, 0.1));
            totalThickness += 0.1;

            // Create regions
            MultiPlaneShape shape;
            double[] surfaceNormal = Math2.Z_AXIS;
            double[] layerNormal = Math2.MINUS_X_AXIS;
            double[] origin = Math2.ORIGIN_3D;
            double[] point =
                    Math2.multiply(-totalThickness / 2.0, Math2.X_AXIS);

            IMaterialScatterModel material;
            double thickness;

            for (Layer layer : layers) {
                thickness = layer.thickness;
                material = layer.material;

                shape =
                        MultiPlaneShape.createFilm(layerNormal, point,
                                thickness);
                shape.addPlane(surfaceNormal, origin); // surface
                new IndexedRegion(chamber, material, shape, layer.index);

                // Calculate next point
                point =
                        Math2.plus(point,
                                Math2.multiply(thickness, Math2.X_AXIS));
            }

            applyRotationTilt(geometryElement, chamber);
        }

    }

    public static final GeometryExtractor GRAIN_BOUNDARIES =
            new GrainBoundariesExtractor();

    /** Thin grain boundaries extractor. */
    protected static class ThinGrainBoundariesExtractor extends
            AbstractGeometryExtractor {

        @Override
        public void extract(Element geometryElement, Region chamber)
                throws IOException, EPQException {
            Map<Integer, IMaterialScatterModel> materials =
                    extractMaterials(geometryElement);
            Map<Integer, Body> bodies =
                    extractBodies(geometryElement, materials);

            // Setup layers
            List<Layer> layers = new ArrayList<Layer>();
            double totalThickness = 0.0;

            int leftSubstrate;
            try {
                leftSubstrate =
                        geometryElement.getAttribute("left_substrate")
                                .getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            layers.add(new Layer(leftSubstrate,
                    bodies.get(leftSubstrate).material, 0.1));
            totalThickness += 0.1;

            String[] indexStrs =
                    geometryElement.getAttributeValue("layers").split(",");
            Layer tmpLayer;
            for (String indexStr : indexStrs) {
                tmpLayer =
                        (Layer) bodies.get(Integer.parseInt(indexStr));
                layers.add(tmpLayer);
                totalThickness += tmpLayer.thickness;
            }

            int rightSubstrate;
            try {
                rightSubstrate =
                        geometryElement.getAttribute("right_substrate")
                                .getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            layers.add(new Layer(rightSubstrate,
                    bodies.get(rightSubstrate).material, 0.1));
            totalThickness += 0.1;

            // Thickness
            float geometryThickness;
            try {
                geometryThickness =
                        geometryElement.getAttribute("thickness")
                                .getFloatValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            // Create regions
            MultiPlaneShape shape;
            double[] surfaceNormal = Math2.Z_AXIS;
            double[] layerNormal = Math2.MINUS_X_AXIS;
            double[] origin = Math2.ORIGIN_3D;
            double[] bottom =
                    Math2.multiply(geometryThickness, Math2.MINUS_Z_AXIS);
            double[] point =
                    Math2.multiply(-totalThickness / 2.0, Math2.X_AXIS);

            IMaterialScatterModel material;
            double thickness;

            for (Layer layer : layers) {
                thickness = layer.thickness;
                material = layer.material;

                shape =
                        MultiPlaneShape.createFilm(layerNormal, point,
                                thickness);
                shape.addPlane(surfaceNormal, origin); // surface
                shape.addPlane(Math2.multiply(-1, surfaceNormal), bottom); // bottom
                new IndexedRegion(chamber, material, shape, layer.index);

                // Calculate next point
                point =
                        Math2.plus(point,
                                Math2.multiply(thickness, Math2.X_AXIS));
            }

            applyRotationTilt(geometryElement, chamber);
        }
    }

    public static final GeometryExtractor THIN_GRAIN_BOUNDARIES =
            new ThinGrainBoundariesExtractor();

}
