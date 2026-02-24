package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Mandate Status as defined in the openFinance API Framework Data Dictionary.
 */
public enum MandateStatus {

    RCVD, RJCT, PATC, ACTV, EXPI, CANC, SUSP;

    @JsonValue
    public String toValue() {
        return name();
    }
}
