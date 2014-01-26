package pymontecarlo.program.nistmonte.input.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;

import java.io.IOException;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pymontecarlo.program.nistmonte.input.limit.Limit;
import pymontecarlo.program.nistmonte.input.limit.ShowersLimit;

/**
 * Factory of limit extractors.
 * 
 * @author ppinard
 */
public class LimitExtractorFactory {

    /** Showers limit extractor. */
    protected static class ShowersLimitExtractor implements LimitExtractor {

        @Override
        public Limit extract(Element limitElement) throws IOException,
                EPQException {
            int showers;
            try {
                showers = limitElement.getAttribute("showers").getIntValue();
            } catch (DataConversionException e) {
                throw new IOException(e);
            }
            return new ShowersLimit(showers);
        }

    }

    public static final LimitExtractor SHOWERS = new ShowersLimitExtractor();

}
