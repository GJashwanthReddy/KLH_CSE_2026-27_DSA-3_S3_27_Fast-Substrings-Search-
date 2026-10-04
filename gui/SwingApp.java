import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;

/**
 * SwingApp.java
 * ============
 * Modern Glassmorphism Desktop UI for Fast Substrings Search Using Suffix Arrays.
 * Features:
 * - Truly semi-transparent window & panels (80-90% opacity) allowing the desktop
 *   wallpaper/background to subtly show through without any bundled wallpaper images.
 * - Layered glass cards, soft drop shadows, rounded corners, thin light borders.
 * - Full window control (custom minimize, maximize/restore, close buttons and window dragging).
 * - High readability for tables, search field, text areas, and statistics.
 * - 100% preservation of core Suffix Array + Binary Search algorithms.
 * - Strictly project-focused with zero personal/college metadata.
 */
public class SwingApp extends JFrame {

    private String loadedText = "";
    private DocumentReader.DocumentMetadata currentMetadata = null;
    private SuffixArray currentSuffixArray = null;

    // Window Translucency & Dragging State
    private boolean isTranslucentMode = false;
    private Point dragOffset = null;

    // Theme Palette (Glassmorphism & Clean Typography)
    private static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);       // Slate 900
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    private static final Color COLOR_TEXT_LIGHT = new Color(148, 163, 184);   // Slate 400
    private static final Color COLOR_BLUE_ACCENT = new Color(37, 99, 235);    // Blue 600
    private static final Color COLOR_GREEN = new Color(22, 163, 74);          // Green 600
    private static final Color COLOR_RED = new Color(220, 38, 38);            // Red 600

    // Calibrated Glass Panel Opacity (80–90% for high contrast and readability)
    private static final Color COLOR_CARD_FILL_TOP = new Color(255, 255, 255, 220);    // ~86% opacity
    private static final Color COLOR_CARD_FILL_BOTTOM = new Color(255, 255, 255, 195); // ~76% opacity
    private static final Color COLOR_BORDER_SUBTLE = new Color(255, 255, 255, 230);    // ~90% opacity
    private static final Color COLOR_TABLE_HEADER = new Color(241, 245, 249, 235);

    // Document Statistics Labels
    private JLabel lblFileName, lblFileType, lblFileSize, lblCharCount, lblWordCount, lblLineCount;
    private JLabel lblSuffixCount, lblBuildTime;
    private StatusBadge lblIndexStatus;

    // Search Controls
    private JTextField txtSearchPattern;
    private JCheckBox chkCaseSensitive;
    private StatusBadge badgeSearchStatus;
    private JLabel lblMetricOccurrences, lblMetricPositions, lblMetricTime;

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

        // Enable per-pixel translucency so the desktop wallpaper subtly shows through
        try {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
                setUndecorated(true);
                setBackground(new Color(0, 0, 0, 0));
                isTranslucentMode = true;
            }
        } catch (Throwable ignored) {
            isTranslucentMode = false;
        }

        setSize(1300, 880);
        setMinimumSize(new Dimension(1080, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();

        // Load Default Working Demo dynamically on startup: "BANANA BANDANA" with query "ANA"
        loadDefaultDemo();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Root Container with Frosted Glass Backdrop (allows desktop to subtly show through)
        FrostedBackgroundPanel rootPanel = new FrostedBackgroundPanel(isTranslucentMode);
        rootPanel.setLayout(new BorderLayout(0, 10));
        rootPanel.setBorder(new EmptyBorder(10, 14, 14, 14));

        // Install window resize listener for undecorated translucent window
        if (isTranslucentMode) {
            installWindowResizer(rootPanel);
        }

        // -------------------------------------------------------------
        // 1. TOP HEADER (Translucent Glass Bar with Window Controls)
        // -------------------------------------------------------------
        GlassCard headerCard = new GlassCard(16,
                new Color(15, 23, 42, 215), new Color(30, 41, 59, 205),
                new Color(255, 255, 255, 45), new Color(0, 0, 0, 25));
        headerCard.setLayout(new BorderLayout(16, 0));
        headerCard.setBorder(new EmptyBorder(12, 20, 12, 16));

        // Enable window dragging and double-click maximize on header
        if (isTranslucentMode) {
            headerCard.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragOffset = e.getPoint();
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        toggleMaximize();
                    }
                }
            });
            headerCard.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragOffset != null) {
                        Point p = e.getLocationOnScreen();
                        setLocation(p.x - dragOffset.x, p.y - dragOffset.y);
                    }
                }
            });
        }

        JLabel titleLabel = new JLabel("FAST SUBSTRINGS SEARCH USING SUFFIX ARRAYS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subTitleLabel = new JLabel("Java Implementation • Suffix Array + Binary Search");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitleLabel.setForeground(COLOR_TEXT_LIGHT);

        JPanel pnlTitleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        pnlTitleBox.setOpaque(false);
        pnlTitleBox.add(titleLabel);
        pnlTitleBox.add(subTitleLabel);

        // Header Actions (Run Test Suite + Window Controls)
        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlHeaderRight.setOpaque(false);

        GlassButton btnHeaderTest = new GlassButton("RUN TEST SUITE", GlassButton.Style.HEADER_NAV);
        btnHeaderTest.setPreferredSize(new Dimension(145, 34));
        btnHeaderTest.addActionListener(e -> executeTestSuite());
        pnlHeaderRight.add(btnHeaderTest);

        if (isTranslucentMode) {
            JPanel pnlWinControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            pnlWinControls.setOpaque(false);

            WindowControlButton btnMin = new WindowControlButton("—", false);
            btnMin.setToolTipText("Minimize");
            btnMin.addActionListener(e -> setState(Frame.ICONIFIED));

            WindowControlButton btnMax = new WindowControlButton("□", false);
            btnMax.setToolTipText("Maximize / Restore");
            btnMax.addActionListener(e -> toggleMaximize());

            WindowControlButton btnClose = new WindowControlButton("✕", true);
            btnClose.setToolTipText("Close");
            btnClose.addActionListener(e -> {
                dispose();
                System.exit(0);
            });

            pnlWinControls.add(btnMin);
            pnlWinControls.add(btnMax);
            pnlWinControls.add(btnClose);
            pnlHeaderRight.add(pnlWinControls);
        }

        headerCard.add(pnlTitleBox, BorderLayout.WEST);
        headerCard.add(pnlHeaderRight, BorderLayout.EAST);
        rootPanel.add(headerCard, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // 2. MAIN SPLIT LAYOUT (Left: Input & Stats, Right: Search & Tabs)
        // -------------------------------------------------------------
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(430);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.0);

        // =============================================================
        // LEFT COLUMN: DOCUMENT INPUT & DOCUMENT STATISTICS
        // =============================================================
        JPanel pnlLeftCol = new JPanel(new BorderLayout(0, 12));
        pnlLeftCol.setOpaque(false);

        // Card A: Document Input
        GlassCard cardInput = new GlassCard(16, COLOR_CARD_FILL_TOP, COLOR_CARD_FILL_BOTTOM, COLOR_BORDER_SUBTLE, new Color(15, 23, 42, 10));
        cardInput.setLayout(new BorderLayout(0, 10));
        cardInput.setBorder(new EmptyBorder(14, 16, 16, 16));

        JLabel lblInputTitle = createCardTitle("DOCUMENT INPUT", "SOURCE TEXT SELECTION");

        // Input Switcher Radio Buttons
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        pnlRadio.setOpaque(false);
        JRadioButton rdoManual = new JRadioButton("Manual Text Entry", true);
        JRadioButton rdoFile = new JRadioButton("File Upload (.txt / .docx)");
        styleRadioButton(rdoManual);
        styleRadioButton(rdoFile);
        ButtonGroup grpMode = new ButtonGroup();
        grpMode.add(rdoManual);
        grpMode.add(rdoFile);

        pnlRadio.add(rdoManual);
        pnlRadio.add(rdoFile);

        // Card Container for Manual Text vs File Upload
        cardInputLayout = new CardLayout();
        pnlInputCard = new JPanel(cardInputLayout);
        pnlInputCard.setOpaque(false);

        // Sub-panel 1: Manual Text
        JPanel pnlManual = new JPanel(new BorderLayout(0, 8));
        pnlManual.setOpaque(false);
        txtManualInput = new JTextArea("BANANA BANDANA");
        txtManualInput.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtManualInput.setForeground(COLOR_TEXT_MAIN);
        txtManualInput.setBackground(new Color(255, 255, 255, 240));
        txtManualInput.setLineWrap(true);
        txtManualInput.setWrapStyleWord(true);
        txtManualInput.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollManual = new JScrollPane(txtManualInput);
        scrollManual.setPreferredSize(new Dimension(360, 120));
        scrollManual.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225, 220), 1, true));

        GlassButton btnBuildManual = new GlassButton("Build Suffix Array from Text", GlassButton.Style.PRIMARY);
        btnBuildManual.setPreferredSize(new Dimension(360, 36));
        btnBuildManual.addActionListener(e -> buildFromManualText());

        pnlManual.add(scrollManual, BorderLayout.CENTER);
        pnlManual.add(btnBuildManual, BorderLayout.SOUTH);

        // Sub-panel 2: File Upload
        JPanel pnlUpload = new JPanel(new GridBagLayout());
        pnlUpload.setOpaque(false);
        pnlUpload.setPreferredSize(new Dimension(360, 160));

        GlassButton btnChooseDoc = new GlassButton("Choose Document (.txt, .docx)", GlassButton.Style.SECONDARY);
        btnChooseDoc.setPreferredSize(new Dimension(300, 48));
        btnChooseDoc.addActionListener(e -> chooseAndLoadFile());
        pnlUpload.add(btnChooseDoc);

        pnlInputCard.add(pnlManual, "MANUAL");
        pnlInputCard.add(pnlUpload, "FILE");

        rdoManual.addActionListener(e -> cardInputLayout.show(pnlInputCard, "MANUAL"));
        rdoFile.addActionListener(e -> cardInputLayout.show(pnlInputCard, "FILE"));

        cardInput.add(lblInputTitle, BorderLayout.NORTH);
        JPanel pnlInputCenter = new JPanel(new BorderLayout(0, 8));
        pnlInputCenter.setOpaque(false);
        pnlInputCenter.add(pnlRadio, BorderLayout.NORTH);
        pnlInputCenter.add(pnlInputCard, BorderLayout.CENTER);
        cardInput.add(pnlInputCenter, BorderLayout.CENTER);

        // Card B: Document Statistics
        GlassCard cardStats = new GlassCard(16, COLOR_CARD_FILL_TOP, COLOR_CARD_FILL_BOTTOM, COLOR_BORDER_SUBTLE, new Color(15, 23, 42, 10));
        cardStats.setLayout(new BorderLayout(0, 12));
        cardStats.setBorder(new EmptyBorder(14, 16, 16, 16));

        JLabel lblStatsTitle = createCardTitle("DOCUMENT STATISTICS", "PROCESSED METRICS");

        JPanel pnlStatsGrid = new JPanel(new GridLayout(9, 2, 8, 8));
        pnlStatsGrid.setOpaque(false);

        lblFileName = createStatValue("-");
        lblFileType = createStatValue("-");
        lblFileSize = createStatValue("-");
        lblCharCount = createStatValue("0");
        lblWordCount = createStatValue("0");
        lblLineCount = createStatValue("0");
        lblSuffixCount = createStatValue("0");
        lblBuildTime = createStatValue("0.000 ms");
        lblIndexStatus = new StatusBadge("NOT INDEXED", StatusBadge.Type.NEUTRAL);

        addStatItem(pnlStatsGrid, "File Name", lblFileName);
        addStatItem(pnlStatsGrid, "File Type", lblFileType);
        addStatItem(pnlStatsGrid, "File Size", lblFileSize);
        addStatItem(pnlStatsGrid, "Characters", lblCharCount);
        addStatItem(pnlStatsGrid, "Words", lblWordCount);
        addStatItem(pnlStatsGrid, "Lines", lblLineCount);
        addStatItem(pnlStatsGrid, "Suffixes Indexed", lblSuffixCount);
        addStatItem(pnlStatsGrid, "SA Build Time", lblBuildTime);
        addStatItem(pnlStatsGrid, "Index Status", lblIndexStatus);

        cardStats.add(lblStatsTitle, BorderLayout.NORTH);
        cardStats.add(pnlStatsGrid, BorderLayout.CENTER);

        pnlLeftCol.add(cardInput, BorderLayout.NORTH);
        pnlLeftCol.add(cardStats, BorderLayout.CENTER);

        // =============================================================
        // RIGHT COLUMN: SUBSTRING SEARCH, SUMMARY METRICS & DSA TABS
        // =============================================================
        JPanel pnlRightCol = new JPanel(new BorderLayout(0, 12));
        pnlRightCol.setOpaque(false);

        // Search Section Glass Card
        GlassCard cardSearch = new GlassCard(16, COLOR_CARD_FILL_TOP, COLOR_CARD_FILL_BOTTOM, COLOR_BORDER_SUBTLE, new Color(15, 23, 42, 10));
        cardSearch.setLayout(new BorderLayout(0, 10));
        cardSearch.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel pnlSearchHeader = new JPanel(new BorderLayout(0, 0));
        pnlSearchHeader.setOpaque(false);
        JLabel lblSearchTitle = createCardTitle("SUBSTRING SEARCH", "SUFFIX ARRAY + BINARY SEARCH");
        pnlSearchHeader.add(lblSearchTitle, BorderLayout.WEST);

        // Search Input Bar with Rounded Field and Action Buttons
        JPanel pnlSearchControls = new JPanel(new BorderLayout(10, 0));
        pnlSearchControls.setOpaque(false);

        txtSearchPattern = new JTextField("ANA");
        txtSearchPattern.setFont(new Font("Consolas", Font.BOLD, 14));
        txtSearchPattern.setForeground(COLOR_TEXT_MAIN);
        txtSearchPattern.setBackground(new Color(255, 255, 255, 240));
        txtSearchPattern.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225, 220), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtSearchPattern.setPreferredSize(new Dimension(300, 38));
        txtSearchPattern.addActionListener(e -> executeSearch());

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlButtons.setOpaque(false);

        GlassButton btnSearch = new GlassButton("SEARCH", GlassButton.Style.PRIMARY);
        btnSearch.setPreferredSize(new Dimension(100, 38));
        btnSearch.addActionListener(e -> executeSearch());

        GlassButton btnClear = new GlassButton("CLEAR", GlassButton.Style.SECONDARY);
        btnClear.setPreferredSize(new Dimension(85, 38));
        btnClear.addActionListener(e -> clearSearch());

        pnlButtons.add(btnSearch);
        pnlButtons.add(btnClear);

        pnlSearchControls.add(txtSearchPattern, BorderLayout.CENTER);
        pnlSearchControls.add(pnlButtons, BorderLayout.EAST);

        chkCaseSensitive = new JCheckBox("Case Sensitive", true);
        chkCaseSensitive.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCaseSensitive.setForeground(COLOR_TEXT_MUTED);
        chkCaseSensitive.setOpaque(false);

        cardSearch.add(pnlSearchHeader, BorderLayout.NORTH);
        cardSearch.add(pnlSearchControls, BorderLayout.CENTER);
        cardSearch.add(chkCaseSensitive, BorderLayout.SOUTH);

        // Search Results Summary Banner (4 Distinct Modern Glass Metric Cards)
        JPanel pnlSummaryRow = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlSummaryRow.setOpaque(false);
        pnlSummaryRow.setPreferredSize(new Dimension(600, 72));

        badgeSearchStatus = new StatusBadge("READY", StatusBadge.Type.INFO);
        JPanel pnlStatusCard = createMetricCard("STATUS", badgeSearchStatus);

        lblMetricOccurrences = new JLabel("0");
        lblMetricOccurrences.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMetricOccurrences.setForeground(COLOR_TEXT_MAIN);
        JPanel pnlOccurrencesCard = createMetricCard("OCCURRENCES", lblMetricOccurrences);

        lblMetricPositions = new JLabel("None");
        lblMetricPositions.setFont(new Font("Consolas", Font.BOLD, 13));
        lblMetricPositions.setForeground(COLOR_TEXT_MAIN);
        JPanel pnlPositionsCard = createMetricCard("POSITIONS", lblMetricPositions);

        lblMetricTime = new JLabel("0.000 ms");
        lblMetricTime.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMetricTime.setForeground(COLOR_BLUE_ACCENT);
        JPanel pnlTimeCard = createMetricCard("SEARCH TIME", lblMetricTime);

        pnlSummaryRow.add(pnlStatusCard);
        pnlSummaryRow.add(pnlOccurrencesCard);
        pnlSummaryRow.add(pnlPositionsCard);
        pnlSummaryRow.add(pnlTimeCard);

        JPanel pnlSearchTop = new JPanel(new BorderLayout(0, 10));
        pnlSearchTop.setOpaque(false);
        pnlSearchTop.add(cardSearch, BorderLayout.NORTH);
        pnlSearchTop.add(pnlSummaryRow, BorderLayout.SOUTH);

        // =============================================================
        // RESULT TABS (Modern Clean Glass Tabs)
        // =============================================================
        GlassCard cardTabs = new GlassCard(16, COLOR_CARD_FILL_TOP, COLOR_CARD_FILL_BOTTOM, COLOR_BORDER_SUBTLE, new Color(15, 23, 42, 10));
        cardTabs.setLayout(new BorderLayout(0, 0));
        cardTabs.setBorder(new EmptyBorder(8, 10, 10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new ModernGlassTabbedPaneUI());
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: Matching Snippets
        modelMatches = new DefaultTableModel(new String[]{"Match #", "Position", "Line #", "Context Snippet"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblMatches = new JTable(modelMatches);
        styleModernTable(tblMatches);
        tblMatches.getColumnModel().getColumn(0).setPreferredWidth(65);
        tblMatches.getColumnModel().getColumn(1).setPreferredWidth(85);
        tblMatches.getColumnModel().getColumn(2).setPreferredWidth(70);
        tblMatches.getColumnModel().getColumn(3).setPreferredWidth(750);
        tabbedPane.addTab("Matching Snippets", createTableScrollPane(tblMatches));

        // Tab 2: Binary Search Trace
        modelBsTrace = new DefaultTableModel(new String[]{
                "Step", "Phase", "Low", "High", "Mid", "Position", "Compared Suffix", "Comparison Result", "Action"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblBsTrace = new JTable(modelBsTrace);
        styleModernTable(tblBsTrace);
        tblBsTrace.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblBsTrace.getColumnModel().getColumn(1).setPreferredWidth(125);
        tblBsTrace.getColumnModel().getColumn(2).setPreferredWidth(50);
        tblBsTrace.getColumnModel().getColumn(3).setPreferredWidth(50);
        tblBsTrace.getColumnModel().getColumn(4).setPreferredWidth(50);
        tblBsTrace.getColumnModel().getColumn(5).setPreferredWidth(65);
        tblBsTrace.getColumnModel().getColumn(6).setPreferredWidth(160);
        tblBsTrace.getColumnModel().getColumn(7).setPreferredWidth(180);
        tblBsTrace.getColumnModel().getColumn(8).setPreferredWidth(260);
        tabbedPane.addTab("Binary Search Trace", createTableScrollPane(tblBsTrace));

        // Tab 3: Suffix Array Table
        modelSaTable = new DefaultTableModel(new String[]{"SA Index", "Position", "Suffix"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblSaTable = new JTable(modelSaTable);
        styleModernTable(tblSaTable);
        tblSaTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblSaTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        tblSaTable.getColumnModel().getColumn(2).setPreferredWidth(780);
        tabbedPane.addTab("Suffix Array Table", createTableScrollPane(tblSaTable));

        // Tab 4: Performance Comparison
        JPanel pnlPerfTab = new JPanel(new BorderLayout(0, 10));
        pnlPerfTab.setOpaque(false);
        pnlPerfTab.setBorder(new EmptyBorder(8, 8, 8, 8));

        modelPerf = new DefaultTableModel(new String[]{
                "Algorithm", "Execution Time", "Occurrences Found", "Theoretical Complexity"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPerf = new JTable(modelPerf);
        styleModernTable(tblPerf);
        tblPerf.getColumnModel().getColumn(0).setPreferredWidth(240);
        tblPerf.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblPerf.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblPerf.getColumnModel().getColumn(3).setPreferredWidth(180);

        lblPerfSummary = new JLabel("Perform a search to calculate live performance metrics comparing Naive Search vs Suffix Array.");
        lblPerfSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPerfSummary.setForeground(COLOR_BLUE_ACCENT);
        lblPerfSummary.setBorder(new EmptyBorder(6, 6, 6, 6));

        pnlPerfTab.add(createTableScrollPane(tblPerf), BorderLayout.CENTER);
        pnlPerfTab.add(lblPerfSummary, BorderLayout.SOUTH);
        tabbedPane.addTab("Performance Comparison", pnlPerfTab);

        // Tab 5: Test Suite
        JPanel pnlTestTab = new JPanel(new BorderLayout(0, 10));
        pnlTestTab.setOpaque(false);
        pnlTestTab.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel pnlTestTop = new JPanel(new BorderLayout(12, 0));
        pnlTestTop.setOpaque(false);

        lblTestSummary = new JLabel("Click 'Run Automated Test Suite' to execute all 15 algorithmic test cases live.");
        lblTestSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTestSummary.setForeground(COLOR_TEXT_MAIN);

        GlassButton btnRunTestsInside = new GlassButton("Run Automated Test Suite", GlassButton.Style.PRIMARY);
        btnRunTestsInside.setPreferredSize(new Dimension(200, 36));
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
        styleModernTable(tblTests);
        tblTests.getColumnModel().getColumn(0).setPreferredWidth(55);
        tblTests.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblTests.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblTests.getColumnModel().getColumn(3).setPreferredWidth(170);
        tblTests.getColumnModel().getColumn(4).setPreferredWidth(170);
        tblTests.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblTests.getColumnModel().getColumn(6).setPreferredWidth(100);

        pnlTestTab.add(pnlTestTop, BorderLayout.NORTH);
        pnlTestTab.add(createTableScrollPane(tblTests), BorderLayout.CENTER);
        tabbedPane.addTab("Test Suite", pnlTestTab);

        cardTabs.add(tabbedPane, BorderLayout.CENTER);

        pnlRightCol.add(pnlSearchTop, BorderLayout.NORTH);
        pnlRightCol.add(cardTabs, BorderLayout.CENTER);

        splitPane.setLeftComponent(pnlLeftCol);
        splitPane.setRightComponent(pnlRightCol);
        rootPanel.add(splitPane, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private void toggleMaximize() {
        if ((getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            setExtendedState(Frame.NORMAL);
        } else {
            setExtendedState(Frame.MAXIMIZED_BOTH);
        }
    }

    private void installWindowResizer(JPanel panel) {
        MouseAdapter resizer = new MouseAdapter() {
            private int cursorType = Cursor.DEFAULT_CURSOR;
            private Point startPos = null;
            private Rectangle startBounds = null;
            private final int BORDER_THICKNESS = 8;

            @Override
            public void mouseMoved(MouseEvent e) {
                if ((getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                int x = e.getX();
                int y = e.getY();
                int w = getWidth();
                int h = getHeight();

                boolean top = y <= BORDER_THICKNESS;
                boolean bottom = y >= h - BORDER_THICKNESS;
                boolean left = x <= BORDER_THICKNESS;
                boolean right = x >= w - BORDER_THICKNESS;

                if (top && left) cursorType = Cursor.NW_RESIZE_CURSOR;
                else if (top && right) cursorType = Cursor.NE_RESIZE_CURSOR;
                else if (bottom && left) cursorType = Cursor.SW_RESIZE_CURSOR;
                else if (bottom && right) cursorType = Cursor.SE_RESIZE_CURSOR;
                else if (top) cursorType = Cursor.N_RESIZE_CURSOR;
                else if (bottom) cursorType = Cursor.S_RESIZE_CURSOR;
                else if (left) cursorType = Cursor.W_RESIZE_CURSOR;
                else if (right) cursorType = Cursor.E_RESIZE_CURSOR;
                else cursorType = Cursor.DEFAULT_CURSOR;

                setCursor(Cursor.getPredefinedCursor(cursorType));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                startPos = e.getLocationOnScreen();
                startBounds = getBounds();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (cursorType == Cursor.DEFAULT_CURSOR || startPos == null || startBounds == null) return;
                Point p = e.getLocationOnScreen();
                int dx = p.x - startPos.x;
                int dy = p.y - startPos.y;

                int newX = startBounds.x;
                int newY = startBounds.y;
                int newW = startBounds.width;
                int newH = startBounds.height;

                int minW = getMinimumSize().width;
                int minH = getMinimumSize().height;

                if (cursorType == Cursor.E_RESIZE_CURSOR || cursorType == Cursor.SE_RESIZE_CURSOR || cursorType == Cursor.NE_RESIZE_CURSOR) {
                    newW = Math.max(minW, startBounds.width + dx);
                }
                if (cursorType == Cursor.S_RESIZE_CURSOR || cursorType == Cursor.SE_RESIZE_CURSOR || cursorType == Cursor.SW_RESIZE_CURSOR) {
                    newH = Math.max(minH, startBounds.height + dy);
                }
                if (cursorType == Cursor.W_RESIZE_CURSOR || cursorType == Cursor.NW_RESIZE_CURSOR || cursorType == Cursor.SW_RESIZE_CURSOR) {
                    int proposedW = startBounds.width - dx;
                    if (proposedW >= minW) {
                        newX = startBounds.x + dx;
                        newW = proposedW;
                    }
                }
                if (cursorType == Cursor.N_RESIZE_CURSOR || cursorType == Cursor.NW_RESIZE_CURSOR || cursorType == Cursor.NE_RESIZE_CURSOR) {
                    int proposedH = startBounds.height - dy;
                    if (proposedH >= minH) {
                        newY = startBounds.y + dy;
                        newH = proposedH;
                    }
                }

                setBounds(newX, newY, newW, newH);
            }
        };

        panel.addMouseListener(resizer);
        panel.addMouseMotionListener(resizer);
    }

    // -------------------------------------------------------------
    // Working Default Demo: BANANA BANDANA with query ANA
    // -------------------------------------------------------------
    private void loadDefaultDemo() {
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
        lblIndexStatus.setBadge("INDEXED", StatusBadge.Type.SUCCESS);

        // Dynamically populate Suffix Array Table
        populateSaTable();

        badgeSearchStatus.setBadge("INDEXED", StatusBadge.Type.SUCCESS);
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
            badgeSearchStatus.setBadge("EMPTY PATTERN", StatusBadge.Type.DANGER);
            lblMetricOccurrences.setText("0");
            lblMetricPositions.setText("None");
            modelMatches.setRowCount(0);
            modelBsTrace.setRowCount(0);
            return;
        }

        boolean caseSens = chkCaseSensitive.isSelected();

        // 1. Execute actual Suffix Array + Binary Search
        SearchResult result = PatternSearch.search(currentSuffixArray, pattern, caseSens, 40);

        // 2. Execute actual Naive Search for real Performance Comparison
        PatternSearch.PerformanceComparison comp = PatternSearch.compareSearch(currentSuffixArray, pattern, caseSens, 40);

        // Update Summary Status Badges & Metrics
        if (result.getStatus() == SearchResult.Status.FOUND) {
            badgeSearchStatus.setBadge("FOUND", StatusBadge.Type.SUCCESS);
        } else {
            badgeSearchStatus.setBadge("NOT FOUND", StatusBadge.Type.DANGER);
        }

        lblMetricOccurrences.setText(String.valueOf(result.getOccurrencesCount()));
        lblMetricTime.setText(PerformanceMonitor.formatDurationMs(result.getSearchTimeMs()));
        lblMetricPositions.setText(result.getPositionsFormatted());

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
                    "Both algorithms found %d occurrences in under 1 ms for text of length %d.",
                    result.getOccurrencesCount(), loadedText.length()
            ));
        }
    }

    private void clearSearch() {
        txtSearchPattern.setText("");
        badgeSearchStatus.setBadge(currentSuffixArray != null ? "INDEXED" : "READY",
                currentSuffixArray != null ? StatusBadge.Type.SUCCESS : StatusBadge.Type.INFO);
        lblMetricOccurrences.setText("0");
        lblMetricTime.setText("0.000 ms");
        lblMetricPositions.setText("None");
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
    // UI Helpers & Component Factories
    // -------------------------------------------------------------
    private static JLabel createCardTitle(String mainTitle, String subTitle) {
        JLabel lbl = new JLabel("<html><span style='font-size:12px; font-weight:bold; color:#0f172a;'>" +
                mainTitle + "</span>&nbsp;&nbsp;<span style='font-size:10px; color:#64748b; font-weight:normal;'>" +
                subTitle + "</span></html>");
        return lbl;
    }

    private static void styleRadioButton(JRadioButton rdo) {
        rdo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rdo.setForeground(COLOR_TEXT_MAIN);
        rdo.setOpaque(false);
        rdo.setFocusPainted(false);
    }

    private static JLabel createStatValue(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_MAIN);
        return lbl;
    }

    private static void addStatItem(JPanel panel, String labelText, JComponent valueComp) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(COLOR_TEXT_MUTED);
        panel.add(lbl);
        panel.add(valueComp);
    }

    private static JPanel createMetricCard(String title, JComponent valueComponent) {
        GlassCard card = new GlassCard(12, new Color(255, 255, 255, 215), new Color(255, 255, 255, 185),
                new Color(255, 255, 255, 240), new Color(15, 23, 42, 6));
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueComponent, BorderLayout.CENTER);
        return card;
    }

    private static JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240, 200), 1, true));
        scroll.getViewport().setBackground(new Color(255, 255, 255, 240));
        return scroll;
    }

    private static void styleModernTable(JTable table) {
        table.setFont(new Font("Consolas", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setBackground(Color.WHITE);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(COLOR_TABLE_HEADER);
        table.getTableHeader().setForeground(COLOR_TEXT_MAIN);
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        table.getTableHeader().setReorderingAllowed(false);

        // High contrast readable rows with subtle alternation
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 255, 255, 248) : new Color(248, 250, 252, 240));
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return c;
            }
        });
    }

    // =============================================================
    // CUSTOM GLASSMORPHIC UI COMPONENTS
    // =============================================================

    /**
     * FrostedBackgroundPanel:
     * Translucent ambient cool-slate backdrop allowing the user's desktop wallpaper
     * to subtly show through at calibrated 80-90% effective opacity.
     */
    public static class FrostedBackgroundPanel extends JPanel {
        private final boolean isTranslucent;

        public FrostedBackgroundPanel(boolean isTranslucent) {
            this.isTranslucent = isTranslucent;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Calibrated alpha: allows desktop wallpaper to subtly show through
            int alphaTop = isTranslucent ? 135 : 255;
            int alphaBottom = isTranslucent ? 155 : 255;

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(241, 245, 249, alphaTop),
                    w, h, new Color(226, 232, 240, alphaBottom)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 18, 18);

            // Subtle atmospheric frosted glow
            RadialGradientPaint rgp1 = new RadialGradientPaint(
                    new Point(w / 4, 0), Math.max(w / 2, 300),
                    new float[]{0.0f, 1.0f},
                    new Color[]{new Color(219, 234, 254, isTranslucent ? 85 : 120), new Color(241, 245, 249, 0)}
            );
            g2.setPaint(rgp1);
            g2.fillRoundRect(0, 0, w, h, 18, 18);

            // Subtle outer window edge highlight
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * GlassCard:
     * Translucent rounded card container (80-88% opacity) with soft drop shadow and crisp border.
     */
    public static class GlassCard extends JPanel {
        private final int radius;
        private final Color fillTop;
        private final Color fillBottom;
        private final Color borderColor;
        private final Color shadowColor;

        public GlassCard(int radius, Color fillTop, Color fillBottom, Color borderColor, Color shadowColor) {
            this.radius = radius;
            this.fillTop = fillTop;
            this.fillBottom = fillBottom;
            this.borderColor = borderColor;
            this.shadowColor = shadowColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Outer soft drop shadow (layered alpha concentric rings)
            int shadowSpread = 3;
            for (int i = 0; i < shadowSpread; i++) {
                int alpha = Math.max(1, shadowColor.getAlpha() / (i + 1));
                g2.setColor(new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), alpha));
                g2.fill(new RoundRectangle2D.Float(i, i + 1, w - i * 2, h - i * 2, radius + 2, radius + 2));
            }

            // Glass translucent body fill
            GradientPaint gp = new GradientPaint(0, 0, fillTop, 0, h, fillBottom);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(2, 2, w - 5, h - 5, radius, radius));

            // Subtle crisp top/border highlight
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(2, 2, w - 5, h - 5, radius, radius));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * GlassButton:
     * Modern rounded interactive button with clean hover/press transitions.
     */
    public static class GlassButton extends JButton {
        public enum Style {
            PRIMARY,
            SECONDARY,
            HEADER_NAV
        }

        private final Style style;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public GlassButton(String text, Style style) {
            super(text);
            this.style = style;
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int radius = 10;

            Color topColor, bottomColor, textColor, borderColor;

            if (style == Style.PRIMARY) {
                textColor = Color.WHITE;
                borderColor = new Color(255, 255, 255, 60);
                if (isPressed) {
                    topColor = new Color(30, 64, 175);
                    bottomColor = new Color(30, 58, 138);
                } else if (isHovered) {
                    topColor = new Color(59, 130, 246);
                    bottomColor = new Color(37, 99, 235);
                } else {
                    topColor = new Color(37, 99, 235);
                    bottomColor = new Color(29, 78, 216);
                }
            } else if (style == Style.HEADER_NAV) {
                textColor = Color.WHITE;
                borderColor = new Color(255, 255, 255, 70);
                if (isPressed) {
                    topColor = new Color(15, 23, 42);
                    bottomColor = new Color(15, 23, 42);
                } else if (isHovered) {
                    topColor = new Color(71, 85, 105);
                    bottomColor = new Color(51, 65, 85);
                } else {
                    topColor = new Color(51, 65, 85);
                    bottomColor = new Color(30, 41, 59);
                }
            } else { // SECONDARY
                textColor = COLOR_TEXT_MAIN;
                borderColor = new Color(203, 213, 225, 220);
                if (isPressed) {
                    topColor = new Color(226, 232, 240, 240);
                    bottomColor = new Color(203, 213, 225, 240);
                } else if (isHovered) {
                    topColor = new Color(255, 255, 255, 255);
                    bottomColor = new Color(241, 245, 249, 255);
                } else {
                    topColor = new Color(255, 255, 255, 230);
                    bottomColor = new Color(248, 250, 252, 210);
                }
            }

            // Fill button background
            GradientPaint gp = new GradientPaint(0, 0, topColor, 0, h, bottomColor);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));

            // Draw clean subtle border
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.1f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));

            // Draw centered text
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = (w - fm.stringWidth(getText())) / 2;
            int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    /**
     * WindowControlButton:
     * Minimalist window controls for undecorated translucent mode (minimize, maximize, close).
     */
    public static class WindowControlButton extends JButton {
        private final boolean isClose;
        private boolean isHovered = false;

        public WindowControlButton(String text, boolean isClose) {
            super(text);
            this.isClose = isClose;
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setPreferredSize(new Dimension(32, 28));
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isHovered) {
                g2.setColor(isClose ? new Color(239, 68, 68, 220) : new Color(255, 255, 255, 60));
                g2.fillRoundRect(0, 0, w, h, 6, 6);
            }

            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }

    /**
     * StatusBadge:
     * Compact pill badge displaying status with colored background and dot indicator.
     */
    public static class StatusBadge extends JPanel {
        public enum Type {
            SUCCESS,
            DANGER,
            INFO,
            NEUTRAL
        }

        private String label;
        private Type type;

        public StatusBadge(String label, Type type) {
            this.label = label;
            this.type = type;
            setOpaque(false);
            setPreferredSize(new Dimension(110, 24));
        }

        public void setBadge(String label, Type type) {
            this.label = label;
            this.type = type;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color bg, fg, dot;
            switch (type) {
                case SUCCESS:
                    bg = new Color(220, 252, 231, 230);
                    fg = new Color(21, 128, 61);
                    dot = new Color(22, 163, 74);
                    break;
                case DANGER:
                    bg = new Color(254, 226, 226, 230);
                    fg = new Color(185, 28, 28);
                    dot = new Color(220, 38, 38);
                    break;
                case INFO:
                    bg = new Color(224, 242, 254, 230);
                    fg = new Color(3, 105, 161);
                    dot = new Color(2, 132, 199);
                    break;
                default:
                    bg = new Color(241, 245, 249, 230);
                    fg = new Color(71, 85, 105);
                    dot = new Color(148, 163, 184);
                    break;
            }

            // Pill background
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(1, 2, w - 2, h - 4, h - 4, h - 4));

            // Status indicator dot
            g2.setColor(dot);
            g2.fillOval(10, (h - 8) / 2, 8, 8);

            // Text
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(label, 24, textY);

            g2.dispose();
        }
    }

    /**
     * ModernGlassTabbedPaneUI:
     * Custom UI for tabs providing sleek glass pills with blue active accent.
     */
    public static class ModernGlassTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(8, 16, 8, 16);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return 36;
        }

        @Override
        protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
            super.paintTabArea(g, tabPlacement, selectedIndex);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                // Active Tab: Royal Blue Pill
                GradientPaint gp = new GradientPaint(x, y, new Color(37, 99, 235), x, y + h, new Color(29, 78, 216));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(x + 2, y + 2, w - 4, h - 4, 10, 10));
            } else {
                // Inactive Tab: Subtle translucent glass pill
                g2.setColor(new Color(255, 255, 255, 160));
                g2.fill(new RoundRectangle2D.Float(x + 2, y + 2, w - 4, h - 4, 10, 10));
                g2.setColor(new Color(226, 232, 240, 200));
                g2.draw(new RoundRectangle2D.Float(x + 2, y + 2, w - 4, h - 4, 10, 10));
            }
            g2.dispose();
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font);

            if (isSelected) {
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(new Color(71, 85, 105));
            }

            int textY = textRect.y + metrics.getAscent();
            g2.drawString(title, textRect.x, textY);
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            // Handled inside paintTabBackground
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // Borderless seamless integration with card container
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // Suppress default focus rectangle for clean modern aesthetic
        }
    }
}
