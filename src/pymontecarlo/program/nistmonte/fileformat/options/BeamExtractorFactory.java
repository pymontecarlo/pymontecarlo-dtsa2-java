package pymontecarlo.program.nistmonte.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;

import java.io.IOException;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pymontecarlo.program.nistmonte.options.beam.GaussianFWHMBeam;
import pymontecarlo.program.nistmonte.options.beam.GaussianFWHMExpTailBeam;
import pymontecarlo.program.nistmonte.options.beam.PencilBeam;

/**
 * Factory of beam extractors.
 * 
 * @author ppinard
 */
public class BeamExtractorFactory {

    protected static class PencilBeamExtractor implements BeamExtractor {

        @Override
        public ElectronGun extract(Element beamElement) throws IOException,
                EPQException {
            PencilBeam beam = new PencilBeam();

            beam.setBeamEnergy(ToSI.eV(extractBeamEnergy(beamElement)));
            beam.setCenter(extractCenter(beamElement));
            beam.setDirection(extractDirection(beamElement));

            return beam;
        }



        /**
         * Extracts the beam energy from the XML element.
         * 
         * @param beamElement
         *            XML element
         * @return beam energy in eV
         */
        protected double extractBeamEnergy(Element beamElement)
                throws IOException {
            double energy;
            try {
                energy = beamElement.getAttribute("energy").getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            return energy;
        }



        /**
         * Extracts the center position from the XML element.
         * 
         * @param beamElement
         *            XML element
         * @return position of beam
         */
        protected double[] extractCenter(Element beamElement)
                throws IOException {
            Element originElement = beamElement.getChild("origin");

            double x, y, z;
            try {
                x = originElement.getAttribute("x").getDoubleValue();
                y = originElement.getAttribute("y").getDoubleValue();
                z = originElement.getAttribute("z").getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            return new double[] { x, y, z };
        }



        /**
         * Extracts the direction from the XML element.
         * 
         * @param beamElement
         *            XML element
         * @return beam direction
         */
        protected double[] extractDirection(Element beamElement)
                throws IOException {
            Element directionElement = beamElement.getChild("direction");

            double x, y, z;
            try {
                x = directionElement.getAttribute("u").getDoubleValue();
                y = directionElement.getAttribute("v").getDoubleValue();
                z = directionElement.getAttribute("w").getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }

            return new double[] { x, y, z };
        }

    }

    public static final BeamExtractor PENCIL = new PencilBeamExtractor();

    protected static class GaussianFWHMBeamExtractor extends
            PencilBeamExtractor implements BeamExtractor {

        @Override
        public ElectronGun extract(Element beamElement)
                throws IOException, EPQException {
            GaussianFWHMBeam beam =
                    new GaussianFWHMBeam(extractDiameter(beamElement));

            beam.setBeamEnergy(ToSI.eV(extractBeamEnergy(beamElement)));
            beam.setCenter(extractCenter(beamElement));
            beam.setDirection(extractDirection(beamElement));

            return beam;
        }



        /**
         * Extracts beam diameter from the XML element.
         * 
         * @param beamElement
         *            XML element
         * @return beam diameter
         */
        protected double extractDiameter(Element beamElement)
                throws IOException {
            double diameter;
            try {
                diameter =
                        beamElement.getAttribute("diameter")
                                .getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            return diameter;
        }
    }

    public static final BeamExtractor GAUSSIAN_FWHM =
            new GaussianFWHMBeamExtractor();

    protected static class GaussianFWHMExpTailBeamExtractor extends
            GaussianFWHMBeamExtractor implements BeamExtractor {

        @Override
        public ElectronGun extract(Element beamElement)
                throws IOException, EPQException {
            double diameter = extractDiameter(beamElement);
            double skirtThreshold = extractSkirtThreshold(beamElement);
            double skirtFactor = extractSkirtFactor(beamElement);

            GaussianFWHMExpTailBeam beam =
                    new GaussianFWHMExpTailBeam(diameter, skirtThreshold,
                            skirtFactor);

            beam.setBeamEnergy(ToSI.eV(extractBeamEnergy(beamElement)));
            beam.setCenter(extractCenter(beamElement));
            beam.setDirection(extractDirection(beamElement));

            return beam;
        }



        /**
         * Extracts skirt threshold from the XML element.
         * 
         * @param beamElement
         *            XML element
         * @return skirt threshold
         */
        protected double extractSkirtThreshold(Element beamElement)
                throws IOException {
            double skirtThreshold;
            try {
                skirtThreshold =
                        beamElement.getAttribute("skirtThreshold")
                                .getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            return skirtThreshold;
        }
        
        /**
         * Extracts skirt threshold from the XML element.
         * 
         * @param beamElement
         *            XML element
         * @return skirt threshold
         */
        protected double extractSkirtFactor(Element beamElement)
                throws IOException {
            double skirtFactor;
            try {
                skirtFactor =
                        beamElement.getAttribute("skirtFactor")
                                .getDoubleValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            return skirtFactor;
        }
    }

    public static final BeamExtractor GAUSSIAN_FWHM_EXP_TAIL =
            new GaussianFWHMExpTailBeamExtractor();

}
