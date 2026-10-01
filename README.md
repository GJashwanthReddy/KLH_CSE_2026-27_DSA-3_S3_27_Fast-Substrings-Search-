# Fast Substrings Search Using Suffix Arrays (Java Implementation)

**B.Tech Data Structures & Algorithms (DSA) Capstone Project**

---

## 1. Project Title
**Fast Substrings Search Using Suffix Arrays and Binary Search**

---

## 2. Problem Statement
Naive substring pattern searching algorithms (such as standard brute-force scans or built-in string searches) scan the text of length $N$ character-by-character for every incoming search pattern query of length $M$. This results in a worst-case time complexity of $O(N \cdot M)$ per query.

When dealing with **large static text documents** (e.g., research papers, digital textbooks, legal contracts, or log archives) where thousands of distinct queries are executed on the same text, naive searching becomes extremely slow and computationally inefficient.

---

## 3. Objectives
1. Build a memory-efficient Data Structure (**Suffix Array**) over text extracted from large text documents (.txt and .docx) using **Java**.
2. Implement **Rank-Based Prefix-Doubling Suffix Array Construction** ($O(N \log^2 N)$ time) storing integer suffix starting indices without duplicating full substring objects.
3. Implement **Binary Search Substring Pattern Matching** ($O(M \log N)$ search time) to find all pattern occurrences and exact character positions without relying on built-in Java string search methods (`String.contains()`, `String.indexOf()`, `regex`).
4. Provide both a modern **Java Swing GUI** (`gui.SwingApp`) and an interactive **Command-Line Interface (CLI)** (`Main --cli`).
5. Provide step-by-step visualizations of Suffix Array tables and Binary Search trace steps for B.Tech project review demonstration.

---

## 4. Proposed Solution
The proposed system preprocesses the input document once to construct a **Suffix Array** (`int[] sa`), which stores all suffix starting positions sorted lexicographically. Substring search queries are then executed over the sorted Suffix Array using **Binary Search**:
- **Lower Bound Binary Search:** Finds the first suffix in the Suffix Array where the suffix prefix is $\ge$ pattern.
- **Upper Bound Binary Search:** Finds the last suffix in the Suffix Array where the suffix prefix matches the pattern.
- The index range `[lowerBound, upperBound]` directly yields **all starting positions** in the original document.

```
Document Text (.txt / .docx)
        ↓
Java DocumentReader (ZipFile XML DOM Parser / Files API)
        ↓
Rank-Based Prefix-Doubling Suffix Array Construction (int[] sa)
        ↓
Binary Search for Lower & Upper Match Bounds (PatternSearch.java)
        ↓
Matching Suffix Position Range [lowerBound, upperBound]
        ↓
Snippet Extraction & Swing GUI / CLI Output
```

---

## 5. System Workflow
1. **Document Selection:** User selects a `.txt` or `.docx` file or enters manual text.
2. **Text Extraction:** `DocumentReader.java` parses text (using `java.nio.file.Files` for `.txt` and `java.util.zip.ZipFile` + `javax.xml.parsers.DocumentBuilderFactory` for `.docx`).
3. **Index Construction:** `SuffixArray.java` builds integer suffix array `sa` and measures construction runtime.
4. **Pattern Input:** User enters search pattern.
5. **Pattern Search:** `PatternSearch.java` executes pure character-by-character comparison binary search over `sa`.
6. **Result Presentation:** Returns matching status (`FOUND`/`NOT_FOUND`), total count, match snippets with line numbers and highlighted patterns, step-by-step binary search trace, and execution time metrics.

---

## 6. Suffix Array Explanation
A **Suffix Array** for a text string $T$ of length $N$ is an array of integers `sa` of size $N$, representing the starting positions of all suffixes of $T$ sorted in lexicographical (dictionary) order.

For example, for $T = \text{"banana"}$:
- Suffix 0: `banana`
- Suffix 1: `anana`
- Suffix 2: `nana`
- Suffix 3: `ana`
- Suffix 4: `na`
- Suffix 5: `a`

Sorted Suffixes lexicographically:
1. Index 5: `a`
2. Index 3: `ana`
3. Index 1: `anana`
4. Index 0: `banana`
5. Index 4: `na`
6. Index 2: `nana`

