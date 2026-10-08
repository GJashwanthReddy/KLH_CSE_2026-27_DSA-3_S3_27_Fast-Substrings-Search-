import javax.swing.SwingUtilities;
import java.util.List;

/**
 * Main.java
 * =========
 * Main entry point for the Fast Substrings Search Java Application.
 * 
 * Usage:
 *   java -cp bin Main           (Launches Standalone Swing GUI)
 *   java -cp bin Main --cli     (Launches Command-Line Terminal Interface)
 *   java -cp bin Main --test    (Runs all automated DSA test cases)
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            if (arg.equals("--cli")) {
                runCliInterface();
                return;
            } else if (arg.equals("--test")) {
                TestCases.main(args);
                return;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printHelp();
                return;
            } else if (args.length >= 2) {
                runDirectSearch(args[0], args[1]);
                return;
            }
        }

        // Default: Launch Java Swing GUI
        System.out.println("======================================================================");
        System.out.println("  FAST SUBSTRINGS SEARCH USING SUFFIX ARRAYS");
        System.out.println("  Java Implementation • Suffix Array + Binary Search");
        System.out.println("======================================================================");
        System.out.println("[*] Launching Application GUI...");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SwingApp app = new SwingApp();
                    app.setVisible(true);
                } catch (Exception e) {
                    System.err.println("Failed to start GUI: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private static void runCliInterface() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        SuffixArray sa = null;
        String text = "";
        DocumentReader.DocumentMetadata meta = null;

        System.out.println("======================================================================");
        System.out.println("  FAST SUBSTRINGS SEARCH USING SUFFIX ARRAYS");
        System.out.println("  Java Implementation • Suffix Array + Binary Search");
        System.out.println("======================================================================");

        while (true) {
            System.out.println("\nMAIN MENU:");
            System.out.println("1. Enter Manual Text");
            System.out.println("2. Load Document File (.txt / .docx)");
            System.out.println("3. Search Pattern (Suffix Array + Binary Search)");
            System.out.println("4. Performance Comparison (Naive Search vs Suffix Array)");
            System.out.println("5. View Suffix Array Table (First 30 Entries)");
            System.out.println("6. Run Automated Test Suite");
            System.out.println("7. Exit");
            System.out.print("\nEnter choice (1-7): ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("\nEnter text: ");
                text = scanner.nextLine();
                if (text.isEmpty()) {
                    System.out.println("[-] Text cannot be empty.");
                    continue;
                }
                DocumentReader.ReadResult doc = DocumentReader.readManualText(text, "Manual Text");
                meta = doc.metadata;
                sa = new SuffixArray(doc.text);
                System.out.println("[+] Text Loaded. Characters: " + meta.characterCount + " | Words: " + meta.wordCount + " | Lines: " + meta.lineCount);
                System.out.println("    SA Build Time: " + PerformanceMonitor.formatDurationMs(sa.getBuildTimeMs()));

            } else if (choice.equals("2")) {
                System.out.print("\nEnter file path (.txt or .docx): ");
                String path = scanner.nextLine().trim().replace("\"", "").replace("'", "");
                java.io.File file = new java.io.File(path);
                try {
                    DocumentReader.ReadResult doc = DocumentReader.readFile(file);
                    text = doc.text;
                    meta = doc.metadata;
                    sa = new SuffixArray(doc.text);
                    System.out.println("[+] Document Loaded Successfully!");
                    System.out.println("    File: " + meta.fileName + " (" + meta.fileType + ")");
                    System.out.println("    Size: " + meta.fileSizeFormatted);
                    System.out.println("    Characters: " + meta.characterCount + " | Words: " + meta.wordCount + " | Lines: " + meta.lineCount);
                    System.out.println("    SA Build Time: " + PerformanceMonitor.formatDurationMs(sa.getBuildTimeMs()));
                } catch (Exception e) {
                    System.out.println("[-] Error loading file: " + e.getMessage());
                }

            } else if (choice.equals("3")) {
                if (sa == null) {
                    System.out.println("[-] No document currently loaded.");
                    continue;
                }
                System.out.print("\nEnter Search Pattern: ");
                String pattern = scanner.nextLine();
                System.out.print("Case sensitive? (y/n, default=y): ");
                boolean caseSens = !scanner.nextLine().trim().equalsIgnoreCase("n");

                SearchResult res = PatternSearch.search(sa, pattern, caseSens, 40);
                System.out.println("\n==================================================");
                System.out.println("SEARCH RESULTS");
                System.out.println("==================================================");
                System.out.println("Status: " + res.getStatus());
                System.out.println("Occurrences: " + res.getOccurrencesCount());
                System.out.println("Search Time: " + PerformanceMonitor.formatDurationMs(res.getSearchTimeMs()));
                System.out.println("Positions: " + res.getPositionsFormatted());

                if (res.getStatus() == SearchResult.Status.FOUND) {
                    System.out.println("\nMATCHES:");
                    List<SearchResult.MatchItem> matches = res.getMatches();
                    for (int i = 0; i < matches.size(); i++) {
                        SearchResult.MatchItem item = matches.get(i);
                        System.out.println(String.format("Match #%d: Position %d (Line %d)", i + 1, item.position, item.lineNumber));
                        System.out.println("  Snippet: " + item.plainSnippet);
                    }
                }

            } else if (choice.equals("4")) {
                if (sa == null) {
                    System.out.println("[-] No document loaded.");
                    continue;
                }
                System.out.print("\nEnter Search Pattern to Compare: ");
                String pattern = scanner.nextLine();
                System.out.print("Case sensitive? (y/n, default=y): ");
                boolean caseSens = !scanner.nextLine().trim().equalsIgnoreCase("n");

                PatternSearch.PerformanceComparison comp = PatternSearch.compareSearch(sa, pattern, caseSens, 40);
                System.out.println("\n================================================================================");
                System.out.println("PERFORMANCE COMPARISON (Same Text & Query)");
                System.out.println("================================================================================");
                System.out.println(String.format("%-30s | %-15s | %-12s | %-15s", "Algorithm", "Search Time", "Occurrences", "Complexity"));
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println(String.format("%-30s | %-15s | %-12d | %-15s", "Naive String Search",
                        PerformanceMonitor.formatDurationMs(comp.naiveResult.searchTimeMs), comp.naiveResult.occurrencesCount, "O(N * M)"));
                System.out.println(String.format("%-30s | %-15s | %-12d | %-15s", "Suffix Array + Binary Search",
                        PerformanceMonitor.formatDurationMs(comp.saResult.getSearchTimeMs()), comp.saResult.getOccurrencesCount(), "O(M log N)"));
                System.out.println("--------------------------------------------------------------------------------");
                if (comp.speedupFactor > 1.0) {
                    System.out.println(String.format("[*] Suffix Array search was %.2fx faster than Naive search.", comp.speedupFactor));
                }

            } else if (choice.equals("5")) {
                if (sa == null) {
                    System.out.println("[-] No document loaded.");
                    continue;
                }
                System.out.println("\n==================================================");
                System.out.println("SUFFIX ARRAY TABLE (First 30 Entries)");
                System.out.println("==================================================");
                System.out.println(String.format("%-10s | %-14s | %-40s", "SA Index", "Text Position", "Suffix"));
                System.out.println("--------------------------------------------------");
                int[] saArr = sa.getSa();
                int limit = Math.min(saArr.length, 30);
                for (int i = 0; i < limit; i++) {
                    int pos = saArr[i];
                    int end = Math.min(text.length(), pos + 40);
                    String prev = text.substring(pos, end);
                    if (end < text.length()) prev += "...";
                    System.out.println(String.format("%-10d | %-14d | %-40s", i, pos, prev));
                }

            } else if (choice.equals("6")) {
                TestCases.runAllTests();

            } else if (choice.equals("7")) {
                System.out.println("Exiting application.");
                break;
            } else {
                System.out.println("Invalid choice. Select 1-7.");
            }
        }
    }

    private static void runDirectSearch(String text, String query) {
        System.out.println("======================================================================");
        System.out.println("  FAST SUBSTRINGS SEARCH USING SUFFIX ARRAYS");
        System.out.println("  Java Implementation • Suffix Array + Binary Search");
        System.out.println("======================================================================");
        System.out.println("Text:    \"" + text + "\"");
        System.out.println("Query:   \"" + query + "\"");
        System.out.println("----------------------------------------------------------------------");

        long start = System.nanoTime();
        SuffixArray sa = new SuffixArray(text);
        double saTime = (System.nanoTime() - start) / 1_000_000.0;
        System.out.println(String.format("[+] Suffix Array constructed in %.3f ms for %d characters.", saTime, text.length()));

        SearchResult result = PatternSearch.search(sa, query, true, 20);
        System.out.println("Status:       " + result.getStatus());
        System.out.println("Occurrences:  " + result.getOccurrencesCount());
        System.out.println("Positions:    " + java.util.Arrays.toString(result.getPositions()));
        System.out.println(String.format("Search Time:  %.4f ms", result.getSearchTimeMs()));

        List<SearchResult.BinarySearchStep> steps = result.getBinarySearchSteps();
        if (steps != null && !steps.isEmpty()) {
            System.out.println("\n--- Binary Search Trace ---");
            System.out.println(String.format("%-5s | %-12s | %-5s | %-5s | %-5s | %-6s | %-16s | %-16s | %-20s",
                    "Step", "Phase", "Low", "High", "Mid", "Pos", "Compared Suffix", "Result", "Action"));
            System.out.println("---------------------------------------------------------------------------------------------------------");
            for (SearchResult.BinarySearchStep s : steps) {
                System.out.println(String.format("%-5d | %-12s | %-5d | %-5d | %-5d | %-6d | %-16s | %-16s | %-20s",
                        s.step, s.phase, s.low, s.high, s.mid, s.suffixPos, s.comparedSuffix, s.comparisonResult, s.action));
            }
        }
        System.out.println("======================================================================");
    }

    private static void printHelp() {
        System.out.println("Fast Substrings Search Using Suffix Arrays (Java Implementation)");
        System.out.println("Usage:");
        System.out.println("  java -cp bin Main                     (Launches Standalone Swing GUI)");
        System.out.println("  java -cp bin Main --cli               (Launches Interactive CLI)");
        System.out.println("  java -cp bin Main --test              (Runs all automated test cases)");
        System.out.println("  java -cp bin Main \"<text>\" \"<query>\"   (Runs direct search and prints trace)");
    }
}
