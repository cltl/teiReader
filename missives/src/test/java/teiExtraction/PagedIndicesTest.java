package teiExtraction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PagedIndicesTest {
    static String testDir = "src/test/resources/";
    static String testFile = testDir + "index_persons.xml";
    static String out = "INDEX VAN PERSOONSNAMEN\n" +
            "Achmad, Sayid, Koning van Kirman, 124, 211, 213\n" +
            "Adipati, Pangéran (Palembang), 78, 136\n" +
            "Adipati Anom, Pangéran (Cheribon), 218\n" +
            "Adipati Anom, Pangéran (Djambi), 199, 200, 226\n" +
            "Adipati Anom, Pangéran (Palembang), Pangéran Mangkubumi, 4, 14, 34, 35, 47, 60, 111, 122, 135, 137, 156, 182, 183, 226, 236\n" +
            "Alber(t)s, Joannes Coenradus, 66\n" +
            "Anom, Sultan, 4, 14, 34, 35, 47, 60, 111, 122, 135, 137, 156, 182, 183, 226, 236\n" +
            "Baning, Entjik, 46\n";
    static String out2 = "INDEX VAN PERSOONSNAMEN\n" +
            "Achmad, Sayid, Koning van Kirman, 124, 211, 213\n" +
            "Adipati, Pangéran (Palembang), 78, 136\n" +
            "Adipati Anom, Pangéran (Cheribon), 218\n" +
            "Adipati Anom, Pangéran (Djambi), 199, 200, 226\n" +
            "Adipati Anom, Pangéran (Palembang), Pangéran Mangkubumi, 4, 14, 34, 35, 47, 60, 111, 122, 135, 137, 156, 182, 183, 226, 236\n" +
            "Alber(t)s, Joannes Coenradus, 66\n" +
            "Anom, Sultan, zie Adipati Anom, Pangéran (Palembang)\n" +
            "Baning, Entjik, 46\n";

    @Test
    public void testIndices() {
        TextExtracter te = new TextExtracter();
        assertEquals(te.extract(testFile).content(), out2);
    }



}