package com.mybank.dbs.payments.integration;

import com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration;
import com.backbase.payments.integration.inbound.ApiClient;
import com.backbase.payments.integration.inbound.api.PaymentOrdersApi;
import com.backbase.payments.integration.inbound.api.RetryFailedOrdersApi;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Getter
@Setter
@Configuration
@ConfigurationProperties("backbase.communication.services.dbs.payment-order-service")
public class ClientConfiguration {

    private String serviceId = "payment-order-service";

    @Value("${backbase.communication.http.default-scheme:http}")
    @Pattern(regexp = "https?")
    private String scheme;

    @Bean
    public RetryFailedOrdersApi createRetryFailedOrdersApi(
        @Qualifier("interServiceRestTemplate") RestTemplate restTemplate) {
        return new RetryFailedOrdersApi(createApiClient(restTemplate));
    }

    @Bean
    public PaymentOrdersApi createPaymentOrdersApi(
        @Qualifier("interServiceRestTemplate") RestTemplate restTemplate) {
        return new PaymentOrdersApi(createApiClient(restTemplate));
    }

    private ApiClient createApiClient(RestTemplate restTemplate) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(String.format("%s://%s", scheme, serviceId));
        apiClient.addDefaultHeader(HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
        return apiClient;
    }

}
