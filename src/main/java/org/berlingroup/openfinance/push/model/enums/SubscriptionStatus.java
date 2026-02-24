package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Subscription Status as defined in the openFinance API Framework Data Dictionary.
 */
public enum SubscriptionStatus {

    received, rejected, partiallyAuthorised, valid, validInChange,
    revokedByPsu, cancelledByAspsp, expired, terminatedByTpp;

    @JsonValue
    public String toValue() {
        return name();
    }
}
