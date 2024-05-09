package com.endava.demo.connector.retry;

public interface RetryStrategy<T> {
    void retryAction(T obj);
}
