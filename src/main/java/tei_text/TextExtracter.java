package tei_text;

import org.apache.commons.cli.*;
import xjc.tei2.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextExtracter {
    TEI2 tei;

    TextExtracter(TEI2 tei) {
        this.tei = tei;
    }

    private boolean isToExtract(Object e) {
        return e instanceof Pb || e instanceof Head || e instanceof P || e instanceof Lg || e instanceof Table;
    }

    public static TextExtracter create(String xml) throws JAXBException {
        TEI2 tei = load(xml);
        return new TextExtracter(tei);
    }

    /**
     * Extracts paragraphs, headings and page breaks from the TEI tree,
     * regardless of the div/chapter structure.
     * @return  a list of paragraphs, headings a page breaks
     */
    List<Object> topParagraphsAndHeadings() {
        Body b = (Body) tei.getText().getAnchorsAndGapsAndFigures().get(0);
        return b.getArgumentsAndBylinesAndDatelines().stream()
                .map(x -> (Div) x)
                .map(d -> d.getArgumentsAndBylinesAndDatelines())
                .flatMap(x -> x.stream())
                .filter(e -> isToExtract(e))
                .collect(Collectors.toList());
    }

    /**
     * Reformats a list of paragraphs by pulling out embedded page breaks.
     * Embedded page breaks are appended after the paragraphs they are extracted from.
     * If multiple page breaks are embedded in the same paragraph, only the last page break is appended.
     * The embedded page breaks are kept within the paragraphs (they are duplicated, not removed).
     * @param paragraphs    a list of paragraphs, headings and page breaks
     * @return  a list of reformatted paragraphs, with isolated page breaks
     */
    List<Object> shiftPageBreaks(List<Object> paragraphs) {
        List<Object> shifted = new LinkedList<>();
        for (Object p: paragraphs) {
            shifted.add(p);
            if (p instanceof P) {
                List<Object> content = ((P) p).getContent();
                List<Pb> pageBreaks = new LinkedList<>();
                if (content.stream().anyMatch(x -> x instanceof Pb)) {
                    for (Object c : content) {
                        if (c instanceof Pb)
                            pageBreaks.add((Pb) c);
                    }
                    shifted.add(((LinkedList<Pb>) pageBreaks).getLast());
                }
            }
        }
        return shifted;
    }



    /**
     * Creates pages from a list of paragraphs and headings placed between page breaks.
     * @param paragraphs    a list of paragraphs, headings and page breaks
     * @return  a list of <code>Page</code> objects
     */
    List<Page> splitPages(List<Object> paragraphs) {
        List<Object> page = new LinkedList<>();
        List<Page> pages = new LinkedList<>();
        for (Object p: paragraphs) {
            if (p instanceof Pb) {
                if (! page.isEmpty()) {
                    pages.add(new Page(page));
                    page = new LinkedList<>();
                }
            }
            page.add(p);
        }
        if (! page.isEmpty())
            pages.add(new Page(page));
        return pages;
    }

    public List<Page> formatPages(boolean completeBrokenParagraphs) {
        List<Object> paragraphs = topParagraphsAndHeadings();
        if (completeBrokenParagraphs)
            paragraphs = shiftPageBreaks(paragraphs);
        return splitPages(paragraphs);
    }

    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "textExtracter", options);
        System.exit(1);
    }

    public static void main(String[] args) throws IOException {

        Options options = new Options();
        options.addOption("p", false, "separate page files");
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
                        .forEach(f -> process(f, cmd.hasOption('p'), outdir)); }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            usage(options);
        }
    }

    private static void process(Path file, boolean paginate, String outdir)  {
        String fileName = file.getFileName().toString().replaceAll("\\.xml", "");
        outdir = prepareOutputDir(paginate, outdir, fileName);

        List<Page> pages = getPages(file.toString());

        try {
            if (! paginate) {
                FileWriter fw = new FileWriter(new File(outdir + fileName + ".txt"));
                for (Page p: pages)
                    fw.write(p.getContent() + "\n");
            } else {
                for (Page p : pages)
                    p.write(outdir + fileName + "_p");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Page> getPages(String fileName) {
        TextExtracter tex = null;
        try {
            tex = TextExtracter.create(fileName);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return tex.formatPages(true);
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

    private static void fileToDir(String inputFile, String outDir) {
        try {
            if (! outDir.endsWith("/"))
                outDir += '/';
            Path path = Paths.get(outDir);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File in = new File(inputFile);
            String baseName = in.getName().replaceAll("\\.xml", "");
            TextExtracter tex = TextExtracter.create(inputFile);
            List<Page> pages = tex.formatPages(true);
            for (Page p: pages) {
                try {
                    p.write(outDir + baseName + "_p");
                } catch (IOException e) {
                    System.out.println("page: " + p.getPageNumber());
                    e.printStackTrace();
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static TEI2 load(String xml) throws JAXBException {
        File file = new File(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(TEI2.class);
        System.setProperty("javax.xml.accessExternalDTD", "all");
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (TEI2) jaxbUnmarshaller.unmarshal(file);
    }


}
