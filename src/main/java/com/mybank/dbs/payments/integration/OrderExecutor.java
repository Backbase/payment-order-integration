package com.mybank.dbs.payments.integration;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.payments.integration.inbound.api.PaymentOrdersApi;
import com.backbase.payments.integration.inbound.api.RetryFailedOrdersApi;
import com.backbase.payments.integration.model.PaymentOrdersPutRequestBody;
import com.backbase.payments.integration.model.RetryPaymentOrdersRequest;
import java.io.File;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExecutor {

    private static final String JSON_EXTENSION = ".json";
    private static String ROOT_ORDER_PATH = "/tmp/orders";

    private static Logger logger = LoggerFactory.getLogger(OrderExecutor.class);

    private final PaymentOrdersApi paymentOrdersApi;
    private final RetryFailedOrdersApi retryFailedOrdersApi;

    OrderExecutor(PaymentOrdersApi paymentOrdersApi, RetryFailedOrdersApi retryFailedOrdersApi) {
        super();
        this.paymentOrdersApi = paymentOrdersApi;
        this.retryFailedOrdersApi = retryFailedOrdersApi;
    }

    @Scheduled(fixedRate = 10000)
    public void execute() {

        Arrays.stream(new File(ROOT_ORDER_PATH).listFiles(f -> f.isFile() && f.getName().endsWith(JSON_EXTENSION)))
            .forEach(f -> {
                String bankReferenceId = f.getName().replace(JSON_EXTENSION, "");
                PaymentOrdersPutRequestBody requestBody = new PaymentOrdersPutRequestBody()
                    .bankReferenceId(bankReferenceId)
                    .bankStatus("DONE")
                    .reasonText("Dev null processing");
                try {
                    paymentOrdersApi.putPaymentOrders(requestBody);
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
        retryFailedOrdersApi.putRetryFailedOrders(new RetryPaymentOrdersRequest());
    }
}
