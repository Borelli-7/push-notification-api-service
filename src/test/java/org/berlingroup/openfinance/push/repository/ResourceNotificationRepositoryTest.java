package org.berlingroup.openfinance.push.repository;

import org.berlingroup.openfinance.push.model.entity.ResourceNotification;
import org.berlingroup.openfinance.push.model.enums.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Data layer tests for {@link ResourceNotificationRepository}.
 */
@DataJpaTest
class ResourceNotificationRepositoryTest {

    @Autowired
    private ResourceNotificationRepository repository;

    private ResourceNotification createEntity(UUID requestId, String paymentId) {
        var entity = new ResourceNotification();
        entity.setRequestId(requestId);
        entity.setPaymentId(paymentId);
        entity.setTransactionStatus(TransactionStatus.ACCP);
        return entity;
    }

    @Test
    @DisplayName("Save and retrieve notification by request ID")
    void shouldSaveAndFindByRequestId() {
        UUID requestId = UUID.randomUUID();
        var entity = createEntity(requestId, "payment-001");

        repository.save(entity);

        var found = repository.findByRequestId(requestId);
        assertThat(found).isPresent();
        assertThat(found.get().getPaymentId()).isEqualTo("payment-001");
        assertThat(found.get().getTransactionStatus()).isEqualTo(TransactionStatus.ACCP);
        assertThat(found.get().getReceivedAt()).isNotNull();
    }

    @Test
    @DisplayName("existsByRequestId returns true for existing request")
    void shouldReturnTrueForExistingRequest() {
        UUID requestId = UUID.randomUUID();
        repository.save(createEntity(requestId, "payment-002"));

        assertThat(repository.existsByRequestId(requestId)).isTrue();
    }

    @Test
    @DisplayName("existsByRequestId returns false for non-existing request")
    void shouldReturnFalseForNonExistingRequest() {
        assertThat(repository.existsByRequestId(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("findByRequestId returns empty for non-existing request")
    void shouldReturnEmptyForNonExistingRequest() {
        assertThat(repository.findByRequestId(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Multiple notifications can be saved with different request IDs")
    void shouldSaveMultipleNotifications() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        repository.save(createEntity(id1, "payment-A"));
        repository.save(createEntity(id2, "payment-B"));

        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findByRequestId(id1).get().getPaymentId()).isEqualTo("payment-A");
        assertThat(repository.findByRequestId(id2).get().getPaymentId()).isEqualTo("payment-B");
    }
}
