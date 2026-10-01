# JAVA PROJECT REVIEW & VIVA PREPARATION NOTES

**Project Title:** Fast Substrings Search Using Suffix Arrays  
**Course:** B.Tech Data Structures & Algorithms (DSA)  
**Implementation Language:** Java (JDK 8+ / JDK 25)  

---

## A. 30-Second Explanation (Elevator Pitch)
"Good morning/afternoon, Respected Faculty. My DSA project implements a fast substring pattern search system for large text documents (.txt and .docx) using **Suffix Arrays and Binary Search in Java**. Instead of scanning a document character-by-character for every search query ($O(N \cdot M)$ naive search), our Java application preprocesses the document text once to build a sorted **Suffix Array** in $O(N \log^2 N)$ time. Any subsequent pattern query is answered in **$O(M \log N)$** time using binary search to find the lower and upper bounds of matching suffixes in the array."

---

## B. 1-Minute Explanation
"My project addresses the problem of fast text searching in large static documents like research papers or text files. 

When a document is loaded, our system extracts its text and constructs a **Suffix Array**—an integer array of starting positions representing all suffixes of the text sorted in dictionary order. We implement a **Rank-Based Prefix-Doubling algorithm** in Java to construct the suffix array efficiently in memory without creating duplicate `String` objects for suffixes.

When a user searches for a pattern, we do NOT use built-in Java methods like `String.indexOf()`, `String.contains()`, or `regex`. Instead, we perform a **Binary Search** over the Suffix Array using `PatternSearch.java` to locate the contiguous range of suffixes that start with the pattern. This yields all match occurrences, exact text offsets, and context snippets in under a millisecond, even for large documents."

---

## C. 3-Minute Explanation
"Respected Panel, pattern searching in text documents is a fundamental problem in computer science. Standard naive substring search or built-in functions check the pattern against every offset in the document, which takes $O(N \cdot M)$ time for text of length $N$ and pattern of length $M$. If a user searches multiple times in the same document, this repeated full-text scanning is extremely inefficient.

In our Java project, we separate document indexing from querying:
1. **Document Ingestion & Text Extraction:** Our `DocumentReader.java` class reads `.txt` files (with `java.nio.file.Files`) and `.docx` files (by parsing XML paragraph elements using `ZipFile` and `javax.xml.parsers.DocumentBuilderFactory`), computing metadata such as file size, word count, and character count.
2. **Suffix Array Construction:** The extracted text is passed to `SuffixArray.java`. We implement the classic **Rank-Based Prefix-Doubling algorithm**. Initially, each suffix is ranked by its single character code `(int) text.charAt(i)`. In each iteration $k = 1, 2, 4, 8 \dots$, we sort suffix indices using rank pairs `(rank[i], rank[i + k])` and update ranks until all suffixes have distinct ranks. This builds the sorted Suffix Array `int[] sa` in $O(N \log^2 N)$ time using $O(N)$ memory.
3. **Binary Search Substring Queries:** Since suffixes in `sa` are sorted lexicographically, all suffixes starting with a given pattern form a continuous block in the array. `PatternSearch.java` uses binary search to locate the **Lower Bound** (first matching suffix) and **Upper Bound** (last matching suffix). The index range directly gives all starting positions in the original text.
4. **User Interface & Visualizations:** We built a Java Swing GUI (`SwingApp.java`) and a CLI (`Main.java --cli`) featuring document stats, snippet highlighting with line numbers, a step-by-step Binary Search visualization trace table, a Suffix Array inspection table, and an automated 12-case Java DSA test suite runner (`TestCases.java`)."

---

## D. Complete Algorithm Explanation

### 1. Rank-Based Prefix Doubling Construction Algorithm (`SuffixArray.java`)
- **Input:** Text $T$ of length $N$.
- **Step 1 (Initial Ranks):** Set `rank[i] = (int) T.charAt(i)` for $0 \le i < N$. `sa = [0, 1, ..., N-1]`.
- **Step 2 (Iterative Doubling $k = 1, 2, 4 \dots$):**
  - Define comparison key for suffix $i$ as `(rank[i], rank[i + k] if i + k < N else -1)`.
  - Sort array `sa` using key.
  - Update `rank` array: equal tuples receive identical ranks; distinct tuples receive increasing integer ranks.
  - If all ranks are distinct ($0$ to $N-1$), break early.
- **Output:** Suffix Array `int[] sa` where `sa[r]` is the starting index of the $r$-th lexicographically smallest suffix.

