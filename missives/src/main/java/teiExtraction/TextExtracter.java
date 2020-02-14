package teiExtraction;

import baseExtraction.ATextExtracter;
import textTree.IText;
import xjc.tei.TEI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class TextExtracter extends ATextExtracter {
    PagedIndices pi;

    TextExtracter() {
        super();
        this.pi = new PagedIndices();
    }

    public static void main(String[] args) {
        TextExtracter te = new TextExtracter();
        te.process(args);
    }

    public TEI load(String xml) {
        File file = new File(xml);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TEI.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            TEI tei = (TEI) jaxbUnmarshaller.unmarshal(file);
            return tei;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IText extract(String fileName) {
        TEI tei = load(fileName);
        if (isIndex(tei)) {
            IText iText = IndexTreeFactory.create(tei);
            return pi.process(iText);
        } else {
            IText iText = TextTreeFactory.create(tei);
            return handler.process(iText);
        }
    }

    public static boolean isIndex(TEI tei) {
        String title = (String) tei.getTeiHeader().getFileDesc().getTitleStmt().getTitles().get(0).getContent().get(0);
        return title.startsWith("Index");
    }

}
