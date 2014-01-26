package pymontecarlo.program.nistmonte.input.detector;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;

import java.awt.event.ActionEvent;
import java.io.IOException;

import pymontecarlo.util.hdf5.HDF5Group;

public class PhotonSpectrumDetector extends AbstractPhotonDetector {

    /** EDS detector that records all x rays (characteristic and background). */
    private final EDSDetector detectorTotal;

    /** EDS detector that records only background x rays. */
    private final EDSDetector detectorBackground;

    /** Counter for the number of trajectories. */
    private int trajectoryCount;



    public PhotonSpectrumDetector(double takeOffAngle, double azimuthAngle,
            double channelWidth, int channels) {
        super(takeOffAngle, azimuthAngle);

        if (channelWidth <= 0.0)
            throw new IllegalArgumentException("Channel width <= 0.0: "
                    + channelWidth);
        if (channels <= 0)
            throw new IllegalArgumentException("Number of channels <= 0: "
                    + channels);

        try {
            detectorTotal =
                    EDSDetector.createPerfectDetector(channels, channelWidth,
                            getDetectorPosition());
            detectorBackground =
                    EDSDetector.createPerfectDetector(channels, channelWidth,
                            getDetectorPosition());
        } catch (EPQException ex) {
            throw new IllegalArgumentException(ex);
        }
    }



    public PhotonSpectrumDetector(double[] pos, double channelWidth,
            int channels) {
        super(pos);

        if (channelWidth <= 0.0)
            throw new IllegalArgumentException("Channel width <= 0.0: "
                    + channelWidth);
        if (channels <= 0)
            throw new IllegalArgumentException("Number of channels <= 0: "
                    + channels);

        try {
            detectorTotal =
                    EDSDetector.createPerfectDetector(channels, channelWidth,
                            getDetectorPosition());
            detectorBackground =
                    EDSDetector.createPerfectDetector(channels, channelWidth,
                            getDetectorPosition());
        } catch (EPQException ex) {
            throw new IllegalArgumentException(ex);
        }
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        switch (e.getID()) {
        case MonteCarloSS.TrajectoryStartEvent:
            trajectoryCount += 1;
            break;
        default:
            break;
        }
    }



    private double[][] arrayFromSpectrum(ISpectrumData spectrum) {
        double[][] array = new double[spectrum.getChannelCount()][2];

        double offset = spectrum.getZeroOffset();
        double channelWidth = spectrum.getChannelWidth();

        double factor = 1.0 / (channelWidth * trajectoryCount);

        for (int i = 0; i < array.length; i++) {
            array[i][0] = offset + (i + 0.5) * channelWidth;
            array[i][1] = spectrum.getCounts(i) * factor;
        }

        return array;
    }



    @Override
    public String getPythonResultClass() {
        return "PhotonSpectrumResult";
    }



    @Override
    public boolean requiresBremmstrahlung() {
        return true;
    }



    @Override
    public void reset() {
        super.reset();
        detectorTotal.reset();
        detectorBackground.reset();
        trajectoryCount = 0;
    }



    @Override
    public void saveResults(HDF5Group root, String key) throws IOException {
        super.saveResults(root, key);

        double[][] spectrumTotal =
                arrayFromSpectrum(detectorTotal.getSpectrum(1.0));
        double[][] spectrumBackground =
                arrayFromSpectrum(detectorBackground.getSpectrum(1.0));

        HDF5Group group = root.requireSubgroup(key);

        group.createDataset("total", spectrumTotal);
        group.createDataset("background", spectrumBackground);
    }



    @Override
    public void setup(MonteCarloSS mcss, XRayTransport3 charac,
            XRayTransport3 bremss, XRayTransport3 characFluo,
            XRayTransport3 bremssFluo) throws EPQException {
        if (charac == null)
            throw new NullPointerException("charact == null");
        charac.addXRayListener(detectorTotal);

        if (bremss == null)
            throw new NullPointerException("bremss == null");
        bremss.addXRayListener(detectorTotal);
        bremss.addXRayListener(detectorBackground);

        if (characFluo != null)
            characFluo.addXRayListener(detectorTotal);

        if (bremssFluo != null)
            bremssFluo.addXRayListener(detectorTotal);
    }
}
