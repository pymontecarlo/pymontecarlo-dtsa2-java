package pymontecarlo.program.nistmonte.input.options;

import gov.nist.microanalysis.EPQLibrary.EPQException;

import java.io.IOException;

import org.jdom2.Element;
import org.junit.Test;

import pymontecarlo.program.nistmonte.input.limit.ShowersLimit;


import static org.junit.Assert.assertEquals;

public class LimitExtractorFactoryTest {

    public static Element createShowersLimitElement() {
        Element element =
                new Element("showersLimit");

        element.setAttribute("showers", "1234");

        return element;
    }



    @Test
    public void testSHOWERS() throws IOException, EPQException {
        // XML element
        Element element = createShowersLimitElement();

        // Extract
        LimitExtractor extractor = LimitExtractorFactory.SHOWERS;
        ShowersLimit limit = (ShowersLimit) extractor.extract(element);

        // Test
        assertEquals(1234, limit.getMaximumShowers());
    }

}