**Suffix Array `sa` = `[5, 3, 1, 0, 4, 2]`**

---

## 7. Suffix Array Construction Algorithm
We implement **Rank-Based Prefix Doubling**:
1. **Initial Ranks ($k = 0$):** Rank each position $i$ using `(int) text.charAt(i)`.
2. **Iterative Doubling ($k = 1, 2, 4, 8 \dots$):**
   - For each index $i$, form rank pair: `(rank[i], rank[i + k] if i + k < N else -1)`.
   - Sort suffix indices based on rank pairs.
   - Reassign new ranks $0, 1, 2 \dots$ based on sorted rank pairs.
   - **Early Exit:** If all ranks become unique ($0$ to $N-1$), sorting terminates immediately.

---

## 8. Binary Search Pattern Matching
Since all suffixes are sorted lexicographically in `sa`:
- Any substring match of pattern $P$ in text $T$ must appear as a prefix of a suffix.
- Because suffixes are sorted, all suffixes starting with $P$ form a **contiguous range** in `sa`.
- Binary Search compares pattern $P$ with suffix `text.substring(sa[mid])` character-by-character.
- Time to find range: $O(M \log N)$ where $M = |P|$ and $N = |T|$.

---

## 9. Document Upload
The application supports loading:
- Plain Text files (`.txt`)
- Word documents (`.docx`)

Upon upload, the system displays:
- File Name & Extension
- File Format Type
- Formatted File Size (KB/MB)
- Total Character Count
- Total Word Count

---

## 10. Text Extraction
- **`.txt` Extraction:** `java.nio.file.Files.readAllBytes()` with UTF-8 decoding and line-ending normalization (`\r\n` $\rightarrow$ `\n`).
- **`.docx` Extraction:** Zero-dependency Java standard library ZipFile parser parsing `word/document.xml` using `javax.xml.parsers.DocumentBuilderFactory`.

---

## 11. Pattern Searching
Users enter any pattern string. The system performs binary search without calling built-in `String.contains()`, `String.indexOf()`, or `regex`.

Output includes:
- Search Status (`FOUND` or `NOT_FOUND`)
- Total number of pattern occurrences
- Matching character positions in text
- Matching line numbers
- Extracted context snippets with pattern highlighting
- Step-by-step binary search execution trace

---

## 12. Multiple Occurrence Detection
When a pattern appears multiple times (e.g. `ana` in `banana`), binary search identifies the full matching index range `[lowerBound, upperBound]` in `sa`. The starting positions `sa[lowerBound ... upperBound]` are extracted to return **all occurrences**, including overlapping matches (e.g. `aa` in `aaa`).

---

## 13. Complexity Analysis

| Operation | Time Complexity | Space Complexity | Explanation |
| :--- | :--- | :--- | :--- |
| **SA Construction** | $O(N \log^2 N)$ | $O(N)$ | Rank-based prefix doubling with $O(N \log N)$ sorting per doubling step. Stores primitive integer arrays `sa` and `rank`. |
| **Pattern Search** | $O(M \log N)$ | $O(1)$ auxiliary | Binary search ($O(\log N)$ steps) comparing up to $M$ characters per step. |
| **Occurrence Retrieval** | $O(K)$ | $O(K)$ | Extracting $K$ matching indices from `sa[lowerBound ... upperBound]`. |

---

## 14. Test Cases
The project includes an automated test runner (`tests/TestCases.java`) covering all 12 required test cases:

1. **Test 1:** `text = "banana"`, `pattern = "ana"` $\rightarrow$ Found at positions `[1, 3]`.
2. **Test 2:** `text = "banana"`, `pattern = "xyz"` $\rightarrow$ Not Found.
3. **Test 3:** `text = "mississippi"`, `pattern = "iss"` $\rightarrow$ Found at positions `[1, 4]`.
4. **Test 4:** `text = "aaa"`, `pattern = "aa"` $\rightarrow$ Found overlapping matches at positions `[0, 1]`.
5. **Test 5:** Pattern at beginning of text $\rightarrow$ Found at position `0`.
6. **Test 6:** Pattern at end of text $\rightarrow$ Found at position `15`.
7. **Test 7:** Pattern longer than document $\rightarrow$ Not Found.
8. **Test 8:** Pattern not present in text $\rightarrow$ Not Found.
9. **Test 9:** Empty pattern input $\rightarrow$ Error handling.
10. **Test 10:** Large repeated-text document (`5000` `'a'`s) $\rightarrow$ Correct occurrence count.
11. **Test 11:** Large `.txt` document $\rightarrow$ Found all matches.
12. **Test 12:** Large `.docx` document $\rightarrow$ Found all matches.

