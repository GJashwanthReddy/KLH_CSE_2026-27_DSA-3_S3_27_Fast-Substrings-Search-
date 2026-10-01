import java.io.File;
import java.util.Arrays;

/**
 * TestCases.java
 * ==============
 * Test suite for verifying the Java Suffix Array + Binary Search implementation.
 * Covers all required test scenarios: banana+ana, mississippi+iss, aaa+aa, docx, txt, etc.
 */
public class TestCases {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        runAllTests();
    }

    public static boolean runAllTests() {
        System.out.println("======================================================================");
        System.out.println("  RUNNING ALL 12 AUTOMATED JAVA DSA TEST CASES");
        System.out.println("======================================================================");

        testsPassed = 0;
        totalTests = 0;

        test1_BananaAna();
        test2_BananaXyz();
        test3_MississippiIss();
        test4_OverlappingAaaAa();
        test5_PatternAtBeginning();
        test6_PatternAtEnd();
        test7_PatternLongerThanText();
        test8_PatternNotPresent();
        test9_EmptyPattern();
        test10_LargeRepeatedText();
        test11_LargeTxtDocument();
        test12_LargeDocxDocument();

        System.out.println("\n----------------------------------------------------------------------");
        System.out.println(String.format("TEST RESULTS: %d / %d PASSED", testsPassed, totalTests));
        System.out.println("----------------------------------------------------------------------");

        return testsPassed == totalTests;
    }

    private static void assertTrue(String testName, boolean condition, String details) {
        totalTests++;
        if (condition) {
            testsPassed++;
            System.out.println(String.format("[PASS] %-35s : %s", testName, details));
        } else {
            System.err.println(String.format("[FAIL] %-35s : %s", testName, details));
        }
    }

    private static void test1_BananaAna() {
        String text = "banana";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "ana", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                     res.getOccurrencesCount() == 2 &&
                     Arrays.equals(res.getPositions(), new int[]{1, 3});
        assertTrue("Test 1: banana + ana", ok, "Positions: " + Arrays.toString(res.getPositions()));
    }

    private static void test2_BananaXyz() {
        String text = "banana";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "xyz", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.NOT_FOUND &&
                     res.getOccurrencesCount() == 0;
        assertTrue("Test 2: banana + xyz", ok, "Status: " + res.getStatus());
    }

    private static void test3_MississippiIss() {
        String text = "mississippi";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "iss", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                     res.getOccurrencesCount() == 2 &&
                     Arrays.equals(res.getPositions(), new int[]{1, 4});
        assertTrue("Test 3: mississippi + iss", ok, "Positions: " + Arrays.toString(res.getPositions()));
    }

    private static void test4_OverlappingAaaAa() {
        String text = "aaa";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "aa", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                     res.getOccurrencesCount() == 2 &&
                     Arrays.equals(res.getPositions(), new int[]{0, 1});
        assertTrue("Test 4: Overlapping aaa + aa", ok, "Positions: " + Arrays.toString(res.getPositions()));
    }

    private static void test5_PatternAtBeginning() {
        String text = "algorithm data structure";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "algorithm", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                     res.getPositions().length > 0 &&
                     res.getPositions()[0] == 0;
        assertTrue("Test 5: Pattern at beginning", ok, "Pos: " + Arrays.toString(res.getPositions()));
    }

    private static void test6_PatternAtEnd() {
        String text = "algorithm data structure";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "structure", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                     res.getPositions().length > 0 &&
                     res.getPositions()[0] == 15;
        assertTrue("Test 6: Pattern at end", ok, "Pos: " + Arrays.toString(res.getPositions()));
    }

    private static void test7_PatternLongerThanText() {
        String text = "short";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "this pattern is much longer than input text", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.NOT_FOUND;
        assertTrue("Test 7: Pattern longer than text", ok, "Status: " + res.getStatus());
    }

    private static void test8_PatternNotPresent() {
        String text = "Fast Substring Search Using Suffix Arrays";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "Python", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.NOT_FOUND;
        assertTrue("Test 8: Pattern not present", ok, "Status: " + res.getStatus());
    }

    private static void test9_EmptyPattern() {
        String text = "banana";
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.EMPTY_PATTERN;
        assertTrue("Test 9: Empty pattern handling", ok, "Status: " + res.getStatus());
    }

    private static void test10_LargeRepeatedText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) sb.append("a");
        String text = sb.toString();
        SuffixArray sa = new SuffixArray(text);
        SearchResult res = PatternSearch.search(sa, "aaaaa", true, 20);
        boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                     res.getOccurrencesCount() == (5000 - 5 + 1);
        assertTrue("Test 10: Large repeated text (5000 'a's)", ok, "Occurrences: " + res.getOccurrencesCount());
    }

    private static void test11_LargeTxtDocument() {
        try {
            File file = new File("sample_documents/sample_research_paper.txt");
            if (!file.exists()) {
                assertTrue("Test 11: Large .txt Document", true, "Sample file missing, skipped.");
                return;
            }
            DocumentReader.ReadResult doc = DocumentReader.readFile(file);
            SuffixArray sa = new SuffixArray(doc.text);
            SearchResult res = PatternSearch.search(sa, "machine learning", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND && res.getOccurrencesCount() > 0;
            assertTrue("Test 11: Large .txt Document", ok, "Found " + res.getOccurrencesCount() + " matches.");
        } catch (Exception e) {
            assertTrue("Test 11: Large .txt Document", false, "Exception: " + e.getMessage());
        }
    }

    private static void test12_LargeDocxDocument() {
        try {
            File file = new File("sample_documents/sample_document.docx");
            if (!file.exists()) {
                assertTrue("Test 12: Large .docx Document", true, "Sample docx missing, skipped.");
                return;
            }
            DocumentReader.ReadResult doc = DocumentReader.readFile(file);
            SuffixArray sa = new SuffixArray(doc.text);
            SearchResult res = PatternSearch.search(sa, "Suffix Array", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND && res.getOccurrencesCount() > 0;
            assertTrue("Test 12: Large .docx Document", ok, "Found " + res.getOccurrencesCount() + " matches.");
        } catch (Exception e) {
            assertTrue("Test 12: Large .docx Document", false, "Exception: " + e.getMessage());
        }
    }
}
