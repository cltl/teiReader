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

    public ATextExtracter() { }

    public abstract IText load(String xml);

    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "textExtracter", options);
        System.exit(1);
    }

    public void process(String[] args) {
        Options options = new Options();
        options.addOption("p", false, "separate page files");
        options.addOption("n", false, "extract full notes");
        options.addOption("d", true, "output directory");
        options.addOption("i", true, "input file / directory");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (! cmd.hasOption('i')) {
                System.out.println("Please specify an input file or directory");
                usage(options);
            }
            final String outdir = cmd.hasOption('d') ? cmd.getOptionValue('d') : "";
            try (Stream<Path> paths = Files.walk(Paths.get(cmd.getOptionValue('i')))) {
                paths.filter(Files::isRegularFile)
                        .forEach(f -> processFile(f, cmd.hasOption('p'), outdir, cmd.hasOption('n')));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            usage(options);
        }
    }

    private void processFile(Path file, boolean paginate, String outdir, boolean extractFootNotes) {
        String fileName = file.getFileName().toString().replaceAll("\\.xml", "");
        outdir = prepareOutputDir(paginate, outdir, fileName);
        try {
            TextHandler handler = createTextHandler(file.toString(), paginate, extractFootNotes);
            handler.process();
            handler.write(outdir + fileName);
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

    public TextHandler createTextHandler(String file, boolean paginate, boolean extractFootNotes) {
        IText itext = load(file);
        return TextHandler.create(itext, paginate, extractFootNotes);
    }

    private static String prepareOutputDir(boolean paginate, String outdir, String fileName) {
        if (! outdir.endsWith("/"))
            outdir += '/';
        if (paginate)
            outdir += fileName + '/';
        if (! outdir.isEmpty()) {
            Path path = Paths.get(outdir);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(Paths.get(outdir));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return outdir;
    }
}
