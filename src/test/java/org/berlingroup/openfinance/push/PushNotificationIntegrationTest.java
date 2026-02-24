package org.berlingroup.openfinance.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.berlingroup.openfinance.push.dto.Amount;
import org.berlingroup.openfinance.push.dto.HrefType;
import org.berlingroup.openfinance.push.dto.PushResourceStatusRequest;
import org.berlingroup.openfinance.push.event.ResourceNotificationEvent;
import org.berlingroup.openfinance.push.model.enums.*;
import org.berlingroup.openfinance.push.repository.ResourceNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test — exercises controller → service → repository → event flow
 * with a real Spring Boot context and H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PushNotificationIntegrationTest.TestEventConfig.class)
class PushNotificationIntegrationTest {

    private static final String ENDPOINT = "/Client-Notification-URL";
    private static final String X_REQUEST_ID = "X-Request-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceNotificationRepository repository;

    @Autowired
    private TestEventListener testEventListener;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        testEventListener.clear();
    }

    @Test
    @DisplayName("Full round-trip: POST notification → persist → event published")
    void shouldProcessFullRoundTrip() throws Exception {
        UUID requestId = UUID.randomUUID();
        var request = new PushResourceStatusRequest(
                "payment-integration-001", null, null, null, null, null,
                null, null, "auth-01", null,
                TransactionStatus.ACSC, null, null, null, null, null,
                SCAStatus.finalised, null,
                null, null, null,
                new Amount("EUR", "500.00"),
                null, "SCT inst", null,
                Map.of("status", new HrefType("https://api.example.com/payments/status"))
        );

        mockMvc.perform(post(ENDPOINT)
                        .header(X_REQUEST_ID, requestId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string(X_REQUEST_ID, requestId.toString()));

        // Verify persistence in H2
        var saved = repository.findByRequestId(requestId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getPaymentId()).isEqualTo("payment-integration-001");
        assertThat(saved.get().getTransactionStatus()).isEqualTo(TransactionStatus.ACSC);
        assertThat(saved.get().getScaStatus()).isEqualTo(SCAStatus.finalised);
        assertThat(saved.get().getAcceptedAmountCurrency()).isEqualTo("EUR");
        assertThat(saved.get().getAcceptedAmountValue()).isEqualTo("500.00");
        assertThat(saved.get().getAcceptedPaymentInstrument()).isEqualTo("SCT inst");
        assertThat(saved.get().getReceivedAt()).isNotNull();

        // Verify event was published
        assertThat(testEventListener.getReceivedEvents()).hasSize(1);
        assertThat(testEventListener.getReceivedEvents().getFirst().getRequestId()).isEqualTo(requestId);
    }

    @Test
    @DisplayName("Duplicate X-Request-ID returns 409 and does not persist twice")
    void shouldRejectDuplicateRequestId() throws Exception {
        UUID requestId = UUID.randomUUID();
        var request = new PushResourceStatusRequest(
                "payment-dup", null, null, null, null, null,
                null, null, null, null,
                TransactionStatus.RCVD, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
        String body = objectMapper.writeValueAsString(request);

        // First request — should succeed
        mockMvc.perform(post(ENDPOINT)
                        .header(X_REQUEST_ID, requestId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // Second request with same ID — should be rejected
        mockMvc.perform(post(ENDPOINT)
                        .header(X_REQUEST_ID, requestId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());

        // Only one record in database
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple notifications with different IDs all persist")
    void shouldAcceptMultipleDistinctNotifications() throws Exception {
        for (int i = 0; i < 5; i++) {
            UUID requestId = UUID.randomUUID();
            var request = new PushResourceStatusRequest(
                    "payment-multi-" + i, null, null, null, null, null,
                    null, null, null, null,
                    TransactionStatus.PDNG, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
            );

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        assertThat(repository.count()).isEqualTo(5);
        assertThat(testEventListener.getReceivedEvents()).hasSize(5);
    }

    @Test
    @DisplayName("Consent notification persists consent status")
    void shouldProcessConsentNotification() throws Exception {
        UUID requestId = UUID.randomUUID();
        var request = new PushResourceStatusRequest(
                null, "consent-int-001", null, null, null, null,
                null, null, null, null,
                null, ConsentStatus.valid, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post(ENDPOINT)
                        .header(X_REQUEST_ID, requestId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var saved = repository.findByRequestId(requestId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getConsentId()).isEqualTo("consent-int-001");
        assertThat(saved.get().getConsentStatus()).isEqualTo(ConsentStatus.valid);
    }

    /**
     * Test configuration to register the event listener bean.
     */
    @TestConfiguration
    static class TestEventConfig {
        @Bean
        TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }

    /**
     * Test event listener to capture published events during integration tests.
     */
    static class TestEventListener {

        private final CopyOnWriteArrayList<ResourceNotificationEvent> receivedEvents = new CopyOnWriteArrayList<>();

        @EventListener
        public void onNotification(ResourceNotificationEvent event) {
            receivedEvents.add(event);
        }

        public CopyOnWriteArrayList<ResourceNotificationEvent> getReceivedEvents() {
            return receivedEvents;
        }

        public void clear() {
            receivedEvents.clear();
        }
    }
}
