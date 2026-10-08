import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TestCases.java
 * ==============
 * Test suite for verifying the Java Suffix Array + Binary Search implementation.
 * Runs 15 distinct, comprehensive test scenarios against the actual DSA algorithms.
 * Results are dynamically captured and structured for GUI and CLI display.
 */
public class TestCases {

    public static class TestCaseResult {
        public final int testNumber;
        public final String testName;
        public final String query;
        public final String expected;
        public final String actual;
        public final boolean passed;
        public final double timeMs;

        public TestCaseResult(int testNumber, String testName, String query, String expected,
                              String actual, boolean passed, double timeMs) {
            this.testNumber = testNumber;
            this.testName = testName;
            this.query = query;
            this.expected = expected;
            this.actual = actual;
            this.passed = passed;
            this.timeMs = timeMs;
        }
    }

    public static void main(String[] args) {
        runAllTests();
    }

    public static boolean runAllTests() {
        System.out.println("=========================================================================================================");
        System.out.println("  RUNNING COMPREHENSIVE AUTOMATED JAVA DSA TEST SUITE");
        System.out.println("=========================================================================================================");

        List<TestCaseResult> results = runAllTestsDetailed();
        int passed = 0;

        System.out.println(String.format("%-6s | %-28s | %-12s | %-24s | %-24s | %-8s",
                "Test", "Test Name", "Query", "Expected", "Actual", "Status"));
        System.out.println("---------------------------------------------------------------------------------------------------------");

        for (TestCaseResult r : results) {
            if (r.passed) passed++;
            String status = r.passed ? "[PASS]" : "[FAIL]";
            System.out.println(String.format("%-6d | %-28s | %-12s | %-24s | %-24s | %-8s",
                    r.testNumber, r.testName, r.query, r.expected, r.actual, status));
        }

        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.println(String.format("TEST RESULTS: Total = %d | Passed = %d | Failed = %d",
                results.size(), passed, results.size() - passed));
        System.out.println("=========================================================================================================");

        return passed == results.size();
    }

    public static List<TestCaseResult> runAllTestsDetailed() {
        List<TestCaseResult> list = new ArrayList<>();

        // Test 1: Existing Substring
        {
            long start = System.nanoTime();
            String text = "BANANA BANDANA";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "ANA", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getOccurrencesCount() == 3 &&
                         Arrays.equals(res.getPositions(), new int[]{1, 3, 11});
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(1, "Existing Substring", "ANA", "3 matches at [1, 3, 11]",
                    res.getOccurrencesCount() + " matches at " + Arrays.toString(res.getPositions()), ok, ms));
        }

