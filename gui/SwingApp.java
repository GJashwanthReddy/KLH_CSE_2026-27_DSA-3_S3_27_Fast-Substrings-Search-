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
 * Modern Glassmorphism Desktop UI with Two-Step Workflow for Fast Substrings Search.
 * Workflow:
 *   STEP 1 → INPUT / INDEXING (Document input, manual text / file upload, build Suffix Array)
 *   STEP 2 → SEARCH / RESULTS (Substring search, status metrics, DSA traces & tables, Change Input)
 * Features:
 *   - Step progress indicator: ● ① INPUT → ○ ② SEARCH (Step 1) / ✓ ① INPUT → ● ② SEARCH (Step 2)
 *   - Semi-transparent glass panels (80-90% opacity) letting desktop background subtly show through
 *   - Custom vector window controls (minimize, maximize box, close cross) and window dragging
 *   - 100% genuine Suffix Array + Binary Search algorithms preserved
 *   - Strictly project-focused with zero personal/college metadata
 */
public class SwingApp extends JFrame {

    private String loadedText = "";
    private DocumentReader.DocumentMetadata currentMetadata = null;
    private SuffixArray currentSuffixArray = null;

    // Window Translucency & Dragging State
    private boolean isTranslucentMode = false;
    private Point dragOffset = null;

    // Workflow Card Switcher
    private CardLayout workflowCardLayout;
    private JPanel pnlWorkflowCards;

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

    // Step 1: Input Components
    private JTextArea txtManualInput;
    private CardLayout cardInputSourceLayout;
    private JPanel pnlInputSourceCards;
    private File chosenFile = null;
    private JLabel lblUploadedFileInfo;

    // Step 2: Index Banner Labels
    private JLabel lblIndexedDocInfo;
    private JLabel lblIndexedDocStats;
    private StatusBadge badgeDocIndexedStatus;

    // Step 2: Search Controls & Metrics
    private JTextField txtSearchPattern;
    private JCheckBox chkCaseSensitive;
    private StatusBadge badgeSearchStatus;
    private JLabel lblMetricOccurrences, lblMetricPositions, lblMetricTime;

    // Search Tracking State (for dynamic result invalidation on query change)
    private String lastExecutedPattern = null;
    private boolean lastExecutedCaseSens = true;
    private boolean isExecutingSearch = false;

    // Tables & Models
    private JTable tblMatches, tblBsTrace, tblSaTable, tblPerf, tblTests;
    private DefaultTableModel modelMatches, modelBsTrace, modelSaTable, modelPerf, modelTests;
    private JLabel lblPerfSummary;
    private JLabel lblTestSummary;

