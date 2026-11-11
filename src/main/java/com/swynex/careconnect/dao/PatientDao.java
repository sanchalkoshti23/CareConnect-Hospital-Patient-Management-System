package com.swynex.careconnect.dao;

import com.swynex.careconnect.model.Patient;
import com.swynex.careconnect.model.DashboardStats;
import com.swynex.careconnect.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PatientDao {
    private static final String SELECT_COLUMNS = """
            patient_id, full_name, age, gender, phone, email, address,
            blood_group, created_at
            """;

    private final DBConnection databaseConnection;

    public PatientDao(DBConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public Patient create(Patient patient) throws SQLException {
        String sql = """
                INSERT INTO patients
                    (full_name, age, gender, phone, email, address, blood_group)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING patient_id, full_name, age, gender, phone, email, address,
                          blood_group, created_at
                """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setPatientFields(statement, patient, 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Creating a patient returned no database row.");
                }
                return mapPatient(resultSet);
            }
        }
    }

    public List<Patient> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM patients ORDER BY patient_id";
        List<Patient> patients = new ArrayList<>();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }
        }
        return patients;
    }

    public Optional<Patient> findById(long patientId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM patients WHERE patient_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapPatient(resultSet))
                        : Optional.empty();
            }
        }
    }

    public List<Patient> search(String query) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS
                + " FROM patients"
                + " WHERE (? = '' OR full_name ILIKE ? OR patient_id::text = ?)"
                + " ORDER BY patient_id";
        String searchTerm = query == null ? "" : query.trim();
        List<Patient> patients = new ArrayList<>();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchTerm);
            statement.setString(2, "%" + searchTerm + "%");
            statement.setString(3, searchTerm);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    patients.add(mapPatient(resultSet));
                }
            }
        }
        return patients;
    }

    public DashboardStats getDashboardStats() throws SQLException {
        String summarySql = """
                SELECT COUNT(*) AS total_patients,
                       COUNT(*) FILTER (WHERE LOWER(gender) = 'male') AS male_patients,
                       COUNT(*) FILTER (WHERE LOWER(gender) = 'female') AS female_patients,
                       COUNT(*) FILTER (
                           WHERE created_at >= CURRENT_DATE
                             AND created_at < CURRENT_DATE + INTERVAL '1 day'
                       ) AS registered_today
                FROM patients
                """;
        String recentSql = "SELECT " + SELECT_COLUMNS
                + " FROM patients ORDER BY created_at DESC, patient_id DESC LIMIT ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement summaryStatement =
                     connection.prepareStatement(summarySql);
             ResultSet summaryResult = summaryStatement.executeQuery()) {
            if (!summaryResult.next()) {
                throw new SQLException("Dashboard statistics query returned no result.");
            }

            List<Patient> recentPatients = new ArrayList<>();
            try (PreparedStatement recentStatement =
                         connection.prepareStatement(recentSql)) {
                recentStatement.setInt(1, 6);
                try (ResultSet recentResult = recentStatement.executeQuery()) {
                    while (recentResult.next()) {
                        recentPatients.add(mapPatient(recentResult));
                    }
                }
            }
            return new DashboardStats(
                    summaryResult.getLong("total_patients"),
                    summaryResult.getLong("male_patients"),
                    summaryResult.getLong("female_patients"),
                    summaryResult.getLong("registered_today"),
                    recentPatients);
        }
    }

    public boolean update(Patient patient) throws SQLException {
        String sql = """
                UPDATE patients
                SET full_name = ?, age = ?, gender = ?, phone = ?, email = ?,
                    address = ?, blood_group = ?
                WHERE patient_id = ?
                """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setPatientFields(statement, patient, 1);
            statement.setLong(8, patient.patientId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(long patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patientId);
            return statement.executeUpdate() == 1;
        }
    }

    private static void setPatientFields(
            PreparedStatement statement, Patient patient, int startIndex)
            throws SQLException {
        statement.setString(startIndex, patient.fullName());
        statement.setInt(startIndex + 1, patient.age());
        statement.setString(startIndex + 2, patient.gender());
        setNullableString(statement, startIndex + 3, patient.phone());
        setNullableString(statement, startIndex + 4, patient.email());
        setNullableString(statement, startIndex + 5, patient.address());
        setNullableString(statement, startIndex + 6, patient.bloodGroup());
    }

    private static void setNullableString(
            PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private static Patient mapPatient(ResultSet resultSet) throws SQLException {
        return new Patient(
                resultSet.getLong("patient_id"),
                resultSet.getString("full_name"),
                resultSet.getInt("age"),
                resultSet.getString("gender"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getString("address"),
                resultSet.getString("blood_group"),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class));
    }
}
