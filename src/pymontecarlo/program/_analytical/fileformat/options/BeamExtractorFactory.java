package pymontecarlo.program._analytical.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;

import java.io.IOException;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

/**
 * Factory of beam extractors.
 * 
 * @author ppinard
 */
public class BeamExtractorFactory {

    protected static class PencilBeamExtractor implements BeamExtractor {

        @Override
        public SpectrumProperties extract(Element beamElement)
                throws IOException,
                EPQException {
            SpectrumProperties props = new SpectrumProperties();

            props.setNumericProperty(SpectrumProperties.BeamEnergy,
                    extractBeamEnergy(beamElement) / 1e3);

            return props;
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

    }

    public static final BeamExtractor PENCIL = new PencilBeamExtractor();

}
