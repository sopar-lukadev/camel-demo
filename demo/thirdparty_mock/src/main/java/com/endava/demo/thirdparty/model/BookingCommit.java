package com.endava.demo.thirdparty.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class BookingCommit {
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
