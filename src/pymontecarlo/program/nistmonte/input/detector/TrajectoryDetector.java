package pymontecarlo.program.nistmonte.input.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.NISTMonte.Electron;
import gov.nist.microanalysis.NISTMonte.IndexedRegion;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.RegionBase;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import pymontecarlo.util.hdf5.HDF5Dataset;
import pymontecarlo.util.hdf5.HDF5Group;

public class TrajectoryDetector extends AbstractDetector {

    protected static class Trajectory {

        public final boolean primary;

        public final int collision;

        public final List<Double[]> interactions;

        public int exitState = -1;



        public Trajectory(boolean primary, int collision) {
            this.primary = primary;
            this.collision = collision;
            this.interactions = new ArrayList<>();
        }

    }

    private final boolean secondary;

    private final List<Trajectory> trajectories;

    private final Stack<Trajectory> cachedTrajectories;



    /**
     * Creates a new <code>TrajectoryDetector</code>.
     * 
     * @param secondary
     *            whether to simulate secondary particles
     */
    public TrajectoryDetector(boolean secondary) {
        this.secondary = secondary;
        trajectories = new ArrayList<>();
        cachedTrajectories = new Stack<>();
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        final MonteCarloSS mcss = (MonteCarloSS) e.getSource();

        Trajectory trajectory;
        switch (e.getID()) {
        case MonteCarloSS.TrajectoryStartEvent:
            trajectory = new Trajectory(true, -1);
            saveTrajectoryInteraction(trajectory, mcss.getElectron());
            cachedTrajectories.push(trajectory);
            break;
        case MonteCarloSS.StartSecondaryEvent:
            trajectory = new Trajectory(false, 3); // Hard inelastic
            saveTrajectoryInteraction(trajectory, mcss.getElectron());
            cachedTrajectories.push(trajectory);
            break;
        case MonteCarloSS.EndSecondaryEvent:
        case MonteCarloSS.TrajectoryEndEvent:
            trajectory = cachedTrajectories.pop();
            if (!trajectory.primary && !secondary) // Do not record secondary
                break;

            if (trajectory.exitState < 0)
                trajectory.exitState = 3; // Absorbed

            trajectories.add(trajectory);
            break;
        case MonteCarloSS.ScatterEvent:
            trajectory = cachedTrajectories.peek();
            saveTrajectoryInteraction(trajectory, mcss.getElectron());
            break;
        case MonteCarloSS.BackscatterEvent:
            trajectory = cachedTrajectories.peek();
            trajectory.exitState = 2; // Backscattered
            // TODO: Should also check for transmitted
            break;
        }
    }



    private void saveTrajectoryInteraction(Trajectory trajectory,
            Electron electron) {
        double[] pos_m = electron.getPosition();
        double energy_eV = FromSI.eV(electron.getEnergy());

        RegionBase region = electron.getCurrentRegion();
        Double regionIndex = -1.0;
        if (region instanceof IndexedRegion)
            regionIndex = (double) ((IndexedRegion) region).getIndex();

        Double[] interaction =
                new Double[] { pos_m[0], pos_m[1], pos_m[2], energy_eV, 0.0,
                        regionIndex };
        trajectory.interactions.add(interaction);
    }



    @Override
    public void setup(MonteCarloSS mcss) throws EPQException {
        super.setup(mcss);
    }



    @Override
    public void reset() {
        super.reset();
        trajectories.clear();
    }



    @Override
    protected void createLog(Properties props) {
        super.createLog(props);

        props.setProperty("trajectories", Integer.toString(trajectories.size()));
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);

        HDF5Group group = root.requireSubgroup(key);

        Trajectory trajectory;
        String name;
        double[][] data;
        int interactionsSize;
        Double[] interaction;
        HDF5Dataset dataset;
        for (int i = 0; i < trajectories.size(); i++) {
            trajectory = trajectories.get(i);

            name = "trajectory" + i;

            interactionsSize = trajectory.interactions.size();
            data = new double[interactionsSize][6];
            for (int j = 0; j < interactionsSize; j++) {
                interaction = trajectory.interactions.get(j);
                data[j][0] = interaction[0];
                data[j][1] = interaction[1];
                data[j][2] = interaction[2];
                data[j][3] = interaction[3];
                data[j][4] = interaction[4];
                data[j][5] = interaction[5];
            }

            dataset = group.createDataset(name, data);

            dataset.setAttribute("primary", trajectory.primary ? 1 : 0);
            dataset.setAttribute("particle", 1); // Electron
            dataset.setAttribute("collision", trajectory.collision);
            dataset.setAttribute("exit_state", trajectory.exitState);
        }
    }



    @Override
    public String getPythonResultClass() {
        return "TrajectoryResult";
    }

}
