package com.endava.demo.connector.model.external;

import lombok.*;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@Builder
public class BookingCommit extends AbstractExternalRequest{
    private String bookingId;
    private Integer confirmationId;

    @Override
    public String toString() {
        return "BookingCommit{" +
                "bookingId=" + bookingId +
                ", confirmationId=" + confirmationId +
                '}';
    }
}
