package org.berlingroup.openfinance.push.dto;

import java.util.Map;
import java.util.UUID;

import org.berlingroup.openfinance.push.model.enums.ConsentStatus;
import org.berlingroup.openfinance.push.model.enums.DocumentStatus;
import org.berlingroup.openfinance.push.model.enums.MandateStatus;
import org.berlingroup.openfinance.push.model.enums.RequestStatus;
import org.berlingroup.openfinance.push.model.enums.SCAStatus;
import org.berlingroup.openfinance.push.model.enums.StatusReasonCode;
import org.berlingroup.openfinance.push.model.enums.SubscriptionEntryStatus;
import org.berlingroup.openfinance.push.model.enums.SubscriptionStatus;
import org.berlingroup.openfinance.push.model.enums.TransactionStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

/**
 * Push Resource Status Notification request body as defined in the 
 * Berlin Group openFinance API Framework v2.3 OpenAPI specification.
 * <p>
 * At least one resource identifier (paymentId, consentId, subscriptionId, 
 * basketId, mandateResourceId, documentResourceId) must be present.
 * </p>
 *
 * @see <a href="https://www.berlin-group.org/openfinance-downloads">Berlin Group oFA</a>
 */
public record PushResourceStatusRequest(

        /** Contained if the push notification is about a payment or RTP initiation. */
        String paymentId,

        /** Contained if the push notification is about establishing a consent. */
        String consentId,

        /** Contained if the push notification is about establishing a subscription. */
        String subscriptionId,

        /** Contained if the push notification is about signing a basket. */
        String basketId,

        /** Contained if the push notification is about establishing a mandate. */
        UUID mandateResourceId,

        /** Contained if the push notification is about a document submission. */
        UUID documentResourceId,

        /** May be used if the status relates to an entry of an RTP bulk. */
        String entryId,

        /** May be used if the status relates to an entry of a subscription. */
        String subscriptionEntryId,

        /** Should be contained if the push notification is about a specific SCA status. */
        String authorisationId,

        /** Should be contained if about a specific SCA status of a cancellation authorisation sub-resource. */
        String cancellationId,

        /** Contained if the related resource contains a transaction status which has changed. */
        TransactionStatus transactionStatus,

        /** Contained if the consent status of the addressed resource has changed. */
        ConsentStatus consentStatus,

        /** Contained if the subscription status of the addressed resource has changed. */
        SubscriptionStatus subscriptionStatus,

        /** Contained if the subscription entry status of the addressed resource has changed. */
        SubscriptionEntryStatus subscriptionEntryStatus,

        /** Contained if the mandate status of the addressed resource has changed. */
        MandateStatus mandateStatus,

        /** Contained if the document status of the addressed resource has changed. */
        DocumentStatus documentStatus,

        /** Contained if the SCA status of the addressed resource has changed. */
        SCAStatus scaStatus,

        /** The status of the related request to pay transaction. */
        RequestStatus requestStatus,

        /** Additional information on the reason for e.g. rejecting the request. */
        StatusReasonCode reasonCode,

        /** Proprietary additional information on the reason. */
        String reasonProprietary,

        /** The date and time when the PSU has decided on accepting/rejecting the related request. */
        String debtorDecisionDateTime,

        /** Contained only if the accepted amount deviates from the instructed amount. */
        @Valid
        Amount acceptedAmount,

        /** Contained only if the agreed execution date deviates from the requested execution date. */
        String acceptanceDateTime,

        /** "SCT" or "SCT inst" as default values. */
        String acceptedPaymentInstrument,

        /** Reference added by the debtor. */
        String statusIdentification,

        /** Hypermedia links: scaStatus, status */
        @JsonProperty("_links")
        Map<String, HrefType> links

) {
    /**
     * Validates that at least one resource identifier is present.
     *
     * @return true if at least one resource identifier is set
     */
    public boolean hasAtLeastOneResourceIdentifier() {
        return paymentId != null
                || consentId != null
                || subscriptionId != null
                || basketId != null
                || mandateResourceId != null
                || documentResourceId != null;
    }
}
