package com.swynex.careconnect.service;

import com.swynex.careconnect.dao.PatientDao;
import com.swynex.careconnect.model.Patient;
import com.swynex.careconnect.util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatientServiceTest {
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(
                new PatientDao(DBConnection.fromEnvironment()));
    }

    @Test
    void rejectsBlankPatientName() {
        IllegalArgumentException exception = assertInvalid(
                new Patient(null, "  ", 42, "Female", "1234567890",
                        "person@example.com", null, "O+", null));

        assertEquals("Full name is required.", exception.getMessage());
    }

    @Test
    void rejectsAgeOutsideOneThroughOneHundredTwenty() {
        assertInvalid(new Patient(null, "Patient Name", 0, "Female", "1234567890",
                "person@example.com", null, "O+", null));
        assertInvalid(new Patient(null, "Patient Name", 121, "Female", "1234567890",
                "person@example.com", null, "O+", null));
    }

    @Test
    void rejectsPhoneNumbersThatDoNotContainTenDigits() {
        assertInvalid(new Patient(null, "Patient Name", 42, "Female", "123456789",
                "person@example.com", null, "O+", null));
        assertInvalid(new Patient(null, "Patient Name", 42, "Female", "12345abc90",
                "person@example.com", null, "O+", null));
    }

    @Test
    void rejectsInvalidEmailAddress() {
        IllegalArgumentException exception = assertInvalid(
                new Patient(null, "Patient Name", 42, "Female", "1234567890",
                        "not-an-email", null, "O+", null));

        assertEquals("Email address is not valid.", exception.getMessage());
    }

    @Test
    void requiresASelectedGenderAndBloodGroup() {
        assertInvalid(new Patient(null, "Patient Name", 42, null, "1234567890",
                "person@example.com", null, "O+", null));
        assertInvalid(new Patient(null, "Patient Name", 42, "Female", "1234567890",
                "person@example.com", null, null, null));
        assertInvalid(new Patient(null, "Patient Name", 42, "Select gender", "1234567890",
                "person@example.com", null, "ABO", null));
    }

    private IllegalArgumentException assertInvalid(Patient patient) {
        return assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(patient));
    }
}
