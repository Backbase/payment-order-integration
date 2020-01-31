package com.mybank.dbs.payments.integration;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.presentation.paymentorder.rest.spec.v2.paymentorders.RetryPaymentOrders;
import com.backbase.payments.presentation.listener.client.v2.paymentorders.PaymentsPresentationPaymentOrdersClient;
import com.backbase.payments.presentation.rest.spec.v2.paymentorders.UpdatePaymentOrderStatusPutRequestBody;
import com.backbase.payments.presentation.rest.spec.v2.paymentorders.UpdatePaymentOrderStatusPutRequestBody.Status;
import java.io.File;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExecutor {

    private static String ROOT_ORDER_PATH = "/tmp/orders";

    private static Logger logger = LoggerFactory.getLogger(OrderExecutor.class);

    private final PaymentsPresentationPaymentOrdersClient paymentsPresentationPaymentOrdersClient;

    OrderExecutor(PaymentsPresentationPaymentOrdersClient paymentsPresentationPaymentOrdersClient) {
        super();
        this.paymentsPresentationPaymentOrdersClient = paymentsPresentationPaymentOrdersClient;
    }

    @Scheduled(fixedRate = 10000)
    public void execute() {

        Arrays.stream(new File(ROOT_ORDER_PATH).listFiles(f -> f.isFile() && f.getName().endsWith(".json")))
            .forEach(f -> {
                String bankReferenceId = f.getName().replace(".json", "");
                UpdatePaymentOrderStatusPutRequestBody requestBody = new UpdatePaymentOrderStatusPutRequestBody()
                    .withBankReferenceId(bankReferenceId)
                    .withStatus(Status.PROCESSED)
                    .withBankStatus("DONE")
                    .withReasonText("Dev null processing");
                try {
                    paymentsPresentationPaymentOrdersClient.putUpdatePaymentOrderStatus(requestBody);
                    f.delete(); // All you order go to /dev/null
                } catch (BadRequestException e) {
                    logger.error("Error sending status update", e);
                    logger.error("Error sending status update: {}", e.getErrors());
                }
            }
         );
    }

    @Scheduled(fixedRate = 20000)
    public void callRetry() {
        paymentsPresentationPaymentOrdersClient.putRetryFailedOrders(new RetryPaymentOrders());
    }
}
