package pymontecarlo.program.xpp;

import gov.nist.microanalysis.EPQLibrary.CorrectionAlgorithm;
import gov.nist.microanalysis.EPQLibrary.CorrectionAlgorithm.PhiRhoZAlgorithm;
import pymontecarlo.program._analytical.AbstractAnalyticalWorker;

public class Worker extends AbstractAnalyticalWorker {

    @Override
    protected PhiRhoZAlgorithm getCorrectionAlgorithm() {
        return CorrectionAlgorithm.XPP;
    }

}
