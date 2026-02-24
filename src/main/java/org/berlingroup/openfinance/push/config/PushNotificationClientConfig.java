package org.berlingroup.openfinance.push.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

/**
 * Configuration for the push notification REST client (ASPSP sender side).
 * Uses Java 21's built-in {@link HttpClient} for optimal virtual thread integration.
 */
@Configuration
public class PushNotificationClientConfig {

    @Value("${push-notification.client.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${push-notification.client.read-timeout-ms:10000}")
    private int readTimeoutMs;

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> {
            var httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                    .build();

            var requestFactory = new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

            builder.requestFactory(requestFactory);
        };
    }
}
