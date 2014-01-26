package pymontecarlo.program.nistmonte.input.detector;

import gov.nist.microanalysis.NISTMonte.MonteCarloSS;

import java.awt.event.ActionEvent;
import java.io.IOException;

import pymontecarlo.util.hdf5.HDF5Group;

/**
 * Listener to record the time elapsed of a simulation.
 * 
 * @author ppinard
 */
public class TimeDetector extends AbstractDetector {

    /** System time when the simulation started. */
    private long startSimulationTime;

    /** System time when a trajectory starts. */
    private long startTrajectoryTime;

    /** Sum of the trajectory times. */
    private double sum;

    /** Square sum of the trajectory times. */
    private double sumSquare;

    /** Number of trajectories. */
    private double count;



    @Override
    public void reset() {
        super.reset();
        startSimulationTime = System.currentTimeMillis();
        sum = 0;
        sumSquare = 0;
        count = 0;
    }



    @Override
    public void actionPerformed(ActionEvent ae) {
        super.actionPerformed(ae);

        switch (ae.getID()) {
        case MonteCarloSS.TrajectoryStartEvent:
            startTrajectoryTime = System.currentTimeMillis();
            break;
        case MonteCarloSS.TrajectoryEndEvent:
            long trajectoryTime =
                    System.currentTimeMillis() - startTrajectoryTime;
            sum += trajectoryTime;
            sumSquare += trajectoryTime * trajectoryTime;
            count += 1;
        default:
            break;
        }

    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);

        HDF5Group group = root.requireSubgroup(key);

        long elapsedTime = System.currentTimeMillis() - startSimulationTime;
        group.setAttribute("simulation_time_s", elapsedTime / 1000.0);

        double mean = sum / count;
        double std = Math.sqrt(sumSquare / count - Math.pow(mean, 2.0));
        group.setAttribute("simulation_speed_s", mean / 1000.0, std / 1000.0);
    }



    @Override
    public String getPythonResultClass() {
        return "TimeResult";
    }

}
