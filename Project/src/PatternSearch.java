import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PatternSearch.java
 * ==================
 * Implements Binary Search over the Suffix Array to find all matching occurrences of a pattern.
 * Performs pure character-by-character comparison via SuffixArray.compareSuffixWithPattern().
 * NO String.contains(), String.indexOf(), or regex are used for the core search.
 * Also includes a genuine Naive Search implementation for empirical performance benchmarking.
 */
public class PatternSearch {

    public static class NaiveSearchResult {
        public final int occurrencesCount;
        public final int[] positions;
        public final double searchTimeMs;

        public NaiveSearchResult(int occurrencesCount, int[] positions, double searchTimeMs) {
            this.occurrencesCount = occurrencesCount;
            this.positions = positions == null ? new int[0] : positions;
            this.searchTimeMs = searchTimeMs;
        }
    }

    public static class PerformanceComparison {
        public final String pattern;
        public final int textLength;
        public final SearchResult saResult;
        public final NaiveSearchResult naiveResult;
        public final double speedupFactor;

        public PerformanceComparison(String pattern, int textLength, SearchResult saResult,
                                     NaiveSearchResult naiveResult) {
            this.pattern = pattern;
            this.textLength = textLength;
            this.saResult = saResult;
            this.naiveResult = naiveResult;
            if (saResult.getSearchTimeMs() > 0) {
                this.speedupFactor = naiveResult.searchTimeMs / saResult.getSearchTimeMs();
            } else {
                this.speedupFactor = 1.0;
            }
        }
    }

    /**
     * Binary Search pattern matching over Suffix Array.
     * Records all real binary search steps for visualization.
     */
    public static SearchResult search(SuffixArray sa, String pattern, boolean caseSensitive, int snippetContext) {
        long startTime = System.nanoTime();

        if (sa == null || sa.getN() == 0) {
            return new SearchResult(SearchResult.Status.NOT_INDEXED, 0, new int[0], null, null, 0.0, "No document loaded.");
        }

        if (pattern == null || pattern.isEmpty()) {
            return new SearchResult(SearchResult.Status.EMPTY_PATTERN, 0, new int[0], null, null, 0.0, "Search pattern cannot be empty.");
        }

        String text = sa.getText();
        String searchPattern = pattern;

        // If case-insensitive, instantiate temporary SuffixArray on lowercased text
        SuffixArray targetSa = sa;
        if (!caseSensitive) {
            targetSa = new SuffixArray(text.toLowerCase());
            searchPattern = pattern.toLowerCase();
        }

        int n = targetSa.getN();
        int[] saArr = targetSa.getSa();
        List<SearchResult.BinarySearchStep> stepsTrace = new ArrayList<>();

        int stepCounter = 1;

        // -------------------------------------------------------------------
        // Phase 1: Binary Search for LOWER BOUND (First suffix matching pattern)
        // -------------------------------------------------------------------
        int low = 0;
        int high = n - 1;
        int lowerBound = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int pos = saArr[mid];

            String preview = getSuffixPreview(text, pos, 40);
            int cmpRes = targetSa.compareSuffixWithPattern(pos, searchPattern);

            String cmpResultText;
            String actionText;

            if (cmpRes == 0) {
                cmpResultText = "Pattern == Suffix Prefix";
                actionText = "Match found; record L=" + mid + " and search left (high=" + (mid - 1) + ")";
                lowerBound = mid;
                high = mid - 1; // Keep searching LEFT for first match
            } else if (cmpRes < 0) {
                cmpResultText = "Pattern < Suffix";
                actionText = "Go Left (high=" + (mid - 1) + ")";
                high = mid - 1;
            } else {
                cmpResultText = "Pattern > Suffix";
                actionText = "Go Right (low=" + (mid + 1) + ")";
                low = mid + 1;
            }

            stepsTrace.add(new SearchResult.BinarySearchStep(
                stepCounter++, "Lower Bound Search", low, high, mid, pos, preview, cmpResultText, actionText
            ));
        }

        // If no matching suffix was found, return NOT_FOUND
        if (lowerBound == -1) {
            double searchTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
            return new SearchResult(SearchResult.Status.NOT_FOUND, 0, new int[0], null, stepsTrace, searchTimeMs, null);
        }

        // -------------------------------------------------------------------
        // Phase 2: Binary Search for UPPER BOUND (Last suffix matching pattern)
        // -------------------------------------------------------------------
        low = lowerBound;
        high = n - 1;
        int upperBound = lowerBound;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int pos = saArr[mid];

            String preview = getSuffixPreview(text, pos, 40);
            int cmpRes = targetSa.compareSuffixWithPattern(pos, searchPattern);

            String cmpResultText;
            String actionText;

