package com.mybank.dbs.payments.integration;

import static com.mybank.dbs.payments.integration.ApplicationConfiguration.JSON_EXTENSION;
import static com.mybank.dbs.payments.integration.ApplicationConfiguration.ROOT_ORDER_FOLDER;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.payments.integration.inbound.api.PaymentOrdersApi;
import com.backbase.payments.integration.inbound.api.RetryFailedOrdersApi;
import com.backbase.payments.integration.model.PaymentOrdersPutRequestBody;
import com.backbase.payments.integration.model.RetryPaymentOrdersRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderExecutor {

    private final PaymentOrdersApi paymentOrdersApi;
    private final RetryFailedOrdersApi retryFailedOrdersApi;

    @Scheduled(fixedRate = 10000)
    public void execute() {
        Arrays.stream(ROOT_ORDER_FOLDER
                .listFiles(f -> f.isFile() && f.getName().endsWith(JSON_EXTENSION)))
            .forEach(f -> {
                String bankReferenceId = f.getName().replace(JSON_EXTENSION, "");
                PaymentOrdersPutRequestBody requestBody = new PaymentOrdersPutRequestBody()
                    .bankReferenceId(bankReferenceId)
                    .bankStatus("PROCESSED")
                    .reasonText("Dev null processing");
                try {
                    paymentOrdersApi.putPaymentOrders(requestBody);
                    f.delete(); // All you order go to /dev/null
                } catch (BadRequestException e) {
                    log.error("Error sending status update", e);
                }
            }
         );
    }

    @Scheduled(fixedRate = 20000)
    public void callRetry() {
        retryFailedOrdersApi.putRetryFailedOrders(new RetryPaymentOrdersRequest());
    }
}
