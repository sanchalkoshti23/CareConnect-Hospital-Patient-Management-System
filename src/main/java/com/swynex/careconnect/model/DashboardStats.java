package com.swynex.careconnect.model;

import java.util.List;

public record DashboardStats(
        long totalPatients,
        long malePatients,
        long femalePatients,
        long registeredToday,
        List<Patient> recentPatients) {
    public DashboardStats {
        recentPatients = List.copyOf(recentPatients);
    }
}
