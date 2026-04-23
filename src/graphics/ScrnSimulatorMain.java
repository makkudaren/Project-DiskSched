package graphics;

import engine.MainEngine;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

public class ScrnSimulatorMain extends JPanel {
    private Branding branding;
    private MainEngine mainEngine;
    private JPanel parentContainer;

    private JPanel mainPanel;
    private JPanel leftPanel, rightPanel, algoListPanel;

    private JTextArea inputArea;
    private JSpinner headPositionSpinner;
    private JComboBox<String> directionCombo;

    // Tracks which algo button is currently selected (radio-style: one at a time)
    private JButton selectedAlgoBtn = null;
    private String selectedAlgoName = null;
    private final Map<String, JButton> algoBtnMap = new HashMap<>();

    // ==================================================
    //               CONSTRUCTION
    // ==================================================

    public ScrnSimulatorMain(MainEngine mainEngine, Branding branding, JPanel parentContainer) {
        this.branding = branding;
        this.mainEngine = mainEngine;
        this.parentContainer = parentContainer;

        setLayout(new BorderLayout());
        setBackground(branding.dark);

        initializeMainPanel();
        initializePanels();
    }

    // ==================================================
    //               LAYOUT SETUP
    // ==================================================

    public void initializeMainPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(branding.dark);

        int mw = 20, mh = 20;
        wrapper.add(blankPanel(branding.dark, 0, mh), BorderLayout.NORTH);
        wrapper.add(blankPanel(branding.dark, 0, mh), BorderLayout.SOUTH);
        wrapper.add(blankPanel(branding.dark, mw, 0), BorderLayout.WEST);
        wrapper.add(blankPanel(branding.dark, mw, 0), BorderLayout.EAST);

        mainPanel = new JPanel(new BorderLayout(15, 0));
        mainPanel.setBackground(branding.dark);
        wrapper.add(mainPanel, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    public void initializePanels() {
        initializeLeftPanel();
        initializeRightPanel();

        JButton backBtn = createOtherBtn("Back To Menu");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> {
            CardLayout cl = (CardLayout) parentContainer.getLayout();
            cl.show(parentContainer, "MainMenu");
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 54));
        topBar.add(backBtn);

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
    }

    // ==================================================
    //               LEFT PANEL
    // ==================================================

