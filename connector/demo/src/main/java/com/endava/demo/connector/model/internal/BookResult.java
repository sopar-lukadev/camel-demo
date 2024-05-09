package com.endava.demo.connector.model.internal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResult {
    private BookParameter bookParameter;
    private Integer externalBookingId;
    private Integer externalBookingPin;
}
