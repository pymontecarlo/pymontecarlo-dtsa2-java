package pymontecarlo.program.nistmonte.input.model;

import java.util.Arrays;
import java.util.List;

import gov.nist.microanalysis.EPQLibrary.AlgorithmClass;

public class FluorescenceMC extends AlgorithmClass {

    protected FluorescenceMC(String name, String ref) {
        super("fluorescence", name, ref);
    }



    @Override
    public List<AlgorithmClass> getAllImplementations() {
        return Arrays.asList(mAllImplementations);
    }



    @Override
    protected void initializeDefaultStrategy() {
        // Don't do anything...
    }

    public static final FluorescenceMC Null =
            new FluorescenceMC("no fluorescence", "N. Ritchie");

    public static final FluorescenceMC Fluorescence =
            new FluorescenceMC("fluorescence", "N. Ritchie");

    public static final FluorescenceMC FluorescenceCompton =
            new FluorescenceMC("fluorescence with Compton", "N. Ritchie");

    static private final AlgorithmClass[] mAllImplementations = {
            Null, Fluorescence, FluorescenceCompton
    };

}
