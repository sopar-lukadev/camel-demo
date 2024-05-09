package com.endava.demo.connector.model.external;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private Integer confirmationNumber;
    private String message;
}
