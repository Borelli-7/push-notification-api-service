package org.berlingroup.openfinance.push.service;

import org.berlingroup.openfinance.push.dto.Amount;
import org.berlingroup.openfinance.push.dto.PushResourceStatusRequest;
import org.berlingroup.openfinance.push.event.ResourceNotificationEvent;
import org.berlingroup.openfinance.push.exception.DuplicateRequestException;
import org.berlingroup.openfinance.push.exception.InvalidNotificationException;
import org.berlingroup.openfinance.push.model.entity.ResourceNotification;
import org.berlingroup.openfinance.push.model.enums.TransactionStatus;
import org.berlingroup.openfinance.push.repository.ResourceNotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ResourceStatusNotificationService}.
 */
@ExtendWith(MockitoExtension.class)
class ResourceStatusNotificationServiceTest {

    @Mock
    private ResourceNotificationRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ResourceStatusNotificationService service;

    private PushResourceStatusRequest createValidPaymentNotification() {
        return new PushResourceStatusRequest(
                "payment-123", null, null, null, null, null,
                null, null, null, null,
                TransactionStatus.ACCP, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
    }

    private PushResourceStatusRequest createNotificationWithAmount() {
        return new PushResourceStatusRequest(
                "payment-456", null, null, null, null, null,
                null, null, null, null,
                TransactionStatus.ACSC, null, null, null, null, null, null, null,
                null, null, null,
                new Amount("EUR", "250.75"),
                null, "SCT", null, null
        );
    }

    private PushResourceStatusRequest createNoIdNotification() {
        return new PushResourceStatusRequest(
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
    }

    @Nested
    @DisplayName("Successful Processing Tests")
    class SuccessTests {

        @Test
        @DisplayName("Valid notification is persisted and event is published")
        void shouldPersistAndPublishEvent() {
            UUID requestId = UUID.randomUUID();
            var request = createValidPaymentNotification();
            when(repository.existsByRequestId(requestId)).thenReturn(false);
            when(repository.save(any(ResourceNotification.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processNotification(requestId, request);

            // Verify persistence
            ArgumentCaptor<ResourceNotification> entityCaptor = ArgumentCaptor.forClass(ResourceNotification.class);
            verify(repository).save(entityCaptor.capture());
            ResourceNotification saved = entityCaptor.getValue();
            assertThat(saved.getRequestId()).isEqualTo(requestId);
            assertThat(saved.getPaymentId()).isEqualTo("payment-123");
            assertThat(saved.getTransactionStatus()).isEqualTo(TransactionStatus.ACCP);

            // Verify event published
            ArgumentCaptor<ResourceNotificationEvent> eventCaptor = ArgumentCaptor.forClass(ResourceNotificationEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getRequestId()).isEqualTo(requestId);
            assertThat(eventCaptor.getValue().getNotification()).isEqualTo(request);
        }

        @Test
        @DisplayName("Notification with accepted amount persists currency and value")
        void shouldPersistAcceptedAmount() {
            UUID requestId = UUID.randomUUID();
            var request = createNotificationWithAmount();
            when(repository.existsByRequestId(requestId)).thenReturn(false);
            when(repository.save(any(ResourceNotification.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processNotification(requestId, request);

            ArgumentCaptor<ResourceNotification> captor = ArgumentCaptor.forClass(ResourceNotification.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getAcceptedAmountCurrency()).isEqualTo("EUR");
            assertThat(captor.getValue().getAcceptedAmountValue()).isEqualTo("250.75");
            assertThat(captor.getValue().getAcceptedPaymentInstrument()).isEqualTo("SCT");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Throws InvalidNotificationException when no resource identifier")
        void shouldRejectMissingResourceIdentifier() {
            UUID requestId = UUID.randomUUID();
            var request = createNoIdNotification();

            assertThatThrownBy(() -> service.processNotification(requestId, request))
                    .isInstanceOf(InvalidNotificationException.class)
                    .hasMessageContaining("At least one resource identifier");

            verify(repository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Throws DuplicateRequestException on duplicate X-Request-ID")
        void shouldRejectDuplicateRequestId() {
            UUID requestId = UUID.randomUUID();
            var request = createValidPaymentNotification();
            when(repository.existsByRequestId(requestId)).thenReturn(true);

            assertThatThrownBy(() -> service.processNotification(requestId, request))
                    .isInstanceOf(DuplicateRequestException.class)
                    .hasMessageContaining(requestId.toString());

            verify(repository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Mapping Tests")
    class MappingTests {

        @Test
        @DisplayName("All fields are correctly mapped to entity")
        void shouldMapAllFieldsCorrectly() {
            UUID requestId = UUID.randomUUID();
            UUID mandateId = UUID.randomUUID();
            var request = new PushResourceStatusRequest(
                    "pay-1", "consent-1", "sub-1", "basket-1",
                    mandateId, null,
                    "entry-1", "sub-entry-1", "auth-1", "cancel-1",
                    TransactionStatus.RCVD, null, null, null, null, null,
                    null, null, null, "proprietary",
                    "2026-01-01T00:00:00Z", null, null, null, "status-ref", null
            );
            when(repository.existsByRequestId(requestId)).thenReturn(false);
            when(repository.save(any(ResourceNotification.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processNotification(requestId, request);

            ArgumentCaptor<ResourceNotification> captor = ArgumentCaptor.forClass(ResourceNotification.class);
            verify(repository).save(captor.capture());
            ResourceNotification entity = captor.getValue();

            assertThat(entity.getPaymentId()).isEqualTo("pay-1");
            assertThat(entity.getConsentId()).isEqualTo("consent-1");
            assertThat(entity.getSubscriptionId()).isEqualTo("sub-1");
            assertThat(entity.getBasketId()).isEqualTo("basket-1");
            assertThat(entity.getMandateResourceId()).isEqualTo(mandateId);
            assertThat(entity.getEntryId()).isEqualTo("entry-1");
            assertThat(entity.getSubscriptionEntryId()).isEqualTo("sub-entry-1");
            assertThat(entity.getAuthorisationId()).isEqualTo("auth-1");
            assertThat(entity.getCancellationId()).isEqualTo("cancel-1");
            assertThat(entity.getReasonProprietary()).isEqualTo("proprietary");
            assertThat(entity.getStatusIdentification()).isEqualTo("status-ref");
        }
    }
}
