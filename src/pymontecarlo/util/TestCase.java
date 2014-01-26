package pymontecarlo.util;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.NISTMonte.BasicMaterialModel;
import gov.nist.microanalysis.NISTMonte.IMaterialScatterModel;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MultiPlaneShape;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.ElectronGun;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;
import gov.nist.microanalysis.NISTMonte.Gen3.BremsstrahlungXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.CharacteristicXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.FluorescenceXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;

import pymontecarlo.program.nistmonte.input.beam.PencilBeam;

/**
 * Generic testing class.
 * 
 * @author ppinard
 */
public class TestCase {

    /**
     * Remove all files and folders inside a directory.
     * 
     * @param dir
     *            a directory
     * @return <code>true</code> if and only if the directory is successfully
     *         deleted; <code>false</code> otherwise
     */
    private static boolean rmdir(File dir) {
        // Check if the dir is really a dir
        if (!dir.isDirectory())
            return false;

        // Go through all the files and subdirs
        File[] files = dir.listFiles();
        if (files == null)
            files = new File[0];

        for (File file : files) {
            if (file.isDirectory()) { // If it is a directory
                rmdir(file); // Remove everything inside
            } else { // If it is a file
                if (!file.delete()) { // Delete the file
                    return false;
                }
            }
        }

        // Delete the base directory
        if (!dir.delete()) {
            return false;
        }

        return true;
    }

    /** Temporary files and directories. */
    private ArrayList<File> files = new ArrayList<File>();



    /**
     * Creates a temporary directory. The directory and all its content are
     * automatically removed after all the tests are run.
     * 
     * @return a temporary directory
     * @throws IOException
     *             if the temporary directory cannot be created
     */
    public File createTempDir() throws IOException {
        File dir = File.createTempFile("tmp", Long.toString(System.nanoTime()));

        if (!dir.delete())
            throw new IOException("Could not delete temp file: "
                    + dir.getAbsolutePath());

        if (!dir.mkdir())
            throw new IOException("Could not create temp dir: "
                    + dir.getAbsolutePath());

        files.add(dir);

        return dir;
    }



    /**
     * Creates a temporary file. The method
     * {@link File#createTempFile(String, String)} is used with a prefix of
     * "tmp". The file is automatically removed after all the tests are run.
     * 
     * @return a temporary file
     * @throws IOException
     *             if an error occurs while creating the file
     */
    public File createTempFile() throws IOException {
        return createTempFile("tmp");
    }



    /**
     * Creates a temporary file. The method
     * {@link File#createTempFile(String, String)} is used with a prefix of
     * "tmp". The file is automatically removed after all the tests are run.
     * 
     * @param ext
     *            extension of the temporary file
     * @return a temporary file
     * @throws IOException
     *             if an error occurs while creating the file
     */
    public File createTempFile(String ext) throws IOException {
        if (ext == null)
            throw new NullPointerException("ext == null");

        File file = File.createTempFile("tmp", "." + ext);
        files.add(file);
        return file;
    }



    /**
     * Retrieves a file by its filename using a location independent procedure.
     * 
     * @param filename
     *            file name using the / character as a path separator
     * @return a <code>File</code> representing the specified file name or
     *         <code>null</code> if the file does not exists or
     *         <code>fileName</code> does not represent a file
     */
    public File getFile(String filename) {
        return new File(getPath(filename));
    }



    /**
     * Returns the path of the file corresponding to the specified filename.
     * 
     * @param filename
     *            file name using the / character as a path separator
     * @return path of the file
     */
    private String getPath(String filename) {
        // Get the URL of the class file
        URL url = getURL(filename);

        // Check if the resource is a file
        if (!url.getProtocol().equalsIgnoreCase("file"))
            Assert.fail(filename + " is not a file.");

        String file = url.getFile();

        // Needed to take care of the space character
        // in the file name under windows
        file = file.replaceAll("%20", " ");

        return file;
    }



