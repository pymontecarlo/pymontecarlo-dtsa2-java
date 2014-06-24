package pymontecarlo.program._analytical.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;

import java.io.IOException;

import org.jdom2.Element;

import pymontecarlo.fileformat.Extractor;
import pymontecarlo.program._analytical.options.detector.Detector;

/**
 * Extractor for a detector implementation.
 * 
 * @author ppinard
 */
public interface DetectorExtractor extends Extractor {

    /**
     * Extract a detector from a XML element.
     * 
     * @param detectorElement
     *            XML element
     * @return spectrum properties
     * @throws IOException
     *             if an error occurs while reading the options
     * @throws EPQException
     *             if an error occurs while setting up the detector
     */
    public Detector extract(Element detectorElement)
            throws IOException, EPQException;
}
