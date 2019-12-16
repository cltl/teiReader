package tei_text;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xjc.tei2.Lg;
import xjc.tei2.Note;
import xjc.tei2.P;
import xjc.tei2.Pb;

import javax.xml.bind.JAXBException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageTest {
    protected static List<Page> pages;
    static String testDir = "src/test/resources/";
    static String testFile = testDir + "diary.xml";
    int startPage = 0;    // TODO update once cf can be processed


    @BeforeAll
    public static void create()  {
        TextExtracter tex = null;
        try {
            tex = TextExtracter.create(testFile);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        pages = tex.formatPages(true);
    }

    @Test
    public void testCFNotRecognized() {
        Page pageI = pages.get(0);
        // TODO cf elements are not recognized under the teixlite.xsd binding
        assertNotEquals(pageI.getPageNumber(), "I");
        assertNotEquals(pageI.getContent(), "DAGBOEK\n" +
                "van\n" +
                "CORNELIS EN PHILIP VAN CAMPENE.");
    }

    @Test
    void testPageAndLineBreak() {
        Page pageV = pages.get(startPage);
        assertEquals(pageV.getElts().size(), 3);

        String content = pageV.getContent();
        // line break after heading and paragraphs
        assertTrue(content.startsWith("[Inleiding]\n\nToen"));
        assertTrue(content.contains("aangehaald geworden.\nEene"));

        // page break transition
        assertTrue(content.contains("vonden. Sloegen"));
    }

    @Test
    void testNote() {
        String literalContent = "<p>Eenige tot heden (...) te Merelbeke een leen had<note n=\"(1)\" place=\"foot\">"
                + "‘De G<hi rend=\"sc\">hijselbrecht van</hi> C<hi rend=\"sc\">amps</hi>, (...) situé en la parroiche"
                + " de Merlebecque, (...).’" +
                "<lb/>" +
                "(<hi rend=\"i\">Compte du bailli du Vieux-Bourg,</hi> 1604.)</note>.</p>";
        String expectedContent = "Verklaren wij ons.\nEenige tot heden (...) te Merelbeke een leen had(1).\n\n"
                + "(1) ‘De Ghijselbrecht van Camps, (...) situé en la parroiche de Merlebecque, (...).’\n\n" +
                "(Compte du bailli du Vieux-Bourg, 1604.)";
        Page pageVII = pages.get(startPage + 2);
        assertEquals(pageVII.getElts().size(), 2);
        String content = pageVII.getContent();
        assertEquals(content, expectedContent);
    }

    @Test
    void testDoubleInTextNoteWithPageBreak() {
        Page page1 = pages.get(startPage + 4);
        assertEquals(page1.getElts().size(), 2);
        assertTrue(page1.getElts().get(1) instanceof P);
        P paragraph = (P) page1.getElts().get(1);
        List<Object> content = paragraph.getContent();
        String textPart1 = "Daghelicxsche experiencie leert ons, (...), schijen";
        String textPart2 = " ende ontsticke cappen, (...) den dienst Gods\n";
        String textPart2b = " ende ontsticke cappen, (...) den dienst Gods ";
        String textPart3 = "mede vermeerdert, (...) dese feniene";
        String textPart4 = " besmedt zijn, (...) zes\nende tsesticht, (...) ziekte.";
        String textPart4b = " besmedt zijn, (...) zesende tsesticht, (...) ziekte.";
        String note1 = "Schijen, voor schenden.";
        String note2 = "Feniene, venijn.";
        assertEquals(content.get(0), textPart1);
        assertTrue(content.get(1) instanceof Note);
        assertEquals(content.get(2), textPart2);
        assertTrue(content.get(3) instanceof Pb);
        assertEquals(content.get(4), textPart3);
        assertTrue(content.get(5) instanceof Note);
        assertEquals(content.get(6), textPart4);

        StringBuilder finalStr = new StringBuilder();
        finalStr.append("Epistele.\n\nEersame, (...), Saluut.\n\n")
                .append(textPart1).append("(1)")
                .append(textPart2b).append(textPart3).append("(1)")
                .append(textPart4b)
                .append("\n\n(1) ").append(note1)
                .append("\n\n(1) ").append(note2);

        assertEquals(page1.getContent(), finalStr.toString());
    }

    @Test
    void testLgElt() {
        Page page7 = pages.get(startPage + 6);
        assertEquals(page7.getElts().size(), 2);
        assertTrue(page7.getElts().get(1) instanceof Lg);
        String content = page7.getContent();
        String expected = "[Collatie.] Item de heeren van der stadt, scepenen, hooftmannen, notable waeren al scepenhuuse als heden vergadert, om met elckanderen te advieseren.\n" +
                "\n[POEM]\n" +
                "Hadden wij beghonnen an cooplieden goedt,[Fameuse briefkins.]\n" +
                "Ende der kercken beelden laeten met vreden,\n" +
                "Ons handen ghewasschen in papens bloedt,\n" +
                "Zoo waeren wij heeren van dorpen en steden.\n\n";
        assertEquals(content, expected);
    }
}