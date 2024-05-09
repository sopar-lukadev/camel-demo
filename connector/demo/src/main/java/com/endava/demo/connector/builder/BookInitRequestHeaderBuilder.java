package com.endava.demo.connector.builder;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.http.common.HttpMethods;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BookInitRequestHeaderBuilder implements Processor {

    @Value("${thirdparty.url.init}")
    private String initUrl;

    @Override
    public void process(Exchange exchange) throws Exception {
        //Camel will copy other headers (e.g. from original request), we can erase them if we don't want that to be forwarded. In this scenario I wanted to forward the headers as well for scenario header.
        Message out = exchange.getMessage();
        out.setBody(exchange.getIn().getBody());
        out.setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
        out.setHeader(Exchange.HTTP_URI, initUrl);
    }
}
