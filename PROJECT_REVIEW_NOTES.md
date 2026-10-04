# Fast Substrings Search Using Suffix Arrays: Viva & Review Guide

## Technical Viva & Project Review Questions

### 1. What is the Core Objective of this Project?
The objective is to implement an ultra-fast substring search system using **Suffix Arrays** and **Binary Search** in Java. For a static text document of length $N$, once the Suffix Array is constructed in $O(N \log^2 N)$ time, any pattern of length $M$ can be queried in $O(M \log N)$ time, significantly faster than naive $O(N \cdot M)$ substring searches.

---

### 2. What is a Suffix Array?
A Suffix Array is an integer array of size $N$ that stores the starting indices of all suffixes of a string $T$ sorted in lexicographical (dictionary) order.
- Storing entire suffix strings would require $O(N^2)$ memory.
- Storing only integer indices requires only $O(N)$ memory (an `int[]` of length $N$).

Example for `banana`:
- Index 0: `banana`
- Index 1: `anana`
- Index 2: `nana`
- Index 3: `ana`
- Index 4: `na`
- Index 5: `a`

Sorted Suffix Array `sa`: `[5, 3, 1, 0, 4, 2]` corresponding to:
- `a` (pos 5)
- `ana` (pos 3)
- `anana` (pos 1)
- `banana` (pos 0)
- `na` (pos 4)
- `nana` (pos 2)

---

### 3. How is the Suffix Array Constructed?
We implement the **Rank-Based Prefix-Doubling** algorithm:
1. **Initial Rank ($k = 0$):** Suffixes are ranked by their initial character ASCII/Unicode value (`text.charAt(i)`).
2. **Doubling Steps ($k = 1, 2, 4, 8 \dots$):**
   - Each suffix at index $i$ is represented by a pair: `(rank[i], rank[i + k])` (using -1 if $i + k \ge N$).
   - Suffix indices are sorted based on these rank pairs.
   - Suffixes with identical pairs receive the same rank; distinct pairs receive incremented ranks.
   - **Early Exit Optimization:** If all ranks from $0$ to $N - 1$ are unique, the sorting terminates early.
- **Time Complexity:** $O(N \log^2 N)$ because there are $O(\log N)$ doubling steps, each taking $O(N \log N)$ comparison sorting.
- **Space Complexity:** $O(N)$ storing primitive `int[]` rank and index arrays.

---

### 4. How does Pattern Matching work using Binary Search?
Because all suffixes are sorted lexicographically in the Suffix Array:
1. Any occurrence of pattern $P$ in text $T$ appears as a prefix of some suffix.
2. All suffixes that start with $P$ must appear **contiguously** in the Suffix Array.
3. We execute two manual Binary Searches:
   - **Lower Bound Search:** Finds the smallest index in `sa` whose suffix prefix equals $P$.
   - **Upper Bound Search:** Finds the largest index in `sa` whose suffix prefix equals $P$.
4. The range `[lowerBound, upperBound]` contains all matches.
5. All character comparisons are executed directly (`text.charAt(pos + i) - pattern.charAt(i)`). Built-in Java search methods (`indexOf`, `contains`, `regex`) are strictly prohibited and not used.
- **Time Complexity:** $O(M \log N)$ where $M$ is pattern length and $N$ is text length.

---

### 5. How are Overlapping and Multiple Occurrences Handled?
Because each occurrence of pattern $P$ at text position $i$ represents a distinct suffix starting at index $i$, all occurrences (whether overlapping like `aa` in `aaa` or distinct like `ana` in `banana`) exist as independent entries in the Suffix Array. Finding the full range `[lowerBound, upperBound]` naturally identifies all $K$ occurrences in $O(K)$ time.

---

### 6. How is Document Ingestion handled for Large Files?
- **Plain Text (`.txt`):** Read using `java.nio.file.Files.readAllBytes()` with UTF-8 decoding.
- **Word Documents (`.docx`):** Handled via standard Java libraries (`java.util.zip.ZipFile` and `javax.xml.parsers.DocumentBuilderFactory`) parsing `word/document.xml` text nodes (`<w:t>`). Zero external jar dependencies are required.
- File extraction is strictly separated from the core DSA indexing and search pipeline.

---

### 7. What is the Algorithmic Difference between Naive Search and Suffix Array Search?

| Metric | Naive String Search | Suffix Array + Binary Search |
| :--- | :--- | :--- |
| **Preprocessing Time** | $0$ (None) | $O(N \log^2 N)$ (Once per document) |
| **Search Time per Query** | $O(N \cdot M)$ | $O(M \log N)$ |
| **Multiple Occurrences** | Scans full document | Direct range extraction $O(K)$ |
| **Suitable For** | Single search on small dynamic text | Multiple queries on large static text |

---

### 8. What are the Key Features of the User Interface?
- **Standalone Java Swing GUI:** Professional dark navy header (`#0f172a`), clean technical aesthetics.
- **Working Default Demo:** Pre-loaded with `BANANA BANDANA` and pattern `ANA`, producing live dynamically computed results on launch.
- **Dynamic Suffix Array Table:** Real interactive table showing SA Index, Position, and Suffix text.
- **Binary Search Trace:** Real step-by-step trace showing Step, Phase, Low, High, Mid, Compared Suffix, and Action.
- **Live Performance Comparison:** Real-time side-by-side benchmark between Naive Search ($O(N \cdot M)$) and Suffix Array ($O(M \log N)$) on identical text and query.
- **Automated Test Runner:** Embedded runner executing 15 comprehensive automated test cases with real-time pass/fail feedback.
