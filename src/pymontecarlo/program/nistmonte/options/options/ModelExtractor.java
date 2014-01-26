package pymontecarlo.program.nistmonte.options.options;

import gov.nist.microanalysis.EPQLibrary.Strategy;

import java.io.IOException;

import org.jdom2.Element;

/**
 * Extractor for the model implementations.
 * 
 * @author ppinard
 */
public interface ModelExtractor extends Extractor {

    /**
     * Extracts the model from a XML element.
     * 
     * @param modelElement
     *            XML element
     * @return strategy
     *            strategy with the define model set
     * @throws IOException
     *             if an error occurs while reading the XML element
     */
    public Strategy extract(Element modelElement) throws IOException;
}
