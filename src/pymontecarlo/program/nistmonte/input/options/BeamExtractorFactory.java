package pymontecarlo.program.nistmonte.input.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;

import java.io.IOException;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pymontecarlo.program.nistmonte.input.beam.GaussianFWHMBeam;
import pymontecarlo.program.nistmonte.input.beam.PencilBeam;

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
                x = directionElement.getAttribute("x").getDoubleValue();
                y = directionElement.getAttribute("y").getDoubleValue();
                z = directionElement.getAttribute("z").getDoubleValue();
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
    
}
