import java.util.ArrayList;
import java.util.List;

/**
 * SearchResult.java
 * =================
 * Data container representing the output of a pattern search query.
 * Contains match status, occurrences count, text positions, snippets, and real binary search trace steps.
 */
public class SearchResult {

    public enum Status {
        INDEXED,
        FOUND,
        NOT_FOUND,
        EMPTY_PATTERN,
        NOT_INDEXED,
        ERROR
    }

    public static class MatchItem {
        public final int position;
        public final int lineNumber;
        public final String plainSnippet;
        public final String highlightedSnippet;

        public MatchItem(int position, int lineNumber, String plainSnippet, String highlightedSnippet) {
            this.position = position;
            this.lineNumber = lineNumber;
            this.plainSnippet = plainSnippet;
            this.highlightedSnippet = highlightedSnippet;
        }
    }

    public static class BinarySearchStep {
        public final int step;
        public final String phase;
        public final int low;
        public final int high;
        public final int mid;
        public final int suffixPos;
        public final String comparedSuffix;
        public final String comparisonResult;
        public final String action;

        public BinarySearchStep(int step, String phase, int low, int high, int mid, int suffixPos,
                                String comparedSuffix, String comparisonResult, String action) {
            this.step = step;
            this.phase = phase;
            this.low = low;
            this.high = high;
            this.mid = mid;
            this.suffixPos = suffixPos;
            this.comparedSuffix = comparedSuffix;
            this.comparisonResult = comparisonResult;
            this.action = action;
        }
    }

    private final Status status;
    private final int occurrencesCount;
    private final int[] positions;
    private final List<MatchItem> matches;
    private final List<BinarySearchStep> binarySearchSteps;
    private final double searchTimeMs;
    private final String errorMessage;

    public SearchResult(Status status, int occurrencesCount, int[] positions, List<MatchItem> matches,
                        List<BinarySearchStep> binarySearchSteps, double searchTimeMs, String errorMessage) {
        this.status = status;
        this.occurrencesCount = occurrencesCount;
        this.positions = positions == null ? new int[0] : positions;
        this.matches = matches == null ? new ArrayList<>() : matches;
        this.binarySearchSteps = binarySearchSteps == null ? new ArrayList<>() : binarySearchSteps;
        this.searchTimeMs = searchTimeMs;
        this.errorMessage = errorMessage;
    }

    public Status getStatus() { return status; }
    public int getOccurrencesCount() { return occurrencesCount; }
    public int[] getPositions() { return positions; }
    public List<MatchItem> getMatches() { return matches; }
    public List<BinarySearchStep> getBinarySearchSteps() { return binarySearchSteps; }
    public double getSearchTimeMs() { return searchTimeMs; }
    public String getErrorMessage() { return errorMessage; }

    public String getPositionsFormatted() {
        if (positions.length == 0) return "None";
        StringBuilder sb = new StringBuilder("[");
        int limit = Math.min(positions.length, 50);
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append(", ");
            sb.append(positions[i]);
        }
        if (positions.length > limit) {
            sb.append(", ... (+").append(positions.length - limit).append(" more)");
        }
        sb.append("]");
        return sb.toString();
    }
}