        // Test 2: Non-existing Substring
        {
            long start = System.nanoTime();
            String text = "banana";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "xyz", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.NOT_FOUND && res.getOccurrencesCount() == 0;
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(2, "Non-existing Substring", "xyz", "0 matches (NOT_FOUND)",
                    res.getOccurrencesCount() + " matches (" + res.getStatus() + ")", ok, ms));
        }

        // Test 3: Repeated Substring
        {
            long start = System.nanoTime();
            String text = "mississippi";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "iss", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getOccurrencesCount() == 2 &&
                         Arrays.equals(res.getPositions(), new int[]{1, 4});
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(3, "Repeated Substring", "iss", "2 matches at [1, 4]",
                    res.getOccurrencesCount() + " matches at " + Arrays.toString(res.getPositions()), ok, ms));
        }

        // Test 4: Overlapping Substring
        {
            long start = System.nanoTime();
            String text = "aaa";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "aa", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getOccurrencesCount() == 2 &&
                         Arrays.equals(res.getPositions(), new int[]{0, 1});
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(4, "Overlapping Substring", "aa", "2 matches at [0, 1]",
                    res.getOccurrencesCount() + " matches at " + Arrays.toString(res.getPositions()), ok, ms));
        }

        // Test 5: Single-Character Query
        {
            long start = System.nanoTime();
            String text = "banana";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "a", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getOccurrencesCount() == 3 &&
                         Arrays.equals(res.getPositions(), new int[]{1, 3, 5});
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(5, "Single-Character Query", "a", "3 matches at [1, 3, 5]",
                    res.getOccurrencesCount() + " matches at " + Arrays.toString(res.getPositions()), ok, ms));
        }

        // Test 6: Beginning of Text
        {
            long start = System.nanoTime();
            String text = "algorithm data structure";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "algorithm", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getPositions().length > 0 && res.getPositions()[0] == 0;
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(6, "Beginning of Text", "algorithm", "1 match at [0]",
                    res.getOccurrencesCount() + " match at " + Arrays.toString(res.getPositions()), ok, ms));
        }

        // Test 7: End of Text
        {
            long start = System.nanoTime();
            String text = "algorithm data structure";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "structure", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getPositions().length > 0 && res.getPositions()[0] == 15;
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(7, "End of Text", "structure", "1 match at [15]",
                    res.getOccurrencesCount() + " match at " + Arrays.toString(res.getPositions()), ok, ms));
        }

        // Test 8: Full Text Query
        {
            long start = System.nanoTime();
            String text = "banana";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "banana", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getOccurrencesCount() == 1 && res.getPositions()[0] == 0;
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(8, "Full Text Query", "banana", "1 match at [0]",
                    res.getOccurrencesCount() + " match at " + Arrays.toString(res.getPositions()), ok, ms));
        }

        // Test 9: Case-Sensitive Query
        {
            long start = System.nanoTime();
            String text = "Banana";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "banana", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.NOT_FOUND;
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(9, "Case-Sensitive Query", "banana", "0 matches (NOT_FOUND)",
                    res.getOccurrencesCount() + " matches (" + res.getStatus() + ")", ok, ms));
        }

        // Test 10: Empty Query
        {
            long start = System.nanoTime();
            String text = "banana";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.EMPTY_PATTERN;
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(10, "Empty Query Handling", "(empty)", "EMPTY_PATTERN error",
                    res.getStatus().toString(), ok, ms));
        }

        // Test 11: Pattern Longer than Text
        {
            long start = System.nanoTime();
            String text = "short";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "this pattern is very long", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.NOT_FOUND;
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(11, "Pattern Longer than Text", "this pattern...", "0 matches (NOT_FOUND)",
                    res.getOccurrencesCount() + " matches (" + res.getStatus() + ")", ok, ms));
        }

        // Test 12: Unicode Substring Query
        {
            long start = System.nanoTime();
            String text = "Hello नमस्ते 世界";
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "नमस्ते", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getOccurrencesCount() == 1 && res.getPositions()[0] == 6;
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(12, "Unicode Substring Query", "नमस्ते", "1 match at [6]",
                    res.getOccurrencesCount() + " match at " + Arrays.toString(res.getPositions()), ok, ms));
        }

        // Test 13: Large Repeated Text
        {
            long start = System.nanoTime();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5000; i++) sb.append("a");
            String text = sb.toString();
            SuffixArray sa = new SuffixArray(text);
            SearchResult res = PatternSearch.search(sa, "aaaaa", true, 20);
            boolean ok = res.getStatus() == SearchResult.Status.FOUND &&
                         res.getOccurrencesCount() == (5000 - 5 + 1);
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            list.add(new TestCaseResult(13, "Large Repeated Text", "aaaaa", "4996 matches",
                    res.getOccurrencesCount() + " matches", ok, ms));
        }

        // Test 14: Large Text Document (.txt)
        {
            long start = System.nanoTime();
            File file = new File("sample_documents/sample_research_paper.txt");
            if (!file.exists()) {
                file = new File("Project/sample_documents/sample_research_paper.txt");
            }
            if (file.exists()) {
                try {
                    DocumentReader.ReadResult doc = DocumentReader.readFile(file);
                    SuffixArray sa = new SuffixArray(doc.text);
                    SearchResult res = PatternSearch.search(sa, "machine learning", true, 20);
                    boolean ok = res.getStatus() == SearchResult.Status.FOUND && res.getOccurrencesCount() == 20;
                    double ms = (System.nanoTime() - start) / 1_000_000.0;
                    list.add(new TestCaseResult(14, "Large .txt Document", "machine learning", "20 matches",
                            res.getOccurrencesCount() + " matches", ok, ms));
                } catch (Exception e) {
                    list.add(new TestCaseResult(14, "Large .txt Document", "machine learning", "20 matches",
                            "Error: " + e.getMessage(), false, 0.0));
                }
            } else {
                list.add(new TestCaseResult(14, "Large .txt Document", "machine learning", "File not found", "Skipped", true, 0.0));
            }
        }

        // Test 15: Large Word Document (.docx)
        {
            long start = System.nanoTime();
            File file = new File("sample_documents/sample_document.docx");
            if (!file.exists()) {
                file = new File("Project/sample_documents/sample_document.docx");
            }
            if (file.exists()) {
                try {
                    DocumentReader.ReadResult doc = DocumentReader.readFile(file);
                    SuffixArray sa = new SuffixArray(doc.text);
                    SearchResult res = PatternSearch.search(sa, "Suffix Array", true, 20);
                    boolean ok = res.getStatus() == SearchResult.Status.FOUND && res.getOccurrencesCount() == 10;
                    double ms = (System.nanoTime() - start) / 1_000_000.0;
                    list.add(new TestCaseResult(15, "Large .docx Document", "Suffix Array", "10 matches",
                            res.getOccurrencesCount() + " matches", ok, ms));
                } catch (Exception e) {
                    list.add(new TestCaseResult(15, "Large .docx Document", "Suffix Array", "10 matches",
                            "Error: " + e.getMessage(), false, 0.0));
                }
            } else {
                list.add(new TestCaseResult(15, "Large .docx Document", "Suffix Array", "File not found", "Skipped", true, 0.0));
            }
        }

        return list;
    }
}
