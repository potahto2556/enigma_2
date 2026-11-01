package com.enigma;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Dark detective case-board UI. The controls call GameController for all game rules. */
public class GameWindow extends JFrame {
    private static final Color BACKGROUND = new Color(10, 15, 24);
    private static final Color PANEL = new Color(17, 25, 38);
    private static final Color PANEL_LIGHT = new Color(24, 35, 51);
    private static final Color BORDER = new Color(43, 58, 76);
    private static final Color TEXT = new Color(231, 237, 245);
    private static final Color MUTED = new Color(143, 158, 178);
    private static final Color RED = new Color(204, 57, 67);
    private static final Color TEAL = new Color(68, 190, 196);

    private final GameController controller;
    private final JTextArea story = new JTextArea();
    private final JTextArea evidence = new JTextArea();
    private final JLabel scoreLabel = new JLabel();
    private final JComboBox<String> locations;
    private final JComboBox<String> suspects;
    private final JButton investigateButton = makeButton("SEARCH LOCATION", TEAL, new Color(8, 20, 26));
    private final JButton interviewButton = makeButton("START INTERVIEW", PANEL_LIGHT, TEXT);
    private final JButton accuseButton = makeButton("MAKE ACCUSATION", RED, Color.WHITE);
    private final JButton deductionsButton = makeButton("REVIEW DEDUCTIONS", PANEL_LIGHT, TEXT);

    public GameWindow(GameController controller) {
        this.controller = controller;
        setTitle("ENIGMA | The Last Broadcast");
        setSize(1120, 760);
        setMinimumSize(new Dimension(900, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        locations = new JComboBox<>(controller.getLocationNames().toArray(new String[0]));
        suspects = new JComboBox<>(controller.getSuspectNames().toArray(new String[0]));
        styleTextAreas();

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(20, 22, 18, 22));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainContent(), BorderLayout.CENTER);
        root.add(buildActionBar(), BorderLayout.SOUTH);
        setContentPane(root);

        investigateButton.addActionListener(event -> investigate());
        interviewButton.addActionListener(event -> interview());
        accuseButton.addActionListener(event -> accuse());
        deductionsButton.addActionListener(event -> showDeductions());

        appendStory("CASE BRIEFING\n\n" + controller.getBriefing());
        refreshEvidenceAndScore();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("E");
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        mark.setForeground(Color.WHITE);
        mark.setOpaque(true);
        mark.setBackground(RED);
        mark.setPreferredSize(new Dimension(46, 46));
        mark.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel headings = new JPanel();
        headings.setOpaque(false);
        headings.setLayout(new BoxLayout(headings, BoxLayout.Y_AXIS));
        JLabel eyebrow = label("CASE FILE 001  /  BROADCAST STATION", 11, TEAL, Font.BOLD);
        JLabel title = label("THE LAST BROADCAST", 23, TEXT, Font.BOLD);
        JLabel subtitle = label("A three-minute blackout. A missing witness. Four stories.", 13, MUTED, Font.PLAIN);
        headings.add(eyebrow);
        headings.add(Box.createVerticalStrut(3));
        headings.add(title);
        headings.add(Box.createVerticalStrut(2));
        headings.add(subtitle);
        brand.add(mark, BorderLayout.WEST);
        brand.add(headings, BorderLayout.CENTER);

        scoreLabel.setForeground(TEXT);
        scoreLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(PANEL_LIGHT);
        scoreLabel.setBorder(new EmptyBorder(11, 16, 11, 16));
        header.add(brand, BorderLayout.WEST);
        header.add(scoreLabel, BorderLayout.EAST);
        return header;
    }

