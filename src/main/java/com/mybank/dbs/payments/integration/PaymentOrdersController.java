package com.mybank.dbs.payments.integration;

import static com.mybank.dbs.payments.integration.ApplicationConfiguration.JSON_EXTENSION;
import static com.mybank.dbs.payments.integration.ApplicationConfiguration.ROOT_ORDER_FOLDER;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.payments.integration.inbound.api.PaymentOrdersApi;
import com.backbase.payments.integration.model.CancelResponse;
import com.backbase.payments.integration.model.PaymentOrderPutRequestBody;
import com.backbase.payments.integration.model.PaymentOrderPutResponseBody;
import com.backbase.payments.integration.model.PaymentOrdersPostRequestBody;
import com.backbase.payments.integration.model.PaymentOrdersPostResponseBody;
import com.backbase.payments.integration.model.PaymentOrdersPutRequestBody;
import com.backbase.payments.integration.outbound.api.PaymentOrderIntegrationOutboundApi;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class PaymentOrdersController implements PaymentOrderIntegrationOutboundApi {

    private final ObjectWriter objectWriter;
    private final PaymentOrdersApi paymentOrdersApi;

    @Override
    public ResponseEntity<PaymentOrdersPostResponseBody> postPaymentOrders(
        @Valid PaymentOrdersPostRequestBody paymentOrdersPostRequestBody) {
        log.info("Initiate payment order {}", paymentOrdersPostRequestBody);
        try {
            String bankReferenceId = UUID.randomUUID().toString();
            objectWriter.writeValue(
                new File(ROOT_ORDER_FOLDER, makePaymentOrderFileName(bankReferenceId)), paymentOrdersPostRequestBody);
            return ResponseEntity.accepted().body(new PaymentOrdersPostResponseBody()
                .bankReferenceId(bankReferenceId)
                .bankStatus("ACCEPTED"));
        } catch (Exception e) {
            log.error("Error saving file", e);
            throw new InternalServerErrorException().withMessage("Saving payment order failed");
        }
    }

    @Override
    public ResponseEntity<PaymentOrderPutResponseBody> putPaymentOrder(String bankReferenceId,
        PaymentOrderPutRequestBody paymentOrderPutRequestBody) {
        log.info("Update payment order {}", paymentOrderPutRequestBody);
        try {
            objectWriter.writeValue(
                new File(ROOT_ORDER_FOLDER, makePaymentOrderFileName(bankReferenceId)), paymentOrderPutRequestBody);
            return ResponseEntity.ok((PaymentOrderPutResponseBody)
                new PaymentOrderPutResponseBody()
                    .bankReferenceId(bankReferenceId)
                    .bankStatus("ACCEPTED"));
        } catch (Exception e) {
            log.error("Error saving file", e);
            throw new InternalServerErrorException().withMessage("Saving payment order failed");
        }
    }

    @Override
    public ResponseEntity<CancelResponse> postCancelPaymentOrder(@Size(max = 64) String bankReferenceId) {
        CancelResponse response = new CancelResponse()
            .accepted(new File(ROOT_ORDER_FOLDER, makePaymentOrderFileName(bankReferenceId)).delete());
        paymentOrdersApi.putPaymentOrders(new PaymentOrdersPutRequestBody()
            .bankReferenceId(bankReferenceId)
            .bankStatus("CANCELLED"));
        return ResponseEntity.ok(response);
    }

    private String makePaymentOrderFileName(String bankReferenceId) {
        return bankReferenceId.concat(JSON_EXTENSION);
    }
}
