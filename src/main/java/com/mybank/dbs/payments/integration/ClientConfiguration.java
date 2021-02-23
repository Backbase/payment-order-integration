package com.mybank.dbs.payments.integration;

import com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration;
import com.backbase.payments.integration.ApiClient;
import javax.validation.constraints.Pattern;
import org.openapitools.client.api.PaymentOrdersApi;
import org.openapitools.client.api.RetryFailedOrdersApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties("backbase.communication.services.dbs.payment-order-service")
public class ClientConfiguration {

    private String serviceId = "payment-order-service";

    @Value("${backbase.communication.http.default-scheme:http}")
    @Pattern(regexp = "https?")
    private String scheme;

    @Autowired
    @Qualifier("interServiceRestTemplate")
    private RestTemplate restTemplate;

    @Bean
    public RetryFailedOrdersApi createRetryFailedOrdersApi() {
        return new RetryFailedOrdersApi(createApiClient());
    }

    @Bean
    public PaymentOrdersApi createPaymentOrdersApi() {
        return new PaymentOrdersApi(createApiClient());
    }

    private ApiClient createApiClient() {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(String.format("%s://%s", scheme, serviceId));
        apiClient.addDefaultHeader(HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
        return apiClient;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

}
