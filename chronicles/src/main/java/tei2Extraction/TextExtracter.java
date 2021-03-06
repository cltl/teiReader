package tei2Extraction;

import baseExtraction.ATextExtracter;
import textTree.IText;
import xjc.tei2.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class TextExtracter extends ATextExtracter {

    public TextExtracter() {
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
            JAXBContext jaxbContext = JAXBContext.newInstance(TEI2.class);
            System.setProperty("javax.xml.accessExternalDTD", "all");
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            TEI2 tei = (TEI2) jaxbUnmarshaller.unmarshal(file);
            iText = TextTreeFactory.create(tei);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return iText;
    }

    @Override
    public IText extract(String file) {
        IText textTree = load(file);
        return handler.process(textTree);
    }


}
