package com.kineplan.notification.application;

import com.kineplan.appointment.domain.Appointment;
import com.kineplan.appointment.domain.AppointmentRepository;
import com.kineplan.appointment.domain.AppointmentStatus;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.patient.domain.Patient;
import com.kineplan.patient.domain.PatientRepository;
import com.kineplan.shared.infrastructure.tenancy.TenantContext;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {
    private final CabinetRepository cabinetRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final NotificationPort notificationPort;
    private final Clock clock = Clock.systemUTC();

    public ReminderService(CabinetRepository cabinetRepository, AppointmentRepository appointmentRepository,
                           PatientRepository patientRepository, NotificationPort notificationPort) {
        this.cabinetRepository = cabinetRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.notificationPort = notificationPort;
    }

    @Scheduled(cron = "${kineplan.notifications.reminder-cron:0 0 9 * * *}", zone = "UTC")
    public void sendDayAheadReminders() {
        Instant from = clock.instant().plus(Duration.ofHours(23));
        Instant to = clock.instant().plus(Duration.ofHours(25));
        for (Cabinet cabinet : cabinetRepository.findAll()) {
            if (!cabinet.isActive()) continue;
            TenantContext.set(cabinet.getId());
            try {
                sendForCabinet(cabinet, from, to);
            } finally {
                TenantContext.clear();
            }
        }
    }

    private void sendForCabinet(Cabinet cabinet, Instant from, Instant to) {
        List<Appointment> appointments = appointmentRepository
                .findByCabinetIdAndStartAtGreaterThanEqualAndStartAtLessThanAndStatusIn(
                        cabinet.getId(), from, to, List.of(AppointmentStatus.PLANIFIE, AppointmentStatus.CONFIRME));
        for (Appointment appointment : appointments) {
            Patient patient = patientRepository.findByIdAndCabinetId(appointment.getPatientId(), cabinet.getId()).orElse(null);
            if (patient == null || patient.getArchivedAt() != null) continue;
            if (patient.isSmsConsent() && patient.getPhone() != null) {
                notificationPort.sendSms(patient.getPhone(), "Rappel de votre rendez-vous");
            } else if (patient.isEmailConsent() && patient.getEmail() != null) {
                notificationPort.sendEmail(patient.getEmail(), "Rappel de rendez-vous", "Rappel de votre rendez-vous");
            }
        }
    }
}