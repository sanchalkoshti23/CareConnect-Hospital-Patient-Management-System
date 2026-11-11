package com.swynex.careconnect.gui.components;

import com.swynex.careconnect.model.Patient;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class PatientFormPanel extends JPanel {
    private static final String[] GENDERS = {"Select gender", "Female", "Male", "Other"};
    private static final String[] BLOOD_GROUPS = {
            "Select blood group", "A+", "A-", "B+", "B-",
            "AB+", "AB-", "O+", "O-"
    };

    private final Patient originalPatient;
    private final JTextField fullNameField = new JTextField(28);
    private final JTextField ageField = new JTextField(10);
    private final JComboBox<String> genderField = new JComboBox<>(GENDERS);
    private final JTextField phoneField = new JTextField(18);
    private final JTextField emailField = new JTextField(28);
    private final JComboBox<String> bloodGroupField = new JComboBox<>(BLOOD_GROUPS);
    private final JTextArea addressField = new JTextArea(4, 28);

    public PatientFormPanel(Patient patient) {
        super(new BorderLayout());
        this.originalPatient = patient;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 8, 6, 8);
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        styleInput(fullNameField);
        styleInput(ageField);
        styleInput(genderField);
        styleInput(phoneField);
        styleInput(emailField);
        styleInput(bloodGroupField);

        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        addressField.setFont(fullNameField.getFont());
        addressField.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        addressField.setMinimumSize(new Dimension(180, 90));
        JScrollPane addressScroll = new JScrollPane(addressField);
        addressScroll.setPreferredSize(new Dimension(520, 96));
        addressScroll.setBorder(BorderFactory.createLineBorder(
                new Color(218, 228, 235)));

        addSection(fields, constraints, 0, "PERSONAL INFORMATION");
        addField(fields, constraints, 0, 1, "Full name", fullNameField);
        addField(fields, constraints, 1, 1, "Age", ageField);
        addField(fields, constraints, 0, 3, "Gender", genderField);
        addField(fields, constraints, 1, 3, "Blood group", bloodGroupField);
        addSection(fields, constraints, 5, "CONTACT INFORMATION");
        addField(fields, constraints, 0, 6, "Phone number", phoneField);
        addField(fields, constraints, 1, 6, "Email address", emailField);
        constraints.gridx = 0;
        constraints.gridy = 8;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        fields.add(fieldLabel("Address"), constraints);
        constraints.gridy = 9;
        fields.add(addressScroll, constraints);

        JLabel requiredNote = new JLabel(
                "Required: name, age (1–120), gender, 10-digit phone, email and blood group. "
                        + "Registration date is assigned automatically.");
        requiredNote.putClientProperty("FlatLaf.style", "font: -1");
        requiredNote.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(fields, BorderLayout.CENTER);
        content.add(requiredNote, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        if (patient != null) {
            populate(patient);
        }
    }

    public Patient toPatient() {
        String ageText = ageField.getText().trim();
        Integer age = null;
        if (!ageText.isEmpty()) {
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Age must be a whole number.");
            }
        }

        return new Patient(
                originalPatient == null ? null : originalPatient.patientId(),
                fullNameField.getText(),
                age,
                selectedValue(genderField, GENDERS[0]),
                phoneField.getText(),
                emailField.getText(),
                addressField.getText(),
                selectedValue(bloodGroupField, BLOOD_GROUPS[0]),
                originalPatient == null ? null : originalPatient.createdAt());
    }

    public void clear() {
        fullNameField.setText("");
        ageField.setText("");
        genderField.setSelectedIndex(0);
        phoneField.setText("");
        emailField.setText("");
        bloodGroupField.setSelectedIndex(0);
        addressField.setText("");
        fullNameField.requestFocusInWindow();
    }

    public void reset() {
        if (originalPatient == null) {
            clear();
        } else {
            populate(originalPatient);
        }
    }

    private void populate(Patient patient) {
        fullNameField.setText(patient.fullName());
        ageField.setText(patient.age() == null ? "" : patient.age().toString());
        genderField.setSelectedItem(patient.gender());
        phoneField.setText(patient.phone());
        emailField.setText(patient.email());
        bloodGroupField.setSelectedItem(patient.bloodGroup());
        addressField.setText(patient.address());
    }

    private static String selectedValue(JComboBox<String> comboBox, String placeholder) {
        Object selected = comboBox.getSelectedItem();
        return selected == null || placeholder.equals(selected) ? null : selected.toString();
    }

    private static void addSection(
            JPanel panel, GridBagConstraints constraints, int row, String title) {
        JLabel section = new JLabel(title);
        section.setForeground(new Color(16, 132, 128));
        section.setFont(section.getFont().deriveFont(Font.BOLD, 11f));
        section.setBorder(BorderFactory.createEmptyBorder(9, 2, 3, 0));
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(9, 8, 3, 8);
        panel.add(section, constraints);
        constraints.insets = new Insets(6, 8, 6, 8);
    }

    private static void styleInput(JComponent field) {
        field.setPreferredSize(new Dimension(
                field.getPreferredSize().width, 38));
        field.putClientProperty("JComponent.minimumWidth", 120);
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(50, 69, 84));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }

    private static void addField(
            JPanel panel,
            GridBagConstraints constraints,
            int column,
            int rowStart,
            String label,
            java.awt.Component field) {
        constraints.gridwidth = 1;
        constraints.weightx = 0.5;
        constraints.gridx = column;
        constraints.gridy = rowStart;
        panel.add(fieldLabel(label), constraints);
        constraints.gridy = rowStart + 1;
        constraints.weightx = 0.5;
        panel.add(field, constraints);
    }
}
