package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status Reason Code as defined in the openFinance API Framework Data Dictionary.
 */
public enum StatusReasonCode {

    AM04, AM21, BEXX, CN01, DS0C, DS0D, DS0A, DS04,
    FOCR, SL11, TKSP, TKXP, AM02, DT05, AC05, AC06,
    DS0K, DT01, UPAY, EOL1;

    @JsonValue
    public String toValue() {
        return name();
    }
}
