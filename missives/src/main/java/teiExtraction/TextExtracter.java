package teiExtraction;

import baseExtraction.ATextExtracter;
import textTree.IText;
import xjc.tei.TEI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class TextExtracter extends ATextExtracter {

    TextExtracter() {
        super();
    }

    public static void main(String[] args) {
        TextExtracter te = new TextExtracter();
        te.process(args);
    }

    public IText load(String xml) {
        File file = new File(xml);
        IText iText = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TEI.class);
            System.setProperty("javax.xml.accessExternalDTD", "all");
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            TEI tei = (TEI) jaxbUnmarshaller.unmarshal(file);
            iText = TextTreeFactory.create(tei);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return iText;
    }


}
