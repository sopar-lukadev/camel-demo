package com.endava.demo.connector.interpterer;

import com.endava.demo.connector.model.external.BookingResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import static com.endava.demo.connector.common.CamelUtils.INIT_CONFIRMATION;

@Component
public class BookInitResponseInterpreter implements Processor {
    @Override
    public void process(Exchange exchange) {
        BookingResponse bookingResponse = exchange.getMessage().getBody(BookingResponse.class);
        exchange.setProperty(INIT_CONFIRMATION, bookingResponse.getConfirmationNumber());
    }
}
