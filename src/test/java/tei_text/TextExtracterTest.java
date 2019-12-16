package tei_text;

import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextExtracterTest {

    String testDir = "src/test/resources/";
    String testFile = testDir + "diary.xml";

    @Test
    public void testExtraction() {

        try {
            TextExtracter tex = TextExtracter.create(testFile);
            List<Object> paragraphs = tex.topParagraphsAndHeadings();
            assertEquals(paragraphs.size(), 43);

            paragraphs = tex.shiftPageBreaks(paragraphs);
            assertEquals(paragraphs.size(), 52);

            List<Page> pages = tex.splitPages(paragraphs);
            assertEquals(pages.size(), 14);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}