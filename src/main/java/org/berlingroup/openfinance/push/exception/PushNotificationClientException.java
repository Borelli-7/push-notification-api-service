package org.berlingroup.openfinance.push.exception;

/**
 * Thrown when the ASPSP sender client fails to push a notification to the API Client.
 */
public class PushNotificationClientException extends RuntimeException {

    private final int statusCode;

    public PushNotificationClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public PushNotificationClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
