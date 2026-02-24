package org.berlingroup.openfinance.push.repository;

import org.berlingroup.openfinance.push.model.entity.ResourceNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ResourceNotification} entities.
 */
@Repository
public interface ResourceNotificationRepository extends JpaRepository<ResourceNotification, Long> {

    /**
     * Find a notification by its X-Request-ID for idempotency checks.
     *
     * @param requestId the X-Request-ID UUID
     * @return the notification if it exists
     */
    Optional<ResourceNotification> findByRequestId(UUID requestId);

    /**
     * Check if a notification with the given X-Request-ID already exists.
     *
     * @param requestId the X-Request-ID UUID
     * @return true if a notification with this request ID exists
     */
    boolean existsByRequestId(UUID requestId);
}
