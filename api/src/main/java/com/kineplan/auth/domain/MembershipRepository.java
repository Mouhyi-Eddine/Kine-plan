package com.kineplan.auth.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    List<Membership> findByCabinetId(UUID cabinetId);
    List<Membership> findByUserIdAndStatus(UUID userId, MembershipStatus status);
    Optional<Membership> findByUserIdAndCabinetId(UUID userId, UUID cabinetId);
    long countByCabinetIdAndRoleAndStatus(UUID cabinetId, MembershipRole role, MembershipStatus status);
}