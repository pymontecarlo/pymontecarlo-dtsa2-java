package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.Utility.Histogram3D;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AbstractHyperMapListener implements ActionListener {

    private final Histogram3D distribution;



    public AbstractHyperMapListener(int width, int height,
            double x0, double y0, double dx, double dy,
            double min, double max, int bins) {
        double xRange = width * dx;
        double xMin = x0 - xRange / 2.0;
        double xMax = x0 + xRange / 2.0;
        double yRange = height * dy;
        double yMin = y0 - yRange / 2.0;
        double yMax = y0 + yRange / 2.0;

        distribution =
                new Histogram3D(xMin, xMax, width, yMin, yMax, height, min,
                        max, bins);
    }



    @Override
    public void actionPerformed(ActionEvent event) {
        switch (event.getID()) {
        case MonteCarloSS.FirstTrajectoryEvent:
            distribution.clear();
            break;
        default:
            break;
        }
    }
    
    protected void add(double x, double y, double val) {
        distribution.add(x, y, val);
    }
    
    /**
     * Return a copy of the hyper map distribution.
     * 
     * @return hyper map distribution
     */
    public Histogram3D getDistribution() {
        return distribution.clone();
    }

}
