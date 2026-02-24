package org.berlingroup.openfinance.push.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Transaction Status as defined in the openFinance API Framework Data Dictionary.
 *
 * @see <a href="https://www.berlin-group.org/openfinance-downloads">openFinance Data Dictionary</a>
 */
public enum TransactionStatus {

    ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP,
    RCVD, PDNG, RJCT, CANC, ACFC, PATC, PART,
    PRES, RVCM, RVNC, RCVC;

    @JsonValue
    public String toValue() {
        return name();
    }
}