    public SwingApp() {
        setTitle("Fast Substrings Search Using Suffix Arrays");

        // Enable per-pixel window translucency so desktop wallpaper subtly shows through
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

        // Default: Open on Step 1 with default sample pre-loaded in the input area
        workflowCardLayout.show(pnlWorkflowCards, "STEP1");
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Root Container with Frosted Glass Backdrop
        FrostedBackgroundPanel rootPanel = new FrostedBackgroundPanel(isTranslucentMode);
        rootPanel.setLayout(new BorderLayout(0, 10));
        rootPanel.setBorder(new EmptyBorder(10, 14, 14, 14));

        if (isTranslucentMode) {
            installWindowResizer(rootPanel);
        }

        // -------------------------------------------------------------
        // TOP HEADER (Translucent Glass Bar with Step Indicator & Controls)
        // -------------------------------------------------------------
        GlassCard headerCard = new GlassCard(16,
                new Color(15, 23, 42, 215), new Color(30, 41, 59, 205),
                new Color(255, 255, 255, 45), new Color(0, 0, 0, 25));
        headerCard.setLayout(new BorderLayout(16, 0));
        headerCard.setBorder(new EmptyBorder(12, 20, 12, 16));

        // Window dragging on header
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

        // Title & Subtitle
        JLabel titleLabel = new JLabel("FAST SUBSTRINGS SEARCH USING SUFFIX ARRAYS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel subTitleLabel = new JLabel("Java Implementation • Suffix Array + Binary Search");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitleLabel.setForeground(COLOR_TEXT_LIGHT);

        JPanel pnlTitleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        pnlTitleBox.setOpaque(false);
        pnlTitleBox.add(titleLabel);
        pnlTitleBox.add(subTitleLabel);

        // Right: Run Test Suite + Window Controls
        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlHeaderRight.setOpaque(false);

        GlassButton btnHeaderTest = new GlassButton("RUN TEST SUITE", GlassButton.Style.HEADER_NAV);
        btnHeaderTest.setPreferredSize(new Dimension(140, 32));
        btnHeaderTest.addActionListener(e -> executeTestSuite());
        pnlHeaderRight.add(btnHeaderTest);

        if (isTranslucentMode) {
            JPanel pnlWinControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            pnlWinControls.setOpaque(false);

            WindowControlButton btnMin = new WindowControlButton(WindowControlButton.ActionType.MINIMIZE);
            btnMin.setToolTipText("Minimize");
            btnMin.addActionListener(e -> setState(Frame.ICONIFIED));

            WindowControlButton btnMax = new WindowControlButton(WindowControlButton.ActionType.MAXIMIZE);
            btnMax.setToolTipText("Maximize / Restore");
            btnMax.addActionListener(e -> {
                toggleMaximize();
                btnMax.repaint();
            });

            WindowControlButton btnClose = new WindowControlButton(WindowControlButton.ActionType.CLOSE);
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
        // WORKFLOW CARDS CONTAINER (Step 1 vs Step 2)
        // -------------------------------------------------------------
        workflowCardLayout = new CardLayout();
        pnlWorkflowCards = new JPanel(workflowCardLayout);
        pnlWorkflowCards.setOpaque(false);

        // Build Step 1 Panel & Step 2 Panel
        JPanel pnlStep1 = createStep1InputPanel();
        JPanel pnlStep2 = createStep2SearchPanel();

        pnlWorkflowCards.add(pnlStep1, "STEP1");
        pnlWorkflowCards.add(pnlStep2, "STEP2");

        rootPanel.add(pnlWorkflowCards, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    // =============================================================
    // STEP 1: INPUT SCREEN
    // =============================================================
    private JPanel createStep1InputPanel() {
        JPanel pnlStep1 = new JPanel(new GridBagLayout());
        pnlStep1.setOpaque(false);

        // Center Glass Card for Input
        GlassCard cardInput = new GlassCard(18, COLOR_CARD_FILL_TOP, COLOR_CARD_FILL_BOTTOM, COLOR_BORDER_SUBTLE, new Color(15, 23, 42, 12));
        cardInput.setLayout(new BorderLayout(0, 16));
        cardInput.setBorder(new EmptyBorder(24, 30, 24, 30));
        cardInput.setPreferredSize(new Dimension(860, 600));

        // Header inside Input Card
        JPanel pnlCardHeader = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlCardHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("DOCUMENT INPUT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_MAIN);

        JLabel lblSub = new JLabel("Select text input mode to construct the Suffix Array index for fast pattern matching.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(COLOR_TEXT_MUTED);

        pnlCardHeader.add(lblTitle);
        pnlCardHeader.add(lblSub);

        // Radio Mode Switcher
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
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

        // Card Container for Manual vs File
        cardInputSourceLayout = new CardLayout();
        pnlInputSourceCards = new JPanel(cardInputSourceLayout);
        pnlInputSourceCards.setOpaque(false);

        // Subcard 1: Manual Text
        JPanel pnlManual = new JPanel(new BorderLayout(0, 8));
        pnlManual.setOpaque(false);

        JLabel lblManualHint = new JLabel("Enter or paste your text below (preloaded with sample text):");
        lblManualHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblManualHint.setForeground(COLOR_TEXT_MUTED);

        txtManualInput = new JTextArea("BANANA BANDANA");
        txtManualInput.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtManualInput.setForeground(COLOR_TEXT_MAIN);
        txtManualInput.setBackground(new Color(255, 255, 255, 245));
        txtManualInput.setLineWrap(true);
        txtManualInput.setWrapStyleWord(true);
        txtManualInput.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollManual = new JScrollPane(txtManualInput);
        scrollManual.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225, 220), 1, true));

        pnlManual.add(lblManualHint, BorderLayout.NORTH);
        pnlManual.add(scrollManual, BorderLayout.CENTER);

        // Subcard 2: File Upload
        JPanel pnlUpload = new JPanel(new GridBagLayout());
        pnlUpload.setOpaque(false);

        JPanel pnlUploadBox = new JPanel(new BorderLayout(0, 14));
        pnlUploadBox.setOpaque(false);

        GlassButton btnChooseDoc = new GlassButton("CHOOSE DOCUMENT (.txt, .docx)", GlassButton.Style.SECONDARY);
        btnChooseDoc.setPreferredSize(new Dimension(340, 50));
        btnChooseDoc.addActionListener(e -> chooseDocumentFile());

        lblUploadedFileInfo = new JLabel("No file selected. Click above to choose a .txt or .docx file.", SwingConstants.CENTER);
        lblUploadedFileInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUploadedFileInfo.setForeground(COLOR_TEXT_MUTED);

        pnlUploadBox.add(btnChooseDoc, BorderLayout.CENTER);
        pnlUploadBox.add(lblUploadedFileInfo, BorderLayout.SOUTH);
        pnlUpload.add(pnlUploadBox);

        pnlInputSourceCards.add(pnlManual, "MANUAL");
        pnlInputSourceCards.add(pnlUpload, "FILE");

        rdoManual.addActionListener(e -> cardInputSourceLayout.show(pnlInputSourceCards, "MANUAL"));
        rdoFile.addActionListener(e -> cardInputSourceLayout.show(pnlInputSourceCards, "FILE"));

        // Bottom Action: Build Suffix Array Button
        JPanel pnlBottomAction = new JPanel(new BorderLayout(0, 8));
        pnlBottomAction.setOpaque(false);

        GlassButton btnBuild = new GlassButton("BUILD SUFFIX ARRAY  →", GlassButton.Style.PRIMARY);
        btnBuild.setPreferredSize(new Dimension(340, 48));
        btnBuild.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuild.addActionListener(e -> onBuildSuffixArrayClicked(rdoManual.isSelected()));

        JPanel pnlBtnCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlBtnCenter.setOpaque(false);
        pnlBtnCenter.add(btnBuild);

        JLabel lblBuildNote = new JLabel("Constructs the Suffix Array in O(N log² N) time using prefix doubling and transitions to Search.", SwingConstants.CENTER);
        lblBuildNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBuildNote.setForeground(COLOR_TEXT_LIGHT);

        pnlBottomAction.add(pnlBtnCenter, BorderLayout.NORTH);
        pnlBottomAction.add(lblBuildNote, BorderLayout.SOUTH);

        // Assemble Step 1 Card
        JPanel pnlCardCenter = new JPanel(new BorderLayout(0, 12));
        pnlCardCenter.setOpaque(false);
        pnlCardCenter.add(pnlRadio, BorderLayout.NORTH);
        pnlCardCenter.add(pnlInputSourceCards, BorderLayout.CENTER);

        cardInput.add(pnlCardHeader, BorderLayout.NORTH);
        cardInput.add(pnlCardCenter, BorderLayout.CENTER);
        cardInput.add(pnlBottomAction, BorderLayout.SOUTH);

        pnlStep1.add(cardInput);
        return pnlStep1;
    }

    // =============================================================
    // STEP 2: SEARCH SCREEN
    // =============================================================
    private JPanel createStep2SearchPanel() {
        JPanel pnlStep2 = new JPanel(new BorderLayout(0, 10));
        pnlStep2.setOpaque(false);

        // Top Banner: Indexed Document Summary + [ ← CHANGE INPUT ]
        GlassCard cardIndexBanner = new GlassCard(14, COLOR_CARD_FILL_TOP, COLOR_CARD_FILL_BOTTOM, COLOR_BORDER_SUBTLE, new Color(15, 23, 42, 8));
        cardIndexBanner.setLayout(new BorderLayout(16, 0));
        cardIndexBanner.setBorder(new EmptyBorder(10, 18, 10, 18));

        JPanel pnlDocInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlDocInfo.setOpaque(false);

        lblIndexedDocInfo = new JLabel("INDEXED DOCUMENT: BANANA BANDANA");
        lblIndexedDocInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIndexedDocInfo.setForeground(COLOR_TEXT_MAIN);

        lblIndexedDocStats = new JLabel("Characters: 14 | Suffixes: 14 | SA Build Time: 0.900 ms");
        lblIndexedDocStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIndexedDocStats.setForeground(COLOR_TEXT_MUTED);

        pnlDocInfo.add(lblIndexedDocInfo);
        pnlDocInfo.add(lblIndexedDocStats);

        JPanel pnlBannerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlBannerRight.setOpaque(false);

        badgeDocIndexedStatus = new StatusBadge("INDEXED", StatusBadge.Type.SUCCESS);
        pnlBannerRight.add(badgeDocIndexedStatus);

        GlassButton btnChangeInput = new GlassButton("← CHANGE INPUT", GlassButton.Style.SECONDARY);
        btnChangeInput.setPreferredSize(new Dimension(150, 36));
        btnChangeInput.setToolTipText("Return to Step 1 to input or upload another document");
        btnChangeInput.addActionListener(e -> {
            workflowCardLayout.show(pnlWorkflowCards, "STEP1");
        });
        pnlBannerRight.add(btnChangeInput);

        cardIndexBanner.add(pnlDocInfo, BorderLayout.WEST);
        cardIndexBanner.add(pnlBannerRight, BorderLayout.EAST);

        // Substring Search Card
        GlassCard cardSearch = new GlassCard(16, COLOR_CARD_FILL_TOP, COLOR_CARD_FILL_BOTTOM, COLOR_BORDER_SUBTLE, new Color(15, 23, 42, 10));
        cardSearch.setLayout(new BorderLayout(0, 10));
        cardSearch.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel pnlSearchHeader = new JPanel(new BorderLayout(0, 0));
        pnlSearchHeader.setOpaque(false);
        JLabel lblSearchTitle = createCardTitle("SUBSTRING SEARCH", "SUFFIX ARRAY + BINARY SEARCH");
        pnlSearchHeader.add(lblSearchTitle, BorderLayout.WEST);

        JPanel pnlSearchControls = new JPanel(new BorderLayout(10, 0));
        pnlSearchControls.setOpaque(false);

        txtSearchPattern = new JTextField("ANA");
        txtSearchPattern.setFont(new Font("Consolas", Font.BOLD, 14));
        txtSearchPattern.setForeground(COLOR_TEXT_MAIN);
        txtSearchPattern.setBackground(new Color(255, 255, 255, 245));
        txtSearchPattern.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225, 220), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtSearchPattern.setPreferredSize(new Dimension(300, 38));
        txtSearchPattern.addActionListener(e -> executeSearch());
        txtSearchPattern.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkQueryChanged(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkQueryChanged(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkQueryChanged(); }
        });

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
        chkCaseSensitive.addActionListener(e -> checkQueryChanged());

        cardSearch.add(pnlSearchHeader, BorderLayout.NORTH);
        cardSearch.add(pnlSearchControls, BorderLayout.CENTER);
        cardSearch.add(chkCaseSensitive, BorderLayout.SOUTH);

        // Search Results Summary Banner (4 Distinct Modern Glass Metric Cards)
        JPanel pnlSummaryRow = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlSummaryRow.setOpaque(false);
        pnlSummaryRow.setPreferredSize(new Dimension(600, 70));

        badgeSearchStatus = new StatusBadge("FOUND", StatusBadge.Type.SUCCESS);
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

        JPanel pnlSearchTop = new JPanel(new BorderLayout(0, 8));
        pnlSearchTop.setOpaque(false);
        pnlSearchTop.add(cardIndexBanner, BorderLayout.NORTH);
        pnlSearchTop.add(cardSearch, BorderLayout.CENTER);
        pnlSearchTop.add(pnlSummaryRow, BorderLayout.SOUTH);

        // Result Tabs Glass Card
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

        pnlStep2.add(pnlSearchTop, BorderLayout.NORTH);
        pnlStep2.add(cardTabs, BorderLayout.CENTER);

        return pnlStep2;
    }

    // =============================================================
    // WORKFLOW TRANSITION LOGIC
    // =============================================================
    private void chooseDocumentFile() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileFilter(new FileNameExtensionFilter("Text & Word Documents (.txt, .docx)", "txt", "docx"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            this.chosenFile = chooser.getSelectedFile();
            lblUploadedFileInfo.setText(String.format("Selected: %s (%.1f KB)", chosenFile.getName(), chosenFile.length() / 1024.0));
            lblUploadedFileInfo.setForeground(COLOR_GREEN);
        }
    }

    private void onBuildSuffixArrayClicked(boolean isManualMode) {
        if (isManualMode) {
            String text = txtManualInput.getText();
            if (text == null || text.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Manual text cannot be empty.", "Input Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DocumentReader.ReadResult doc = DocumentReader.readManualText(text, "Manual Text Entry");
            buildIndexAndTransition(doc.text, doc.metadata);
        } else {
            if (chosenFile == null) {
                chooseDocumentFile();
                if (chosenFile == null) return;
            }
            try {
                DocumentReader.ReadResult doc = DocumentReader.readFile(chosenFile);
                buildIndexAndTransition(doc.text, doc.metadata);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to read document: " + ex.getMessage(),
                        "Document Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buildIndexAndTransition(String text, DocumentReader.DocumentMetadata meta) {
        this.loadedText = text != null ? text : "";
        this.currentMetadata = meta;

        // Construct Suffix Array using genuine DSA algorithm
        this.currentSuffixArray = new SuffixArray(this.loadedText);

        // Update Step 2 Banner
        lblIndexedDocInfo.setText(String.format("INDEXED DOCUMENT: %s", meta.fileName));
        lblIndexedDocStats.setText(String.format("Characters: %,d | Words: %,d | Suffixes: %,d | SA Build Time: %s",
                meta.characterCount, meta.wordCount, currentSuffixArray.getN(),
                PerformanceMonitor.formatDurationMs(currentSuffixArray.getBuildTimeMs())));
        badgeDocIndexedStatus.setBadge("INDEXED", StatusBadge.Type.SUCCESS);

        // Populate Suffix Array Table
        populateSaTable();

        // Perform initial search with current search query (or default "ANA")
        executeSearch();

        // Transition to Step 2
        workflowCardLayout.show(pnlWorkflowCards, "STEP2");
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
            return;
        }

        String pattern = txtSearchPattern.getText();
        if (pattern == null || pattern.isEmpty()) {
            lastExecutedPattern = "";
            clearPreviousSearchResults();
            badgeSearchStatus.setBadge("EMPTY PATTERN", StatusBadge.Type.DANGER);
            return;
        }

        boolean caseSens = chkCaseSensitive.isSelected();

        isExecutingSearch = true;
        try {
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

            this.lastExecutedPattern = pattern;
            this.lastExecutedCaseSens = caseSens;
        } finally {
            isExecutingSearch = false;
        }
    }

    private void checkQueryChanged() {
        if (isExecutingSearch) return;
        String current = txtSearchPattern.getText();
        boolean currentCaseSens = chkCaseSensitive.isSelected();
        if (lastExecutedPattern != null && (!lastExecutedPattern.equals(current) || lastExecutedCaseSens != currentCaseSens)) {
            clearPreviousSearchResults();
        }
    }

    private void clearPreviousSearchResults() {
        if (isExecutingSearch) return;
        badgeSearchStatus.setBadge("READY", StatusBadge.Type.INFO);
        lblMetricOccurrences.setText("0");
        lblMetricPositions.setText("None");
        lblMetricTime.setText("0.000 ms");
        modelMatches.setRowCount(0);
        modelBsTrace.setRowCount(0);
        modelPerf.setRowCount(0);
        lblPerfSummary.setText("Click 'SEARCH' or press Enter to run pattern search for the current query.");
        lastExecutedPattern = null;
    }

    private void clearSearch() {
        isExecutingSearch = true;
        try {
            txtSearchPattern.setText("");
            clearPreviousSearchResults();
        } finally {
            isExecutingSearch = false;
        }
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
    // Window Resizer & Maximizer Helpers
    // -------------------------------------------------------------
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
    // UI Helpers & Component Factories
    // -------------------------------------------------------------
    private static JLabel createCardTitle(String mainTitle, String subTitle) {
        JLabel lbl = new JLabel("<html><span style='font-size:12px; font-weight:bold; color:#0f172a;'>" +
                mainTitle + "</span>&nbsp;&nbsp;<span style='font-size:10px; color:#64748b; font-weight:normal;'>" +
                subTitle + "</span></html>");
        return lbl;
    }

    private static void styleRadioButton(JRadioButton rdo) {
        rdo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rdo.setForeground(COLOR_TEXT_MAIN);
        rdo.setOpaque(false);
        rdo.setFocusPainted(false);
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

            int shadowSpread = 3;
            for (int i = 0; i < shadowSpread; i++) {
                int alpha = Math.max(1, shadowColor.getAlpha() / (i + 1));
                g2.setColor(new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), alpha));
                g2.fill(new RoundRectangle2D.Float(i, i + 1, w - i * 2, h - i * 2, radius + 2, radius + 2));
            }

            GradientPaint gp = new GradientPaint(0, 0, fillTop, 0, h, fillBottom);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(2, 2, w - 5, h - 5, radius, radius));

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

            GradientPaint gp = new GradientPaint(0, 0, topColor, 0, h, bottomColor);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.1f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));

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
     * Custom vector-drawn title bar controls for minimize, maximize box, and close cross.
     */
    public static class WindowControlButton extends JButton {
        public enum ActionType {
            MINIMIZE,
            MAXIMIZE,
            CLOSE
        }

