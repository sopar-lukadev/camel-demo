package com.endava.demo.connector.interpterer;

import com.endava.demo.connector.model.external.BookingResponse;
import com.endava.demo.connector.model.internal.BookParameter;
import com.endava.demo.connector.model.internal.BookResult;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import static com.endava.demo.connector.common.CamelUtils.BOOK_PARAMETER;
import static com.endava.demo.connector.common.CamelUtils.INIT_CONFIRMATION;

@Component
public class BookCommitResponseInterpreter implements Processor {
    @Override
    public void process(Exchange exchange) {
        BookingResponse bookingResponse = exchange.getMessage().getBody(BookingResponse.class);
        Integer bookConfirmation = exchange.getProperty(INIT_CONFIRMATION, Integer.class);
        BookParameter bookParameter = exchange.getProperty(BOOK_PARAMETER, BookParameter.class);

        BookResult bookResult = BookResult.builder()
                .bookParameter(bookParameter)
                .externalBookingId(bookConfirmation)
                .externalBookingPin(bookingResponse.getConfirmationNumber())
                .build();

        exchange.getMessage().setBody(bookResult);
    }
}
