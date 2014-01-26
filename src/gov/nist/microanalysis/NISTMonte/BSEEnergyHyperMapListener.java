package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.EPQLibrary.FromSI;

import java.awt.event.ActionEvent;

public class BSEEnergyHyperMapListener extends AbstractHyperMapListener {

    public BSEEnergyHyperMapListener(int width, int height, double x0, double y0,
            double dx, double dy, double min, double max, int bins) {
        super(width, height, x0, y0, dx, dy, min, max, bins);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
        
        MonteCarloSS mcss = (MonteCarloSS) event.getSource();
        
        switch (event.getID()) {
        case MonteCarloSS.BackscatterEvent:
            Electron el = mcss.getElectron();
            double[] pos = el.getPrevPosition();
            add(pos[0], pos[1], FromSI.eV(el.getEnergy()));
            break;
        default:
            break;
        }
    }
    
    

}
