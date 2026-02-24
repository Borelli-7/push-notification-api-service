package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Document Status as defined in the openFinance API Framework Data Dictionary.
 */
public enum DocumentStatus {

    received, rejected, accessible, withdrawn, replaced, expired;

    @JsonValue
    public String toValue() {
        return name();
    }
}
