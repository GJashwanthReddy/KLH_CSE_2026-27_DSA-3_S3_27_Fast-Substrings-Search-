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
 *   java -cp bin Main --test    (Runs all 12 automated DSA test cases)
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
            }
        }

        // Default: Launch Java Swing GUI
        System.out.println("======================================================================");
        System.out.println("  FAST SUBSTRINGS SEARCH USING SUFFIX ARRAYS & BINARY SEARCH (JAVA)");
        System.out.println("  B.Tech Data Structures & Algorithms (DSA) Project");
        System.out.println("======================================================================");
        System.out.println("[*] Launching Java Swing GUI Window...");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SwingApp app = new SwingApp();
                    app.setVisible(true);
                } catch (Exception e) {
                    System.err.println("Failed to start Swing GUI: " + e.getMessage());
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
        System.out.println("  FAST SUBSTRINGS SEARCH USING SUFFIX ARRAYS (JAVA CLI)");
        System.out.println("======================================================================");

        while (true) {
            System.out.println("\nMAIN MENU:");
            System.out.println("1. Enter Manual Text");
            System.out.println("2. Load Document File (.txt / .docx)");
            System.out.println("3. Search Pattern (Suffix Array + Binary Search)");
            System.out.println("4. View Suffix Array Table (First 30 Entries)");
            System.out.println("5. Run All 12 Automated Test Cases");
            System.out.println("6. Exit");
            System.out.print("\nEnter choice (1-6): ");

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
                System.out.println("[+] Text Loaded. Characters: " + meta.characterCount + " | Words: " + meta.wordCount);
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
                    System.out.println("    Characters: " + meta.characterCount + " | Words: " + meta.wordCount);
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
                System.out.println("\n==================================================");
                System.out.println("SUFFIX ARRAY TABLE (First 30 Entries)");
                System.out.println("==================================================");
                System.out.println(String.format("%-10s | %-14s | %-40s", "SA Index", "Text Position", "Suffix Preview"));
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

            } else if (choice.equals("5")) {
                TestCases.runAllTests();

            } else if (choice.equals("6")) {
                System.out.println("Exiting. Good luck with your project review!");
                break;
            } else {
                System.out.println("Invalid choice. Select 1-6.");
            }
        }
    }

    private static void printHelp() {
        System.out.println("Fast Substrings Search Using Suffix Arrays (Java Implementation)");
        System.out.println("Usage:");
        System.out.println("  java -cp bin Main         (Launches Standalone Swing GUI)");
        System.out.println("  java -cp bin Main --cli   (Launches Interactive CLI)");
        System.out.println("  java -cp bin Main --test  (Runs all 12 automated test cases)");
    }
}
