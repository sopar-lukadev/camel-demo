package com.endava.demo.thirdparty.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BookingInit {
    @NonNull
    private String hotelId;
    @NonNull
    private String roomCode;
    @NonNull
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
}
