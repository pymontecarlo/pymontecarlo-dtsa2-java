package pymontecarlo.program.nistmonte.ui.cui;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import pymontecarlo.program.nistmonte.runner.Worker;

/**
 * Command line interface to run a simulated with NistMonte from options
 * generate by pyMonteCarlo.
 * 
 * @author ppinard
 */
public final class Main {

    /**
     * Main entry.
     * 
     * @param args
     *            arguments passed to the program
     * @throws Exception
     *             if an exception occurs while executing the program
     */
    @SuppressWarnings("static-access")
    public static void main(String[] inargs) throws Exception {

        // Create parser
        Options options = new Options();

        options.addOption(OptionBuilder.withDescription("Print this help")
                .withLongOpt("help").create('h'));
        options.addOption(OptionBuilder.hasArg().withArgName("outputdir")
                .withDescription("Output directory").create('o'));
        options.addOption(OptionBuilder.withArgName("quite")
                .withDescription("Quite mode").create('q'));

        CommandLineParser parser = new PosixParser();

        // Parse
        CommandLine cmd;
        try {
            cmd = parser.parse(options, inargs);
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            return;
        }

        if (cmd.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Main [options] OPTIONS_XML_FILE", options);
            return;
        }

        File outputDir = new File(System.getProperty("user.dir"));
        if (cmd.hasOption('o'))
            outputDir = new File(cmd.getOptionValue("o"));
        if (!outputDir.exists()) {
            System.err.println("Output directory does not exist: " + outputDir);
            return;
        }
        
        boolean quite = false;
        if (cmd.hasOption('q'))
            quite = true;
        
        String[] args = cmd.getArgs();
        if (args.length == 0) {
            System.err.println("Please specify one options XML file");
            return;
        } else if (args.length > 1) {
            System.err.println("Please specify only one options XML file");
            return;
        }
        
        File optionsFile = new File(args[0]);
        if (!optionsFile.exists()) {
            System.err.println("Options XML file does not exist: " + optionsFile);
            return;
        }
        
        Worker worker = new Worker(optionsFile, outputDir, quite);
        worker.run();
    }
}
