package org.berlingroup.openfinance.push;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;

/**
 * Spring Boot application entry point for the Berlin Group openFinance 
 * Resource Status Notification Service (v2.3).
 * <p>
 * OAuth2 Resource Server auto-configuration is excluded by default.
 * Enable it by removing the exclusion and providing a valid JWT issuer-uri
 * in application.yml for production use.
 * </p>
 */
@SpringBootApplication(exclude = OAuth2ResourceServerAutoConfiguration.class)
public class PushNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PushNotificationApplication.class, args);
    }
}
