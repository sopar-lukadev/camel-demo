package com.endava.demo.routes;

import com.endava.demo.connector.builder.BookCommitRequestBodyBuilder;
import com.endava.demo.connector.builder.BookCommitRequestHeaderBuilder;
import com.endava.demo.connector.builder.BookInitRequestBodyBuilder;
import com.endava.demo.connector.builder.BookInitRequestHeaderBuilder;
import com.endava.demo.connector.interpterer.*;
import com.endava.demo.connector.routes.BookingRoute;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;

import static com.endava.demo.connector.routes.BookingRoute.BOOK_COMMIT_ROUTE_ID;
import static com.endava.demo.connector.routes.BookingRoute.BOOK_INIT_ROUTE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookingRouteTest extends CamelTestSupport {

    private static final String MOCK_API_ENDPOINT = "http:";

    private static String initSuccess;
    private static String initWishesTooLong;
    private static String commitSuccess;
    private static String commitNotReady;
    private static String bookingParameter;
    private static String successfulBooking;


    @Produce(BookingRoute.URI)
    private ProducerTemplate apiProducer;

    @EndpointInject("mock:" + MOCK_API_ENDPOINT)
    private MockEndpoint mockEndpointApiRoute;

    @Mock
    private Clock clock;

    @BeforeAll
    public static void load() throws IOException {
        initSuccess = new String(Files.readAllBytes(Paths.get("src/test/resources/init_mock_success.json")));
        initWishesTooLong = new String(Files.readAllBytes(Paths.get("src/test/resources/init_mock_wishes.json")));
        commitSuccess = new String(Files.readAllBytes(Paths.get("src/test/resources/commit_mock_success.json")));
        commitNotReady = new String(Files.readAllBytes(Paths.get("src/test/resources/commit_mock_not_ready.json")));
        bookingParameter = new String(Files.readAllBytes(Paths.get("src/test/resources/booking_parameter.json")));
        successfulBooking = new String(Files.readAllBytes(Paths.get("src/test/resources/successful_booking.json")));

    }

    @BeforeEach
    private void initMock() {
        when(clock.millis()).thenReturn(13L);
    }

    @Override
    public RouteBuilder[] createRouteBuilders() {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpBookErrorInterpreter httpBookErrorInterpreter = new HttpBookErrorInterpreter(objectMapper);
        return new RouteBuilder[] {
                new BookingRoute(new BookInitRequestBodyBuilder(),
                        new BookInitRequestHeaderBuilder(),
                        new BookInitResponseInterpreter(),
                        new BookCommitRequestBodyBuilder(),
                        new BookCommitRequestHeaderBuilder(),
                        new BookCommitResponseInterpreter(),
                        new HttpBookErrorInterpreter(objectMapper),
                        new BookErrorInterpreter(),
                        new BookRetryProcessor(httpBookErrorInterpreter, clock),
                        clock)
        };
    }

    @Test
    public void bookingRoute_successful() throws Exception {
        context.start();

        AdviceWith.adviceWith(context, BOOK_INIT_ROUTE_ID, a -> a.weaveByToUri(MOCK_API_ENDPOINT + "*").replace().to(mockEndpointApiRoute));
        AdviceWith.adviceWith(context, BOOK_COMMIT_ROUTE_ID, a -> a.weaveByToUri(MOCK_API_ENDPOINT + "*").replace().to(mockEndpointApiRoute));

        mockEndpointApiRoute.expectedMessageCount(2);
        mockEndpointApiRoute.whenExchangeReceived(1, exchangeWithBody(initSuccess));
        mockEndpointApiRoute.whenExchangeReceived(2, exchangeWithBody(commitSuccess));

        String bookResponseJson = apiProducer.requestBody(BookingRoute.URI, bookingParameter, String.class);
        MockEndpoint.assertIsSatisfied(context);

        assertEquals(successfulBooking, bookResponseJson);
        context.stop();
    }

    @Test
    public void bookingRoute_toolongwish() throws Exception {
        context.start();

        AdviceWith.adviceWith(context, BOOK_INIT_ROUTE_ID, a -> a.weaveByToUri(MOCK_API_ENDPOINT + "*").replace().to(mockEndpointApiRoute));
        AdviceWith.adviceWith(context, BOOK_COMMIT_ROUTE_ID, a -> a.weaveByToUri(MOCK_API_ENDPOINT + "*").replace().to(mockEndpointApiRoute));

        mockEndpointApiRoute.expectedMessageCount(3);
        mockEndpointApiRoute.whenExchangeReceived(1, throwException(new HttpOperationFailedException("Test URI", 500, "Test error", "localhost", null, initWishesTooLong)));
        mockEndpointApiRoute.whenExchangeReceived(2, exchangeWithBody(initSuccess));
        mockEndpointApiRoute.whenExchangeReceived(3, exchangeWithBody(commitSuccess));

        String bookParamToSend = bookingParameter.replace("Non-s", "This is very long wish that won't go through.");
        String bookResponseJson = apiProducer.requestBody(BookingRoute.URI, bookParamToSend, String.class);
        MockEndpoint.assertIsSatisfied(context);

        assertEquals(successfulBooking.replace("Non-s", "This is very long wish that won't go through."), bookResponseJson);
        context.stop();
    }

    @Test
    public void bookingRoute_notReadySuccessRetry() throws Exception {
        context.start();

        AdviceWith.adviceWith(context, BOOK_INIT_ROUTE_ID, a -> a.weaveByToUri(MOCK_API_ENDPOINT + "*").replace().to(mockEndpointApiRoute));
        AdviceWith.adviceWith(context, BOOK_COMMIT_ROUTE_ID, a -> a.weaveByToUri(MOCK_API_ENDPOINT + "*").replace().to(mockEndpointApiRoute));

        mockEndpointApiRoute.expectedMessageCount(3);
        mockEndpointApiRoute.whenExchangeReceived(1, exchangeWithBody(initSuccess));
        mockEndpointApiRoute.whenExchangeReceived(2, throwException(new HttpOperationFailedException("Test URI", 500, "Test error", "localhost", null, commitNotReady)));
        mockEndpointApiRoute.whenExchangeReceived(3, exchangeWithBody(commitSuccess));

        String bookResponseJson = apiProducer.requestBody(BookingRoute.URI, bookingParameter, String.class);
        MockEndpoint.assertIsSatisfied(context);

        assertEquals(successfulBooking, bookResponseJson);
        context.stop();
    }

    @Test
    public void bookingRoute_notReady() throws Exception {
        context.start();

        AdviceWith.adviceWith(context, BOOK_INIT_ROUTE_ID, a -> a.weaveByToUri(MOCK_API_ENDPOINT + "*").replace().to(mockEndpointApiRoute));
        AdviceWith.adviceWith(context, BOOK_COMMIT_ROUTE_ID, a -> a.weaveByToUri(MOCK_API_ENDPOINT + "*").replace().to(mockEndpointApiRoute));

        mockEndpointApiRoute.expectedMessageCount(7);
        mockEndpointApiRoute.whenExchangeReceived(1, exchangeWithBody(initSuccess));
        mockEndpointApiRoute.whenExchangeReceived(2, throwException(new HttpOperationFailedException("Test URI", 500, "Test error", "localhost", null, commitNotReady)));
        mockEndpointApiRoute.whenExchangeReceived(3, throwException(new HttpOperationFailedException("Test URI", 500, "Test error", "localhost", null, commitNotReady)));
        mockEndpointApiRoute.whenExchangeReceived(4, throwException(new HttpOperationFailedException("Test URI", 500, "Test error", "localhost", null, commitNotReady)));
        mockEndpointApiRoute.whenExchangeReceived(5, throwException(new HttpOperationFailedException("Test URI", 500, "Test error", "localhost", null, commitNotReady)));
        mockEndpointApiRoute.whenExchangeReceived(6, throwException(new HttpOperationFailedException("Test URI", 500, "Test error", "localhost", null, commitNotReady)));
        mockEndpointApiRoute.whenExchangeReceived(7, throwException(new HttpOperationFailedException("Test URI", 500, "Test error", "localhost", null, commitNotReady)));

        String bookResponseJson = apiProducer.requestBody(BookingRoute.URI, bookingParameter, String.class);
        MockEndpoint.assertIsSatisfied(context);

        assertEquals("{\"errorType\":\"BF_NOT_DONE\"}", bookResponseJson);
        context.stop();
    }

    public static <T> Processor exchangeWithBody(T body) {
        return exchange -> exchange.getIn().setBody(body);
    }

    public static Processor throwException(Exception exception) {
        return (exchange) -> {
            throw exception;
        };
    }



}
