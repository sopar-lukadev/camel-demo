package com.endava.demo.connector.builder;

import com.endava.demo.connector.model.external.AbstractExternalRequest;
import com.endava.demo.connector.model.external.BookingCommit;
import com.endava.demo.connector.model.internal.BookParameter;
import com.endava.demo.connector.retry.RetryStrategy;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.endava.demo.connector.common.CamelUtils.*;


@Component
@Log4j2
public class BookCommitRequestBodyBuilder {
    @Handler
    public BookingCommit createBookingInitRequest(@ExchangeProperty(BOOK_PARAMETER) BookParameter bookParameter,
                                                  @ExchangeProperty(INIT_CONFIRMATION) Integer initConfirmation,
                                                  @ExchangeProperty(RETRY_ACTIONS) List<RetryStrategy<BookingCommit>> retryStrategies) {
        log.info("Starting booking commit body builder");
        BookingCommit bookingCommit = BookingCommit.builder()
                .bookingId(bookParameter.getInternalBookId())
                .confirmationId(initConfirmation)
                .build();
        for (RetryStrategy<BookingCommit> retryStrategy : retryStrategies) {
            retryStrategy.retryAction(bookingCommit);
        }
        return bookingCommit;
    }
}
