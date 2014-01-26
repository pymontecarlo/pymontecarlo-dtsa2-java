package pymontecarlo.program.nistmonte.input.options;

import java.util.HashMap;
import java.util.Map;

public class ExtractorManager {

    private static final Map<String, Extractor> extractors = new HashMap<>();



    public static void reset() {
        extractors.clear();
    }



    public static void register(String tag, Extractor klass) {
        if (extractors.put(tag, klass) != null)
            throw new IllegalArgumentException("A class (" + klass
                    + ") is already registered with the tag (" + tag + ")");
    }



    public static Extractor getExtractor(String tag) {
        Extractor extractor = extractors.get(tag);
        if (extractor == null)
            throw new IllegalArgumentException("No class found for tag (" + tag
                    + ")");
        return extractor;
    }
}