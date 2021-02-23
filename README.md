Example code for payments integration service. Goes with (newer version of) https://community.backbase.com/documentation/DBS/latest/payments_implement_integration_service

# my-bank-payment-order-integration-service

Very naive implementation of a payment integration service. It receives payments from the Backbase payment-order-service and stores them on the file system in `/tmp/orders`. It processes these payments on a schedule to call back to payment-order-service to update the status before deleting the file. Functionality also includes calling the retry endpoint to receive payments that were submitted to the payment-order-service while the integration was down.

## Dependencies

Requires a running Eureka registry, by default on port 8080.

## Configuration

Service configuration is under `src/main/resources/application.yml`.

## Running

To run the service in development mode, use:
- `mvn spring-boot:run`

To run the service from the built binaries, use:
- `java -jar target/payments-integration-1.0.0-SNAPSHOT.war`

## Authorization

This service uses service-2-service authentication on its receiving endpoints. It assumes mTLS is *turned off* on payment-order-service.

## Todo

* Add mTLS security.
* Add confirmation resync call.