---

## 15. Project Structure
```
DSA_Project/
│
├── src/
│   ├── SuffixArray.java        # Core DSA: Prefix-Doubling SA Construction
│   ├── PatternSearch.java      # Binary Search Pattern Matching
│   ├── DocumentReader.java     # Text extraction for .txt & .docx (ZipFile XML DOM)
│   ├── SearchResult.java       # Container for search status, occurrences, positions
│   ├── PerformanceMonitor.java # Execution timing and memory profiling
│   └── Main.java               # Launcher entry point (GUI / CLI / Test Runner)
│
├── gui/
│   └── SwingApp.java           # Standalone Java Swing GUI Application
│
├── tests/
│   └── TestCases.java          # Test suite for 12 test cases
│
├── sample_documents/
│   ├── sample.txt
│   ├── sample.docx
│   └── sample_research_paper.txt
│
├── bin/                        # Compiled Java class files
├── README.md                   # Complete documentation
└── PROJECT_REVIEW_NOTES.md     # Java DSA viva review guide & 26 Q&As
```

---

## 16. How to Compile and Run

### System Requirements
- **Java JDK Version:** Java 8+ (Tested with Java 25 JDK).
- **Dependencies:** **Zero external dependencies!** Uses standard Java JDK libraries (`java.util`, `java.io`, `java.nio`, `javax.swing`, `javax.xml.parsers`).

### Step 1: Compile the Java Project
```powershell
mkdir -p bin
javac -d bin src/*.java gui/*.java tests/*.java
```

### Step 2: Run the Application

#### Option A: Launch Standalone Swing GUI (Recommended for Review)
```powershell
java -cp bin Main
```

#### Option B: Launch Interactive CLI
```powershell
java -cp bin Main --cli
```

#### Option C: Run All 12 Automated Unit Tests
```powershell
java -cp bin Main --test
```

---

## 17. Sample Output

```
======================================================================
  RUNNING ALL 12 AUTOMATED JAVA DSA TEST CASES
======================================================================
[PASS] Test 1: banana + ana                : Positions: [1, 3]
[PASS] Test 2: banana + xyz                : Status: NOT_FOUND
[PASS] Test 3: mississippi + iss           : Positions: [1, 4]
[PASS] Test 4: Overlapping aaa + aa        : Positions: [0, 1]
[PASS] Test 5: Pattern at beginning        : Pos: [0]
[PASS] Test 6: Pattern at end              : Pos: [15]
[PASS] Test 7: Pattern longer than text    : Status: NOT_FOUND
[PASS] Test 8: Pattern not present         : Status: NOT_FOUND
[PASS] Test 9: Empty pattern handling      : Status: EMPTY_PATTERN
[PASS] Test 10: Large repeated text (5000 'a's) : Occurrences: 4996
[PASS] Test 11: Large .txt Document        : Found 20 matches.
[PASS] Test 12: Large .docx Document       : Found 10 matches.

----------------------------------------------------------------------
TEST RESULTS: 12 / 12 PASSED
----------------------------------------------------------------------
```

---

## 18. Limitations
1. **Case-Sensitivity:** Case-insensitive search requires constructing a lowercase suffix array index.
2. **Memory Limit:** While SA stores primitive integers, extremely huge texts (>500MB) require disk-backed or compressed suffix structures (e.g. FM-index / Suffix Automata).

---

## 19. Future Enhancements
1. Implement **SA-IS** ($O(N)$ linear time Suffix Array construction algorithm).
2. Add **LCP Array (Longest Common Prefix)** with Kasai's algorithm to enable $O(M + \log N)$ pattern search.
3. Support additional document formats such as `.pdf` and `.rtf`.
