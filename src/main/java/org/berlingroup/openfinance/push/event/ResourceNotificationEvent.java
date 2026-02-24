package org.berlingroup.openfinance.push.event;

import java.util.UUID;

import org.berlingroup.openfinance.push.dto.PushResourceStatusRequest;
import org.springframework.context.ApplicationEvent;

/**
 * Spring Application Event published when a Resource Status Notification 
 * is received and successfully persisted.
 * <p>
 * Downstream consumers in the parent project can listen for this event
 * to react to resource status changes.
 * </p>
 */
public class ResourceNotificationEvent extends ApplicationEvent {

    private final UUID requestId;
    private final PushResourceStatusRequest notification;

    public ResourceNotificationEvent(Object source, UUID requestId, PushResourceStatusRequest notification) {
        super(source);
        this.requestId = requestId;
        this.notification = notification;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public PushResourceStatusRequest getNotification() {
        return notification;
    }
}
