package com.marcelohdez.bedroom.main;

import com.marcelohdez.bedroom.enums.SetTime;
import com.marcelohdez.bedroom.settings.SettingsDialog;
import com.marcelohdez.bedroom.util.Ops;
import com.marcelohdez.bedroom.util.Theme;
import com.marcelohdez.bedroom.util.Time;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.swing.*;

public class UI extends JPanel implements ActionListener, KeyListener {

    private final Window parent;
    private static final DecimalFormat twoDecs = new DecimalFormat("#.00");
    private static StringBuilder str;

    // Time Variables
    private static int hr = 0;
    private static int min = 0;
    private static int sec = 0;
    private static long totalSecClockedIn = 0;
    private static long secondsTillClockIn = -1;
    private static long secondsTillLeaveBreak = -1;

    // Components used outside of constructor
    private static final JTextArea stats = new JTextArea("Please clock in.\n\n");
    private static final JButton breakButton = new JButton("Set Break");
    private static final JButton addOrder = new JButton("Add Order");

    // Stats
    private static long orders = 0;
    public static boolean inBreak = false;
    public static boolean freeze = true; // Ignore entering/leaving break and changing orders
    public static boolean clockInTimePassed = false;
    public static int target = 0; // Target orders/hr
    private static long ordersNeeded = 0;

    // Time values
    public static LocalTime clockInTime, clockOutTime, breakInTime, breakOutTime;
    public static boolean breakTimesChosen = false;

    // ======= Public reusable colors & fonts =======
    public static Font boldText = new Font(Font.SANS_SERIF, Font.BOLD, 14);

    // Get colors for UI from user's preferences
    public static Color textColor = loadColorOf("text", 240);
    // Get color of button text
    public static Color buttonTextColor = loadColorOf("buttonText", 240);
    // Get color of buttons
    public static Color buttonColor = loadColorOf("button", 80);
    // Get color of background
    public static Color bg = loadColorOf("bg", 64);

