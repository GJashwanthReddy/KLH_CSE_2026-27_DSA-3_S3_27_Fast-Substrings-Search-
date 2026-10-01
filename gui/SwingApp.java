import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * SwingApp.java
 * ============
 * Standalone Java Swing GUI application for project review demonstration.
 * Provides file upload (.txt, .docx), manual text entry, Suffix Array construction,
 * Binary Search querying, step-by-step visualization, and automated test runner.
 */
public class SwingApp extends JFrame {

    private String loadedText = "";
    private DocumentReader.DocumentMetadata currentMetadata = null;
    private SuffixArray currentSuffixArray = null;

    // UI Components
    private JLabel lblFileName, lblFileType, lblFileSize, lblCharCount, lblWordCount, lblBuildTime;
    private JTextField txtSearchPattern;
    private JCheckBox chkCaseSensitive;
    private JLabel lblStatus, lblOccurrences, lblSearchTime;
    private JTable tblMatches, tblBsTrace, tblSaTable;
    private DefaultTableModel modelMatches, modelBsTrace, modelSaTable;
    private JTextArea txtManualInput;
    private JTextArea txtTestLog;
    private CardLayout cardInputLayout;
    private JPanel pnlInputCard;

    public SwingApp() {
        setTitle("Fast Substrings Search Using Suffix Arrays | Java DSA Project");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        // Set System Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        
        JLabel titleLabel = new JLabel("Fast Substrings Search Using Suffix Arrays");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subTitleLabel = new JLabel("B.Tech DSA Capstone Project • Java Implementation (Suffix Array + Binary Search)");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitleLabel.setForeground(new Color(148, 163, 184));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(titleLabel);
        titleBox.add(subTitleLabel);

        JButton btnRunTestsNav = new JButton("Run 12 Test Cases");
        btnRunTestsNav.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRunTestsNav.addActionListener(e -> runAutomatedTests());

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnRunTestsNav, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Split View (Left: Document Panel, Right: Search & Visualizations)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(450);

        // Left Panel: Input & Document Stats
        JPanel pnlLeft = new JPanel(new BorderLayout(10, 10));

        // Input Card Panel
        cardInputLayout = new CardLayout();
        pnlInputCard = new JPanel(cardInputLayout);

        // Card 1: File Upload Panel
        JPanel pnlFileUpload = new JPanel(new GridBagLayout());
        pnlFileUpload.setBorder(BorderFactory.createTitledBorder("Upload Document (.txt / .docx)"));

        JButton btnChooseFile = new JButton("Choose Document (.txt, .docx)");
        btnChooseFile.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnChooseFile.setPreferredSize(new Dimension(280, 50));
        btnChooseFile.addActionListener(e -> chooseAndLoadFile());

        pnlFileUpload.add(btnChooseFile);

        // Card 2: Manual Text Panel
        JPanel pnlManualText = new JPanel(new BorderLayout(5, 5));
        pnlManualText.setBorder(BorderFactory.createTitledBorder("Enter Manual Text"));
        txtManualInput = new JTextArea("banana\nmississippi\nalgorithm data structure");
        txtManualInput.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollManual = new JScrollPane(txtManualInput);

        JButton btnBuildManual = new JButton("Build Suffix Array from Text");
        btnBuildManual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuildManual.addActionListener(e -> buildFromManualText());

        pnlManualText.add(scrollManual, BorderLayout.CENTER);
        pnlManualText.add(btnBuildManual, BorderLayout.SOUTH);

        pnlInputCard.add(pnlFileUpload, "FILE_UPLOAD");
        pnlInputCard.add(pnlManualText, "MANUAL_TEXT");

        // Input Mode Switch Buttons
        JPanel pnlModeSwitch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton rdoFile = new JRadioButton("File Upload", true);
        JRadioButton rdoManual = new JRadioButton("Manual Text");
        ButtonGroup grpMode = new ButtonGroup();
        grpMode.add(rdoFile);
        grpMode.add(rdoManual);

        rdoFile.addActionListener(e -> cardInputLayout.show(pnlInputCard, "FILE_UPLOAD"));
        rdoManual.addActionListener(e -> cardInputLayout.show(pnlInputCard, "MANUAL_TEXT"));

        pnlModeSwitch.add(rdoFile);
        pnlModeSwitch.add(rdoManual);

        JPanel pnlTopLeft = new JPanel(new BorderLayout());
        pnlTopLeft.add(pnlModeSwitch, BorderLayout.NORTH);
        pnlTopLeft.add(pnlInputCard, BorderLayout.CENTER);

        // Document Stats Panel
        JPanel pnlStats = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlStats.setBorder(BorderFactory.createTitledBorder("Document Statistics"));

        lblFileName = new JLabel("-");
        lblFileType = new JLabel("-");
        lblFileSize = new JLabel("-");
        lblCharCount = new JLabel("0");
        lblWordCount = new JLabel("0");
        lblBuildTime = new JLabel("0.000 ms");

        pnlStats.add(new JLabel("File Name:")); pnlStats.add(lblFileName);
        pnlStats.add(new JLabel("File Type:")); pnlStats.add(lblFileType);
        pnlStats.add(new JLabel("File Size:")); pnlStats.add(lblFileSize);
        pnlStats.add(new JLabel("Characters:")); pnlStats.add(lblCharCount);
        pnlStats.add(new JLabel("Words:")); pnlStats.add(lblWordCount);
        pnlStats.add(new JLabel("SA Build Time:")); pnlStats.add(lblBuildTime);

        pnlLeft.add(pnlTopLeft, BorderLayout.NORTH);
        pnlLeft.add(pnlStats, BorderLayout.CENTER);

        // Right Panel: Search Box & Visualizations
        JPanel pnlRight = new JPanel(new BorderLayout(10, 10));

        // Search Input Box Panel
        JPanel pnlSearchBox = new JPanel(new BorderLayout(8, 8));
        pnlSearchBox.setBorder(BorderFactory.createTitledBorder("Binary Substring Search (Suffix Array)"));

        txtSearchPattern = new JTextField("ana");
        txtSearchPattern.setFont(new Font("Monospaced", Font.PLAIN, 15));
        txtSearchPattern.addActionListener(e -> executeSearch());

        JButton btnSearch = new JButton("SEARCH");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setBackground(new Color(14, 165, 233));
        btnSearch.setForeground(Color.BLACK);
        btnSearch.addActionListener(e -> executeSearch());

        chkCaseSensitive = new JCheckBox("Case Sensitive", true);

        JPanel pnlSearchControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearchControls.add(chkCaseSensitive);

        pnlSearchBox.add(txtSearchPattern, BorderLayout.CENTER);
        pnlSearchBox.add(btnSearch, BorderLayout.EAST);
        pnlSearchBox.add(pnlSearchControls, BorderLayout.SOUTH);

        // Search Results Summary Banner
        JPanel pnlSummary = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        pnlSummary.setBorder(BorderFactory.createEtchedBorder());

        lblStatus = new JLabel("READY");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblOccurrences = new JLabel("Occurrences: 0");
        lblOccurrences.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblSearchTime = new JLabel("Time: 0.000 ms");
        lblSearchTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        pnlSummary.add(new JLabel("Status: "));
        pnlSummary.add(lblStatus);
        pnlSummary.add(lblOccurrences);
        pnlSummary.add(lblSearchTime);

        JPanel pnlSearchTop = new JPanel(new BorderLayout(5, 5));
        pnlSearchTop.add(pnlSearchBox, BorderLayout.NORTH);
        pnlSearchTop.add(pnlSummary, BorderLayout.SOUTH);

        // Tabbed Visualizations Panel
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Matching Snippets
        modelMatches = new DefaultTableModel(new String[]{"Match #", "Position", "Line #", "Context Snippet"}, 0);
        tblMatches = new JTable(modelMatches);
        tblMatches.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tblMatches.setRowHeight(28);
        if (tblMatches.getColumnModel().getColumnCount() > 3) {
            tblMatches.getColumnModel().getColumn(0).setPreferredWidth(60);
            tblMatches.getColumnModel().getColumn(1).setPreferredWidth(90);
            tblMatches.getColumnModel().getColumn(2).setPreferredWidth(80);
            tblMatches.getColumnModel().getColumn(3).setPreferredWidth(700);
        }
        tabbedPane.addTab("Matching Snippets", new JScrollPane(tblMatches));

        // Tab 2: Binary Search Trace Step-by-Step
        modelBsTrace = new DefaultTableModel(new String[]{"Step", "Phase", "Low", "Mid", "High", "Pos", "Suffix Preview", "Comparison Decision"}, 0);
        tblBsTrace = new JTable(modelBsTrace);
        tblBsTrace.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tblBsTrace.setRowHeight(22);
        tabbedPane.addTab("Binary Search Trace", new JScrollPane(tblBsTrace));

        // Tab 3: Suffix Array Table
        modelSaTable = new DefaultTableModel(new String[]{"SA Index", "Text Position", "Suffix Preview"}, 0);
        tblSaTable = new JTable(modelSaTable);
        tblSaTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tblSaTable.setRowHeight(22);
        tabbedPane.addTab("Suffix Array Table", new JScrollPane(tblSaTable));

        // Tab 4: Automated Test Runner Log
        txtTestLog = new JTextArea("Click 'Run 12 Test Cases' to verify all test scenarios in real-time.\n");
        txtTestLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtTestLog.setEditable(false);
        tabbedPane.addTab("Test Suite Log", new JScrollPane(txtTestLog));

        pnlRight.add(pnlSearchTop, BorderLayout.NORTH);
        pnlRight.add(tabbedPane, BorderLayout.CENTER);

        mainSplitPane.setLeftComponent(pnlLeft);
        mainSplitPane.setRightComponent(pnlRight);
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Load initial default sample
        loadManualTextInternal("banana", "Default Sample");
    }

