package com.endava.demo.connector.interpterer;

import com.endava.demo.connector.model.internal.ErrorResult;
import com.endava.demo.connector.model.internal.ErrorType;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class BookErrorInterpreter {
    @Handler
    public ErrorResult handleError(@ExchangeProperty(Exchange.EXCEPTION_CAUGHT) Exception exception) {
        log.error("Caught internal error: ", exception);
        return ErrorResult.builder().errorType(ErrorType.BF_ERROR).build();
    }
}