### 2. Binary Search Substring Pattern Matching (`PatternSearch.java`)
- **Input:** Pattern $P$ of length $M$, Text $T$ of length $N$, Suffix Array `sa`.
- **Lower Bound Search:**
  - Binary search over `sa[0 ... N-1]`. Compare $P$ with $T.substring(sa[mid])$ character by character using `compareSuffixWithPattern()`.
  - If $P \le \text{suffix prefix}$, record `lowerBound = mid` and move left `high = mid - 1`.
  - Else move right `low = mid + 1`.
- **Upper Bound Search:**
  - Binary search over `sa[lowerBound ... N-1]`. Compare $P$ with $T.substring(sa[mid])$.
  - If $P$ matches prefix of suffix, record `upperBound = mid` and move right `low = mid + 1`.
  - Else move left `high = mid - 1`.
- **Match Output:** Positions `sa[lowerBound ... upperBound]`.

---

## E. Why Suffix Arrays?
- **Precomputable Index:** Preprocessing text once allows unlimited fast queries.
- **Fast Search:** $O(M \log N)$ search time vs $O(N \cdot M)$ naive search.
- **Space Efficiency:** Requires only an array of $N$ integers ($4N$ bytes), whereas Suffix Trees require $10\times - 20\times$ more memory due to object pointer overhead.

---

## F. Why Binary Search?
Because the Suffix Array stores suffix starting positions in **lexicographically sorted order**, pattern matching reduces to finding a prefix match in a sorted list. Binary Search is the optimal algorithm ($O(\log N)$ steps) for searching sorted arrays.

---

## G. How Large Files Are Handled?
- Suffix starting positions are stored as primitive integer array indices (`int[] sa`).
- The Java application **NEVER generates or duplicates full suffix String objects** in memory.
- Character comparisons during binary search read directly from the original text string via starting position offsets `text.charAt(sa[mid] + j)`.

---

## H. How .txt Files Are Read?
- Read using `java.nio.file.Files.readAllBytes()` with UTF-8 decoding.
- Line breaks (`\r\n`) are normalized to `\n` to ensure character index alignment.

---

## I. How .docx Files Are Read?
- `.docx` files are ZIP archives containing XML document structures.
- `DocumentReader.java` uses standard Java JDK classes (`java.util.zip.ZipFile` and `javax.xml.parsers.DocumentBuilderFactory`) to parse `word/document.xml` and extract text paragraphs.

---

## J. How Multiple Occurrences Are Found?
The binary search identifies the matching index range `[lowerBound, upperBound]` in `sa`. All indices in this range represent suffixes that start with the query pattern. Extracting `sa[i]` for `i` from `lowerBound` to `upperBound` yields all starting positions, including overlapping occurrences (e.g., `aa` in `aaa`).

---

## K. Time Complexity

| Phase | Time Complexity | Details |
| :--- | :--- | :--- |
| **SA Construction** | $O(N \log^2 N)$ | Rank-based prefix doubling algorithm in Java |
| **Pattern Search** | $O(M \log N)$ | Binary search over SA with $M$-char comparisons |
| **Snippet Generation** | $O(K)$ | Extracting $K$ match snippets |

---

## L. Space Complexity
- **Text Storage:** $O(N)$ bytes for original text string.
- **Suffix Array:** $O(N)$ primitive integers (`int[] sa`).
- **Total Auxiliary Memory:** $O(N)$ space.

---

## M. Limitations
1. Construction time is $O(N \log^2 N)$ (can be improved to $O(N)$ using SA-IS algorithm).
2. Document updates require rebuilding the suffix array.

---

## N. Future Improvements
1. Implement SA-IS ($O(N)$ linear-time suffix array construction).
2. Integrate LCP Array (Longest Common Prefix) for $O(M + \log N)$ search.

---

## O. CRITICAL VIVA QUESTION

### "Why are you using a suffix array instead of directly searching the document?"

**Answer:**
> "Direct document searching (naive scan or built-in string methods like `indexOf()`) scans through the entire document text character-by-character for every search query. If the document has $N$ characters and the pattern has $M$ characters, direct search takes $O(N \cdot M)$ time per query. For large documents queried multiple times, this is extremely slow.
>
> By using a **Suffix Array**, we preprocess the document text **once** into a sorted array of suffix starting positions. After preprocessing, any search pattern can be located in **$O(M \log N)$ time using Binary Search**, regardless of where it appears in the document. This provides logarithmic search speeds for all subsequent queries."

---

## P. 26 LIKELY VIVA QUESTIONS AND ANSWERS

### Q1: What is a Suffix Array?
**A:** A Suffix Array is an array of integers giving the starting positions of all suffixes of a string, sorted in lexicographical (dictionary) order.

