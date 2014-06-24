package pymontecarlo.program.xpp.ui.cui;

import pymontecarlo.program.xpp.Worker;
import pymontecarlo.ui.cui.AbstractMain;

/**
 * Command line interface to run a simulated with NistMonte from options
 * generate by pyMonteCarlo.
 * 
 * @author ppinard
 */
public final class Main extends AbstractMain {

    /** 
     * Main entry.
     * 
     * @param args
     *            arguments passed to the program
     * @throws Exception
     *             if an exception occurs while executing the program
     */
    public static void main(String[] inargs) throws Exception {
        Worker worker = new Worker();
        main(worker, inargs);
    }
}
