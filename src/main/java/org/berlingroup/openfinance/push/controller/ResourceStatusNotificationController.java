package org.berlingroup.openfinance.push.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.berlingroup.openfinance.push.dto.PushResourceStatusRequest;
import org.berlingroup.openfinance.push.service.ResourceStatusNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller implementing the API Client (receiver) side of the 
 * Berlin Group openFinance Resource Status Notification API.
 * <p>
 * Endpoint: POST /Client-Notification-URL
 * </p>
 */
@RestController
@Tag(name = "Resource Status Notification",
        description = "The Resource Status Notification API offers notification messages on resource status changes by the ASPSP")
public class ResourceStatusNotificationController {

    private static final Logger log = LoggerFactory.getLogger(ResourceStatusNotificationController.class);

    private final ResourceStatusNotificationService notificationService;

    public ResourceStatusNotificationController(ResourceStatusNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Receives a push resource status notification from the ASPSP.
     *
     * @param xRequestId         unique ID of the request (required UUID header)
     * @param digest             hash of the message body (optional)
     * @param xJwsSignature      JSON Web Signature (optional)
     * @param bodySigProfile     signature profile used for signing (optional)
     * @param bodyEncProfile     encryption profile (optional)
     * @param bodyEncList        list of encrypted body elements (optional)
     * @param request            the push resource status notification body
     * @return 200 OK with X-Request-ID header
     */
    @PostMapping(value = "/Client-Notification-URL", consumes = "application/json")
    @Operation(
            summary = "Push Resource Status with JSON encoding",
            description = "Creates a Resource Notification on the API Client server.",
            operationId = "pushResourceStatus",
            security = {
                    @SecurityRequirement(name = ""),
                    @SecurityRequirement(name = "BearerAuthOAuth")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok",
                    headers = @Header(name = "X-Request-ID", description = "ID of the request")),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorised"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "405", description = "Method not allowed"),
            @ApiResponse(responseCode = "406", description = "Not Acceptable"),
            @ApiResponse(responseCode = "408", description = "Request Timeout"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "503", description = "Service Unavailable")
    })
    public ResponseEntity<Void> pushResourceStatus(
            @Parameter(description = "ID of the request, unique to the call", required = true, example = "99391c7e-ad88-49ec-a2ad-99ddcb1f7721")
            @RequestHeader("X-Request-ID") UUID xRequestId,

            @Parameter(description = "Hash of the message body")
            @RequestHeader(value = "Digest", required = false) String digest,

            @Parameter(description = "JSON Web Signature")
            @RequestHeader(value = "x-jws-signature", required = false) String xJwsSignature,

            @Parameter(description = "Signature profile used for signing the body")
            @RequestHeader(value = "Body-Sig-Profile", required = false) String bodySigProfile,

            @Parameter(description = "Encryption profile used for encrypting the body")
            @RequestHeader(value = "Body-Enc-Profile", required = false) String bodyEncProfile,

            @Parameter(description = "List of encrypted body elements")
            @RequestHeader(value = "Body-Enc-List", required = false) String bodyEncList,

            @Valid @RequestBody PushResourceStatusRequest request
    ) {
        log.info("Received push resource status notification with X-Request-ID: {}", xRequestId);

        notificationService.processNotification(xRequestId, request);

        return ResponseEntity.ok()
                .header("X-Request-ID", xRequestId.toString())
                .build();
    }
}
