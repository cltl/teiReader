package tei_text;

import xjc.tei2.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected two parameters: [infile] [outDir]. Found " + args.length);
            System.exit(1);
        }

        try {
            String inputFile = args[0];
            String outDir = args[1];
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
