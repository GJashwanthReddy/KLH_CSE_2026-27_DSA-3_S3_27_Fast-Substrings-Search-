import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * SwingApp.java
 * ============
 * Standalone Java Swing GUI Application for Fast Substrings Search Using Suffix Arrays.
 * Clean, technical, professional DSA software tool interface.
 * Strictly project-focused with zero personal/college metadata.
 */
public class SwingApp extends JFrame {

    private String loadedText = "";
    private DocumentReader.DocumentMetadata currentMetadata = null;
    private SuffixArray currentSuffixArray = null;

    // UI Colors
    private static final Color COLOR_NAVY = new Color(15, 23, 42);
    private static final Color COLOR_BG = new Color(248, 250, 252);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_BLUE_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_GREEN = new Color(22, 163, 74);
    private static final Color COLOR_RED = new Color(220, 38, 38);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    // Document Statistics Labels
    private JLabel lblFileName, lblFileType, lblFileSize, lblCharCount, lblWordCount, lblLineCount;
    private JLabel lblSuffixCount, lblBuildTime, lblIndexStatus;

    // Search Controls
    private JTextField txtSearchPattern;
    private JCheckBox chkCaseSensitive;
    private JLabel lblStatus, lblOccurrences, lblSearchTime, lblPositions;

    // Tables & Models
    private JTable tblMatches, tblBsTrace, tblSaTable, tblPerf, tblTests;
    private DefaultTableModel modelMatches, modelBsTrace, modelSaTable, modelPerf, modelTests;

    // Input Components
    private JTextArea txtManualInput;
    private CardLayout cardInputLayout;
    private JPanel pnlInputCard;
    private JLabel lblPerfSummary;
    private JLabel lblTestSummary;

