package org.berlingroup.openfinance.push.exception;

/**
 * Thrown when the push notification request body is invalid
 * (e.g., no resource identifier present).
 */
public class InvalidNotificationException extends RuntimeException {

    public InvalidNotificationException(String message) {
        super(message);
    }
}
