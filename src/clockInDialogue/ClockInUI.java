import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClockInUI extends JPanel implements ActionListener {

    private JButton choose = new JButton("Choose");
    private String[] amPMOptions = {"AM","PM"}, hr = {"1:", "2:", "3:", "4:", "5:", "6:",
                                                    "7:", "8:", "9:", "10:", "11:", "12:"},
                                                min = {"00", "01", "02", "03", "04", "05",
                                                    "06","07", "08", "09", "10", "11", 
                                                    "12", "13", "14", "15", "16", "17", 
                                                    "18", "19", "20", "21", "22", "23", 
                                                    "24", "25", "26", "27", "28", "29", 
                                                    "30", "31", "32", "33", "34", "35", 
                                                    "36", "37", "38", "39", "40", "41", 
                                                    "42", "43", "44", "45", "46", "47", 
                                                    "48", "49", "50", "51", "52", "53", 
                                                    "54", "55", "56", "57", "58", "59"};
                                                    
    private JComboBox amPM = new JComboBox<>(amPMOptions), hrBox = new JComboBox<>(hr),
            minBox = new JComboBox<>(min);

    public ClockInUI() {

        setFocusable(true);
        setLayout(new BorderLayout());

        choose.addActionListener(this);
        //amPM.addActionListener(this);

        add(choose, BorderLayout.SOUTH);
        add(hrBox, BorderLayout.WEST);
        add(minBox, BorderLayout.CENTER);
        add(amPM, BorderLayout.EAST);

    }

    public void actionPerformed(ActionEvent e) {



    }

}