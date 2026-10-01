import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PatternSearch.java
 * ==================
 * Implements Binary Search over the Suffix Array to find all matching occurrences of a pattern.
 * Performs pure character-by-character comparison via SuffixArray.compareSuffixWithPattern().
 * NO String.contains(), String.indexOf(), or regex are used.
 */
public class PatternSearch {

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

            String preview = getSuffixPreview(text, pos, 50);
            int cmpRes = targetSa.compareSuffixWithPattern(pos, searchPattern);

            String cmpDesc;
            if (cmpRes == 0) {
                cmpDesc = "Pattern matches Suffix prefix! (Searching left for lower bound)";
                lowerBound = mid;
                high = mid - 1; // Keep searching LEFT for first match
            } else if (cmpRes < 0) {
                cmpDesc = "Pattern < Suffix (Go Left)";
                high = mid - 1;
            } else {
                cmpDesc = "Pattern > Suffix (Go Right)";
                low = mid + 1;
            }

            stepsTrace.add(new SearchResult.BinarySearchStep(
                stepCounter++, "Lower Bound Search", low, mid, high, pos, preview, cmpDesc
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

            String preview = getSuffixPreview(text, pos, 50);
            int cmpRes = targetSa.compareSuffixWithPattern(pos, searchPattern);

            String cmpDesc;
            if (cmpRes == 0) {
                cmpDesc = "Pattern matches Suffix! (Searching right for upper bound)";
                upperBound = mid;
                low = mid + 1; // Keep searching RIGHT for last match
            } else if (cmpRes < 0) {
                cmpDesc = "Pattern < Suffix (Go Left)";
                high = mid - 1;
            } else {
                cmpDesc = "Pattern > Suffix (Go Right)";
                low = mid + 1;
            }

            stepsTrace.add(new SearchResult.BinarySearchStep(
                stepCounter++, "Upper Bound Search", low, mid, high, pos, preview, cmpDesc
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
