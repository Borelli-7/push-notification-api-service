package org.berlingroup.openfinance.push.client;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.berlingroup.openfinance.push.dto.PushResourceStatusRequest;
import org.berlingroup.openfinance.push.exception.PushNotificationClientException;
import org.berlingroup.openfinance.push.model.enums.TransactionStatus;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link PushNotificationClient} using MockRestServiceServer.
 */
class PushNotificationClientTest {

    private PushNotificationClient client;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String NOTIFICATION_URL = "https://api.client.com/psd2/Client-Notification-URL";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new PushNotificationClient(builder);
    }

    private PushResourceStatusRequest createRequest() {
        return new PushResourceStatusRequest(
                "payment-001", null, null, null, null, null,
                null, null, null, null,
                TransactionStatus.ACCP, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("Successful push returns X-Request-ID")
    void shouldPushSuccessfully() {
        mockServer.expect(requestTo(NOTIFICATION_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header("X-Request-ID", notNullValue()))
                .andRespond(withSuccess());

        UUID requestId = client.pushResourceStatus(NOTIFICATION_URL, createRequest());

        assertThat(requestId).isNotNull();
        mockServer.verify();
    }

    @Test
    @DisplayName("Push with additional headers includes all headers")
    void shouldIncludeAdditionalHeaders() {
        mockServer.expect(requestTo(NOTIFICATION_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Digest", "SHA-256=abc"))
                .andExpect(header("x-jws-signature", "eyJhbGci"))
                .andRespond(withSuccess());

        Map<String, String> headers = Map.of(
                "Digest", "SHA-256=abc",
                "x-jws-signature", "eyJhbGci"
        );

        client.pushResourceStatus(NOTIFICATION_URL, createRequest(), headers);
        mockServer.verify();
    }

    @Test
    @DisplayName("Push to unavailable server throws PushNotificationClientException")
    void shouldThrowOnServerError() {
        mockServer.expect(requestTo(NOTIFICATION_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.pushResourceStatus(NOTIFICATION_URL, createRequest()))
                .isInstanceOf(PushNotificationClientException.class);

        mockServer.verify();
    }

    @Test
    @DisplayName("Push returns 400 throws PushNotificationClientException")
    void shouldThrowOnBadRequest() {
        mockServer.expect(requestTo(NOTIFICATION_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        assertThatThrownBy(() -> client.pushResourceStatus(NOTIFICATION_URL, createRequest()))
                .isInstanceOf(PushNotificationClientException.class);

        mockServer.verify();
    }
}