    private void initializeLeftPanel() {
        leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(branding.dark);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(branding.light);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 18, 18);
                g2.dispose();
            }
        };
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(700, 0));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 30, 20, 30));

        // ---- Input Sequence Label ----
        JLabel inputLabel = createHeaderLabel("Input Sequence (from 0-199)", false);
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(inputLabel);
        content.add(Box.createVerticalStrut(4));

        JLabel subtextLabel = createHeaderLabel("Space Separated Values (e.g. 6 7 9 10)", true);
        subtextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtextLabel);
        content.add(Box.createVerticalStrut(10));

        // ---- Text Area ----
        inputArea = new JTextArea();
        inputArea.setFont(branding.jetBrainsRMedium);
        inputArea.setForeground(branding.light);
        inputArea.setBackground(branding.dark);
        inputArea.setCaretColor(branding.light);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(branding.light, 2, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        inputArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputArea.setPreferredSize(new Dimension(Integer.MAX_VALUE, 110));
        inputArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        content.add(inputArea);
        content.add(Box.createVerticalStrut(24));

        // ---- Row: Initial Head Position + Direction ----
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Head Position block
        JPanel headBlock = new JPanel();
        headBlock.setOpaque(false);
        headBlock.setLayout(new BoxLayout(headBlock, BoxLayout.Y_AXIS));

        JLabel headLabel = createHeaderLabel("Initial Head Position", false);
        headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headBlock.add(headLabel);
        headBlock.add(Box.createVerticalStrut(8));

        SpinnerNumberModel headModel = new SpinnerNumberModel(0, 0, 199, 1);
        headPositionSpinner = new JSpinner(headModel);
        headPositionSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        headPositionSpinner.setMaximumSize(new Dimension(160, 44));
        headPositionSpinner.setPreferredSize(new Dimension(160, 44));
        styleSpinner(headPositionSpinner);
        headBlock.add(headPositionSpinner);

        rowPanel.add(headBlock);

        // Direction block
        JPanel dirBlock = new JPanel();
        dirBlock.setOpaque(false);
        dirBlock.setLayout(new BoxLayout(dirBlock, BoxLayout.Y_AXIS));

        JLabel dirLabel = createHeaderLabel("Direction", false);
        dirLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dirBlock.add(dirLabel);
        dirBlock.add(Box.createVerticalStrut(8));

        directionCombo = new JComboBox<>(new String[]{"Left", "Right"});
        directionCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        directionCombo.setMaximumSize(new Dimension(160, 44));
        directionCombo.setPreferredSize(new Dimension(160, 44));
        styleComboBox(directionCombo);
        dirBlock.add(directionCombo);

        rowPanel.add(dirBlock);
        content.add(rowPanel);
        content.add(Box.createVerticalStrut(24));

        // ---- Random / Import Buttons ----
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JButton randomBtn = createOtherBtn("🎲  Random Sequence");
        randomBtn.setPreferredSize(new Dimension(220, 44));
        randomBtn.setMaximumSize(new Dimension(220, 44));
        randomBtn.addActionListener(e -> generateRandomInput());

        JButton importBtn = createOtherBtn("📂  Import Text File");
        importBtn.setPreferredSize(new Dimension(220, 44));
        importBtn.setMaximumSize(new Dimension(220, 44));
        importBtn.addActionListener(e -> importFromFile());

        btnRow.add(randomBtn);
        btnRow.add(importBtn);
        content.add(btnRow);
        content.add(Box.createVerticalGlue());

        leftPanel.add(content, BorderLayout.CENTER);

        // ---- Simulate Button (footer) ----
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 30, 30, 30));

        JButton simulateBtn = new JButton("Simulate") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(branding.light);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        simulateBtn.setFont(branding.jetBrainsBMedium);
        simulateBtn.setForeground(branding.light);
        simulateBtn.setBackground(branding.dark);
        simulateBtn.setContentAreaFilled(false);
        simulateBtn.setBorderPainted(false);
        simulateBtn.setFocusPainted(false);
        simulateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        simulateBtn.setPreferredSize(new Dimension(0, 52));
        simulateBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { simulateBtn.setBackground(branding.darkGray); }
            @Override public void mouseExited(MouseEvent e)   { simulateBtn.setBackground(branding.dark);     }
            @Override public void mousePressed(MouseEvent e)  { simulateBtn.setBackground(branding.darkGray); }
            @Override public void mouseReleased(MouseEvent e) { simulateBtn.setBackground(branding.darkGray); }
        });
        simulateBtn.addActionListener(e -> onSimulate());

        footer.add(simulateBtn, BorderLayout.CENTER);
        leftPanel.add(footer, BorderLayout.SOUTH);
    }

    // ==================================================
    //               RIGHT PANEL (Algorithm List)
    // ==================================================

    public void initializeRightPanel() {
        rightPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(branding.dark);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(branding.light);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                g2.dispose();
            }
        };
        rightPanel.setOpaque(false);

        JLabel algorithmLabel = createHeaderLabel("Algorithms", false);
        algorithmLabel.setBorder(new EmptyBorder(20, 20, 8, 20));
        rightPanel.add(algorithmLabel, BorderLayout.NORTH);

        algoListPanel = new JPanel();
        algoListPanel.setOpaque(false);
        algoListPanel.setLayout(new BoxLayout(algoListPanel, BoxLayout.Y_AXIS));
        algoListPanel.setBorder(new EmptyBorder(0, 16, 16, 16));
        rightPanel.add(algoListPanel, BorderLayout.CENTER);

        String[] algorithms = {
            "First Come First Serve",
            "Shortest Seek Time First",
            "SCAN",
            "C-SCAN",
            "LOOK",
            "C-LOOK"
        };

        for (String algo : algorithms) {
            JButton algoBtn = createOtherBtn(algo);
            algoBtn.putClientProperty("selected", false);

            algoBtn.addActionListener(e -> selectAlgorithm(algo, algoBtn));
            algoBtn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    algoBtn.setBackground(branding.darkGray);
                }
                @Override public void mouseExited(MouseEvent e) {
                    Boolean sel = (Boolean) algoBtn.getClientProperty("selected");
                    algoBtn.setBackground(Boolean.TRUE.equals(sel) ? branding.selected : branding.dark);
                }
                @Override public void mousePressed(MouseEvent e)  { algoBtn.setBackground(branding.selected); }
                @Override public void mouseReleased(MouseEvent e) {
                    Boolean sel = (Boolean) algoBtn.getClientProperty("selected");
                    algoBtn.setBackground(Boolean.TRUE.equals(sel) ? branding.selected : branding.dark);
                }
            });

            algoBtnMap.put(algo, algoBtn);
            algoListPanel.add(Box.createRigidArea(new Dimension(0, 14)));
            algoListPanel.add(algoBtn);
        }
    }

    // ==================================================
    //               ALGORITHM RADIO SELECTION
    // ==================================================

    /** Deselects any previously chosen algorithm and selects the new one. */
    private void selectAlgorithm(String name, JButton btn) {
        // Deselect previous
        if (selectedAlgoBtn != null && selectedAlgoBtn != btn) {
            selectedAlgoBtn.putClientProperty("selected", false);
            selectedAlgoBtn.setBackground(branding.dark);
            mainEngine.setChosenAlgorithm(toEngineKey(selectedAlgoName), false);
        }

        boolean nowSelected = !Boolean.TRUE.equals(btn.getClientProperty("selected"));
        btn.putClientProperty("selected", nowSelected);
        btn.setBackground(nowSelected ? branding.selected : branding.dark);

        if (nowSelected) {
            selectedAlgoBtn  = btn;
            selectedAlgoName = name;
            mainEngine.setChosenAlgorithm(toEngineKey(name), true);
        } else {
            selectedAlgoBtn  = null;
            selectedAlgoName = null;
            mainEngine.setChosenAlgorithm(toEngineKey(name), false);
        }
    }

    /**
     * Maps the short display name used in the UI to the full key used by MainEngine.
     * Extend this mapping if additional algorithms are registered in MainEngine.
     */
    private String toEngineKey(String displayName) {
        if (displayName == null) return "";
        return switch (displayName) {
            case "First Come First Serve"   -> "First-Come, First-Served (FCFS)";
            case "Shortest Seek Time First" -> "Shortest Seek Time First (SSTF)";
            default                         -> displayName; // SCAN, C-SCAN, LOOK, C-LOOK match exactly
        };
    }

    // ==================================================
    //               SIMULATE ACTION
    // ==================================================

    private void onSimulate() {
        if (!validatePreSimulation()) return;

        int headPos   = (int) headPositionSpinner.getValue();
        String dir    = (String) directionCombo.getSelectedItem();
        String[] tokens = inputArea.getText().trim().split("\\s+");
        int[] refString = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++)
            refString[i] = Integer.parseInt(tokens[i]);

        // frameCount is repurposed as headPos for disk scheduling;
        // direction is carried via AlgoResult (see MainEngine.runSimulation).
        java.util.List<ScrnSimulatorOutput.AlgoResult> results =
            mainEngine.runSimulation(refString, headPos, dir);

        mainEngine.getGUI().getSimulatorOutput().loadSimulationResults(results);

        CardLayout cl = (CardLayout) parentContainer.getLayout();
        cl.show(parentContainer, "SimulatorOutput");
    }

    // ==================================================
    //               VALIDATION
    // ==================================================

    public boolean validatePreSimulation() {
        String raw = inputArea.getText();
        if (raw == null || raw.trim().isEmpty()) {
            showValidationError("Input sequence cannot be empty.");
            return false;
        }

        String[] tokens = raw.trim().split("\\s+");
        if (tokens.length < 1 || tokens.length > 40) {
            showValidationError("Input sequence must contain between 1 and 40 values.");
            return false;
        }

        for (String tok : tokens) {
            if (!isInteger(tok)) {
                showValidationError("Input sequence must contain only integers separated by spaces.");
                return false;
            }
            int v = Integer.parseInt(tok);
            if (v < 0 || v > 199) {
                showValidationError("Cylinder values must be between 0 and 199. Found: " + v);
                return false;
            }
        }

        if (selectedAlgoName == null) {
            showValidationError("Please select a disk scheduling algorithm.");
            return false;
        }

        return true;
    }

    // ==================================================
    //               FILE IMPORT
    // ==================================================

    public void importFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Input Sequence from Text File");
        chooser.setFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter("Text files (*.txt)", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        MainEngine.ImportResult result = mainEngine.importFromFile(chooser.getSelectedFile());
        if (!result.valid) { showValidationError(result.errorMessage); return; }

        inputArea.setText(result.inputString);
        headPositionSpinner.setValue(result.headPosition);

        // Set direction if present in file
        if (result.direction != null) {
            directionCombo.setSelectedItem(result.direction);
        }
    }

    // ==================================================
    //               RANDOM INPUT
    // ==================================================

    public void generateRandomInput() {
        MainEngine.RandomInput ri = mainEngine.generateRandomInput();
        inputArea.setText(ri.inputString);
        headPositionSpinner.setValue(ri.headPosition);
        directionCombo.setSelectedItem(ri.direction);
    }

    // ==================================================
    //               STYLING HELPERS
    // ==================================================

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(branding.dark);
        spinner.setForeground(branding.light);
        spinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(branding.light, 2, true),
            new EmptyBorder(5, 5, 5, 5)
        ));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            JTextField tf = de.getTextField();
            tf.setFont(branding.jetBrainsRMedium);
            tf.setForeground(branding.light);
            tf.setBackground(branding.dark);
            tf.setCaretColor(branding.light);
            tf.setBorder(new EmptyBorder(0, 8, 0, 4));
            tf.setHorizontalAlignment(JTextField.LEFT);
        }

        for (Component comp : spinner.getComponents()) {
            if (comp instanceof JButton button) {
                button.setBackground(branding.dark);
                button.setForeground(branding.light);
                button.setBorder(new EmptyBorder(2, 4, 2, 4));
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
                button.setOpaque(false);
                String name = button.getName();
                button.setIcon(new Icon() {
                    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(branding.light);
                        int w = getIconWidth(), h = getIconHeight();
                        if ("Spinner.nextButton".equals(name)) {
                            int[] xp = {x, x + w / 2, x + w};
                            int[] yp = {y + h, y, y + h};
                            g2.fillPolygon(xp, yp, 3);
                        } else {
                            int[] xp = {x, x + w / 2, x + w};
                            int[] yp = {y, y + h, y};
                            g2.fillPolygon(xp, yp, 3);
                        }
                        g2.dispose();
                    }
                    @Override public int getIconWidth()  { return 8; }
                    @Override public int getIconHeight() { return 6; }
                });
            }
        }
    }

    public void styleComboBox(JComboBox<String> box) {
        box.setFont(branding.jetBrainsRMedium);
        box.setForeground(branding.light);
        box.setBackground(branding.dark);
        box.setBorder(new LineBorder(branding.light, 2, true));
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? branding.darkGray : branding.dark);
                setForeground(branding.light);
                setFont(branding.jetBrainsRMedium);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });

        // Style the popup arrow button
        for (int i = 0; i < box.getComponentCount(); i++) {
            Component c = box.getComponent(i);
            if (c instanceof AbstractButton ab) {
                ab.setBackground(branding.dark);
                ab.setBorderPainted(false);
                ab.setContentAreaFilled(false);
                ab.setIcon(new Icon() {
                    @Override public void paintIcon(Component comp, Graphics g, int x, int y) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(branding.light);
                        int w = 10, h = 6;
                        int cx = x + w / 2;
                        int[] xp = {x, cx, x + w};
                        int[] yp = {y, y + h, y};
                        g2.fillPolygon(xp, yp, 3);
                        g2.dispose();
                    }
                    @Override public int getIconWidth()  { return 10; }
                    @Override public int getIconHeight() { return 6;  }
                });
            }
        }
    }

    public JButton createOtherBtn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(branding.light);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(branding.jetBrainsBMedium);
        btn.setForeground(branding.light);
        btn.setBackground(branding.dark);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(120, 44));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { btn.setBackground(branding.darkGray); }
            @Override public void mouseExited(MouseEvent e)   { btn.setBackground(branding.dark);     }
            @Override public void mousePressed(MouseEvent e)  { btn.setBackground(branding.darkGray); }
            @Override public void mouseReleased(MouseEvent e) { btn.setBackground(branding.darkGray); }
        });
        return btn;
    }

    public JLabel createHeaderLabel(String text, boolean muted) {
        JLabel lbl = new JLabel(text, SwingConstants.LEFT);
        lbl.setFont(branding.jetBrainsBMedium);
        lbl.setForeground(muted ? branding.darkGray : branding.light);
        return lbl;
    }

    public JPanel blankPanel(Color bg, int w, int h) {
        JPanel p = new JPanel();
        p.setBackground(bg);
        if (w > 0) p.setPreferredSize(new Dimension(w, 0));
        if (h > 0) p.setPreferredSize(new Dimension(0, h));
        return p;
    }

    public void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        try { Integer.parseInt(str); return true; }
        catch (NumberFormatException e) { return false; }
    }

    // ==================================================
    //               REFRESH STYLES
    // ==================================================

    public void refreshStyles() {
        inputArea.setForeground(branding.light);
        inputArea.setBackground(branding.dark);
        inputArea.setCaretColor(branding.light);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(branding.light, 2, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        styleSpinner(headPositionSpinner);
        styleComboBox(directionCombo);
        leftPanel.repaint();
        rightPanel.repaint();
        refreshButtonStyles(leftPanel);
        refreshButtonStyles(rightPanel);
        refreshButtonStyles(algoListPanel);
    }

    private void refreshButtonStyles(java.awt.Container container) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof JButton btn) {
                btn.setForeground(branding.light);
                Boolean selected = (Boolean) btn.getClientProperty("selected");
                btn.setBackground(Boolean.TRUE.equals(selected) ? branding.selected : branding.dark);
            } else if (c instanceof java.awt.Container cont) {
                refreshButtonStyles(cont);
            }
        }
    }

    // ==================================================
    //               GETTERS
    // ==================================================

    public JPanel getLeftPanel()      { return leftPanel; }
    public JPanel getRightPanel()     { return rightPanel; }
    public JTextArea getInputArea()   { return inputArea; }
    public int getHeadPosition()      { return (int) headPositionSpinner.getValue(); }
    public String getDirection()      { return (String) directionCombo.getSelectedItem(); }
}