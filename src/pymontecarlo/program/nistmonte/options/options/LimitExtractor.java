package pymontecarlo.program.nistmonte.options.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;

import java.io.IOException;

import org.jdom2.Element;

import pymontecarlo.program.nistmonte.options.limit.Limit;


/**
 * Extractor for a limit implementation.
 * 
 * @author ppinard
 */
public interface LimitExtractor extends Extractor {

    /**
     * Extract a limit from a XML element.
     * 
     * @param limitElement
     *            XML element
     * @return limit
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the detector
     */
    public Limit extract(Element limitElement) throws IOException, EPQException;
}