    public SwingApp() {
        setTitle("Fast Substrings Search Using Suffix Arrays");
        setSize(1260, 840);
        setMinimumSize(new Dimension(1050, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();

        // Working Default Demo on Launch: "BANANA BANDANA" with query "ANA"
        loadDefaultDemo();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(COLOR_BG);

        // -------------------------------------------------------------
        // Top Header Panel (Dark Navy, Clean Typography, Project Info ONLY)
        // -------------------------------------------------------------
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(COLOR_NAVY);
        headerPanel.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel titleLabel = new JLabel("FAST SUBSTRINGS SEARCH USING SUFFIX ARRAYS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(Color.WHITE);

        JLabel subTitleLabel = new JLabel("Java Implementation • Suffix Array + Binary Search");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitleLabel.setForeground(new Color(148, 163, 184));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);
        titleBox.add(titleLabel);
        titleBox.add(subTitleLabel);

        JButton btnQuickTestNav = new JButton("Run Test Suite");
        btnQuickTestNav.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickTestNav.setBackground(new Color(30, 41, 59));
        btnQuickTestNav.setForeground(Color.WHITE);
        btnQuickTestNav.setFocusPainted(false);
        btnQuickTestNav.addActionListener(e -> executeTestSuite());

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnQuickTestNav, BorderLayout.EAST);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // Main Split Pane (Left: Document Panel, Right: Search & Tabs)
        // -------------------------------------------------------------
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(440);
        splitPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        splitPane.setBackground(COLOR_BG);

        // Left Panel (Input & Document Stats)
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
        pnlLeft.setBackground(COLOR_BG);

        // Input Card Switcher
        cardInputLayout = new CardLayout();
        pnlInputCard = new JPanel(cardInputLayout);
        pnlInputCard.setBackground(COLOR_CARD_BG);
        pnlInputCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 8, 8, 8)
        ));

        // Card 1: File Upload
        JPanel pnlFileUpload = new JPanel(new GridBagLayout());
        pnlFileUpload.setBackground(COLOR_CARD_BG);
        JButton btnChooseFile = new JButton("Choose Document (.txt, .docx)");
        btnChooseFile.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChooseFile.setPreferredSize(new Dimension(280, 46));
        btnChooseFile.setBackground(new Color(241, 245, 249));
        btnChooseFile.setFocusPainted(false);
        btnChooseFile.addActionListener(e -> chooseAndLoadFile());
        pnlFileUpload.add(btnChooseFile);

        // Card 2: Manual Text
        JPanel pnlManualText = new JPanel(new BorderLayout(0, 6));
        pnlManualText.setBackground(COLOR_CARD_BG);
        txtManualInput = new JTextArea("BANANA BANDANA");
        txtManualInput.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtManualInput.setLineWrap(true);
        txtManualInput.setWrapStyleWord(true);
        JScrollPane scrollManual = new JScrollPane(txtManualInput);
        scrollManual.setPreferredSize(new Dimension(380, 120));

        JButton btnBuildManual = new JButton("Build Suffix Array from Text");
        btnBuildManual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuildManual.setBackground(COLOR_BLUE_PRIMARY);
        btnBuildManual.setForeground(Color.WHITE);
        btnBuildManual.setFocusPainted(false);
        btnBuildManual.addActionListener(e -> buildFromManualText());

        pnlManualText.add(scrollManual, BorderLayout.CENTER);
        pnlManualText.add(btnBuildManual, BorderLayout.SOUTH);

        pnlInputCard.add(pnlManualText, "MANUAL_TEXT");
        pnlInputCard.add(pnlFileUpload, "FILE_UPLOAD");

        // Input Mode Radio Switcher
        JPanel pnlModeSwitch = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        pnlModeSwitch.setBackground(COLOR_BG);
        JRadioButton rdoManual = new JRadioButton("Manual Text Entry", true);
        JRadioButton rdoFile = new JRadioButton("File Upload (.txt / .docx)");
        rdoManual.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rdoFile.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rdoManual.setBackground(COLOR_BG);
        rdoFile.setBackground(COLOR_BG);
        ButtonGroup grpMode = new ButtonGroup();
        grpMode.add(rdoManual);
        grpMode.add(rdoFile);

        rdoManual.addActionListener(e -> cardInputLayout.show(pnlInputCard, "MANUAL_TEXT"));
        rdoFile.addActionListener(e -> cardInputLayout.show(pnlInputCard, "FILE_UPLOAD"));

        pnlModeSwitch.add(rdoManual);
        pnlModeSwitch.add(rdoFile);

        JPanel pnlInputContainer = new JPanel(new BorderLayout(0, 4));
        pnlInputContainer.setBackground(COLOR_BG);
        pnlInputContainer.setBorder(createCleanTitledBorder("DOCUMENT INPUT"));
        pnlInputContainer.add(pnlModeSwitch, BorderLayout.NORTH);
        pnlInputContainer.add(pnlInputCard, BorderLayout.CENTER);

        // Document Statistics Panel
        JPanel pnlStats = new JPanel(new GridLayout(9, 2, 8, 6));
        pnlStats.setBackground(COLOR_CARD_BG);
        pnlStats.setBorder(BorderFactory.createCompoundBorder(
                createCleanTitledBorder("DOCUMENT STATISTICS"),
                new EmptyBorder(8, 12, 10, 12)
        ));

        lblFileName = createValueLabel("-");
        lblFileType = createValueLabel("-");
        lblFileSize = createValueLabel("-");
        lblCharCount = createValueLabel("0");
        lblWordCount = createValueLabel("0");
        lblLineCount = createValueLabel("0");
        lblSuffixCount = createValueLabel("0");
        lblBuildTime = createValueLabel("0.000 ms");
        lblIndexStatus = createValueLabel("NOT INDEXED");

        addStatRow(pnlStats, "File Name:", lblFileName);
        addStatRow(pnlStats, "File Type:", lblFileType);
        addStatRow(pnlStats, "File Size:", lblFileSize);
        addStatRow(pnlStats, "Characters:", lblCharCount);
        addStatRow(pnlStats, "Words:", lblWordCount);
        addStatRow(pnlStats, "Lines:", lblLineCount);
        addStatRow(pnlStats, "Suffixes Indexed:", lblSuffixCount);
        addStatRow(pnlStats, "SA Build Time:", lblBuildTime);
        addStatRow(pnlStats, "Index Status:", lblIndexStatus);

        pnlLeft.add(pnlInputContainer, BorderLayout.NORTH);
        pnlLeft.add(pnlStats, BorderLayout.CENTER);

        // -------------------------------------------------------------
        // Right Panel (Search Controls & Tabbed Visualizations)
        // -------------------------------------------------------------
        JPanel pnlRight = new JPanel(new BorderLayout(0, 10));
        pnlRight.setBackground(COLOR_BG);

        // Search Input Card
        JPanel pnlSearchBox = new JPanel(new BorderLayout(8, 8));
        pnlSearchBox.setBackground(COLOR_CARD_BG);
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
                createCleanTitledBorder("SUBSTRING SEARCH (SUFFIX ARRAY + BINARY SEARCH)"),
                new EmptyBorder(8, 12, 8, 12)
        ));

        txtSearchPattern = new JTextField("ANA");
        txtSearchPattern.setFont(new Font("Consolas", Font.BOLD, 14));
        txtSearchPattern.setPreferredSize(new Dimension(250, 34));
        txtSearchPattern.addActionListener(e -> executeSearch());

        JButton btnSearch = new JButton("SEARCH");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(COLOR_BLUE_PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> executeSearch());

        JButton btnClear = new JButton("CLEAR");
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setBackground(new Color(241, 245, 249));
        btnClear.setForeground(new Color(51, 65, 85));
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> clearSearch());

        JPanel pnlSearchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlSearchButtons.setBackground(COLOR_CARD_BG);
        pnlSearchButtons.add(btnSearch);
        pnlSearchButtons.add(btnClear);

        chkCaseSensitive = new JCheckBox("Case Sensitive", true);
        chkCaseSensitive.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCaseSensitive.setBackground(COLOR_CARD_BG);

        JPanel pnlSearchTopBar = new JPanel(new BorderLayout(8, 0));
        pnlSearchTopBar.setBackground(COLOR_CARD_BG);
        pnlSearchTopBar.add(txtSearchPattern, BorderLayout.CENTER);
        pnlSearchTopBar.add(pnlSearchButtons, BorderLayout.EAST);

        pnlSearchBox.add(pnlSearchTopBar, BorderLayout.CENTER);
        pnlSearchBox.add(chkCaseSensitive, BorderLayout.SOUTH);

        // Search Results Summary Banner
        JPanel pnlSummary = new JPanel(new GridLayout(2, 2, 10, 4));
        pnlSummary.setBackground(COLOR_CARD_BG);
        pnlSummary.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        lblStatus = new JLabel("READY");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(COLOR_BLUE_PRIMARY);

        lblOccurrences = new JLabel("Occurrences: 0");
        lblOccurrences.setFont(new Font("Segoe UI", Font.BOLD, 13));

        lblSearchTime = new JLabel("Search Time: 0.000 ms");
        lblSearchTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblPositions = new JLabel("Positions: None");
        lblPositions.setFont(new Font("Consolas", Font.PLAIN, 12));

        pnlSummary.add(lblStatus);
        pnlSummary.add(lblOccurrences);
        pnlSummary.add(lblSearchTime);
        pnlSummary.add(lblPositions);

        JPanel pnlSearchHeader = new JPanel(new BorderLayout(0, 6));
        pnlSearchHeader.setBackground(COLOR_BG);
        pnlSearchHeader.add(pnlSearchBox, BorderLayout.NORTH);
        pnlSearchHeader.add(pnlSummary, BorderLayout.SOUTH);

        // -------------------------------------------------------------
        // Tabbed Visualizations Panel
        // -------------------------------------------------------------
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab 1: Matching Snippets
        modelMatches = new DefaultTableModel(new String[]{"Match #", "Position", "Line #", "Context Snippet"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblMatches = new JTable(modelMatches);
        styleTable(tblMatches);
        tblMatches.getColumnModel().getColumn(0).setPreferredWidth(65);
        tblMatches.getColumnModel().getColumn(1).setPreferredWidth(85);
        tblMatches.getColumnModel().getColumn(2).setPreferredWidth(70);
        tblMatches.getColumnModel().getColumn(3).setPreferredWidth(750);
        tabbedPane.addTab("Matching Snippets", new JScrollPane(tblMatches));

        // Tab 2: Binary Search Trace
        modelBsTrace = new DefaultTableModel(new String[]{
                "Step", "Phase", "Low", "High", "Mid", "Position", "Compared Suffix", "Comparison Result", "Action"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblBsTrace = new JTable(modelBsTrace);
        styleTable(tblBsTrace);
        tblBsTrace.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblBsTrace.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblBsTrace.getColumnModel().getColumn(2).setPreferredWidth(50);
        tblBsTrace.getColumnModel().getColumn(3).setPreferredWidth(50);
        tblBsTrace.getColumnModel().getColumn(4).setPreferredWidth(50);
        tblBsTrace.getColumnModel().getColumn(5).setPreferredWidth(65);
        tblBsTrace.getColumnModel().getColumn(6).setPreferredWidth(160);
        tblBsTrace.getColumnModel().getColumn(7).setPreferredWidth(180);
        tblBsTrace.getColumnModel().getColumn(8).setPreferredWidth(260);
        tabbedPane.addTab("Binary Search Trace", new JScrollPane(tblBsTrace));

        // Tab 3: Suffix Array Table
        modelSaTable = new DefaultTableModel(new String[]{"SA Index", "Position", "Suffix"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblSaTable = new JTable(modelSaTable);
        styleTable(tblSaTable);
        tblSaTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblSaTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        tblSaTable.getColumnModel().getColumn(2).setPreferredWidth(780);
        tabbedPane.addTab("Suffix Array Table", new JScrollPane(tblSaTable));

        // Tab 4: Performance Comparison
        JPanel pnlPerfTab = new JPanel(new BorderLayout(0, 8));
        pnlPerfTab.setBackground(COLOR_CARD_BG);
        pnlPerfTab.setBorder(new EmptyBorder(8, 8, 8, 8));

        modelPerf = new DefaultTableModel(new String[]{
                "Algorithm", "Execution Time", "Occurrences Found", "Theoretical Complexity"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPerf = new JTable(modelPerf);
        styleTable(tblPerf);
        tblPerf.getColumnModel().getColumn(0).setPreferredWidth(240);
        tblPerf.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblPerf.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblPerf.getColumnModel().getColumn(3).setPreferredWidth(180);

        lblPerfSummary = new JLabel("Perform a search to calculate live performance metrics comparing Naive Search vs Suffix Array.");
        lblPerfSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPerfSummary.setForeground(COLOR_BLUE_PRIMARY);
        lblPerfSummary.setBorder(new EmptyBorder(6, 6, 6, 6));

        pnlPerfTab.add(new JScrollPane(tblPerf), BorderLayout.CENTER);
        pnlPerfTab.add(lblPerfSummary, BorderLayout.SOUTH);
        tabbedPane.addTab("Performance Comparison", pnlPerfTab);

        // Tab 5: Test Suite
        JPanel pnlTestTab = new JPanel(new BorderLayout(0, 8));
        pnlTestTab.setBackground(COLOR_CARD_BG);
        pnlTestTab.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel pnlTestTop = new JPanel(new BorderLayout(10, 0));
        pnlTestTop.setBackground(COLOR_CARD_BG);

        lblTestSummary = new JLabel("Click 'Run Automated Test Suite' to execute all 15 algorithmic test cases live.");
        lblTestSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTestSummary.setForeground(COLOR_NAVY);

        JButton btnRunTestsInside = new JButton("Run Automated Test Suite");
        btnRunTestsInside.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRunTestsInside.setBackground(COLOR_BLUE_PRIMARY);
        btnRunTestsInside.setForeground(Color.WHITE);
        btnRunTestsInside.setFocusPainted(false);
        btnRunTestsInside.addActionListener(e -> executeTestSuite());

        pnlTestTop.add(lblTestSummary, BorderLayout.CENTER);
        pnlTestTop.add(btnRunTestsInside, BorderLayout.EAST);

        modelTests = new DefaultTableModel(new String[]{
                "Test #", "Test Name", "Query", "Expected Result", "Actual Output", "Status", "Execution Time"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTests = new JTable(modelTests);
        styleTable(tblTests);
        tblTests.getColumnModel().getColumn(0).setPreferredWidth(55);
        tblTests.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblTests.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblTests.getColumnModel().getColumn(3).setPreferredWidth(170);
        tblTests.getColumnModel().getColumn(4).setPreferredWidth(170);
        tblTests.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblTests.getColumnModel().getColumn(6).setPreferredWidth(100);

        pnlTestTab.add(pnlTestTop, BorderLayout.NORTH);
        pnlTestTab.add(new JScrollPane(tblTests), BorderLayout.CENTER);
        tabbedPane.addTab("Test Suite", pnlTestTab);

        pnlRight.add(pnlSearchHeader, BorderLayout.NORTH);
        pnlRight.add(tabbedPane, BorderLayout.CENTER);

        splitPane.setLeftComponent(pnlLeft);
        splitPane.setRightComponent(pnlRight);
        contentPane.add(splitPane, BorderLayout.CENTER);

        setContentPane(contentPane);
    }

    private void loadDefaultDemo() {
        // Default sample as requested: "BANANA BANDANA" with default query "ANA"
        txtManualInput.setText("BANANA BANDANA");
        txtSearchPattern.setText("ANA");
        loadManualTextInternal("BANANA BANDANA", "Sample: BANANA BANDANA");
        executeSearch();
    }

    private void chooseAndLoadFile() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileFilter(new FileNameExtensionFilter("Text & Word Documents (.txt, .docx)", "txt", "docx"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                DocumentReader.ReadResult doc = DocumentReader.readFile(selectedFile);
                updateDocument(doc.text, doc.metadata);
                executeSearch();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load document: " + ex.getMessage(),
                        "Document Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buildFromManualText() {
        String text = txtManualInput.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Manual text cannot be empty.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadManualTextInternal(text, "Manual Text Entry");
        executeSearch();
    }

    private void loadManualTextInternal(String text, String name) {
        DocumentReader.ReadResult doc = DocumentReader.readManualText(text, name);
        updateDocument(doc.text, doc.metadata);
    }

    private void updateDocument(String text, DocumentReader.DocumentMetadata meta) {
        this.loadedText = text != null ? text : "";
        this.currentMetadata = meta;

        // Construct Suffix Array dynamically
        this.currentSuffixArray = new SuffixArray(this.loadedText);

        // Update Statistics Panel
        lblFileName.setText(meta.fileName);
        lblFileType.setText(meta.fileType);
        lblFileSize.setText(meta.fileSizeFormatted);
        lblCharCount.setText(String.format("%,d", meta.characterCount));
        lblWordCount.setText(String.format("%,d", meta.wordCount));
        lblLineCount.setText(String.format("%,d", meta.lineCount));
        lblSuffixCount.setText(String.format("%,d", currentSuffixArray.getN()));
        lblBuildTime.setText(PerformanceMonitor.formatDurationMs(currentSuffixArray.getBuildTimeMs()));
        lblIndexStatus.setText("INDEXED");
        lblIndexStatus.setForeground(COLOR_GREEN);

        // Dynamically populate Suffix Array Table
        populateSaTable();

        lblStatus.setText("INDEXED");
        lblStatus.setForeground(COLOR_GREEN);
    }

    private void populateSaTable() {
        modelSaTable.setRowCount(0);
        if (currentSuffixArray == null || loadedText.isEmpty()) return;

        int[] sa = currentSuffixArray.getSa();
        int n = currentSuffixArray.getN();
        int limit = Math.min(n, 500); // Display all for small texts, up to 500 for large docs
        String text = currentSuffixArray.getText();

        for (int i = 0; i < limit; i++) {
            int pos = sa[i];
            int end = Math.min(text.length(), pos + (n <= 100 ? 100 : 60));
            String preview = text.substring(pos, end).replace("\r", " ").replace("\n", " ");
            if (end < text.length()) preview += "...";

            modelSaTable.addRow(new Object[]{i, pos, preview});
        }
    }

    private void executeSearch() {
        if (currentSuffixArray == null || loadedText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load a document or text first.",
                    "Search Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String pattern = txtSearchPattern.getText();
        if (pattern == null || pattern.isEmpty()) {
            lblStatus.setText("EMPTY PATTERN");
            lblStatus.setForeground(COLOR_RED);
            lblOccurrences.setText("Occurrences: 0");
            lblPositions.setText("Positions: None");
            modelMatches.setRowCount(0);
            modelBsTrace.setRowCount(0);
            return;
        }

        boolean caseSens = chkCaseSensitive.isSelected();

        // 1. Execute actual Suffix Array + Binary Search
        SearchResult result = PatternSearch.search(currentSuffixArray, pattern, caseSens, 40);

        // 2. Execute actual Naive Search for real Performance Comparison
        PatternSearch.PerformanceComparison comp = PatternSearch.compareSearch(currentSuffixArray, pattern, caseSens, 40);

        // Update Summary Status Bar
        if (result.getStatus() == SearchResult.Status.FOUND) {
            lblStatus.setText("FOUND");
            lblStatus.setForeground(COLOR_GREEN);
        } else {
            lblStatus.setText("NOT FOUND");
            lblStatus.setForeground(COLOR_RED);
        }

        lblOccurrences.setText("Occurrences: " + result.getOccurrencesCount());
        lblSearchTime.setText("Search Time: " + PerformanceMonitor.formatDurationMs(result.getSearchTimeMs()));
        lblPositions.setText("Positions: " + result.getPositionsFormatted());

        // Update Tab 1: Matching Snippets Table
        modelMatches.setRowCount(0);
        List<SearchResult.MatchItem> matches = result.getMatches();
        for (int i = 0; i < matches.size(); i++) {
            SearchResult.MatchItem item = matches.get(i);
            modelMatches.addRow(new Object[]{i + 1, item.position, item.lineNumber, item.highlightedSnippet});
        }

        // Update Tab 2: Binary Search Trace Table
        modelBsTrace.setRowCount(0);
        List<SearchResult.BinarySearchStep> steps = result.getBinarySearchSteps();
        for (SearchResult.BinarySearchStep s : steps) {
            modelBsTrace.addRow(new Object[]{
                    s.step, s.phase, s.low, s.high, s.mid, s.suffixPos, s.comparedSuffix, s.comparisonResult, s.action
            });
        }

        // Update Tab 4: Performance Comparison Table
        modelPerf.setRowCount(0);
        modelPerf.addRow(new Object[]{
                "Naive String Search",
                PerformanceMonitor.formatDurationMs(comp.naiveResult.searchTimeMs),
                comp.naiveResult.occurrencesCount,
                "O(N * M)"
        });
        modelPerf.addRow(new Object[]{
                "Suffix Array + Binary Search",
                PerformanceMonitor.formatDurationMs(comp.saResult.getSearchTimeMs()),
                comp.saResult.getOccurrencesCount(),
                "O(M log N)"
        });

        if (comp.speedupFactor > 1.0) {
            lblPerfSummary.setText(String.format(
                    "Result: Suffix Array + Binary Search was %.2fx faster than Naive Search for query \"%s\" on %d characters.",
                    comp.speedupFactor, pattern, loadedText.length()
            ));
        } else {
            lblPerfSummary.setText(String.format(
                    "Both algorithms found %d occurrences in under 1 ms for small text of length %d.",
                    result.getOccurrencesCount(), loadedText.length()
            ));
        }
    }

    private void clearSearch() {
        txtSearchPattern.setText("");
        lblStatus.setText(currentSuffixArray != null ? "INDEXED" : "READY");
        lblStatus.setForeground(currentSuffixArray != null ? COLOR_GREEN : COLOR_BLUE_PRIMARY);
        lblOccurrences.setText("Occurrences: 0");
        lblSearchTime.setText("Search Time: 0.000 ms");
        lblPositions.setText("Positions: None");
        modelMatches.setRowCount(0);
        modelBsTrace.setRowCount(0);
        modelPerf.setRowCount(0);
        lblPerfSummary.setText("Perform a search to calculate live performance metrics comparing Naive Search vs Suffix Array.");
    }

    private void executeTestSuite() {
        modelTests.setRowCount(0);
        List<TestCases.TestCaseResult> results = TestCases.runAllTestsDetailed();
        int passed = 0;

        for (TestCases.TestCaseResult r : results) {
            if (r.passed) passed++;
            String statusHtml = r.passed ?
                    "<html><b style='color:#16a34a;'>PASSED</b></html>" :
                    "<html><b style='color:#dc2626;'>FAILED</b></html>";

            modelTests.addRow(new Object[]{
                    r.testNumber, r.testName, r.query, r.expected, r.actual, statusHtml,
                    PerformanceMonitor.formatDurationMs(r.timeMs)
            });
        }

        int total = results.size();
        int failed = total - passed;
        lblTestSummary.setText(String.format(
                "Test Suite Execution Complete: Total = %d | Passed = %d | Failed = %d",
                total, passed, failed
        ));

        if (failed == 0) {
            lblTestSummary.setForeground(COLOR_GREEN);
        } else {
            lblTestSummary.setForeground(COLOR_RED);
        }
    }

    // -------------------------------------------------------------
    // Helper Styling & UI Factory Methods
    // -------------------------------------------------------------
    private static void styleTable(JTable table) {
        table.setFont(new Font("Consolas", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setGridColor(COLOR_BORDER);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(30, 41, 59));
        table.getTableHeader().setReorderingAllowed(false);
    }

    private static TitledBorder createCleanTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDER, 1),
                " " + title + " "
        );
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 11));
        border.setTitleColor(COLOR_TEXT_MUTED);
        return border;
    }

    private static JLabel createValueLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(30, 41, 59));
        return lbl;
    }

    private static void addStatRow(JPanel panel, String labelText, JLabel valueLabel) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(COLOR_TEXT_MUTED);
        panel.add(lbl);
        panel.add(valueLabel);
    }
}
