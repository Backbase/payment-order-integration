package com.mybank.dbs.payments.integration;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.payments.integration.model.CancelResponse;
import com.backbase.payments.integration.model.PaymentOrdersPostRequestBody;
import com.backbase.payments.integration.model.PaymentOrdersPostResponseBody;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import org.openapitools.api.PaymentOrderIntegrationOutboundApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public class PaymentOrdersController implements PaymentOrderIntegrationOutboundApi {

    private static final String JSON_EXTENSION = ".json";
    private static String ROOT_ORDER_PATH = "/tmp/orders";

    private Logger log = LoggerFactory.getLogger(PaymentOrdersController.class);

    private ObjectWriter objectMapper = new ObjectMapper().writer(new DefaultPrettyPrinter());

    @Override
    public ResponseEntity<PaymentOrdersPostResponseBody> postPaymentOrders(
        @Valid PaymentOrdersPostRequestBody paymentOrdersPostRequestBody) {
        log.info("Received payment order {}", paymentOrdersPostRequestBody);
        try {
            String bankReferenceId = UUID.randomUUID().toString();
            objectMapper.writeValue(
                new File(ROOT_ORDER_PATH, makePaymentOrderFileName(bankReferenceId)), paymentOrdersPostRequestBody);
            return ResponseEntity.accepted().body(new PaymentOrdersPostResponseBody()
                .bankReferenceId(bankReferenceId)
                .bankStatus("ACCEPTED"));
        } catch (Exception e) {
            log.error("Error saving file", e);
            throw new InternalServerErrorException().withMessage("Saving payment order failed");
        }
    }

    @Override
    public ResponseEntity<CancelResponse> postCancelPaymentOrder(@Size(max = 64) String bankReferenceId) {
        return ResponseEntity.ok(new CancelResponse()
            .accepted(new File(ROOT_ORDER_PATH, makePaymentOrderFileName(bankReferenceId)).delete()));
    }

    private String makePaymentOrderFileName(String bankReferenceId) {
        return bankReferenceId.concat(JSON_EXTENSION);
    }
}
