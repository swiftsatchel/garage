package com.swiftsatchel.bedroom.dialog;

import com.swiftsatchel.bedroom.enums.ErrorType;
import com.swiftsatchel.bedroom.enums.SetTime;
import com.swiftsatchel.bedroom.enums.TimeWindowType;
import com.swiftsatchel.bedroom.Main;
import com.swiftsatchel.bedroom.util.Ops;
import com.swiftsatchel.bedroom.util.Theme;
import com.swiftsatchel.bedroom.util.Time;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SelectTimeUI extends JPanel implements ActionListener {

    private final TimeWindowType type;
    private final SelectTimeDialog parent;
    private final GridBagLayout layout;
    private final GridBagConstraints gbc;

    // ======= List boxes: =======
    private final JComboBox<String> amPMBox = new JComboBox<>(new String[]{"AM", "PM"});
    private final JComboBox<String> hrBox = new JComboBox<>(Ops.createNumberList(true, 1, 12, ":"));
    // Create minutes (0-59) and hourly targets (1-24)
    private final JComboBox<String> minBox = new JComboBox<>(Ops.createNumberList(true, 0, 59));
    private final JComboBox<String> targetListBox = new JComboBox<>(Ops.createNumberList(true, 1, 24));

    // Other components:
    private final JButton select = new JButton("Select");   // Select button
    private final JLabel topText = new JLabel("CLOCK IN time:"); // Top text label
    private final JLabel targetLabel = new JLabel("Your hourly target:"); // Select target text

    public SelectTimeUI(SelectTimeDialog parent) {

        type = parent.type;
        this.parent = parent;
        layout = new GridBagLayout();
        gbc = new GridBagConstraints();

        setBackground(Theme.getBgColor()); // Set background color
        setLayout(layout);
        setListBoxIndexes(SetTime.CURRENT); // Set to current time
        addKeyListener(parent);

        switch (type) { // Set window type-specific things
            case CLOCK_OUT -> { // For clock out time window, add its specific components as well
                topText.setText("CLOCK OUT time:");
                addComponent(targetLabel, 0, 2, 2, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0));
                addComponent(targetListBox, 2, 2, 1, GridBagConstraints.BOTH, new Insets(2, 2, 2, 4));
            }
            case START_BREAK -> topText.setText("Break start:");
            case END_BREAK -> topText.setText("Break end:");
            case EARLY_CLOCK_OUT -> {
                topText.setText("Clocking out early?");
                select.setText("Clock Out");
            }
        }

        // Add components
        addComponent(topText, 0, 0, 3, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 2, 4));
        addComponent(hrBox, 0, 1, 1, GridBagConstraints.BOTH, new Insets(4, 4, 4, 2));
        addComponent(minBox, 1, 1, 1, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4));
        addComponent(amPMBox, 2, 1, 1, GridBagConstraints.BOTH, new Insets(4, 2, 4, 4));
        addComponent(select, 0, 4, 3, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4));

        // Customize components
        topText.setFont(Theme.getBoldText());
        topText.setHorizontalAlignment(JLabel.CENTER);
        select.addActionListener(this);
        select.addKeyListener(parent);
        hrBox.addKeyListener(parent);
        minBox.addKeyListener(parent);
        amPMBox.addKeyListener(parent);
        targetLabel.setHorizontalAlignment(JLabel.CENTER);
        targetListBox.setSelectedIndex(8); // Set default to 9 (what I need @ my job, so a lil Easter egg)
        targetListBox.addKeyListener(parent);

        colorSelf(); // Color components
        Ops.setHandCursorOnCompsFrom(this); // Set hand cursor on needed components

    }

    private void addComponent(JComponent comp, int x, int y, int width, int fill, Insets insets) {

        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = insets;
        gbc.fill = fill;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = 1;
        layout.setConstraints(comp, gbc);
        add(comp, gbc);

    }

    public void setListBoxIndexes(SetTime type) { // Set list boxes to a time:

        int hour = LocalTime.now().getHour();

        switch (type) { // Set minBox depending on type and get wanted hour int

            case CURRENT -> minBox.setSelectedIndex(LocalTime.now().getMinute()); // Set to current time

            case CLOCK_IN_PLUS_DEFAULT -> { // Set to chosen default hour value after clock in time
                hour = Main.clockInTime.getHour() + Main.userPrefs.getInt("defaultShiftLength", 4);
                if (hour >= 24) hour -= 24;             // If it's over 24 now, loop it
                minBox.setSelectedIndex(Main.clockInTime.getMinute()); // Set minBox to clock in time's minute
            }

            case BREAK_START_PLUS_30M -> { // Set leave break window's default minutes to 30 above break in time.
                int minute = Main.breakInTime.getMinute() + 30; // +30 minutes after break start
                hour = Main.breakInTime.getHour();      // Get break start time's hour
                if (minute > 59) {                      // If it is over 59, loop it and add an hour
                    minute -= 60;
                    hour = Main.breakInTime.getHour() + 1;
                }
                minBox.setSelectedIndex(minute);        // Set minBox's index to the minute value now
            }

        }

        setListBoxesByHour(hour); // Do maths for when hour value is over 12, ex 16:00 -> 4PM

    }

    void selectTime() {

        // Parse the current date and time in format: "2021-8-16T17:20" for 5:20PM on Aug 18, 2021
        LocalDateTime newTime = LocalDateTime.parse(LocalDate.now() + "T" +
                Time.makeTime24Hour(hrBox.getSelectedIndex() + 1,
                minBox.getSelectedIndex(), (amPMBox.getSelectedIndex() == 1)));

        switch (type) {
            case CLOCK_IN -> {
                Main.clockInTime = newTime; // Set clock in time
                proceedWith(TimeWindowType.CLOCK_OUT);
            }
            case CLOCK_OUT -> setClockOutTime(newTime);
            case START_BREAK -> setBreakStartTime(newTime);
            case END_BREAK -> setBreakEndTime(newTime);
            case EARLY_CLOCK_OUT -> clockOutEarly(newTime);
        }

        if (Main.timesChosen) Main.update();

    }

    private void setClockOutTime(LocalDateTime time) {

        // Since the default date is the user's current date, if the clock out time is before
        // the clock in time, assume it is an overnight shift and set the clock out time's date
        // to the current date + 1 day.
        Main.clockOutTime = time.isAfter(Main.clockInTime) ? time : time.plusDays(1);

        Main.target = targetListBox.getSelectedIndex() + 1; // Set target
        Main.timesChosen = true;                // Clock out time is now chosen
        parent.dispose();                       // Close clock out time window

    }

    private void setBreakStartTime(LocalDateTime time) {

        if ((time.isAfter(Main.clockInTime)) && time.isBefore(Main.clockOutTime)) {

            Main.breakInTime = time; // Set enter break time
            proceedWith(TimeWindowType.END_BREAK); // Open end break window

        } else {
            new AlertDialog(parent, ErrorType.BREAK_OUT_OF_SHIFT);
        }

    }

    private void setBreakEndTime(LocalDateTime time) {

        if (time.isAfter(Main.breakInTime) && time.isBefore(Main.clockOutTime)) {

            Main.breakOutTime = time; // Set leave break time
            Main.breakTimesChosen = true;
            parent.dispose();       // Close leave break window

        } else {
            new AlertDialog(parent, ErrorType.NEGATIVE_BREAK_TIME);
        }

    }

    private void clockOutEarly(LocalDateTime time) {

        if (time.isAfter(Main.clockInTime)) {

            if (time.isBefore(Main.clockOutTime)) {

                Main.clockOut(time); // Save this shift's performance and close application

            } else new AlertDialog(parent, ErrorType.EARLY_CLOCK_OUT_NOT_EARLY);

        } else new AlertDialog(parent, ErrorType.NON_POSITIVE_SHIFT_TIME);

    }

    private void proceedWith(TimeWindowType newType) {

        parent.setVisible(false);
        new SelectTimeDialog(parent, newType);

    }

    private void setListBoxesByHour(int hour) { // Convert time to 12-hour format for list boxes

        if (hour >= 12) {
            amPMBox.setSelectedIndex(1);            // Set AM/PM list box to PM
            // This if statement sets the list box index to current hour,
            // since LocalTime is in 24hr format, we have to do some maths
            // to get it to 12hr am/pm.
            if (hour != 12) {                       // Set hour to 1-11pm
                hrBox.setSelectedIndex(hour - 13);
            } else hrBox.setSelectedIndex(11); // Set hour to 12pm
        } else {
            amPMBox.setSelectedIndex(0);            // Set AM/PM list box to AM
            if (hour != 0) {                        // Set hour to 1-11am
                hrBox.setSelectedIndex(hour - 1);
            } else hrBox.setSelectedIndex(11); // Set hour to 12am (or 0 in 24hr)
        }

    }

    public void colorSelf() {

        Theme.colorThese(new JComponent[]{this, topText, select, hrBox,
                minBox, amPMBox});

        if (type.equals(TimeWindowType.CLOCK_OUT)) {
            Theme.colorThese(new JComponent[]{targetLabel, targetListBox});
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selectTime();
    }

}