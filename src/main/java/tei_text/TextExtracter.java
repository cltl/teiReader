package tei_text;

import org.apache.commons.cli.*;
import xjc.tei2.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ConcurrentModificationException;
import java.util.stream.Stream;

public class TextExtracter {

    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "textExtracter", options);
        System.exit(1);
    }

    public static void main(String[] args) throws IOException {

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
                        .forEach(f -> process(f, cmd.hasOption('p'), outdir, cmd.hasOption('n'))); }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            usage(options);
        }
    }

    private static void process(Path file, boolean paginate, String outdir, boolean extractFootNotes) {
        String fileName = file.getFileName().toString().replaceAll("\\.xml", "");
        outdir = prepareOutputDir(paginate, outdir, fileName);
        try {
            TextHandler handler = TextHandler.create(load(file.toString()), paginate, extractFootNotes);
            handler.process();
            handler.write(outdir + fileName);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("Error while extracting text from file: " + file.getFileName());
            e.printStackTrace();
        } catch (ConcurrentModificationException e) {
            System.out.println("Error while processing file: " + file.getFileName());
            e.printStackTrace();
        }
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


    public static TEI2 load(String xml) throws JAXBException {
        File file = new File(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(TEI2.class);
        System.setProperty("javax.xml.accessExternalDTD", "all");
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (TEI2) jaxbUnmarshaller.unmarshal(file);
    }


}
