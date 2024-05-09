package com.endava.demo.connector.builder;

import com.endava.demo.connector.model.external.BookingInit;
import com.endava.demo.connector.model.internal.BookParameter;
import com.endava.demo.connector.retry.RetryStrategy;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.endava.demo.connector.common.CamelUtils.BOOK_PARAMETER;
import static com.endava.demo.connector.common.CamelUtils.RETRY_ACTIONS;

@Component
@Log4j2
public class BookInitRequestBodyBuilder {
    @Handler
    public BookingInit createBookingInitRequest(@ExchangeProperty(BOOK_PARAMETER) BookParameter bookParameter,
                                                @ExchangeProperty(RETRY_ACTIONS) List<RetryStrategy<BookingInit>> retryStrategies) {
        log.info("Starting booking init body builder");
        BookingInit bookingInit = BookingInit.builder()
                .hotelId(bookParameter.getHotel().getHotelId())
                .roomCode(bookParameter.getRoomCode())
                .rateCode(bookParameter.getRateCode())
                .wishes(bookParameter.getWishes())
                .build();
        for (RetryStrategy<BookingInit> retryStrategy : retryStrategies) {
            retryStrategy.retryAction(bookingInit);
        }
        return bookingInit;
    }
}
