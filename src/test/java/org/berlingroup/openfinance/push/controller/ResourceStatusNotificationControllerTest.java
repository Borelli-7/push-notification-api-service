package org.berlingroup.openfinance.push.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.berlingroup.openfinance.push.config.SecurityConfig;
import org.berlingroup.openfinance.push.dto.Amount;
import org.berlingroup.openfinance.push.dto.HrefType;
import org.berlingroup.openfinance.push.dto.PushResourceStatusRequest;
import org.berlingroup.openfinance.push.exception.DuplicateRequestException;
import org.berlingroup.openfinance.push.exception.GlobalExceptionHandler;
import org.berlingroup.openfinance.push.exception.InvalidNotificationException;
import org.berlingroup.openfinance.push.model.enums.*;
import org.berlingroup.openfinance.push.service.ResourceStatusNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ResourceStatusNotificationController}.
 * Uses @WebMvcTest for a focused controller slice test.
 */
@WebMvcTest(ResourceStatusNotificationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ResourceStatusNotificationControllerTest {

    private static final String ENDPOINT = "/Client-Notification-URL";
    private static final String X_REQUEST_ID = "X-Request-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResourceStatusNotificationService notificationService;

    // --- Helper methods ---

    private PushResourceStatusRequest createValidPaymentNotification() {
        return new PushResourceStatusRequest(
                "payment-123", null, null, null, null, null,
                null, null, null, null,
                TransactionStatus.ACCP, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
    }

    private PushResourceStatusRequest createFullNotification() {
        return new PushResourceStatusRequest(
                "payment-456", "consent-789", "sub-001", "basket-002",
                UUID.randomUUID(), UUID.randomUUID(),
                "entry-01", "sub-entry-01", "auth-01", "cancel-01",
                TransactionStatus.ACSC, ConsentStatus.valid,
                SubscriptionStatus.valid, SubscriptionEntryStatus.valid,
                MandateStatus.ACTV, DocumentStatus.accessible,
                SCAStatus.finalised, RequestStatus.ACCP,
                StatusReasonCode.AM04, "proprietary-reason",
                "2026-02-24T10:00:00Z",
                new Amount("EUR", "100.50"),
                "2026-02-25T10:00:00Z", "SCT", "status-id-001",
                Map.of("status", new HrefType("https://api.example.com/status"))
        );
    }

    private PushResourceStatusRequest createNoResourceIdNotification() {
        return new PushResourceStatusRequest(
                null, null, null, null, null, null,
                null, null, null, null,
                TransactionStatus.ACCP, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
    }

    // --- Tests ---

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("POST with valid payment notification returns 200 with X-Request-ID header")
        void shouldReturn200ForValidPaymentNotification() throws Exception {
            UUID requestId = UUID.randomUUID();
            PushResourceStatusRequest request = createValidPaymentNotification();

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(X_REQUEST_ID, requestId.toString()));

            verify(notificationService).processNotification(eq(requestId), any(PushResourceStatusRequest.class));
        }

        @Test
        @DisplayName("POST with full notification body returns 200")
        void shouldReturn200ForFullNotification() throws Exception {
            UUID requestId = UUID.randomUUID();
            PushResourceStatusRequest request = createFullNotification();

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(X_REQUEST_ID, requestId.toString()));

            verify(notificationService).processNotification(eq(requestId), any(PushResourceStatusRequest.class));
        }

        @Test
        @DisplayName("POST with optional headers (Digest, x-jws-signature) returns 200")
        void shouldReturn200WithOptionalHeaders() throws Exception {
            UUID requestId = UUID.randomUUID();
            PushResourceStatusRequest request = createValidPaymentNotification();

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .header("Digest", "SHA-256=abc123")
                            .header("x-jws-signature", "eyJhbGciOi...")
                            .header("Body-Sig-Profile", "JAdES_JS")
                            .header("Body-Enc-Profile", "JWE_CS")
                            .header("Body-Enc-List", "BODY")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Error Response Tests")
    class ErrorResponseTests {

        @Test
        @DisplayName("POST without X-Request-ID returns 400")
        void shouldReturn400WhenMissingRequestId() throws Exception {
            PushResourceStatusRequest request = createValidPaymentNotification();

            mockMvc.perform(post(ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST with invalid X-Request-ID (not UUID) returns 400")
        void shouldReturn400ForInvalidRequestId() throws Exception {
            PushResourceStatusRequest request = createValidPaymentNotification();

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, "not-a-valid-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST with no resource identifier returns 400")
        void shouldReturn400WhenNoResourceIdentifier() throws Exception {
            UUID requestId = UUID.randomUUID();
            PushResourceStatusRequest request = createNoResourceIdNotification();

            doThrow(new InvalidNotificationException("At least one resource identifier must be present"))
                    .when(notificationService).processNotification(eq(requestId), any());

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string(X_REQUEST_ID, requestId.toString()));
        }

        @Test
        @DisplayName("POST with duplicate X-Request-ID returns 409")
        void shouldReturn409ForDuplicateRequestId() throws Exception {
            UUID requestId = UUID.randomUUID();
            PushResourceStatusRequest request = createValidPaymentNotification();

            doThrow(new DuplicateRequestException(requestId.toString()))
                    .when(notificationService).processNotification(eq(requestId), any());

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(header().string(X_REQUEST_ID, requestId.toString()));
        }

        @Test
        @DisplayName("POST with empty body returns error status")
        void shouldReturnErrorForEmptyBody() throws Exception {
            UUID requestId = UUID.randomUUID();

            var result = mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andReturn();

            int status = result.getResponse().getStatus();
            assertThat(status).isGreaterThanOrEqualTo(400);
        }

        @Test
        @DisplayName("GET on POST-only endpoint returns 405")
        void shouldReturn405ForGetMethod() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .header(X_REQUEST_ID, UUID.randomUUID().toString()))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("POST with unsupported media type returns 415")
        void shouldReturn415ForUnsupportedMediaType() throws Exception {
            UUID requestId = UUID.randomUUID();

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_XML)
                            .content("<notification/>"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("Service Interaction Tests")
    class ServiceInteractionTests {

        @Test
        @DisplayName("Service is called exactly once for a valid request")
        void shouldCallServiceExactlyOnce() throws Exception {
            UUID requestId = UUID.randomUUID();
            PushResourceStatusRequest request = createValidPaymentNotification();

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(notificationService, times(1)).processNotification(eq(requestId), any());
        }

        @Test
        @DisplayName("Service exception results in 500")
        void shouldReturn500WhenServiceThrowsUnexpectedException() throws Exception {
            UUID requestId = UUID.randomUUID();
            PushResourceStatusRequest request = createValidPaymentNotification();

            doThrow(new RuntimeException("Unexpected error"))
                    .when(notificationService).processNotification(eq(requestId), any());

            mockMvc.perform(post(ENDPOINT)
                            .header(X_REQUEST_ID, requestId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }
}
