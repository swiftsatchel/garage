package me.soggysandwich.bedroom.dialog.history;

import me.soggysandwich.bedroom.dialog.alert.AlertDialog;
import me.soggysandwich.bedroom.dialog.alert.YesNoDialog;
import me.soggysandwich.bedroom.util.Ops;
import me.soggysandwich.bedroom.util.Settings;
import me.soggysandwich.bedroom.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;

public class ShiftHistoryWindow extends JFrame implements KeyListener, WindowListener, MouseListener {

    private final Component parent;
    private final ShiftHistoryChart chart = new ShiftHistoryChart();

    private final JPanel topRow = new JPanel();
    private final JLabel showingLabel = new JLabel("Data points to show:");
    private final JComboBox<String> ptsAmount = new JComboBox<>(getAllowedAmounts());
    private final JLabel datesShown = new JLabel("None");
    private final JButton leftButton = new JButton("<");
    private final JLabel pagesLabel = new JLabel("Page 1/1");
    private final JButton rightButton = new JButton(">");

    private final JPanel botRow = new JPanel(); // Bottom row panel

    private int clickedDateIndex = -1;

    public ShiftHistoryWindow(Component parent) {
        this.parent = parent;
        parent.setEnabled(false); // Disable parent window
        chart.addMouseListener(this);

        // Set window properties
        setAlwaysOnTop(Settings.getAlwaysOnTop());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Shift Performance History");
        addWindowListener(this);
        addKeyListener(this);
        init(); // Initialize everything
        updatePageInfo(); // Get correct page numbers and disable left/right buttons as needed
        pack();
        setMinimumSize(new Dimension((int) (getWidth()*1.1), (int) (getWidth()/1.4)));

        Ops.setHandCursorOnCompsFrom(getContentPane()); // Add hand cursor to needed components
        setLocationRelativeTo(parent); // Center on parent window
        setVisible(true); // Show dialog

    }

    private String[] getAllowedAmounts() {

        ArrayList<String> amounts = new ArrayList<>();
        amounts.add("8"); // View of 8 will always be available
        if (chart.totalDates() > 8) {
            if (chart.totalDates() > 16) {
                amounts.add("16");
                if (chart.totalDates() > 32) {
                    amounts.add("32");
                }
            }
            amounts.add("All");
        }

        return amounts.toArray(new String[0]);

    }

    private void init() {
        JButton historyFolderButton = new JButton("Open history directory");
        // Right-click menu stuffs:
        JPopupMenu delMenu = new JPopupMenu();
        JMenuItem deleteDate = new JMenuItem("Delete");

        // Apply listeners to needed components
        ptsAmount.addItemListener((e) -> {
            if (ptsAmount.getSelectedIndex() == (ptsAmount.getItemCount() - 1)) { // "All" is always last item on list
                chart.showAll();
            } else if (ptsAmount.getSelectedItem() != null)
                chart.show(Integer.parseInt((String) ptsAmount.getSelectedItem()));

            chart.repaint();
            updatePageInfo();
        });

        leftButton.addActionListener((e) -> {
            chart.prevPage();
            updatePageInfo();
        });
        rightButton.addActionListener((e) -> {
            chart.nextPage();
            updatePageInfo();
        });
        historyFolderButton.addActionListener((e) -> SwingUtilities.invokeLater(() -> {
            try {
                openHistoryDirectory();
            } catch (Exception ex) { ex.printStackTrace(); }
        }));
        deleteDate.addActionListener(e -> {
            System.out.println(clickedDateIndex);
            if (clickedDateIndex >= 0) {
                if (new YesNoDialog(this, """
                        Are you sure you want to
                        delete your shift from
                        $d?"""
                        .replace("$d", chart.getDateAt(clickedDateIndex)
                                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)).toUpperCase(Locale.ROOT)))
                        .accepted()) {

                    chart.deleteDateAt(clickedDateIndex);
                    updatePageInfo();
                }
            } else new AlertDialog(null, "No existing date selected");
        });

        // Add to their respective places
        delMenu.add(deleteDate);
        chart.setComponentPopupMenu(delMenu);
        topRow.add(showingLabel);
        topRow.add(ptsAmount);
        topRow.add(datesShown);
        topRow.add(leftButton);
        topRow.add(pagesLabel);
        topRow.add(rightButton);
        botRow.add(historyFolderButton);
        add(topRow, BorderLayout.NORTH);
        add(chart, BorderLayout.CENTER);
        add(botRow, BorderLayout.SOUTH);

        // Color bottom row a bit brighter than the background:
        botRow.setBackground(Theme.contrastWithShade(Theme.getBgColor(), Settings.isContrastEnabled() ? 200 : 20));

    }

    public void updatePageInfo() {

        pagesLabel.setText("Page " + chart.page() + "/" + chart.totalPages());
        leftButton.setEnabled(chart.page() != 1); // Disable left button if on first page
        rightButton.setEnabled(chart.page() != chart.totalPages()); // Disable right button if on last page
        datesShown.setText(chart.pageDateRange());

    }

    /**
     * Open working directory in system's explorer
     */
    private void openHistoryDirectory() throws IOException {
        try {
            // Create instance of history file to select it in explorer
            File shiftHistoryFile = new File(Settings.getWorkingDir() + File.separator + "shift.history");
            Desktop.getDesktop().browseFileDirectory(shiftHistoryFile); // Only works on macOS and Win7/8 :(

        } catch (Exception e) {
            if (System.getProperty("os.name").contains("Windows")) {
                // Due to browseFileDirectory not working on Windows10+ we use specific commands:
                Runtime.getRuntime().exec("explorer \"$d\"".replace("$d", Settings.getWorkingDir()));
            } else new AlertDialog(null, """
                        Unable to open directory, this
                        desktop's file explorer is not
                        supported, wanted directory:
                        
                        """ + limitLineLength(Settings.getWorkingDir()));
        }
    }

    private String limitLineLength(String s) {
        // Get how many times 30 goes into the string length
        int limit = 30;
        int divisions = s.length() / limit;

        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < divisions) { // Append all *limit* length lines:
            sb.append(s, i * limit, (i * limit) + limit).append("\n");
            i++;
        } // Then append what's left of the last line:
        sb.append(s, i * limit, (i * limit) + s.length() % limit);

        return sb.toString();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose(); // Close self with Escape
    }

    @Override
    public void windowClosed(WindowEvent e) {
        parent.setEnabled(true); // Re-enable the summoner window
        parent.requestFocus();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        clickedDateIndex = chart.getDateFromBarAt(e.getX());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        clickedDateIndex = chart.getDateFromBarAt(e.getX());
    }

    // Unused
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowClosing(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

}