    private JSplitPane buildMainContent() {
        JPanel dialoguePanel = new JPanel(new BorderLayout(0, 12));
        dialoguePanel.setBackground(PANEL);
        dialoguePanel.setBorder(cardBorder());
        JPanel dialogueHeader = new JPanel(new BorderLayout());
        dialogueHeader.setOpaque(false);
        dialogueHeader.add(label("INVESTIGATION LOG", 12, TEAL, Font.BOLD), BorderLayout.WEST);
        dialogueHeader.add(label("LIVE CASE NOTES", 11, MUTED, Font.PLAIN), BorderLayout.EAST);
        dialoguePanel.add(dialogueHeader, BorderLayout.NORTH);
        dialoguePanel.add(new JScrollPane(story), BorderLayout.CENTER);

        JPanel evidencePanel = new JPanel(new BorderLayout(0, 12));
        evidencePanel.setBackground(PANEL);
        evidencePanel.setBorder(cardBorder());
        JPanel evidenceHeader = new JPanel(new BorderLayout());
        evidenceHeader.setOpaque(false);
        evidenceHeader.add(label("EVIDENCE BOARD", 12, TEAL, Font.BOLD), BorderLayout.WEST);
        evidenceHeader.add(label("COLLECTED CLUES", 10, MUTED, Font.PLAIN), BorderLayout.EAST);
        evidencePanel.add(evidenceHeader, BorderLayout.NORTH);
        evidencePanel.add(new JScrollPane(evidence), BorderLayout.CENTER);

        JPanel suspectPanel = new JPanel();
        suspectPanel.setOpaque(false);
        suspectPanel.setLayout(new BoxLayout(suspectPanel, BoxLayout.Y_AXIS));
        suspectPanel.add(label("PERSONS OF INTEREST", 11, MUTED, Font.BOLD));
        suspectPanel.add(Box.createVerticalStrut(7));
        for (String name : controller.getSuspectNames()) {
            JButton suspectCard = makeButton("◉   " + name.toUpperCase(), PANEL_LIGHT, TEXT);
            suspectCard.setHorizontalAlignment(SwingConstants.LEFT);
            suspectCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            suspectCard.addActionListener(event -> suspects.setSelectedItem(name));
            suspectPanel.add(suspectCard);
            suspectPanel.add(Box.createVerticalStrut(5));
        }
        evidencePanel.add(suspectPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dialoguePanel, evidencePanel);
        split.setResizeWeight(0.69);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setOpaque(false);
        split.setBackground(BACKGROUND);
        return split;
    }

    private JPanel buildActionBar() {
        JPanel outer = new JPanel(new BorderLayout(12, 10));
        outer.setOpaque(false);

        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        selectors.setOpaque(false);
        selectors.add(label("LOCATION", 10, MUTED, Font.BOLD));
        styleCombo(locations);
        selectors.add(locations);
        selectors.add(label("SUSPECT", 10, MUTED, Font.BOLD));
        styleCombo(suspects);
        selectors.add(suspects);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(investigateButton);
        actions.add(interviewButton);
        actions.add(deductionsButton);
        actions.add(accuseButton);

        outer.add(selectors, BorderLayout.NORTH);
        outer.add(actions, BorderLayout.SOUTH);
        return outer;
    }

    private void styleTextAreas() {
        story.setEditable(false);
        story.setLineWrap(true);
        story.setWrapStyleWord(true);
        story.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        story.setForeground(TEXT);
        story.setBackground(PANEL);
        story.setCaretColor(TEAL);
        story.setBorder(new EmptyBorder(6, 4, 6, 8));

        evidence.setEditable(false);
        evidence.setLineWrap(true);
        evidence.setWrapStyleWord(true);
        evidence.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        evidence.setForeground(TEXT);
        evidence.setBackground(PANEL);
        evidence.setCaretColor(TEAL);
        evidence.setBorder(new EmptyBorder(6, 4, 6, 8));
    }

    private void investigate() {
        String location = (String) locations.getSelectedItem();
        if (location != null) appendStory(controller.investigate(location));
        refreshEvidenceAndScore();
    }

    private void interview() {
        String suspect = (String) suspects.getSelectedItem();
        if (suspect != null) appendStory(controller.interview(suspect));
        refreshEvidenceAndScore();
    }

    private void accuse() {
        String suspect = (String) suspects.getSelectedItem();
        if (suspect == null) return;
        appendStory(controller.accuse(suspect));
        refreshEvidenceAndScore();
        if (controller.isSolved()) {
            investigateButton.setEnabled(false);
            interviewButton.setEnabled(false);
            accuseButton.setEnabled(false);
        }
    }

    private void showDeductions() {
        appendStory(controller.getDeductions());
    }

    private void appendStory(String text) {
        story.append("\n\n────────────────────────────────────────\n" + text);
        story.setCaretPosition(story.getDocument().getLength());
    }

    private void refreshEvidenceAndScore() {
        evidence.setText(controller.getEvidenceList());
        evidence.setCaretPosition(0);
        scoreLabel.setText("DETECTIVE  •  " + controller.getScore() + " XP");
    }

    private static JLabel label(String text, int size, Color color, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, style, size));
        label.setForeground(color);
        return label;
    }

    private static JButton makeButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 13, 10, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        combo.setForeground(TEXT);
        combo.setBackground(PANEL_LIGHT);
        combo.setPreferredSize(new Dimension(155, 36));
    }

    private static EmptyBorder cardBorder() {
        return new EmptyBorder(15, 16, 14, 16);
    }
}
