package com.swynex.careconnect.gui.components;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;

public class SectionPanel extends JPanel {
    public SectionPanel(String title) {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                title,
                TitledBorder.LEADING,
                TitledBorder.TOP));
    }
}
