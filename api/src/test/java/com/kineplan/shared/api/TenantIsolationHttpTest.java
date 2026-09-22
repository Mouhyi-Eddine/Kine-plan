package com.kineplan.shared.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kineplan.appointment.api.AppointmentController;
import com.kineplan.appointment.application.AppointmentNotFoundException;
import com.kineplan.appointment.application.AppointmentService;
import com.kineplan.clinical.api.ClinicalController;
import com.kineplan.clinical.application.ClinicalService;
import com.kineplan.patient.api.PatientController;
import com.kineplan.patient.application.PatientNotFoundException;
import com.kineplan.patient.application.PatientService;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TenantIsolationHttpTest {
    private final UUID cabinetA = UUID.randomUUID();
    private final UUID userA = UUID.randomUUID();
    private final UUID patientB = UUID.randomUUID();
    private final UUID appointmentB = UUID.randomUUID();
    private MockMvc mockMvc;
    private PatientService patientService;
    private AppointmentService appointmentService;
    private ClinicalService clinicalService;

    @BeforeEach
    void setUp() {
        patientService = org.mockito.Mockito.mock(PatientService.class);
        appointmentService = org.mockito.Mockito.mock(AppointmentService.class);
        clinicalService = org.mockito.Mockito.mock(ClinicalService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PatientController(patientService),
                        new AppointmentController(appointmentService),
                        new ClinicalController(clinicalService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void cabinetA_gets404ForPatientIdFromCabinetB() throws Exception {
        when(patientService.get(cabinetA, patientB)).thenThrow(new PatientNotFoundException());

        mockMvc.perform(get("/api/v1/patients/{patientId}", patientB).with(cabinetRequest()))
                .andExpect(status().isNotFound());

        verify(patientService).get(eq(cabinetA), eq(patientB));
    }

    @Test
    void cabinetA_cannotCancelAppointmentFromCabinetB() throws Exception {
        doThrow(new AppointmentNotFoundException()).when(appointmentService)
                .cancel(eq(cabinetA), eq(userA), eq(appointmentB), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/api/v1/appointments/{appointmentId}/cancel", appointmentB)
                .with(cabinetRequest())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isNotFound());

        verify(appointmentService).cancel(eq(cabinetA), eq(userA), eq(appointmentB), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cabinetA_gets404ForClinicalRecordOfCabinetB() throws Exception {
        when(clinicalService.notes(cabinetA, patientB)).thenThrow(new PatientNotFoundException());

        mockMvc.perform(get("/api/v1/patients/{patientId}/clinical-notes", patientB).with(cabinetRequest()))
                .andExpect(status().isNotFound());

        verify(clinicalService).notes(eq(cabinetA), eq(patientB));
    }

    private UsernamePasswordAuthenticationToken cabinetAuthentication() {
        var claims = Jwts.claims()
                .subject(userA.toString())
                .add("token_type", "cabinet")
                .add("cabinet_id", cabinetA.toString())
                .add("membership_id", UUID.randomUUID().toString())
                .add("role", "ADMIN")
                .issuedAt(Date.from(Instant.now()))
                .build();
        var authentication = new UsernamePasswordAuthenticationToken(userA, null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        authentication.setDetails(claims);
        return authentication;
    }

    private RequestPostProcessor cabinetRequest() {
        return request -> {
            request.setUserPrincipal(cabinetAuthentication());
            return request;
        };
    }
}
