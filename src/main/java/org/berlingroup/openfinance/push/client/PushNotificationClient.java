package org.berlingroup.openfinance.push.client;

import org.berlingroup.openfinance.push.dto.PushResourceStatusRequest;
import org.berlingroup.openfinance.push.exception.PushNotificationClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

/**
 * ASPSP-side HTTP client that pushes resource status notifications to the API Client.
 * <p>
 * Uses Spring Boot 3.2+ {@link RestClient} — the modern, synchronous HTTP client
 * that works seamlessly with Java 21 virtual threads.
 * </p>
 */
@Component
public class PushNotificationClient {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationClient.class);

    private final RestClient restClient;

    public PushNotificationClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * Push a resource status notification to the API Client's notification URL.
     *
     * @param clientNotificationUrl the full URL of the API Client's notification endpoint
     * @param request               the notification payload
     * @param additionalHeaders     optional additional headers (Digest, x-jws-signature, etc.)
     * @return the X-Request-ID used for this push
     * @throws PushNotificationClientException if the push fails
     */
    public UUID pushResourceStatus(String clientNotificationUrl,
                                    PushResourceStatusRequest request,
                                    Map<String, String> additionalHeaders) {
        UUID xRequestId = UUID.randomUUID();

        log.info("Pushing resource status notification to {} with X-Request-ID: {}",
                clientNotificationUrl, xRequestId);

        try {
            var requestSpec = restClient.post()
                    .uri(clientNotificationUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-ID", xRequestId.toString());

            // Add any additional headers (Digest, x-jws-signature, Body-Sig-Profile, etc.)
            if (additionalHeaders != null) {
                additionalHeaders.forEach(requestSpec::header);
            }

            requestSpec.body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, response) -> {
                        throw new PushNotificationClientException(
                                "Push notification failed with status: " + response.getStatusCode(),
                                response.getStatusCode().value());
                    })
                    .toBodilessEntity();

            log.info("Successfully pushed notification with X-Request-ID: {}", xRequestId);
            return xRequestId;

        } catch (PushNotificationClientException e) {
            throw e;
        } catch (Exception e) {
            throw new PushNotificationClientException(
                    "Failed to push notification to " + clientNotificationUrl, e);
        }
    }

    /**
     * Push a resource status notification with only the required X-Request-ID header.
     *
     * @param clientNotificationUrl the full URL of the API Client's notification endpoint
     * @param request               the notification payload
     * @return the X-Request-ID used for this push
     */
    public UUID pushResourceStatus(String clientNotificationUrl,
                                    PushResourceStatusRequest request) {
        return pushResourceStatus(clientNotificationUrl, request, Map.of());
    }
}