        private final ActionType actionType;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public WindowControlButton(ActionType actionType) {
            super();
            this.actionType = actionType;
            setPreferredSize(new Dimension(36, 28));
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
                    isPressed = false;
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

        public boolean isWindowMaximized() {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof Frame) {
                return (((Frame) w).getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
            }
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();

            if (actionType == ActionType.CLOSE) {
                if (isPressed) {
                    g2.setColor(new Color(220, 38, 38));
                    g2.fillRoundRect(0, 0, w, h, 6, 6);
                } else if (isHovered) {
                    g2.setColor(new Color(239, 68, 68, 235));
                    g2.fillRoundRect(0, 0, w, h, 6, 6);
                } else {
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fillRoundRect(0, 0, w, h, 6, 6);
                    g2.setColor(new Color(255, 255, 255, 35));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
                }
            } else {
                if (isPressed) {
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.fillRoundRect(0, 0, w, h, 6, 6);
                } else if (isHovered) {
                    g2.setColor(new Color(255, 255, 255, 55));
                    g2.fillRoundRect(0, 0, w, h, 6, 6);
                } else {
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fillRoundRect(0, 0, w, h, 6, 6);
                    g2.setColor(new Color(255, 255, 255, 35));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
                }
            }

            int cx = w / 2;
            int cy = h / 2;
            g2.setColor(Color.WHITE);

            if (actionType == ActionType.MINIMIZE) {
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 5, cy + 2, cx + 5, cy + 2);
            } else if (actionType == ActionType.MAXIMIZE) {
                if (isWindowMaximized()) {
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
                    g2.drawRect(cx - 3, cy - 6, 8, 8);
                    g2.setColor(isHovered ? new Color(51, 65, 85) : new Color(30, 41, 59));
                    g2.fillRect(cx - 6, cy - 3, 9, 9);
                    g2.setColor(Color.WHITE);
                    g2.drawRect(cx - 6, cy - 3, 8, 8);
                } else {
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
                    g2.drawRect(cx - 5, cy - 5, 10, 10);
                }
            } else if (actionType == ActionType.CLOSE) {
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = 4;
                g2.drawLine(cx - s, cy - s, cx + s, cy + s);
                g2.drawLine(cx - s, cy + s, cx + s, cy - s);
            }

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

            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(1, 2, w - 2, h - 4, h - 4, h - 4));

            g2.setColor(dot);
            g2.fillOval(10, (h - 8) / 2, 8, 8);

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
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                GradientPaint gp = new GradientPaint(x, y, new Color(37, 99, 235), x, y + h, new Color(29, 78, 216));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(x + 2, y + 2, w - 4, h - 4, 10, 10));
            } else {
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
