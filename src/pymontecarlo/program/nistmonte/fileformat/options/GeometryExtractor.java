package pymontecarlo.program.nistmonte.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;

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
     * @param chamber
     *            region of the chamber as defined in <code>MonteCarloSS</code>
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the geometry
     */
    public void extract(Element geometryElement, Region chamber)
            throws IOException, EPQException;
}
