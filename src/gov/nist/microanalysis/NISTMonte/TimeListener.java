package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.Utility.UncertainValue2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Listener to record the time elapsed of a simulation.
 * 
 * @author ppinard
 */
public class TimeListener implements ActionListener {

    /** Monte Carlo simulator. */
    private final MonteCarloSS mcss;

    /** System time when the simulation started (in ms). */
    private long startSimulationTime;

    /** System time when the simulation ended(in ms). */
    private long elapsedTime;

    /** System time when a trajectory starts(in ms). */
    private long startTrajectoryTime;

    /** Sum of the trajectory times(in ms). */
    private double sum;

    /** Square sum of the trajectory times. */
    private double sumSquare;

    /** Number of trajectories. */
    private double count;



    /**
     * Creates a new <code>TimeListener</code>.
     * 
     * @param mcss
     *            Monte Carlo simulator
     */
    public TimeListener(MonteCarloSS mcss) {
        if (mcss == null)
            throw new NullPointerException("mcss == null");
        this.mcss = mcss;
    }



    @Override
    public void actionPerformed(ActionEvent ae) {
        assert (ae.getSource() instanceof MonteCarloSS);
        assert (ae.getSource() == mcss);

        switch (ae.getID()) {
        case MonteCarloSS.FirstTrajectoryEvent:
            startSimulationTime = System.currentTimeMillis();
            elapsedTime = 0;

            sum = 0;
            sumSquare = 0;
            count = 0;
            break;
        case MonteCarloSS.LastTrajectoryEvent:
            elapsedTime = System.currentTimeMillis() - startSimulationTime;
            break;
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



    /**
     * Returns the elapsed time to run the simulation in milliseconds.
     * 
     * @return elapsed time in seconds
     */
    public double getSimulationTime() {
        return elapsedTime / 1000.0;
    }



    /**
     * Returns the mean time to execute one trajectory (and its uncertainty).
     * 
     * @return mean trajectory time in seconds
     */
    public UncertainValue2 getMeanTrajectoryTime() {
        double mean = sum / count;
        double std = Math.sqrt(sumSquare / count - Math.pow(mean, 2.0));
        return new UncertainValue2(mean / 1000.0, std / 1000.0);
    }



    /**
     * Writes the simulation and trajectory time in the specified file.
     * 
     * @param outputFile
     *            output file
     * @throws IOException
     *             if an error occurs while writing the file
     */
    public void dumpToFile(File outputFile) throws IOException {
        FileWriter writer = new FileWriter(outputFile);
        String eol = System.getProperty("line.separator");

        writer.append("Simulation time: " + getSimulationTime() + " s"
                + eol);

        UncertainValue2 trajTime = getMeanTrajectoryTime();
        writer.append("Average trajectory time: " + trajTime.doubleValue()
                + " +- " + trajTime.uncertainty() + " ms");

        writer.close();
    }
}