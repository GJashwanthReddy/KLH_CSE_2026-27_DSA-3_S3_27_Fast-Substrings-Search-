import java.util.Arrays;
import java.util.Comparator;

/**
 * SuffixArray.java
 * ================
 * Core DSA Implementation: Rank-Based Prefix-Doubling Suffix Array Construction (O(N log^2 N)).
 * Stores integer starting positions in array `sa`, comparing directly against text buffer.
 * Memory Efficient: Does NOT duplicate substring objects for suffixes.
 */
public class SuffixArray {

    private final String text;
    private final int n;
    private final int[] sa;
    private final double buildTimeMs;

    public SuffixArray(String text) {
        this.text = text == null ? "" : text;
        this.n = this.text.length();
        this.sa = new int[n];

        long startTime = System.nanoTime();
        if (n > 0) {
            buildSuffixArray();
        }
        long endTime = System.nanoTime();
        this.buildTimeMs = (endTime - startTime) / 1_000_000.0;
    }

    /**
     * Builds the Suffix Array using Rank-Based Prefix Doubling.
     * Step length k doubles each iteration (1, 2, 4, 8...).
     */
    private void buildSuffixArray() {
        if (n == 1) {
            sa[0] = 0;
            return;
        }

        // Rank array storing equivalence classes
        int[] rank = new int[n];
        Integer[] saBoxed = new Integer[n];

        // Step 1: Initial Ranks based on character code points
        for (int i = 0; i < n; i++) {
            rank[i] = (int) text.charAt(i);
            saBoxed[i] = i;
        }

        int[] tempRank = new int[n];

        // Step 2: Iterative Prefix Doubling
        for (int k = 1; k < n; k *= 2) {
            final int currentK = k;
            final int[] currentRank = rank;

            // Sort suffix indices based on tuple: (rank[i], rank[i + k])
            Arrays.sort(saBoxed, new Comparator<Integer>() {
                @Override
                public int compare(Integer i, Integer j) {
                    if (currentRank[i] != currentRank[j]) {
                        return Integer.compare(currentRank[i], currentRank[j]);
                    }
                    int nextRankI = (i + currentK < n) ? currentRank[i + currentK] : -1;
                    int nextRankJ = (j + currentK < n) ? currentRank[j + currentK] : -1;
                    return Integer.compare(nextRankI, nextRankJ);
                }
            });

            // Update ranks after sorting
            tempRank[saBoxed[0]] = 0;
            int uniqueRanks = 1;

            for (int i = 1; i < n; i++) {
                int prevSuffix = saBoxed[i - 1];
                int currSuffix = saBoxed[i];

                int prevRank1 = currentRank[prevSuffix];
                int prevRank2 = (prevSuffix + currentK < n) ? currentRank[prevSuffix + currentK] : -1;

                int currRank1 = currentRank[currSuffix];
                int currRank2 = (currSuffix + currentK < n) ? currentRank[currSuffix + currentK] : -1;

                if (prevRank1 == currRank1 && prevRank2 == currRank2) {
                    tempRank[currSuffix] = tempRank[prevSuffix];
                } else {
                    tempRank[currSuffix] = tempRank[prevSuffix] + 1;
                    uniqueRanks++;
                }
            }

            rank = Arrays.copyOf(tempRank, n);

            // Optimization: If all ranks are unique (0 to n-1), sorting is complete!
            if (uniqueRanks == n) {
                break;
            }
        }

        // Copy boxed array to primitive int array
        for (int i = 0; i < n; i++) {
            sa[i] = saBoxed[i];
        }
    }

    /**
     * Character-by-character comparison of pattern against suffix at `suffixIdx`.
     * Does NOT use built-in String methods like indexOf, contains, regex, or find.
     * 
     * @return -1 if pattern < suffix prefix
     *          0 if pattern == suffix prefix (pattern is a prefix of suffix)
     *          1 if pattern > suffix prefix
     */
    public int compareSuffixWithPattern(int suffixIdx, String pattern) {
        int m = pattern.length();
        for (int j = 0; j < m; j++) {
            int pos = suffixIdx + j;
            if (pos >= n) {
                // Suffix ended before pattern finished -> pattern is GREATER
                return 1;
            }
            char charText = text.charAt(pos);
            char charPat = pattern.charAt(j);

            if (charPat < charText) {
                return -1;
            } else if (charPat > charText) {
                return 1;
            }
        }
        // All pattern characters matched prefix of suffix
        return 0;
    }

    public String getText() {
        return text;
    }

    public int getN() {
        return n;
    }

    public int[] getSa() {
        return sa;
    }

    public double getBuildTimeMs() {
        return buildTimeMs;
    }
}