            if (cmpRes == 0) {
                cmpResultText = "Pattern == Suffix Prefix";
                actionText = "Match found; record R=" + mid + " and search right (low=" + (mid + 1) + ")";
                upperBound = mid;
                low = mid + 1; // Keep searching RIGHT for last match
            } else if (cmpRes < 0) {
                cmpResultText = "Pattern < Suffix";
                actionText = "Go Left (high=" + (mid - 1) + ")";
                high = mid - 1;
            } else {
                cmpResultText = "Pattern > Suffix";
                actionText = "Go Right (low=" + (mid + 1) + ")";
                low = mid + 1;
            }

            stepsTrace.add(new SearchResult.BinarySearchStep(
                stepCounter++, "Upper Bound Search", low, high, mid, pos, preview, cmpResultText, actionText
            ));
        }

        // Collect matching positions
        int count = upperBound - lowerBound + 1;
        int[] positions = new int[count];
        for (int i = 0; i < count; i++) {
            positions[i] = saArr[lowerBound + i];
        }
        Arrays.sort(positions);

        // Extract context snippets
        List<SearchResult.MatchItem> matches = new ArrayList<>();
        int patLen = pattern.length();

        for (int pos : positions) {
            int startContext = Math.max(0, pos - snippetContext);
            int endContext = Math.min(n, pos + patLen + snippetContext);

            String beforeStr = text.substring(startContext, pos);
            String matchedStr = text.substring(pos, pos + patLen);
            String afterStr = text.substring(pos + patLen, endContext);

            // Clean newlines to spaces so snippets render on a single line cleanly
            String cleanBefore = beforeStr.replace("\r", " ").replace("\n", " ");
            String cleanAfter = afterStr.replace("\r", " ").replace("\n", " ");

            if (startContext > 0) cleanBefore = "..." + cleanBefore;
            if (endContext < n) cleanAfter = cleanAfter + "...";

            String plainSnippet = cleanBefore + " [" + matchedStr + "] " + cleanAfter;
            String highlightedSnippet = "<html>" + escapeHtml(cleanBefore) + 
                " <b style='color:#0284c7;background-color:#fef08a;'>[" + escapeHtml(matchedStr) + "]</b> " + 
                escapeHtml(cleanAfter) + "</html>";

            int lineNumber = countLines(text, pos);
            matches.add(new SearchResult.MatchItem(pos, lineNumber, plainSnippet, highlightedSnippet));
        }

        double searchTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        return new SearchResult(SearchResult.Status.FOUND, count, positions, matches, stepsTrace, searchTimeMs, null);
    }

    /**
     * Naive Substring Search Algorithm (Brute-force sliding window).
     * Compares character-by-character at every index without using String.indexOf or regex.
     * Used for empirical performance comparison against Suffix Array + Binary Search.
     */
    public static NaiveSearchResult naiveSearch(String text, String pattern, boolean caseSensitive) {
        long startTime = System.nanoTime();

        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            double durationMs = (System.nanoTime() - startTime) / 1_000_000.0;
            return new NaiveSearchResult(0, new int[0], durationMs);
        }

        int n = text.length();
        int m = pattern.length();
        List<Integer> matchPositions = new ArrayList<>();

        for (int i = 0; i <= n - m; i++) {
            boolean match = true;
            for (int j = 0; j < m; j++) {
                char cText = text.charAt(i + j);
                char cPat = pattern.charAt(j);

                if (!caseSensitive) {
                    cText = Character.toLowerCase(cText);
                    cPat = Character.toLowerCase(cPat);
                }

                if (cText != cPat) {
                    match = false;
                    break;
                }
            }
            if (match) {
                matchPositions.add(i);
            }
        }

        int[] positions = new int[matchPositions.size()];
        for (int k = 0; k < matchPositions.size(); k++) {
            positions[k] = matchPositions.get(k);
        }

        double durationMs = (System.nanoTime() - startTime) / 1_000_000.0;
        return new NaiveSearchResult(positions.length, positions, durationMs);
    }

    /**
     * Executes both Suffix Array + Binary Search and Naive Search on the same text and query,
     * measuring the actual real-time execution difference.
     */
    public static PerformanceComparison compareSearch(SuffixArray sa, String pattern, boolean caseSensitive, int snippetContext) {
        SearchResult saRes = search(sa, pattern, caseSensitive, snippetContext);
        NaiveSearchResult naiveRes = naiveSearch(sa != null ? sa.getText() : "", pattern, caseSensitive);
        return new PerformanceComparison(pattern, sa != null ? sa.getN() : 0, saRes, naiveRes);
    }

    private static String getSuffixPreview(String text, int pos, int maxLen) {
        int n = text.length();
        int end = Math.min(n, pos + maxLen);
        String preview = text.substring(pos, end).replace("\r", " ").replace("\n", " ");
        if (end < n) preview += "...";
        return preview;
    }

    private static int countLines(String text, int pos) {
        int lines = 1;
        for (int i = 0; i < pos && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
