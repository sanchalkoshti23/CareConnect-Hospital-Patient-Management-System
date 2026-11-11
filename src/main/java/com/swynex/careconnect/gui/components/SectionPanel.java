package com.swynex.careconnect.gui.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Font;

public class SectionPanel extends JPanel {
    public SectionPanel(String title) {
        super(new BorderLayout(0, 6));
        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 233, 239)),
                BorderFactory.createEmptyBorder(15, 17, 15, 17)));

        JLabel heading = new JLabel(title);
        heading.setForeground(new Color(20, 43, 68));
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
        add(heading, BorderLayout.NORTH);
    }
}
