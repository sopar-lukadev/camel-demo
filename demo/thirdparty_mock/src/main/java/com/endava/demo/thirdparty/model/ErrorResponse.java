package com.endava.demo.thirdparty.model;

import lombok.*;

@NoArgsConstructor
@Builder
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
}
