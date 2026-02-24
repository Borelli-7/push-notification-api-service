package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * SCA Status as defined in the openFinance API Framework Data Dictionary.
 */
public enum SCAStatus {

    received, psuIdentified, psuAuthenticated, scaMethodSelected,
    started, unconfirmed, finalised, failed, exempted;

    @JsonValue
    public String toValue() {
        return name();
    }
}
