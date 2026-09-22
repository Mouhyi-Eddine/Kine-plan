package com.kineplan.planning.application;

import com.kineplan.appointment.domain.Appointment;
import com.kineplan.appointment.domain.AppointmentRepository;
import com.kineplan.appointment.domain.AppointmentStatus;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.caretype.domain.CareType;
import com.kineplan.caretype.domain.CareTypeRepository;
import com.kineplan.planning.api.AvailabilityResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvailabilityService {
    private final CabinetRepository cabinetRepository;
    private final MembershipRepository membershipRepository;
    private final CareTypeRepository careTypeRepository;
    private final com.kineplan.planning.domain.WorkingHourRepository workingHourRepository;
    private final com.kineplan.planning.domain.PractitionerTimeOffRepository timeOffRepository;
    private final com.kineplan.planning.domain.CabinetClosureRepository closureRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(CabinetRepository cabinetRepository, MembershipRepository membershipRepository,
                               CareTypeRepository careTypeRepository,
                               com.kineplan.planning.domain.WorkingHourRepository workingHourRepository,
                               com.kineplan.planning.domain.PractitionerTimeOffRepository timeOffRepository,
                               com.kineplan.planning.domain.CabinetClosureRepository closureRepository,
                               AppointmentRepository appointmentRepository) {
        this.cabinetRepository = cabinetRepository;
        this.membershipRepository = membershipRepository;
        this.careTypeRepository = careTypeRepository;
        this.workingHourRepository = workingHourRepository;
        this.timeOffRepository = timeOffRepository;
        this.closureRepository = closureRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> find(UUID cabinetId, UUID practitionerMembershipId, UUID careTypeId,
                                            LocalDate from, LocalDate to) {
        if (to.isBefore(from) || from.plusMonths(3).isBefore(to)) {
            throw new IllegalArgumentException("Availability period must be between one day and three months");
        }
        Cabinet cabinet = cabinetRepository.findById(cabinetId)
                .filter(Cabinet::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet not found"));
        membershipRepository.findById(practitionerMembershipId)
                .filter(membership -> membership.isActive() && membership.getCabinetId().equals(cabinetId)
                        && membership.getRole() == MembershipRole.KINESITHERAPEUTE)
                .orElseThrow(() -> new IllegalArgumentException("Practitioner not found"));
        CareType careType = careTypeRepository.findByIdAndCabinetId(careTypeId, cabinetId)
                .filter(CareType::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Care type not found"));

        ZoneId zone = ZoneId.of(cabinet.getTimezone());
        Instant rangeStart = from.atStartOfDay(zone).toInstant();
        Instant rangeEnd = to.plusDays(1).atStartOfDay(zone).toInstant();
        var timeOffs = timeOffRepository.findByCabinetIdAndPractitionerMembershipIdAndStartAtLessThanAndEndAtGreaterThan(
                cabinetId, practitionerMembershipId, rangeEnd, rangeStart);
        var closures = closureRepository.findByCabinetIdAndStartAtLessThanAndEndAtGreaterThan(
                cabinetId, rangeEnd, rangeStart);
        var appointments = appointmentRepository
                .findByCabinetIdAndPractitionerMembershipIdAndStartAtLessThanAndEndAtGreaterThan(
                        cabinetId, practitionerMembershipId, rangeEnd, rangeStart);
        var hours = workingHourRepository.findByCabinetIdAndPractitionerMembershipId(cabinetId, practitionerMembershipId);
        List<AvailabilityResponse> result = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            for (var hour : hours) {
                if (hour.getDayOfWeek() != date.getDayOfWeek()) {
                    continue;
                }
                LocalDateTime slot = LocalDateTime.of(date, hour.getStartTime());
                LocalDateTime closing = LocalDateTime.of(date, hour.getEndTime());
                while (!slot.plusMinutes(careType.getDefaultDurationMinutes()).isAfter(closing)) {
                    Instant start = slot.atZone(zone).toInstant();
                    Instant end = slot.plusMinutes(careType.getDefaultDurationMinutes()).atZone(zone).toInstant();
                    if (isFree(start, end, timeOffs, closures, appointments)) {
                        result.add(new AvailabilityResponse(start, end));
                    }
                    slot = slot.plusMinutes(careType.getDefaultDurationMinutes());
                }
            }
        }
        return result;
    }

    private boolean isFree(Instant start, Instant end, List<?> timeOffs, List<?> closures, List<Appointment> appointments) {
        return timeOffs.stream().noneMatch(item -> overlaps(start, end,
                ((com.kineplan.planning.domain.PractitionerTimeOff) item).getStartAt(),
                ((com.kineplan.planning.domain.PractitionerTimeOff) item).getEndAt()))
                && closures.stream().noneMatch(item -> overlaps(start, end,
                ((com.kineplan.planning.domain.CabinetClosure) item).getStartAt(),
                ((com.kineplan.planning.domain.CabinetClosure) item).getEndAt()))
                && appointments.stream().filter(item -> item.getStatus() != AppointmentStatus.ANNULE)
                .noneMatch(item -> overlaps(start, end, item.getStartAt(), item.getEndAt()));
    }

    private boolean overlaps(Instant firstStart, Instant firstEnd, Instant secondStart, Instant secondEnd) {
        return firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart);
    }
}