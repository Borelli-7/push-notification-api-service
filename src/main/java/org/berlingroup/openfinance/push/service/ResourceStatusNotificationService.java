package org.berlingroup.openfinance.push.service;

import org.berlingroup.openfinance.push.dto.PushResourceStatusRequest;
import org.berlingroup.openfinance.push.event.ResourceNotificationEvent;
import org.berlingroup.openfinance.push.exception.DuplicateRequestException;
import org.berlingroup.openfinance.push.exception.InvalidNotificationException;
import org.berlingroup.openfinance.push.model.entity.ResourceNotification;
import org.berlingroup.openfinance.push.repository.ResourceNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service handling incoming Resource Status Notifications (API Client / receiver side).
 * <p>
 * Validates the request, checks idempotency via X-Request-ID, persists the notification,
 * and publishes a {@link ResourceNotificationEvent} for downstream consumers.
 * </p>
 */
@Service
public class ResourceStatusNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ResourceStatusNotificationService.class);

    private final ResourceNotificationRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public ResourceStatusNotificationService(ResourceNotificationRepository repository,
                                              ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Process an incoming push resource status notification.
     *
     * @param requestId the X-Request-ID header value (must be a valid UUID)
     * @param request   the notification request body
     * @throws DuplicateRequestException    if a notification with the same X-Request-ID already exists
     * @throws InvalidNotificationException if no resource identifier is present in the request
     */
    @Transactional
    public void processNotification(UUID requestId, PushResourceStatusRequest request) {
        log.debug("Processing notification with X-Request-ID: {}", requestId);

        // Validate at least one resource identifier is present
        if (!request.hasAtLeastOneResourceIdentifier()) {
            throw new InvalidNotificationException(
                    "At least one resource identifier (paymentId, consentId, subscriptionId, "
                            + "basketId, mandateResourceId, documentResourceId) must be present");
        }

        // Idempotency check
        if (repository.existsByRequestId(requestId)) {
            throw new DuplicateRequestException(requestId.toString());
        }

        // Map to entity and persist
        ResourceNotification entity = mapToEntity(requestId, request);
        repository.save(entity);

        log.info("Persisted notification with X-Request-ID: {}", requestId);

        // Publish event for downstream consumers
        eventPublisher.publishEvent(new ResourceNotificationEvent(this, requestId, request));
    }

    private ResourceNotification mapToEntity(UUID requestId, PushResourceStatusRequest request) {
        var entity = new ResourceNotification();
        entity.setRequestId(requestId);
        entity.setPaymentId(request.paymentId());
        entity.setConsentId(request.consentId());
        entity.setSubscriptionId(request.subscriptionId());
        entity.setBasketId(request.basketId());
        entity.setMandateResourceId(request.mandateResourceId());
        entity.setDocumentResourceId(request.documentResourceId());
        entity.setEntryId(request.entryId());
        entity.setSubscriptionEntryId(request.subscriptionEntryId());
        entity.setAuthorisationId(request.authorisationId());
        entity.setCancellationId(request.cancellationId());
        entity.setTransactionStatus(request.transactionStatus());
        entity.setConsentStatus(request.consentStatus());
        entity.setSubscriptionStatus(request.subscriptionStatus());
        entity.setSubscriptionEntryStatus(request.subscriptionEntryStatus());
        entity.setMandateStatus(request.mandateStatus());
        entity.setDocumentStatus(request.documentStatus());
        entity.setScaStatus(request.scaStatus());
        entity.setRequestStatus(request.requestStatus());
        entity.setReasonCode(request.reasonCode());
        entity.setReasonProprietary(request.reasonProprietary());
        entity.setDebtorDecisionDateTime(request.debtorDecisionDateTime());
        if (request.acceptedAmount() != null) {
            entity.setAcceptedAmountCurrency(request.acceptedAmount().currency());
            entity.setAcceptedAmountValue(request.acceptedAmount().amount());
        }
        entity.setAcceptanceDateTime(request.acceptanceDateTime());
        entity.setAcceptedPaymentInstrument(request.acceptedPaymentInstrument());
        entity.setStatusIdentification(request.statusIdentification());
        return entity;
    }
}
