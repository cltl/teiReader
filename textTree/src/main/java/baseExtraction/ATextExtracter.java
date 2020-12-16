package baseExtraction;

import org.apache.commons.cli.*;
import textTree.IText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ConcurrentModificationException;
import java.util.stream.Stream;

public abstract class ATextExtracter {

    public TextHandler handler;
    TextWriter writer;

    public ATextExtracter() {

    }

    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "textExtracter", options);
        System.exit(1);
    }

    public void process(String[] args) {
        Options options = new Options();
        options.addOption("n", false, "extract full notes");
        options.addOption("c", false, "complete interrupted paragraphs");
        options.addOption("p", false, "separate page files");
        options.addOption("s", false, "split notes into separate files");
        options.addOption("d", true, "output directory");
        options.addOption("i", true, "input file / directory");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (! cmd.hasOption('i')) {
                System.out.println("Please specify an input file or directory");
                usage(options);
            }
            final String outdir = cmd.hasOption('d') ? cmd.getOptionValue('d') : "./";
            handler = TextHandler.create(cmd.hasOption('n'), cmd.hasOption('c'));
            writer = TextWriter.create(cmd.hasOption('p'), cmd.hasOption('s'));
            try (Stream<Path> paths = Files.walk(Paths.get(cmd.getOptionValue('i')))) {
                paths.filter(Files::isRegularFile)
                        .forEach(f -> processFile(f, outdir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            usage(options);
        }
    }

    public abstract IText extract(String fileName);

    private void processFile(Path file, String outdir) {
        String fileName = file.getFileName().toString().replaceAll("\\.xml", "");
        try {
            IText textTree = extract(file.toString());
            writer.write(textTree, outdir, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("Error while extracting text from file: " + file.getFileName());
            e.printStackTrace();
        } catch (ConcurrentModificationException e) {
            System.out.println("Error while processing file: " + file.getFileName());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Error while processing file: " + file.getFileName());
            e.printStackTrace();

        }
    }
}
