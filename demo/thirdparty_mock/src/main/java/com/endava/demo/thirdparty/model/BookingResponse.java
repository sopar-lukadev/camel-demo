package com.endava.demo.thirdparty.model;

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
