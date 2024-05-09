package com.endava.demo.connector.model.internal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResult {
    private ErrorType errorType;
}
