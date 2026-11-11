package com.swynex.careconnect.gui.components;

import com.swynex.careconnect.model.Patient;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
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
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addField(fields, constraints, 0, 0, "Full name", fullNameField);
        addField(fields, constraints, 1, 0, "Age", ageField);
        addField(fields, constraints, 0, 1, "Gender", genderField);
        addField(fields, constraints, 1, 1, "Phone number", phoneField);
        addField(fields, constraints, 0, 2, "Email address", emailField);
        addField(fields, constraints, 1, 2, "Blood group", bloodGroupField);

        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressField);
        addressScroll.setBorder(BorderFactory.createLineBorder(
                javax.swing.UIManager.getColor("Component.borderColor")));
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.weighty = 0;
        fields.add(new JLabel("Address"), constraints);
        constraints.gridy = 7;
        fields.add(addressScroll, constraints);

        JLabel requiredNote = new JLabel(
                "Required: name, age (1–120), gender, 10-digit phone, email and blood group.");
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

    private static void addField(
            JPanel panel,
            GridBagConstraints constraints,
            int column,
            int row,
            String label,
            java.awt.Component field) {
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.gridx = column;
        constraints.gridy = row * 2;
        panel.add(new JLabel(label), constraints);
        constraints.gridy = row * 2 + 1;
        constraints.weightx = 1;
        panel.add(field, constraints);
    }
}
