package pymontecarlo.program;

/**
 * Runner for NistMonte.
 * 
 * @author ppinard
 */
public abstract class AbstractWorker implements Worker {

    /** Whether to run in quite mode. */
    private boolean quite = true;



    @Override
    public boolean isQuite() {
        return quite;
    }



    @Override
    public void setQuite(boolean state) {
        this.quite = state;
    }



    /**
     * Report progress and status.
     * 
     * @param progress
     *            current progress (between 0.0 and 1.0)
     * @param status
     *            current status
     */
    protected void report(double progress, String status) {
        if (!quite) {
            System.out.println(progress + "\t" + status);
            System.out.flush();
        }
    }
}
