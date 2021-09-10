package com.swiftsatchel.bedroom.settings;

import com.swiftsatchel.bedroom.alert.AlertDialog;
import com.swiftsatchel.bedroom.enums.ErrorType;
import com.swiftsatchel.bedroom.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;

public class WorkAppsManager extends JDialog implements ActionListener, WindowListener {

    private final WindowParent parent;

    private ArrayList<String> workAppDirs; // Keep track of work apps' directories
    private DefaultListModel<String> workAppNames; // Work apps' names
    private JList<String> list; // The JList to be displayed

    public WorkAppsManager(SettingsDialog parent) {

        this.parent = parent;

        setTitle("Work Apps");
        addWindowListener(this);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setAlwaysOnTop(Settings.getAlwaysOnTop());
        setResizable(false);

        JPanel content = new JPanel(); // Content panel to set a background color
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.getBgColor());

        content.add(createList());
        content.add(createToolsPanel());
        Ops.setHandCursorOnCompsFrom(content); // Set hand cursor on needed components

        add(content);
        pack();

        // Center on parent window
        setLocation(parent.getX() + ((parent.getWidth()/2) - (getWidth()/2)),
                parent.getY() + ((parent.getHeight()/2) - (getHeight()/2)));

        setVisible(true);

    }

    private JPanel createToolsPanel() {

        // Create components
        JPanel panel = new JPanel();
        JButton add = new JButton("Add");
        JButton remove = new JButton("Remove");

        // Customize em
        Theme.colorThese(new JComponent[]{panel, add, remove});
        add.addActionListener(this);
        remove.addActionListener(this);

        // Add tools to panel
        panel.add(add);
        panel.add(remove);

        return panel;

    }

    private JPanel createList() {

        // Create stuffs
        JPanel panel = new JPanel();

        // Add work apps
        workAppDirs = new ArrayList<>();
        workAppNames = new DefaultListModel<>();
        for (String app : Settings.getWorkAppsList()) {
            if (!app.equals("")) {
                workAppDirs.add(app);
                workAppNames.addElement(new File(app).getName());
            }
        }


        // Create list
        list = new JList<>(workAppNames);
        list.setVisibleRowCount(7);

        // Customize
        Theme.colorThese(new JComponent[]{list, panel});
        JScrollPane sp = new JScrollPane(list);

        // Add to panel
        panel.add(sp);
        pack();
        sp.setPreferredSize(new Dimension((int)(getWidth()*1.5), getWidth()));
        pack();

        return panel;

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {

            case "Add" -> addAnApp();
            case "Remove" -> removeAnApp();

        }

    }

    private void addAnApp() {

        if (workAppNames.getSize() < 7) { // Add a work app if under limit

            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(
                    // Set a filter of apps, text files (for scripts), and python scripts (for tech-y ppl)
                    "Programs/Scripts", "exe", "app", "lnk", "txt", "docx", "odt", "rtf", "py"));
            fc.setApproveButtonText("Add");
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                workAppNames.addElement(fc.getSelectedFile().getName()); // Add name of app
                workAppDirs.add(fc.getSelectedFile().toString()); // Add its directory
            }


        } else {
            new AlertDialog(parent, ErrorType.WORK_APPS_FULL);
        }

    }

    private void removeAnApp() {

        // Check if we have something selected
        if (!list.isSelectionEmpty()) {

            int selected = list.getSelectedIndex(); // Get selected index
            removeAppFromBoth(selected);
            list.setSelectedIndex(selected);    // Keep cursor on same position
            if (list.isSelectionEmpty())
                // If where we put the cursor is empty, move it up.
                list.setSelectedIndex(selected - 1);

        }

    }

    private void removeAppFromBoth(int index) {
        workAppNames.remove(index);
        workAppDirs.remove(index);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        Settings.saveWorkApps(workAppDirs.toString());
    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
