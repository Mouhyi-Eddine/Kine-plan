package com.kineplan.notification.infrastructure;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
    long countByCabinetIdAndChannelAndStatusAndSentAtGreaterThanEqualAndSentAtLessThan(
            UUID cabinetId, String channel, String status, Instant from, Instant to);
}
