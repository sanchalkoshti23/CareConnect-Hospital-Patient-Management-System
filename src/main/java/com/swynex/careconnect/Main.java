package com.swynex.careconnect;

import com.formdev.flatlaf.FlatLightLaf;
import com.swynex.careconnect.dao.PatientDao;
import com.swynex.careconnect.gui.CareConnectFrame;
import com.swynex.careconnect.service.PatientService;
import com.swynex.careconnect.util.DBConnection;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public final class Main {
    private static final System.Logger LOGGER =
            System.getLogger(Main.class.getName());

    private Main() {
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.focusColor", new Color(66, 169, 157));
        UIManager.put("Component.borderColor", new Color(218, 228, 235));
        UIManager.put("Button.arc", 12);
        UIManager.put("Button.default.background", new Color(17, 139, 132));
        UIManager.put("TextComponent.arc", 9);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 11);
        UIManager.put("Table.rowHeight", 38);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.selectionBackground", new Color(220, 241, 238));
        UIManager.put("Table.selectionForeground", new Color(20, 43, 68));
        UIManager.put("TableHeader.background", new Color(245, 248, 250));
        UIManager.put("TableHeader.foreground", new Color(65, 83, 98));
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));

        SwingUtilities.invokeLater(() -> {
            try {
                DBConnection databaseConnection = DBConnection.fromEnvironment();
                PatientService patientService =
                        new PatientService(new PatientDao(databaseConnection));
                CareConnectFrame frame = new CareConnectFrame(patientService);
                frame.setExtendedState(
                        frame.getExtendedState() | javax.swing.JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            } catch (RuntimeException exception) {
                LOGGER.log(System.Logger.Level.ERROR, "CareConnect could not start.", exception);
                JOptionPane.showMessageDialog(
                        null,
                        "CareConnect could not start. Check the application configuration "
                                + "and review the application log for details.",
                        "CareConnect startup error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
