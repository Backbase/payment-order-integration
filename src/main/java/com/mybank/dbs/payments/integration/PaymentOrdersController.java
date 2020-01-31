package com.mybank.dbs.payments.integration;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.integration.paymentorder.rest.spec.v2.paymentorders.CancelResponse;
import com.backbase.payments.integration.rest.spec.serviceapi.v2.paymentorders.PaymentOrdersApi;
import com.backbase.payments.integration.rest.spec.v2.paymentorders.PaymentOrdersPostRequestBody;
import com.backbase.payments.integration.rest.spec.v2.paymentorders.PaymentOrdersPostResponseBody;
import com.backbase.payments.integration.rest.spec.v2.paymentorders.PaymentOrdersPostResponseBody.Status;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentOrdersController implements PaymentOrdersApi {

    private Logger log = LoggerFactory.getLogger(PaymentOrdersController.class);

    private static String ROOT_ORDER_PATH = "/tmp/orders";
    private ObjectWriter objectMapper = new ObjectMapper().writer(new DefaultPrettyPrinter());

    @Override
    public PaymentOrdersPostResponseBody postPaymentOrders(
        @Valid PaymentOrdersPostRequestBody paymentOrdersPostRequestBody, HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse) throws BadRequestException, InternalServerErrorException {

        log.info("Received payment order {}", paymentOrdersPostRequestBody);
        try {
            String bankReferenceId = UUID.randomUUID().toString();
            objectMapper.writeValue(
                new File(ROOT_ORDER_PATH, bankReferenceId + ".json"), paymentOrdersPostRequestBody);
            return new PaymentOrdersPostResponseBody()
                .withBankReferenceId(bankReferenceId)
                .withStatus(Status.ACCEPTED)
                .withBankStatus("SAVED");
        } catch (Exception e) {
            log.error("Error saving file", e);
            throw new InternalServerErrorException().withMessage("Saving payment order failed");
        }
    }

    @Override
    public CancelResponse postCancelPaymentOrder(String bankReferenceId, HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse)
        throws BadRequestException, InternalServerErrorException, NotFoundException {

        return new CancelResponse().withAccepted(new File(ROOT_ORDER_PATH, bankReferenceId).delete());
    }
}
