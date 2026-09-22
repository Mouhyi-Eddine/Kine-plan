package com.kineplan.appointment.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    java.util.Optional<Appointment> findByIdAndCabinetId(UUID id, UUID cabinetId);

    Page<Appointment> findByCabinetIdAndStartAtGreaterThanEqualAndStartAtLessThan(UUID cabinetId,
                                                                                    Instant from, Instant to,
                                                                                    Pageable pageable);

    @Query("select count(a) > 0 from Appointment a where a.cabinetId = :cabinetId "
            + "and a.practitionerMembershipId = :practitionerMembershipId "
            + "and a.deletedAt is null and a.status <> com.kineplan.appointment.domain.AppointmentStatus.ANNULE "
            + "and a.startAt < :endAt and a.endAt > :startAt "
            + "and (:appointmentId is null or a.id <> :appointmentId)")
    boolean existsActiveOverlap(@Param("cabinetId") UUID cabinetId,
                                @Param("practitionerMembershipId") UUID practitionerMembershipId,
                                @Param("startAt") Instant startAt, @Param("endAt") Instant endAt,
                                @Param("appointmentId") UUID appointmentId);

        java.util.List<Appointment> findByCabinetIdAndPractitionerMembershipIdAndStartAtLessThanAndEndAtGreaterThan(
                        UUID cabinetId, UUID practitionerMembershipId, Instant to, Instant from);

            List<Appointment> findByCabinetIdAndStartAtGreaterThanEqualAndStartAtLessThanAndStatusIn(
                    UUID cabinetId, Instant from, Instant to, List<AppointmentStatus> statuses);

        long countByCabinetIdAndStartAtGreaterThanEqualAndStartAtLessThan(UUID cabinetId, Instant from, Instant to);
        long countByCabinetIdAndStartAtGreaterThanEqualAndStartAtLessThanAndStatus(UUID cabinetId, Instant from, Instant to,
                                                                                                                                                                AppointmentStatus status);
}