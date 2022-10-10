package com.mybank.dbs.payments.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.buildingblocks.test.http.TestRestTemplateConfiguration;
import com.backbase.payments.integration.inbound.api.PaymentOrdersApi;
import com.backbase.payments.integration.model.CancelResponse;
import com.backbase.payments.integration.model.PaymentOrdersPostRequestBody;
import com.backbase.payments.integration.model.PaymentOrdersPostResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@DirtiesContext
@WebAppConfiguration
@SpringBootTest(classes = {Application.class, TestRestTemplateConfiguration.class})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("it")
public class PaymentOrdersControllerIT {

    public static final String BASE_PATH = "/service-api/v2/payment-orders";
    public static final String TEST_JWT =
        "Bearer eyJhbGciOiJIUzI1NiJ9."
            + "eyJzdWIiOiJteS1zZXJ2aWNlIiwic2NvcGUiOlsiYXBpOnNlcnZpY2UiXSwiZXhwIjoyMTQ3NDgzNjQ3LCJpYXQiOjE0ODQ4MjAxOTZ9."
            + "G13i2kk5zKSJws2TXfmxBxefBywArcqWUj6jOgYaUcU";
    @MockBean
    private PaymentOrdersApi paymentOrdersApi;
    @MockBean
    private OrderExecutor orderExecutor;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mvc;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders
            .webAppContextSetup(wac)
            .apply(springSecurity())
            .build();
    }

    @Test
    public void canSubmitPaymentToCore() throws Exception {
        PaymentOrdersPostRequestBody payment = new PaymentOrdersPostRequestBody()
            .id(UUID.randomUUID().toString())
            .requestedExecutionDate(LocalDate.now());

        PaymentOrdersPostResponseBody response = submitPayment(payment);

        assertThat(response.getBankReferenceId(), notNullValue());
        assertThat(response.getBankStatus(), equalTo("ACCEPTED"));
    }

    @Test
    public void canCancelPayment() throws Exception {
        PaymentOrdersPostRequestBody payment = new PaymentOrdersPostRequestBody()
            .id(UUID.randomUUID().toString())
            .requestedExecutionDate(LocalDate.now());
        PaymentOrdersPostResponseBody submittedPayment = submitPayment(payment);

        CancelResponse response = cancelPayment(submittedPayment.getBankReferenceId());

        assertThat(response.getAccepted(), equalTo(true));
        verify(paymentOrdersApi).putPaymentOrders(any());
    }

    private PaymentOrdersPostResponseBody submitPayment(PaymentOrdersPostRequestBody payment) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(BASE_PATH)
            .header(AUTHORIZATION, TEST_JWT)
            .content(mapper.writeValueAsBytes(payment))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8);
        ResultActions result = mvc.perform(requestBuilder).andDo(print());

        String resultString = result
            .andExpect(status().isAccepted())
            .andReturn().getResponse().getContentAsString();

        return mapper.readValue(resultString, PaymentOrdersPostResponseBody.class);
    }

    private CancelResponse cancelPayment(String bankReferenceId) throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
            post(BASE_PATH + "/{bankReferenceId}/cancel", bankReferenceId)
                .header(AUTHORIZATION, TEST_JWT)
                .contentType(APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);
        ResultActions result = mvc.perform(requestBuilder).andDo(print());

        String resultString = result
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        return mapper.readValue(resultString, CancelResponse.class);
    }
}