    private void chooseAndLoadFile() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileFilter(new FileNameExtensionFilter("Documents (.txt, .docx)", "txt", "docx"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                DocumentReader.ReadResult doc = DocumentReader.readFile(selectedFile);
                updateDocument(doc.text, doc.metadata);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to read document: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buildFromManualText() {
        String text = txtManualInput.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Manual text cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadManualTextInternal(text, "Manual Text");
    }

    private void loadManualTextInternal(String text, String name) {
        DocumentReader.ReadResult doc = DocumentReader.readManualText(text, name);
        updateDocument(doc.text, doc.metadata);
    }

    private void updateDocument(String text, DocumentReader.DocumentMetadata meta) {
        this.loadedText = text;
        this.currentMetadata = meta;

        // Build Suffix Array
        this.currentSuffixArray = new SuffixArray(this.loadedText);

        // Update Stats UI
        lblFileName.setText(meta.fileName);
        lblFileType.setText(meta.fileType);
        lblFileSize.setText(meta.fileSizeFormatted);
        lblCharCount.setText(String.format("%,d", meta.characterCount));
        lblWordCount.setText(String.format("%,d", meta.wordCount));
        lblBuildTime.setText(PerformanceMonitor.formatDurationMs(currentSuffixArray.getBuildTimeMs()));

        // Populate Suffix Array Table View (first 100 entries)
        populateSaTable();

        lblStatus.setText("INDEXED");
        lblStatus.setForeground(new Color(16, 185, 129));
    }

    private void populateSaTable() {
        modelSaTable.setRowCount(0);
        if (currentSuffixArray == null) return;

        int[] sa = currentSuffixArray.getSa();
        int limit = Math.min(sa.length, 100);
        String text = currentSuffixArray.getText();

        for (int i = 0; i < limit; i++) {
            int pos = sa[i];
            int end = Math.min(text.length(), pos + 60);
            String preview = text.substring(pos, end);
            if (end < text.length()) preview += "...";

            modelSaTable.addRow(new Object[]{i, pos, preview});
        }
    }

    private void executeSearch() {
        if (currentSuffixArray == null || loadedText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load a document first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pattern = txtSearchPattern.getText();
        if (pattern == null || pattern.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search pattern cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean caseSens = chkCaseSensitive.isSelected();
        SearchResult result = PatternSearch.search(currentSuffixArray, pattern, caseSens, 40);

        // Update Summary Banner
        if (result.getStatus() == SearchResult.Status.FOUND) {
            lblStatus.setText("FOUND");
            lblStatus.setForeground(new Color(16, 185, 129));
        } else {
            lblStatus.setText("NOT FOUND");
            lblStatus.setForeground(new Color(239, 68, 68));
        }

        lblOccurrences.setText("Occurrences: " + result.getOccurrencesCount());
        lblSearchTime.setText("Time: " + PerformanceMonitor.formatDurationMs(result.getSearchTimeMs()));

        // Populate Matches Table
        modelMatches.setRowCount(0);
        List<SearchResult.MatchItem> matches = result.getMatches();
        for (int i = 0; i < matches.size(); i++) {
            SearchResult.MatchItem item = matches.get(i);
            modelMatches.addRow(new Object[]{i + 1, item.position, item.lineNumber, item.highlightedSnippet});
        }

        // Populate Binary Search Trace Table
        modelBsTrace.setRowCount(0);
        List<SearchResult.BinarySearchStep> steps = result.getBinarySearchSteps();
        for (SearchResult.BinarySearchStep s : steps) {
            modelBsTrace.addRow(new Object[]{s.step, s.phase, s.low, s.mid, s.high, s.suffixPos, s.suffixPreview, s.comparison});
        }
    }

    private void runAutomatedTests() {
        txtTestLog.setText("Executing 12 Automated Test Cases...\n\n");
        boolean allPassed = TestCases.runAllTests();
        txtTestLog.append("Results summary check complete. Status: " + (allPassed ? "ALL 12 PASSED" : "SOME FAILED") + "\n");
        JOptionPane.showMessageDialog(this, allPassed ? "ALL 12 TEST CASES PASSED SUCCESSFULLY!" : "Some tests failed. Check log.", "Test Runner", allPassed ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}