    /**
     * Finds a file using a location independent procedure.
     * 
     * @param filename
     *            file name using the / character as a path separator
     * @return an <code>URL</code> representing the specified file name or
     *         <code>null</code> if the file does not exists or
     *         <code>fileName</code> does not represent a file
     */
    public URL getURL(String filename) {
        ClassLoader cl = TestCase.class.getClassLoader();
        if (cl == null) // If bootstrap classloader
            cl = ClassLoader.getSystemClassLoader();

        URL url = cl.getResource(filename);

        if (url == null)
            Assert.fail(filename + " not found.");

        return url;
    }



    /**
     * Cleans temporary files and directories after all tests are run.
     * 
     * @throws Exception
     *             if an error occurs
     */
    @After
    public void tearDown() throws Exception {
        ArrayList<File> errors = new ArrayList<File>();

        for (File file : files) {
            if (!file.exists())
                continue;

            if (file.isFile()) {
                if (!file.delete())
                    errors.add(file);
            } else if (file.isDirectory()) {
                if (!rmdir(file))
                    errors.add(file);
            }
        }

        if (!errors.isEmpty())
            throw new IOException(
                    "The following files/directories could not be deleted:"
                            + errors.toString());

        files.clear();
    }



    public MonteCarloSS getMonteCarloSS() throws EPQException {
        MonteCarloSS mcss = createMonteCarloSS();

        ElectronGun beam = createElectronGun();
        mcss.setBeamEnergy(beam.getBeamEnergy());
        mcss.setElectronGun(beam);

        createGeometry(mcss.getChamber());

        return mcss;
    }



    public XRayTransport3 getCharacteristicTransport(MonteCarloSS mcss)
            throws EPQException {
        double[] pos = getDetectorPosition();
        CharacteristicXRayGeneration3 characGen =
                CharacteristicXRayGeneration3.create(mcss);
        return XRayTransport3.create(mcss, pos, characGen);
    }



    public XRayTransport3 getBremmstrahlungTransport(MonteCarloSS mcss)
            throws EPQException {
        double[] pos = getDetectorPosition();
        BremsstrahlungXRayGeneration3 bremssGen =
                BremsstrahlungXRayGeneration3.create(mcss);
        return XRayTransport3.create(mcss, pos, bremssGen);
    }



    public XRayTransport3 getCharacteristicFluoTransport(MonteCarloSS mcss,
            XRayTransport3 charac) throws EPQException {
        double[] pos = getDetectorPosition();
        FluorescenceXRayGeneration3 charactGenFluo =
                FluorescenceXRayGeneration3.create(mcss, charac.getSource());
        charactGenFluo.setIncludeCompton(true);
        return XRayTransport3.create(mcss, pos, charactGenFluo);
    }



    public XRayTransport3 getBremmstrahlungFluoTransport(MonteCarloSS mcss,
            XRayTransport3 bremss) throws EPQException {
        double[] pos = getDetectorPosition();
        FluorescenceXRayGeneration3 bremssGenFluo =
                FluorescenceXRayGeneration3.create(mcss, bremss.getSource());
        return XRayTransport3.create(mcss, pos, bremssGenFluo);
    }



    protected MonteCarloSS createMonteCarloSS() {
        return new MonteCarloSS();
    }



    protected ElectronGun createElectronGun() {
        PencilBeam beam = new PencilBeam();

        beam.setBeamEnergy(getBeamEnergy());
        beam.setCenter(new double[] { 0.0, 0.0,
                0.99 * MonteCarloSS.ChamberRadius });
        beam.setDirection(new double[] { 0.0, 0.0, 1.0 });

        return beam;
    }



    protected void createGeometry(Region chamber) throws EPQException {
        Material mat = MaterialFactory.createPureElement(Element.Au);
        IMaterialScatterModel model = new BasicMaterialModel(mat);

        double dim = 0.01; // 1 mm
        double[] dims = new double[] { dim, dim, dim };
        double[] pos = new double[] { 0.0, 0.0, -dim / 2.0 };
        MultiPlaneShape shape = MultiPlaneShape.createBlock(dims, pos, 0, 0, 0);

        new Region(chamber, model, shape);
    }



    public double[] getDetectorPosition() {
        return new double[] { 0.07652784, 0.0, 0.06421448 };
    }



    public double getBeamEnergy() {
        return ToSI.eV(15e3);
    }
}
