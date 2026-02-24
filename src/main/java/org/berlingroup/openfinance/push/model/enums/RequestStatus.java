package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Request Status as defined in the openFinance API Framework Data Dictionary.
 */
public enum RequestStatus {

    ACCP, ACWC, RCVD, PDNG, RJCT;

    @JsonValue
    public String toValue() {
        return name();
    }
}
