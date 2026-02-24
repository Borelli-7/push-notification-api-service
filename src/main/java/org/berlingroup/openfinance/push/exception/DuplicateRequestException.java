package org.berlingroup.openfinance.push.exception;

/**
 * Thrown when a duplicate X-Request-ID is detected (idempotency violation).
 */
public class DuplicateRequestException extends RuntimeException {

    private final String requestId;

    public DuplicateRequestException(String requestId) {
        super("Duplicate request detected with X-Request-ID: " + requestId);
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
