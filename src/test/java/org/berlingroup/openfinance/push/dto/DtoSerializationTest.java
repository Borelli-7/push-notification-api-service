package org.berlingroup.openfinance.push.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.berlingroup.openfinance.push.model.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for DTO serialization/deserialization via Jackson.
 */
class DtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("PushResourceStatusRequest serializes and deserializes correctly")
    void shouldSerializeAndDeserializeRequest() throws Exception {
        UUID mandateId = UUID.randomUUID();
        var request = new PushResourceStatusRequest(
                "pay-1", "consent-1", "sub-1", "basket-1",
                mandateId, null,
                "entry-1", "sub-entry-1", "auth-1", "cancel-1",
                TransactionStatus.ACSC, ConsentStatus.valid,
                SubscriptionStatus.valid, SubscriptionEntryStatus.valid,
                MandateStatus.ACTV, DocumentStatus.accessible,
                SCAStatus.finalised, RequestStatus.ACCP,
                StatusReasonCode.AM04, "reason",
                "2026-02-24T10:00:00Z",
                new Amount("EUR", "100.00"),
                "2026-02-25T10:00:00Z", "SCT", "ref-001",
                Map.of("status", new HrefType("https://example.com/status"))
        );

        String json = objectMapper.writeValueAsString(request);
        assertThat(json).contains("pay-1");
        assertThat(json).contains("ACSC");
        assertThat(json).contains("\"_links\"");

        PushResourceStatusRequest deserialized = objectMapper.readValue(json, PushResourceStatusRequest.class);
        assertThat(deserialized.paymentId()).isEqualTo("pay-1");
        assertThat(deserialized.transactionStatus()).isEqualTo(TransactionStatus.ACSC);
        assertThat(deserialized.consentStatus()).isEqualTo(ConsentStatus.valid);
        assertThat(deserialized.acceptedAmount().currency()).isEqualTo("EUR");
        assertThat(deserialized.acceptedAmount().amount()).isEqualTo("100.00");
        assertThat(deserialized.links()).containsKey("status");
        assertThat(deserialized.mandateResourceId()).isEqualTo(mandateId);
    }

    @Test
    @DisplayName("Amount record serializes correctly")
    void shouldSerializeAmount() throws Exception {
        var amount = new Amount("USD", "1234.56");
        String json = objectMapper.writeValueAsString(amount);
        assertThat(json).contains("USD");
        assertThat(json).contains("1234.56");

        Amount deserialized = objectMapper.readValue(json, Amount.class);
        assertThat(deserialized.currency()).isEqualTo("USD");
        assertThat(deserialized.amount()).isEqualTo("1234.56");
    }

    @Test
    @DisplayName("hasAtLeastOneResourceIdentifier returns true when paymentId is set")
    void shouldReturnTrueWhenPaymentIdSet() {
        var request = new PushResourceStatusRequest(
                "pay-1", null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
        assertThat(request.hasAtLeastOneResourceIdentifier()).isTrue();
    }

    @Test
    @DisplayName("hasAtLeastOneResourceIdentifier returns false when no ID is set")
    void shouldReturnFalseWhenNoIdSet() {
        var request = new PushResourceStatusRequest(
                null, null, null, null, null, null,
                null, null, null, null,
                TransactionStatus.ACCP, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
        assertThat(request.hasAtLeastOneResourceIdentifier()).isFalse();
    }

    @Test
    @DisplayName("All enum values serialize to their name")
    void shouldSerializeEnumValues() throws Exception {
        for (TransactionStatus status : TransactionStatus.values()) {
            String json = objectMapper.writeValueAsString(status);
            assertThat(json).isEqualTo("\"" + status.name() + "\"");
        }
        for (ConsentStatus status : ConsentStatus.values()) {
            String json = objectMapper.writeValueAsString(status);
            assertThat(json).isEqualTo("\"" + status.name() + "\"");
        }
        for (SCAStatus status : SCAStatus.values()) {
            String json = objectMapper.writeValueAsString(status);
            assertThat(json).isEqualTo("\"" + status.name() + "\"");
        }
    }
}
