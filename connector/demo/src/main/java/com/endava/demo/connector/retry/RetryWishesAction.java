package com.endava.demo.connector.retry;

import com.endava.demo.connector.model.external.BookingInit;

public class RetryWishesAction implements RetryStrategy<BookingInit> {
    private final static int MAX_WISHES_LENGTH = 10;
    @Override
    public void retryAction(BookingInit bookingInit) {
        bookingInit.cutWishes(MAX_WISHES_LENGTH);
    }
}
