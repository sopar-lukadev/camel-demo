package com.endava.demo.connector.model.external;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class BookingInit extends AbstractExternalRequest{
    private String hotelId;
    private String roomCode;
    private String rateCode;
    private String wishes;

    @Override
    public String toString() {
        return "BookingInit{" +
                "hotelId='" + hotelId + '\'' +
                ", roomCode='" + roomCode + '\'' +
                ", rateCode='" + rateCode + '\'' +
                ", wishes='" + wishes + '\'' +
                '}';
    }

    public void cutWishes(int length) {
        if (wishes != null && wishes.length() >= length) {
            wishes = wishes.substring(0, length);
        }
    }
}
