package ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Dashboard extends JFrame {
    private JTextArea logArea;
    private JLabel totalAttacksLabel;
    private Map<Integer, Integer> portCounters = new HashMap<>();
    private Map<Integer, JLabel> portLabels = new HashMap<>();
    private DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();

    private ChartPanel chartPanel;

    private boolean isDarkMode = false;

    public Dashboard() {
        // Set light theme by default
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to set theme.");
        }

        setTitle("Honeypot Dashboard");
        setSize(800, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Text log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(800, 150)); // Set height as needed


        // Top Panel for status
        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        totalAttacksLabel = new JLabel("Total Attacks: 0", SwingConstants.CENTER);
        topPanel.add(totalAttacksLabel);

        JPanel portStatusPanel = new JPanel(new GridLayout(1, 4));
        for (int port : new int[]{21, 22, 8080, 2222}) {
            portCounters.put(port, 0);
            JLabel label = new JLabel("Port " + port + ": 0", SwingConstants.CENTER);
            portLabels.put(port, label);
            portStatusPanel.add(label);
            pieDataset.setValue("Port " + port, 1);
        }
        topPanel.add(portStatusPanel);

        // Pie chart
        JFreeChart chart = ChartFactory.createPieChart("Attack Distribution", pieDataset, true, true, false);
        chartPanel = new ChartPanel(chart);

        // Bottom Panel: logs + dark mode toggle
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JButton toggleThemeBtn = new JButton("Enable Dark Mode");
        toggleThemeBtn.addActionListener(e -> switchTheme());

        bottomPanel.add(toggleThemeBtn, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        // Layout
        add(topPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void addLog(String log) {
        logArea.append(log + "\n");

    // Auto-scroll to bottom
    logArea.setCaretPosition(logArea.getDocument().getLength());

    // Force layout update to fix the visibility issue
    logArea.revalidate();
    logArea.repaint();
    }

    public void updateAttack(int port) {
        SwingUtilities.invokeLater(() -> {
            int total = Integer.parseInt(totalAttacksLabel.getText().split(": ")[1]) + 1;
            totalAttacksLabel.setText("Total Attacks: " + total);
    
            int newCount = portCounters.get(port) + 1;
            portCounters.put(port, newCount);
            portLabels.get(port).setText("Port " + port + ": " + newCount);
    
            // Update pie chart
            pieDataset.setValue("Port " + port, newCount);
        });
    }
    

    private void switchTheme() {
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            }
            isDarkMode = !isDarkMode;

            // Refresh all components
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
