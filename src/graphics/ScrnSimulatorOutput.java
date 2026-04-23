package graphics;

import engine.MainEngine;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

public class ScrnSimulatorOutput extends JPanel {
    private Branding branding;
    private MainEngine mainEngine;
    private JPanel parentContainer;

    // Top-bar widgets
    private JLabel timerLabel;
    private JButton speedButton;
    private JButton exportPngBtn;
    private JButton exportPdfBtn;
    private float currentSpeed = 1.0f;
    private static final float[] SPEEDS = {0.5f, 1.0f, 1.5f, 2.0f, 4.0f, 8.0f, 16.0f, 32.0f};

    // Scroll area
    private JPanel scrollContent;
    private JScrollPane mainScroll;

    // Animation state
    private Timer simulationTimer;
    private int currentTime = 0;
    private final List<SeekChartPanel> chartPanels = new ArrayList<>();

    public static class AlgoResult {
        public String algorithmName;
        public int[] seekSequence;
        public int initialHead;
        public String direction;
        public int totalSeekTime;

        public int[] referenceString;
        public int pageFrameCount;
        public int totalPageFaults;
        public int[][]  frameStates;
        public boolean[] hits;
    }

    private class SeekChartPanel extends JPanel {
        private final AlgoResult r;
        private int revealedSegments = 0;
        private boolean animationComplete = false;
        private int animSeekTime = 0;

        // Layout constants
        private static final int RULER_H = 52;
        private static final int STEP_H = 36;
        private static final int LABEL_LEFT = 10;
        private static final int PAD_RIGHT = 10;
        private static final int PAD_BOTTOM = 20;
        private static final int DOT_R = 5;
        private static final int FIXED_W = 1100;

        SeekChartPanel(AlgoResult r) {
            this.r = r;
            setOpaque(false);
        }
        
        void advance() {
            int totalSegments = r.seekSequence.length - 1;
            if (revealedSegments < totalSegments) {
                revealedSegments++;
                // Accumulate seek time dynamically
                int from = r.seekSequence[revealedSegments - 1];
                int to = r.seekSequence[revealedSegments];
                animSeekTime += Math.abs(to - from);
                if (revealedSegments == totalSegments) {
                    animationComplete = true;
                }
                repaint();
            }
        }

        void reset() {
            revealedSegments = 0;
            animationComplete = false;
            animSeekTime = 0;
            repaint();
        }

        boolean isComplete() { return animationComplete; }

        @Override
        public Dimension getPreferredSize() {
            int steps = r.seekSequence.length;
            int h = RULER_H + steps * STEP_H + PAD_BOTTOM;
            return new Dimension(FIXED_W, h);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (r.seekSequence == null || r.seekSequence.length == 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int chartLeft = LABEL_LEFT;
            int chartW = getWidth() - chartLeft - PAD_RIGHT;

            // Build a set of all queue positions
            java.util.Set<Integer> queueSet = new java.util.HashSet<>();
            if (r.seekSequence != null) {
                for (int pos : r.seekSequence) queueSet.add(pos);
            }

            // Ruler baseline
            g2.setColor(branding.light);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(chartLeft, RULER_H, chartLeft + chartW, RULER_H);

            // Draw every cylinder 0-199
            g2.setFont(branding.jetBrainsBSmall);
            FontMetrics fm = g2.getFontMetrics();

            for (int cyl = 0; cyl <= 199; cyl++) {
                int x = cylToX(cyl, chartLeft, chartW);
                boolean isQueue = queueSet.contains(cyl);

                if (isQueue) {
                    g2.setColor(branding.light);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(x, RULER_H - 10, x, RULER_H + 4);
                    
                    String lbl = String.valueOf(cyl);
                    int lx = x - fm.stringWidth(lbl) / 2;
                    g2.drawString(lbl, lx, RULER_H - 13);
                } else {
                    Color dim = new Color(branding.light.getRed(), branding.light.getGreen(),
                                         branding.light.getBlue(), 55);
                    g2.setColor(dim);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawLine(x, RULER_H - 4, x, RULER_H + 2);

                    // Only label every 25 to avoid clutter for non-queue positions
                    if (cyl % 25 == 0) {
                        String lbl = String.valueOf(cyl);
                        int lx = x - fm.stringWidth(lbl) / 2;
                        g2.drawString(lbl, lx, RULER_H - 7);
                    }
                }
            }

            // Seek path
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int steps = r.seekSequence.length;

            for (int i = 0; i <= revealedSegments && i < steps; i++) {
                int cx = cylToX(r.seekSequence[i], chartLeft, chartW);
                int cy = RULER_H + (i + 1) * STEP_H;

                // Draw dot for each visited position
                Color dotColor = (i == 0) ? new Color(0x4CAF50) : branding.light;
                g2.setColor(dotColor);
                g2.fillOval(cx - DOT_R, cy - DOT_R, DOT_R * 2, DOT_R * 2);

                // Cylinder label beside dot
                String posLbl = String.valueOf(r.seekSequence[i]);
                int lx = cx + DOT_R + 4;
                int maxX = FIXED_W - PAD_RIGHT - fm.stringWidth(posLbl) - 2;
                if (lx > maxX) lx = cx - DOT_R - fm.stringWidth(posLbl) - 4;
                g2.setColor(branding.light);
                g2.drawString(posLbl, lx, cy + fm.getAscent() / 2);

                // Draw connecting line to previous point
                if (i > 0) {
                    int px = cylToX(r.seekSequence[i - 1], chartLeft, chartW);
                    int py = RULER_H + i * STEP_H;
                    g2.setColor(branding.light);
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(px, py, cx, cy);
                }

                // Dashed vertical guide line (faint)
                float[] dash = {4, 6};
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10, dash, 0));
                g2.setColor(new Color(branding.light.getRed(), branding.light.getGreen(),
                        branding.light.getBlue(), 40));
                g2.drawLine(cx, RULER_H, cx, cy);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }

