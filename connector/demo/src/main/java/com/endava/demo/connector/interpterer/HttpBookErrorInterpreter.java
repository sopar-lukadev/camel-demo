package com.endava.demo.connector.interpterer;

import com.endava.demo.connector.model.external.ErrorResponse;
import com.endava.demo.connector.model.internal.ErrorResult;
import com.endava.demo.connector.model.internal.ErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class HttpBookErrorInterpreter {

    private final ObjectMapper objectMapper;

    @Autowired
    public HttpBookErrorInterpreter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Handler
    public ErrorResult handleError(@ExchangeProperty(Exchange.EXCEPTION_CAUGHT) HttpOperationFailedException exception) {
        try {
            return ErrorResult.builder().errorType(mapErrorType(exception.getStatusCode(), objectMapper.readValue(exception.getResponseBody(), ErrorResponse.class))).build();
        } catch (Exception ex) {
            log.error("Caught exception while handling http exception:", ex);
            return ErrorResult.builder().errorType(ErrorType.BF_ERROR).build();
        }
    }

    private ErrorType mapErrorType(int statusCode, ErrorResponse errorResponse) {
        if (statusCode == 500) {
            if (errorResponse == null || errorResponse.getMessage() == null || errorResponse.getStatus() == null) {
                log.error("Error response does not contain data for mapping");
                return ErrorType.BF_ERROR;
            }
            log.error("Error response retrieved from thirdParty with message: " + errorResponse.getMessage());
            return switch (errorResponse.getStatus()) {
                case 5874:
                    yield ErrorType.BF_SOLD_OUT;
                case 4003:
                    yield ErrorType.BF_LONG_WISHES;
                case 6043:
                    yield ErrorType.BF_NOT_DONE;
                default:
                    yield ErrorType.BF_ERROR;
            };
        } else if (statusCode == 504) {
            return ErrorType.BF_CONNECTION_TIMEOUT;
        }
        return ErrorType.BF_ERROR;

    }
}
