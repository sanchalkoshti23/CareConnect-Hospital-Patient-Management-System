package com.swynex.careconnect.gui.components;

import com.swynex.careconnect.model.Patient;

import javax.swing.table.AbstractTableModel;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PatientTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "ID", "Patient name", "Age", "Gender",
            "Phone", "Email", "Blood group", "Registered"
    };
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private List<Patient> patients = List.of();

    @Override
    public int getRowCount() {
        return patients.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Long.class;
            case 2 -> Integer.class;
            case 7 -> OffsetDateTime.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Patient patient = patients.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> patient.patientId();
            case 1 -> patient.fullName();
            case 2 -> patient.age();
            case 3 -> patient.gender();
            case 4 -> patient.phone();
            case 5 -> patient.email();
            case 6 -> patient.bloodGroup();
            case 7 -> patient.createdAt();
            default -> throw new IndexOutOfBoundsException(
                    "Unknown patient table column: " + columnIndex);
        };
    }

    public Patient getPatientAt(int modelRow) {
        return patients.get(modelRow);
    }

    public void setPatients(List<Patient> patients) {
        this.patients = List.copyOf(patients);
        fireTableDataChanged();
    }

    public static String formatDate(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : DATE_FORMAT.format(dateTime);
    }
}