            g2.dispose();
        }
        
        private int cylToX(int cyl, int chartLeft, int chartW) {
            return chartLeft + (int) Math.round(cyl / 199.0 * chartW);
        }
    }

    public ScrnSimulatorOutput(MainEngine mainEngine, Branding branding, JPanel parentContainer) {
        this.branding = branding;
        this.mainEngine = mainEngine;
        this.parentContainer = parentContainer;
        setLayout(new BorderLayout());
        setBackground(branding.dark);
        buildTopBar();
        buildScrollArea();
    }

    private void buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(branding.dark);
        bar.setBorder(new EmptyBorder(16, 20, 10, 20));

        // Back button
        JButton backBtn = makePillButton("Back To Simulator");
        backBtn.setPreferredSize(new Dimension(270, 48));
        backBtn.addActionListener(e -> {
            stopTimer();
            CardLayout cl = (CardLayout) parentContainer.getLayout();
            cl.show(parentContainer, "SimulatorMain");
        });

        // Timer label
        timerLabel = new JLabel("Timer: 0:00");
        timerLabel.setFont(branding.jetBrainsBMedium);
        timerLabel.setForeground(branding.light);

        // Speed dropdown
        speedButton = makePillButton("2.0x  ▼");
        currentSpeed = 2.0f;
        speedButton.setPreferredSize(new Dimension(130, 40));
        speedButton.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            menu.setBackground(branding.dark);
            menu.setBorder(BorderFactory.createLineBorder(branding.light, 2, true));
            for (float spd : SPEEDS) {
                String label = spd + "x";
                JMenuItem item = new JMenuItem(label) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(getBackground());
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                item.setFont(branding.jetBrainsBMedium);
                item.setForeground(branding.light);
                item.setBackground(branding.dark);
                item.setOpaque(true);
                item.setBorder(new EmptyBorder(8, 16, 8, 16));
                item.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { item.setBackground(branding.darkGray); }
                    @Override public void mouseExited(MouseEvent e) { item.setBackground(branding.dark); }
                });
                item.addActionListener(ev -> {
                    currentSpeed = spd;
                    speedButton.setText(label + "  ▼");
                    if (simulationTimer != null && simulationTimer.isRunning())
                        simulationTimer.setDelay((int)(1000 / currentSpeed));
                });
                menu.add(item);
            }
            menu.show(speedButton, 0, speedButton.getHeight());
        });

        JLabel speedLbl = new JLabel("Speed");
        speedLbl.setFont(branding.jetBrainsBMedium);
        speedLbl.setForeground(branding.light);
        speedLbl.setBorder(new EmptyBorder(0, 24, 0, 8));

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        center.setOpaque(false);
        center.add(timerLabel);
        center.add(speedLbl);
        center.add(speedButton);

        // Export buttons (disabled until animation finishes)
        exportPdfBtn = makePillButton("Export to PDF");
        exportPdfBtn.setPreferredSize(new Dimension(180, 48));
        exportPdfBtn.addActionListener(e -> exportToPdf());

        exportPngBtn = makePillButton("Export to PNG");
        exportPngBtn.setPreferredSize(new Dimension(180, 48));
        exportPngBtn.addActionListener(e -> exportToPng());

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exportPanel.setOpaque(false);
        exportPanel.add(exportPdfBtn);
        exportPanel.add(exportPngBtn);

        bar.add(backBtn,     BorderLayout.WEST);
        bar.add(center,      BorderLayout.CENTER);
        bar.add(exportPanel, BorderLayout.EAST);

        add(bar, BorderLayout.NORTH);
    }

    private void buildScrollArea() {
        scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(branding.dark);
        scrollContent.setBorder(new EmptyBorder(0, 20, 20, 20));

        mainScroll = new JScrollPane(scrollContent,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setBorder(null);
        mainScroll.setBackground(branding.dark);
        mainScroll.getViewport().setBackground(branding.dark);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.getHorizontalScrollBar().setUnitIncrement(16);
        styleScrollBar(mainScroll.getVerticalScrollBar());
        styleScrollBar(mainScroll.getHorizontalScrollBar());

        add(mainScroll, BorderLayout.CENTER);
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = branding.light;
                trackColor = branding.dark;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
        });
    }

    public void loadSimulationResults(List<AlgoResult> results) {
        stopTimer();
        scrollContent.removeAll();
        chartPanels.clear();
        setExportEnabled(false);

        for (AlgoResult r : results) {
            scrollContent.add(Box.createVerticalStrut(16));
            scrollContent.add(buildAlgoPanel(r));
        }
        scrollContent.add(Box.createVerticalStrut(16));
        scrollContent.revalidate();
        scrollContent.repaint();
        startTimer();
    }

    private JPanel buildAlgoPanel(AlgoResult r) {

        // Seek chart (animated)
        SeekChartPanel chart = new SeekChartPanel(r);
        chartPanels.add(chart);

        // Live seek-time label
        JLabel seekLabel = new JLabel("Total Seek Time: 0");
        seekLabel.setFont(branding.jetBrainsBMedium);
        seekLabel.setForeground(branding.light);
        seekLabel.setBorder(new EmptyBorder(10, 24, 16, 24));

        chart.addPropertyChangeListener("seekTime", evt ->
            seekLabel.setText("Total Seek Time: " + evt.getNewValue())
        );
        chart.putClientProperty("seekLabel", seekLabel);

        // Outer card panel
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(branding.dark);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(branding.light);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 24, 8, 24));

        JLabel titleLbl = monoLabel(r.algorithmName, branding.jetBrainsBMedium, 14);
        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));

        // Format queue without the initial head position
        if (r.seekSequence != null && r.seekSequence.length > 1) {
            int[] queue = new int[r.seekSequence.length - 1];
            System.arraycopy(r.seekSequence, 1, queue, 0, queue.length);
            header.add(monoLabel("Queue: " + joinInts(queue), branding.jetBrainsRMedium, 12));
        }
        header.add(monoLabel("Initial Head Position: " + r.initialHead, branding.jetBrainsRMedium, 12));
        header.add(monoLabel("Direction: " + (r.direction != null ? r.direction : "N/A"),
                branding.jetBrainsRMedium, 12));
        
        JScrollPane chartScroll = new JScrollPane(chart,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chartScroll.setBorder(new EmptyBorder(0, 14, 0, 14));
        chartScroll.setOpaque(false);
        chartScroll.getViewport().setOpaque(false);
        chartScroll.getViewport().setBackground(branding.dark);
        styleScrollBar(chartScroll.getHorizontalScrollBar());
        chartScroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 6));

        card.add(header, BorderLayout.NORTH);
        card.add(chartScroll, BorderLayout.CENTER);
        card.add(seekLabel, BorderLayout.SOUTH);
        return card;
    }

    private void startTimer() {
        currentTime = 0;
        timerLabel.setText("Timer: 0:00");

        simulationTimer = new Timer((int)(1000 / currentSpeed), null);
        simulationTimer.addActionListener(e -> {
            currentTime++;
            int m = currentTime / 60, s = currentTime % 60;
            timerLabel.setText(String.format("Timer: %d:%02d", m, s));

            boolean allDone = true;
            for (SeekChartPanel cp : chartPanels) {
                cp.advance();
                
                JLabel lbl = (JLabel) cp.getClientProperty("seekLabel");
                if (lbl != null) lbl.setText("Total Seek Time: " + cp.animSeekTime);
                if (!cp.isComplete()) allDone = false;
            }

            if (allDone) {
                simulationTimer.stop();
                setExportEnabled(true);
            }
        });
        simulationTimer.start();
    }

    private void stopTimer() {
        if (simulationTimer != null && simulationTimer.isRunning())
            simulationTimer.stop();
    }

    private void exportToPng() {
        try {
            String ts = new java.text.SimpleDateFormat("MMddyy_HHmmss").format(new java.util.Date());
            File   file = new File(ts + "_DS.png");

            int panelW = scrollContent.getWidth();
            int panelH = scrollContent.getHeight();
            BufferedImage img = new BufferedImage(panelW, panelH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(branding.dark);
            g2.fillRect(0, 0, panelW, panelH);
            scrollContent.paint(g2);
            g2.dispose();
            ImageIO.write(img, "PNG", file);

            JOptionPane.showMessageDialog(this,
                    "Exported to: " + file.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToPdf() {
        try {
            String ts = new java.text.SimpleDateFormat("MMddyy_HHmmss").format(new java.util.Date());
            File   file = new File(ts + "_DS.pdf");

            int panelW = scrollContent.getWidth();
            int panelH = scrollContent.getHeight();
            BufferedImage img = new BufferedImage(panelW, panelH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(branding.dark);
            g2.fillRect(0, 0, panelW, panelH);
            scrollContent.paint(g2);
            g2.dispose();

            ByteArrayOutputStream jpegBaos = new ByteArrayOutputStream();
            ImageIO.write(img, "JPEG", jpegBaos);
            byte[] jpegBytes = jpegBaos.toByteArray();

            final double PT_PER_PX = 72.0 / 96.0;
            int pageW = (int) Math.ceil(panelW * PT_PER_PX);
            int pageH = (int) Math.ceil(panelH * PT_PER_PX);

            ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
            int[] off = new int[6];

            writePdf(pdfBaos, "%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n");

            off[1] = pdfBaos.size();
            writePdf(pdfBaos, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            off[2] = pdfBaos.size();
            writePdf(pdfBaos, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            off[3] = pdfBaos.size();
            writePdf(pdfBaos, "3 0 obj\n<< /Type /Page /Parent 2 0 R "
                    + "/MediaBox [0 0 " + pageW + " " + pageH + "] "
                    + "/Contents 5 0 R /Resources << /XObject << /Img 4 0 R >> >> >>\nendobj\n");

            off[4] = pdfBaos.size();
            writePdf(pdfBaos, "4 0 obj\n<< /Type /XObject /Subtype /Image "
                    + "/Width " + panelW + " /Height " + panelH
                    + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode "
                    + "/Length " + jpegBytes.length + " >>\nstream\n");
            pdfBaos.write(jpegBytes);
            writePdf(pdfBaos, "\nendstream\nendobj\n");

            String cont = "q\n" + pageW + " 0 0 " + pageH + " 0 0 cm\n/Img Do\nQ\n";
            byte[] contBytes = cont.getBytes("US-ASCII");
            off[5] = pdfBaos.size();
            writePdf(pdfBaos, "5 0 obj\n<< /Length " + contBytes.length + " >>\nstream\n");
            pdfBaos.write(contBytes);
            writePdf(pdfBaos, "\nendstream\nendobj\n");

            int xrefOff = pdfBaos.size();
            writePdf(pdfBaos, "xref\n0 6\n0000000000 65535 f \n");
            for (int i = 1; i <= 5; i++)
                writePdf(pdfBaos, String.format("%010d 00000 n \n", off[i]));
            writePdf(pdfBaos, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefOff + "\n%%EOF\n");

            try (FileOutputStream fos = new FileOutputStream(file)) { pdfBaos.writeTo(fos); }

            JOptionPane.showMessageDialog(this,
                    "Exported to: " + file.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writePdf(OutputStream out, String s) throws IOException {
        out.write(s.getBytes("ISO-8859-1"));
    }

    private void setExportEnabled(boolean enabled) {
        if (exportPngBtn != null) exportPngBtn.setEnabled(enabled);
        if (exportPdfBtn != null) exportPdfBtn.setEnabled(enabled);
    }

    private JLabel monoLabel(String text, Font font, int size) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font.deriveFont((float) size));
        lbl.setForeground(branding.light);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton makePillButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(branding.light);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, getHeight(), getHeight());
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
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(branding.darkGray); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(branding.dark);     }
        });
        return btn;
    }

    private String joinInts(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public void refreshStyles() {
        setBackground(branding.dark);
        scrollContent.setBackground(branding.dark);
        mainScroll.setBackground(branding.dark);
        mainScroll.getViewport().setBackground(branding.dark);
        styleScrollBar(mainScroll.getVerticalScrollBar());
        styleScrollBar(mainScroll.getHorizontalScrollBar());
        revalidate();
        repaint();
    }
}