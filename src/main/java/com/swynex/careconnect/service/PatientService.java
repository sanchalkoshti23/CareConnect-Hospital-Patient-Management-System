package com.swynex.careconnect.service;

import com.swynex.careconnect.dao.PatientDao;
import com.swynex.careconnect.model.DashboardStats;
import com.swynex.careconnect.model.Patient;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class PatientService {
    private static final Set<String> BLOOD_GROUPS =
            Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private static final Set<String> GENDERS = Set.of("Female", "Male", "Other");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{10}");

    private final PatientDao patientDao;

    public PatientService(PatientDao patientDao) {
        this.patientDao = patientDao;
    }

    public Patient createPatient(Patient patient) throws SQLException {
        Patient normalized = validateAndNormalize(patient);
        if (normalized.patientId() != null) {
            throw new IllegalArgumentException("A new patient must not have an ID.");
        }
        return patientDao.create(normalized);
    }

    public List<Patient> getAllPatients() throws SQLException {
        return patientDao.findAll();
    }

    public List<Patient> searchPatients(String query) throws SQLException {
        return patientDao.search(query);
    }

    public DashboardStats getDashboardStats() throws SQLException {
        return patientDao.getDashboardStats();
    }

    public Optional<Patient> getPatientById(long patientId) throws SQLException {
        requireValidId(patientId);
        return patientDao.findById(patientId);
    }

    public boolean updatePatient(Patient patient) throws SQLException {
        if (patient == null || patient.patientId() == null) {
            throw new IllegalArgumentException("An existing patient ID is required.");
        }
        requireValidId(patient.patientId());
        return patientDao.update(validateAndNormalize(patient));
    }

    public boolean deletePatient(long patientId) throws SQLException {
        requireValidId(patientId);
        return patientDao.deleteById(patientId);
    }

    private static Patient validateAndNormalize(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient data is required.");
        }

        String fullName = requiredText(patient.fullName(), "Full name", 150);
        if (patient.age() == null || patient.age() < 1 || patient.age() > 120) {
            throw new IllegalArgumentException("Age must be between 1 and 120.");
        }
        String gender = requiredText(patient.gender(), "Gender", 30);
        if (!GENDERS.contains(gender)) {
            throw new IllegalArgumentException("Select a valid gender.");
        }
        String phone = optionalText(patient.phone(), "Phone", 20);
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Phone number must contain exactly 10 digits.");
        }
        String email = optionalText(patient.email(), "Email", 254);
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email address is not valid.");
        }
        String address = optionalText(patient.address(), "Address", Integer.MAX_VALUE);
        String bloodGroup = optionalText(patient.bloodGroup(), "Blood group", 3);
        if (bloodGroup == null
                || !BLOOD_GROUPS.contains(bloodGroup.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Blood group must be a recognized ABO/Rh group.");
        }

        return new Patient(
                patient.patientId(),
                fullName,
                patient.age(),
                gender,
                phone,
                email,
                address,
                bloodGroup.toUpperCase(Locale.ROOT),
                patient.createdAt());
    }

    private static String requiredText(String value, String field, int maxLength) {
        String normalized = optionalText(value, field, maxLength);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return normalized;
    }

    private static String optionalText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " must be no more than " + maxLength + " characters.");
        }
        return normalized;
    }

    private static void requireValidId(long patientId) {
        if (patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be positive.");
        }
    }
}
