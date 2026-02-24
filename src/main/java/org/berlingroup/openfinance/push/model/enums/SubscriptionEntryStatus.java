package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Subscription Entry Status as defined in the openFinance API Framework Data Dictionary.
 */
public enum SubscriptionEntryStatus {

    received, rejected, partiallyAuthorised, valid,
    revokedByPsu, expired, terminatedByTpp;

    @JsonValue
    public String toValue() {
        return name();
    }
}
