package com.endava.demo.connector.routes;

import com.endava.demo.connector.builder.BookCommitRequestBodyBuilder;
import com.endava.demo.connector.builder.BookCommitRequestHeaderBuilder;
import com.endava.demo.connector.builder.BookInitRequestBodyBuilder;
import com.endava.demo.connector.builder.BookInitRequestHeaderBuilder;
import com.endava.demo.connector.interpterer.*;
import com.endava.demo.connector.model.external.BookingCommit;
import com.endava.demo.connector.model.external.BookingInit;
import com.endava.demo.connector.model.external.BookingResponse;
import com.endava.demo.connector.model.internal.BookParameter;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;

import static com.endava.demo.connector.common.CamelUtils.*;

@Component
public class BookingRoute extends RouteBuilder {
    public static final String URI = "direct:startRoute";
    public static final String DIRECT_BOOK_INIT = "direct:bookInit";
    public static final String DIRECT_BOOK_COMMIT = "direct:bookCommit";

    public static final String INITIALIZE_ROUTE_ID = "initialize_route_id";
    public static final String BOOK_INIT_ROUTE_ID = "book_init_route_id";
    public static final String BOOK_COMMIT_ROUTE_ID = "book_commit_route_id";

    private final BookInitRequestBodyBuilder bookInitRequestBodyBuilder;
    private final BookInitRequestHeaderBuilder bookInitRequestHeaderBuilder;
    private final BookInitResponseInterpreter bookInitResponseInterpreter;
    private final BookCommitRequestBodyBuilder bookCommitRequestBodyBuilder;
    private final BookCommitRequestHeaderBuilder bookCommitRequestHeaderBuilder;
    private final BookCommitResponseInterpreter bookCommitResponseInterpreter;
    private final HttpBookErrorInterpreter httpBookErrorInterpreter;
    private final BookErrorInterpreter bookErrorInterpreter;
    private final BookRetryProcessor bookRetryProcessor;
    private final Clock clock;


    public BookingRoute(BookInitRequestBodyBuilder bookInitRequestBodyBuilder, BookInitRequestHeaderBuilder bookInitRequestHeaderBuilder, BookInitResponseInterpreter bookInitResponseInterpreter, BookCommitRequestBodyBuilder bookCommitRequestBodyBuilder, BookCommitRequestHeaderBuilder bookCommitRequestHeaderBuilder, BookCommitResponseInterpreter bookCommitResponseInterpreter, HttpBookErrorInterpreter httpBookErrorInterpreter, BookErrorInterpreter bookErrorInterpreter, BookRetryProcessor bookRetryProcessor, Clock clock) {
        this.bookInitRequestBodyBuilder = bookInitRequestBodyBuilder;
        this.bookInitRequestHeaderBuilder = bookInitRequestHeaderBuilder;
        this.bookInitResponseInterpreter = bookInitResponseInterpreter;
        this.bookCommitRequestBodyBuilder = bookCommitRequestBodyBuilder;
        this.bookCommitRequestHeaderBuilder = bookCommitRequestHeaderBuilder;
        this.bookCommitResponseInterpreter = bookCommitResponseInterpreter;
        this.httpBookErrorInterpreter = httpBookErrorInterpreter;
        this.bookRetryProcessor = bookRetryProcessor;
        this.bookErrorInterpreter = bookErrorInterpreter;
        this.clock = clock;
    }

    @Override
    public void configure() throws Exception {
        this.exceptionHandling();

        rest("/endava")
                .post("/booking/")
                .to(URI);

        //Initialize route
        from(URI)
                .routeId(INITIALIZE_ROUTE_ID)
                .log("Received book request.")
                .unmarshal(new JacksonDataFormat(BookParameter.class))
                .log("BookParameter: ${body}")
                .setProperty(BOOK_PARAMETER, body())
                .process(this::startRequestTimer)
                //todo add validation
                .process(this::emptyRetry)
                .to(DIRECT_BOOK_INIT);

        from(DIRECT_BOOK_INIT)
                .routeId(BOOK_INIT_ROUTE_ID)
                .errorHandler(noErrorHandler())
                .log("Starting book init.")
                .bean(bookInitRequestBodyBuilder)
                .marshal(new JacksonDataFormat(BookingInit.class))
                .log("Book init request body: ${body}")
                .process(bookInitRequestHeaderBuilder)
                .to("http:bookInit")
                .setProperty(BOOK_INIT_RESPONSE, bodyAs(String.class))
                .log("Book response: ${body}")
                .unmarshal(new JacksonDataFormat(BookingResponse.class))
                .bean(bookInitResponseInterpreter)
                .process(this::emptyRetry)
                .to(DIRECT_BOOK_COMMIT);

        from(DIRECT_BOOK_COMMIT)
                .routeId(BOOK_COMMIT_ROUTE_ID)
                .log("Starting build commit.")
                .bean(bookCommitRequestBodyBuilder)
                .marshal(new JacksonDataFormat(BookingCommit.class))
                .log("Book commit request body: ${body}")
                .process(bookCommitRequestHeaderBuilder)
                .to("http:bookCommit")
                .setProperty(BOOK_COMMIT_RESPONSE, bodyAs(String.class))
                .log("Book response: ${body}")
                .unmarshal(new JacksonDataFormat(BookingResponse.class))
                .bean(bookCommitResponseInterpreter)
                .marshal(new JacksonDataFormat());
    }

    private void emptyRetry(Exchange exchange) {
        exchange.setProperty(RETRY_ACTIONS, new ArrayList<>());
    }

    private void exceptionHandling() {
        onException(HttpOperationFailedException.class)
                .onWhen(this::shouldRetryThisException)
                .maximumRedeliveries(5)
                .delayPattern("1:10;2:100;3:1000;4:2000;5:4000")
                .logRetryAttempted(true)
                .onRedelivery(bookRetryProcessor)
                .bean(httpBookErrorInterpreter)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .marshal(new JacksonDataFormat())
                .end();
        onException(HttpOperationFailedException.class)
                .bean(httpBookErrorInterpreter)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .marshal(new JacksonDataFormat())
                .end();
        onException(Exception.class)
                .bean(bookErrorInterpreter)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .marshal(new JacksonDataFormat())
                .end();
    }

    private boolean shouldRetryThisException(Exchange exchange) {
        return bookRetryProcessor.isErrorForRetry(exchange);
    }

    protected void startRequestTimer(Exchange exchange) {
        exchange.setProperty(REQUEST_START_TIME, clock.millis());
    }

}
