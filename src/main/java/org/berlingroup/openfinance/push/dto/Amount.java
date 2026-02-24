package org.berlingroup.openfinance.push.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Amount type as defined in the openFinance API Framework Data Dictionary.
 *
 * @param currency ISO 4217 Alpha 3 currency code
 * @param amount   The amount with fractional digits, up to 14 significant figures
 */
public record Amount(

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a valid ISO 4217 Alpha 3 code")
        String currency,

        @NotBlank(message = "Amount is required")
        @Pattern(regexp = "-?[0-9]{1,14}(\\.[0-9]{1,3})?", message = "Amount format is invalid")
        String amount
) {
}
