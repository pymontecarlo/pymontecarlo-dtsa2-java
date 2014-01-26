package pymontecarlo.program.nistmonte.input.options;

import org.jdom2.Namespace;

/**
 * Extractor of options from a XML element.
 * 
 * @author ppinard
 */
public interface Extractor {

    public static final Namespace ns = Namespace.getNamespace("mc",
            "http://pymontecarlo.sf.net");
}
