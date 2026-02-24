package org.berlingroup.openfinance.push.model.entity;

import jakarta.persistence.*;
import org.berlingroup.openfinance.push.model.enums.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity persisting received Resource Status Notifications.
 * Tracks the X-Request-ID for idempotency.
 */
@Entity
@Table(name = "resource_notification", indexes = {
        @Index(name = "idx_request_id", columnList = "requestId", unique = true)
})
public class ResourceNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID requestId;

    private String paymentId;
    private String consentId;
    private String subscriptionId;
    private String basketId;
    private UUID mandateResourceId;
    private UUID documentResourceId;
    private String entryId;
    private String subscriptionEntryId;
    private String authorisationId;
    private String cancellationId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
    private ConsentStatus consentStatus;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    @Enumerated(EnumType.STRING)
    private SubscriptionEntryStatus subscriptionEntryStatus;

    @Enumerated(EnumType.STRING)
    private MandateStatus mandateStatus;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus;

    @Enumerated(EnumType.STRING)
    private SCAStatus scaStatus;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Enumerated(EnumType.STRING)
    private StatusReasonCode reasonCode;

    private String reasonProprietary;
    private String debtorDecisionDateTime;

    private String acceptedAmountCurrency;
    private String acceptedAmountValue;

    private String acceptanceDateTime;
    private String acceptedPaymentInstrument;
    private String statusIdentification;

    @Column(nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    protected void onCreate() {
        this.receivedAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getBasketId() { return basketId; }
    public void setBasketId(String basketId) { this.basketId = basketId; }

    public UUID getMandateResourceId() { return mandateResourceId; }
    public void setMandateResourceId(UUID mandateResourceId) { this.mandateResourceId = mandateResourceId; }

    public UUID getDocumentResourceId() { return documentResourceId; }
    public void setDocumentResourceId(UUID documentResourceId) { this.documentResourceId = documentResourceId; }

    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }

    public String getSubscriptionEntryId() { return subscriptionEntryId; }
    public void setSubscriptionEntryId(String subscriptionEntryId) { this.subscriptionEntryId = subscriptionEntryId; }

    public String getAuthorisationId() { return authorisationId; }
    public void setAuthorisationId(String authorisationId) { this.authorisationId = authorisationId; }

    public String getCancellationId() { return cancellationId; }
    public void setCancellationId(String cancellationId) { this.cancellationId = cancellationId; }

    public TransactionStatus getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(TransactionStatus transactionStatus) { this.transactionStatus = transactionStatus; }

    public ConsentStatus getConsentStatus() { return consentStatus; }
    public void setConsentStatus(ConsentStatus consentStatus) { this.consentStatus = consentStatus; }

    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public SubscriptionEntryStatus getSubscriptionEntryStatus() { return subscriptionEntryStatus; }
    public void setSubscriptionEntryStatus(SubscriptionEntryStatus subscriptionEntryStatus) { this.subscriptionEntryStatus = subscriptionEntryStatus; }

    public MandateStatus getMandateStatus() { return mandateStatus; }
    public void setMandateStatus(MandateStatus mandateStatus) { this.mandateStatus = mandateStatus; }

    public DocumentStatus getDocumentStatus() { return documentStatus; }
    public void setDocumentStatus(DocumentStatus documentStatus) { this.documentStatus = documentStatus; }

    public SCAStatus getScaStatus() { return scaStatus; }
    public void setScaStatus(SCAStatus scaStatus) { this.scaStatus = scaStatus; }

    public RequestStatus getRequestStatus() { return requestStatus; }
    public void setRequestStatus(RequestStatus requestStatus) { this.requestStatus = requestStatus; }

    public StatusReasonCode getReasonCode() { return reasonCode; }
    public void setReasonCode(StatusReasonCode reasonCode) { this.reasonCode = reasonCode; }

    public String getReasonProprietary() { return reasonProprietary; }
    public void setReasonProprietary(String reasonProprietary) { this.reasonProprietary = reasonProprietary; }

    public String getDebtorDecisionDateTime() { return debtorDecisionDateTime; }
    public void setDebtorDecisionDateTime(String debtorDecisionDateTime) { this.debtorDecisionDateTime = debtorDecisionDateTime; }

    public String getAcceptedAmountCurrency() { return acceptedAmountCurrency; }
    public void setAcceptedAmountCurrency(String acceptedAmountCurrency) { this.acceptedAmountCurrency = acceptedAmountCurrency; }

    public String getAcceptedAmountValue() { return acceptedAmountValue; }
    public void setAcceptedAmountValue(String acceptedAmountValue) { this.acceptedAmountValue = acceptedAmountValue; }

    public String getAcceptanceDateTime() { return acceptanceDateTime; }
    public void setAcceptanceDateTime(String acceptanceDateTime) { this.acceptanceDateTime = acceptanceDateTime; }

    public String getAcceptedPaymentInstrument() { return acceptedPaymentInstrument; }
    public void setAcceptedPaymentInstrument(String acceptedPaymentInstrument) { this.acceptedPaymentInstrument = acceptedPaymentInstrument; }

    public String getStatusIdentification() { return statusIdentification; }
    public void setStatusIdentification(String statusIdentification) { this.statusIdentification = statusIdentification; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
}