### Q2: What is the size of a Suffix Array for a string of length N?
**A:** The size of the Suffix Array is exactly $N$ integers (`int[] sa`).

### Q3: What is the time complexity of Naive Substring Search?
**A:** $O(N \cdot M)$ in the worst case, where $N$ is text length and $M$ is pattern length.

### Q4: What is the time complexity of pattern searching using a Suffix Array?
**A:** $O(M \log N)$ using Binary Search.

### Q5: How do you handle multiple occurrences of a pattern?
**A:** Suffixes matching the pattern form a contiguous range `[lowerBound, upperBound]` in the sorted Suffix Array. All elements in this range represent valid match positions.

### Q6: What algorithm did you use for Suffix Array Construction in Java?
**A:** Rank-Based Prefix Doubling (Manber-Myers style algorithm).

### Q7: What is the time complexity of your Suffix Array Construction?
**A:** $O(N \log^2 N)$ using prefix doubling with standard comparison sorting per step.

### Q8: Can Suffix Array construction be done in O(N) linear time?
**A:** Yes, using advanced algorithms like SA-IS (Suffix Array by Induced Sorting) or DC3 (Kärkkäinen-Sanders algorithm).

### Q9: Why didn't you store complete String objects in the array?
**A:** Storing $N$ String objects would take $O(N^2)$ memory (gigabytes for large files). Storing primitive integer starting indices (`int[] sa`) takes only $O(N)$ space ($4N$ bytes).

### Q10: How do you compare pattern with suffix during binary search in Java?
**A:** By comparing characters one-by-one using `text.charAt(sa[mid] + j)` vs `pattern.charAt(j)` for $0 \le j < M$.

### Q11: How do you extract text from .docx files in Java?
**A:** `.docx` files are ZIP archives. We parse `word/document.xml` using Java's standard library `ZipFile` and `javax.xml.parsers.DocumentBuilderFactory` to extract paragraph text nodes (`<w:t>`).

### Q12: Did you use built-in substring search like `String.indexOf()` or `String.contains()` for pattern matching?
**A:** No. The core algorithm strictly uses Binary Search over the integer Suffix Array with pure character-by-character comparisons in `PatternSearch.java`.

### Q13: What happens if the pattern is not present in the document?
**A:** Binary search fails to find a matching prefix at `lowerBound`, returning status `NOT_FOUND` with 0 occurrences.

### Q14: How are overlapping matches handled?
**A:** Since all suffixes are sorted, overlapping occurrences (like `aa` in `aaa` at pos 0 and 1) are distinct suffixes and both appear in the Suffix Array range.

### Q15: What is the space complexity of your Suffix Array?
**A:** $O(N)$ space where $N$ is text length.

### Q16: What is an LCP Array?
**A:** Longest Common Prefix array. It stores the length of the longest common prefix between adjacent suffixes in the Suffix Array.

### Q17: How would an LCP Array help in searching?
**A:** It reduces binary search comparison time from $O(M \log N)$ to $O(M + \log N)$ by skipping redundant character comparisons.

### Q18: What is the difference between a Suffix Tree and a Suffix Array?
**A:** A Suffix Tree is a compressed trie of all suffixes ($O(N)$ space but high memory overhead per node object). A Suffix Array is a flat integer array storing suffix starting positions ($O(N)$ memory, much smaller memory footprint in Java).

### Q19: How do you determine line numbers for matches in Java?
**A:** By counting newline characters `\n` in the original text up to the matching character position.

### Q20: What is Lower Bound in Binary Search?
**A:** The first index in the Suffix Array where the suffix is lexicographically $\ge$ pattern.

### Q21: What is Upper Bound in Binary Search?
**A:** The last index in the Suffix Array where the suffix starts with the pattern.

### Q22: What happens if the search pattern is longer than the text?
**A:** `compareSuffixWithPattern()` detects that the suffix ended before pattern completion and returns `pattern > suffix`, correctly resulting in `NOT_FOUND`.

### Q23: Is Suffix Array search case-sensitive in Java?
**A:** By default yes, because `text.charAt()` uses character Unicode values. For case-insensitive search, text is lowercased prior to indexing.

### Q24: What external JAR libraries are required to run your project?
**A:** **Zero external libraries!** The project relies entirely on Java standard JDK (Java 8 or higher).

### Q25: Why is Suffix Array preferred for static text databases?
**A:** Because static texts are read often and modified rarely. Building the index once enables ultra-fast $O(M \log N)$ querying.

### Q26: How do you compile and launch the project from the terminal?
**A:** `javac -d bin src/*.java gui/*.java tests/*.java` to compile, and `java -cp bin Main` to launch the GUI window.
