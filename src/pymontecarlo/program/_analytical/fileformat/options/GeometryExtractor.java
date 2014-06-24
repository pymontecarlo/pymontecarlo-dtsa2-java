package pymontecarlo.program._analytical.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;

import java.io.IOException;

import org.jdom2.Element;

import pymontecarlo.fileformat.Extractor;

/**
 * Extractor for the geometry implementation.
 * 
 * @author ppinard
 */
public interface GeometryExtractor extends Extractor {

    /**
     * Extracts the geometry from a XML element.
     * 
     * @param geometryElement
     *            XML element
     * @return composition of sample
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the geometry
     */
    public SpectrumProperties extract(Element geometryElement)
            throws IOException, EPQException;
}
