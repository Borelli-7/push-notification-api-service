package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Consent Status as defined in the openFinance API Framework Data Dictionary.
 */
public enum ConsentStatus {

    received, rejected, partiallyAuthorised, valid,
    revokedByPsu, expired, terminatedByTpp, replacedByTpp;

    @JsonValue
    public String toValue() {
        return name();
    }
}
