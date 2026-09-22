package com.kineplan.dashboard.api;

public record DashboardResponse(long appointments, long absences, double absenceRate) { }