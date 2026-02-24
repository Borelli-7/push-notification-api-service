package org.berlingroup.openfinance.push.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Href Type as defined in the openFinance API Framework Data Dictionary.
 *
 * @param href the hyperlink reference
 */
public record HrefType(
        @NotBlank(message = "href is required")
        String href
) {
}
