package com.endava.demo.connector.interpterer;

import com.endava.demo.connector.model.internal.ErrorResult;
import com.endava.demo.connector.model.internal.ErrorType;
import com.endava.demo.connector.retry.RetryWishesAction;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;

import static com.endava.demo.connector.common.CamelUtils.*;

@Component
@Log4j2
public class BookRetryProcessor implements Processor {

    private final HttpBookErrorInterpreter httpBookErrorInterpreter;
    private final Clock clock;

    private static final int MAX_REDELIVERY_TIME = 10000;
    @Autowired
    public BookRetryProcessor(HttpBookErrorInterpreter httpBookErrorInterpreter, Clock clock) {
        this.httpBookErrorInterpreter = httpBookErrorInterpreter;
        this.clock = clock;
    }

    public boolean isErrorForRetry(Exchange exchange) {
        HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);

        //e.g. we could measure time to see if the retry will happen or not.
        long elapsedTime = getElapsedTime(exchange);
        if (elapsedTime > MAX_REDELIVERY_TIME) {
            log.info("Retry attempt will not happen, not enough time for booking.");
            return false;
        }

        ErrorResult errorResult = httpBookErrorInterpreter.handleError(exception);

        //Check if we want to retry based on the error:
        if (shouldRetryWithoutChangingMessage(errorResult.getErrorType()) || shouldRetryChangeMessage(errorResult.getErrorType())) {
            log.info("Marking error for retry.");
            exchange.setProperty(ERROR_RESULT, errorResult);
            return true;
        }
        return false;
    }

    @Override
    public void process(Exchange exchange) {
        log.debug("Starting retry processor");

        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        if (exception == null) {
            log.warn("Exception in retry processor is null");
            return;
        }

        ErrorResult errorResult = exchange.getProperty(ERROR_RESULT, ErrorResult.class);
        if (errorResult == null) {
            log.warn("Error result is null");
            return;
        }

        //Check if we want to retry based on the error:
        if (shouldRetryChangeMessage(errorResult.getErrorType())) {
            log.info("Retry with changing message.");
            addRetryAction(errorResult.getErrorType(), exchange);
        } else if (shouldRetryWithoutChangingMessage(errorResult.getErrorType())) {
            log.info("Retry without changing message.");
        } else {
            log.info("No need for retry for error: " + errorResult.getErrorType());
        }
    }

    private void addRetryAction(ErrorType errorType, Exchange exchange) {
        if (errorType == ErrorType.BF_LONG_WISHES) {
            exchange.getProperty(RETRY_ACTIONS, ArrayList.class).add(new RetryWishesAction());
        }
    }

    private boolean shouldRetryChangeMessage(ErrorType errorType) {
        return errorType == ErrorType.BF_LONG_WISHES;
    }

    private boolean shouldRetryWithoutChangingMessage(ErrorType errorType) {
        return errorType == ErrorType.BF_NOT_DONE;
    }

    protected long getElapsedTime(Exchange exchange) {
        long startTime = (long) exchange.getProperty(REQUEST_START_TIME);
        return clock.millis() - startTime;
    }
}