    public UI(Window parent) { // Set UI's properties

        this.parent = parent;

        setFocusable(true);
        addKeyListener(this);

        // Set components' properties
        stats.setEditable(false);
        stats.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        stats.addKeyListener(this);
        addOrder.addKeyListener(this);
        addOrder.addActionListener(this);
        breakButton.addKeyListener(this);
        breakButton.addActionListener(this);
        breakButton.setToolTipText("<html><b>Currently no break is set</b></html>"); // Default tooltip

        // Set colors
        colorComponents();

        // Add components
        add(breakButton);
        add(addOrder);
        add(stats);

        showStats();

    }

    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {
            case "Add Order" -> changeOrders(1);
            case "Set Break" -> enterBreak();
        }

    }

    public static void tick() { // Change time values

        while (sec > 59) {
            sec -= 60;
            min++;
            if (min > 59) {
                min -= 60;
                hr++;
            }
        }
        totalSecClockedIn++;
        sec++;

        showStats();

    }

    private static void showStats() {

        str = new StringBuilder();

        if (clockInTimePassed) { // Get stats =======

            if (!inBreak) { // Show time clocked in
                str.append("Time: ");
                Time.appendReadableTimeTo(str, hr, min, sec);
                str.append("\n") // Line break
                        .append(getStats());
            } else { // Show time left until our break ends =======
                str.append("On break, ");
                Time.appendReadableTimeTo(str, Time.shrinkTime(secondsTillLeaveBreak));
                str.append(" left\n");
            }

            stats.setText(str.toString());
            setTooltips();

        } else if (Main.timesChosen) { // Show "Time till clock in" =======
            str.append("Time until clocked in:\n");
            Time.appendReadableTimeTo(str, Time.shrinkTime(secondsTillClockIn));
            stats.setText(str.append("\n").toString());
        }

    }

    private static String getStats() {

        return """
                Orders: $orders ($perHr/hr)
                Needed: $needed, $left left"""
                .replace("$orders", String.valueOf(orders))
                .replace("$perHr",
                        String.valueOf(twoDecs.format((double) (orders*3600)/ totalSecClockedIn)))
                .replace("$needed", String.valueOf(ordersNeeded))
                .replace("$left", (orders < ordersNeeded) ?
                        String.valueOf(ordersNeeded - orders) : "0");

    }

    private void enterBreak() {
        if (!freeze) {
            Main.enterBreakWnd.centerOnMainWindow(); // Set to current center of main window
            Main.enterBreakWnd.setUITime(SetTime.CURRENT);
            Main.enterBreakWnd.setVisible(true);
        }
    }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        System.out.println(e.getKeyCode());
        // ======= Shortcuts =======
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN ->
                    changeOrders(-1); // Remove orders with BckSpc & Down Arrow
            case KeyEvent.VK_0 -> enterBreak();             // Set break times with 0
            case KeyEvent.VK_UP -> changeOrders(1); // Add orders with up arrow
            case KeyEvent.VK_DELETE, KeyEvent.VK_ESCAPE, KeyEvent.VK_BACK_SPACE ->
                    new SettingsDialog(new int[]{parent.getX(), parent.getY(),
                            parent.getWidth(), parent.getHeight()}); // Open settings with Del or Esc keys
        }
    }

    private void changeOrders(int amount) { // Change orders

        if (!freeze && !inBreak) {
            orders += amount;
            if (orders < 0) orders = 0;
            showStats();
        }

        Main.wnd.pack(); // Call the Window to pack itself

    }

    public static void getTime() {

        if (Main.timesChosen) // Have we chosen clock in and out times?
            // Has our clock in time passed?
            if (clockInTime.compareTo(LocalTime.now()) <= 0) {

                freeze = false;
                clockInTimePassed = true;

                if (breakTimesChosen) { // Have we chosen break times?
                    getBreakTime();
                } else { // If not, set totalSecClocked to time from clock in to now
                    totalSecClockedIn = clockInTime.until(LocalTime.now(), ChronoUnit.SECONDS);
                }

                getOrdersNeeded();
                sec = (int) (totalSecClockedIn % 60);
                min = (int) (totalSecClockedIn / 60) % 60;
                hr = (int) Math.floor(totalSecClockedIn/60F/60F);
                tick(); // Update time and show on screen

            } else {
                // Get seconds left until we have to clock in
                secondsTillClockIn = LocalTime.now().until(clockInTime, ChronoUnit.SECONDS) + 1;
                showStats(); // Display it on screen
            }

        Main.wnd.pack();

    }

    private static void getBreakTime() {

        if (breakInTime.compareTo(LocalTime.now()) <= 0) { // Has our break started?
            if (breakOutTime.compareTo(LocalTime.now()) <= 0) { // Has our break ended?
                inBreak = false; // If so, we are not in break.
                // Set totalSecClocked to the seconds from clocking in to the break's start,
                // then from break end to the current time.
                totalSecClockedIn = (clockInTime.until(breakInTime, ChronoUnit.SECONDS) +
                        breakOutTime.until(LocalTime.now(), ChronoUnit.SECONDS) - 1);
            } else { // If our break has not ended:
                inBreak = true; // We are still in break
                // Set totalSecClocked to the seconds from clocking in to the break's start
                totalSecClockedIn = clockInTime.until(breakInTime, ChronoUnit.SECONDS) - 1;
                secondsTillLeaveBreak = // Seconds until our break ends
                        LocalTime.now().until(breakOutTime, ChronoUnit.SECONDS);
            }
        }

    }

    private static void getOrdersNeeded() {

        if (!breakTimesChosen) { // Check if we have not chosen break times
            ordersNeeded = Math.round(target *
                    // If so, get ordersNeeded with clock in and out times
                    ((double) clockInTime.until(clockOutTime, ChronoUnit.MINUTES) / 60));
        } else ordersNeeded = Math.round(target *
                // If we did choose break times, then get ordersNeeded from clock in
                // and clock out times minus the difference of our break's start and end times
                (((double) clockInTime.until(clockOutTime, ChronoUnit.MINUTES) -
                        (double) breakInTime.until(breakOutTime, ChronoUnit.MINUTES)) / 60));

    }

    private static void setTooltips() {

        // Add Order's tool tips
        double neededForTarget = (double) totalSecClockedIn/3600 * target;
        if (neededForTarget > orders) { // Tell us how many orders we need to reach our target

            str = new StringBuilder();
            int amountMissing = (int) Math.round(Math.ceil(neededForTarget - orders));
            addOrder.setToolTipText(str.append("<html><b>You are ")
                    .append(amountMissing)
                    .append(" order")
                    .append(Ops.isPlural(amountMissing))
                    .append(" behind your hourly target</b></html>").toString());

        } else addOrder.setToolTipText("<html><b>You are on track with your hourly target</b></html>");

        // Set Break's tool tips
        if (breakTimesChosen) { // If we have chosen break times, change the tooltip to them.

            str = new StringBuilder();
            str.append("<html><b>Current: ");
            Time.append12HrTimeTo(str, breakInTime);
            str.append("-");
            Time.append12HrTimeTo(str, breakOutTime);
            str.append("</b></html>");

            breakButton.setToolTipText(str.toString());

        }

    }

    private static Color loadColorOf(String component, int def) {

        return new Color(Main.userPrefs.getInt(component + "Red", def),
                Main.userPrefs.getInt(component + "Green", def),
                Main.userPrefs.getInt(component + "Blue", def));

    }

    public void reloadColors() {

        textColor = loadColorOf("text", 240);
        buttonTextColor = loadColorOf("buttonText", 240);
        buttonColor = loadColorOf("button", 80);
        bg = loadColorOf("bg", 64);

        this.colorComponents();

    }

    private void colorComponents() {

        Theme.colorThis(breakButton);
        Theme.colorThis(addOrder);
        Theme.colorThis(stats);
        setBackground(bg);

    }

    void sizeButtons() {

        // Get the largest width * 1.2 for some buffer
        int length = (int) ((Math.max(addOrder.getWidth(), breakButton.getWidth())) * 1.2);
        // Set the buttons to that width, and half that for height to make identical rectangles
        addOrder.setPreferredSize(new Dimension(length, length/2));
        breakButton.setPreferredSize(new Dimension(length, length/2));

    }

}
