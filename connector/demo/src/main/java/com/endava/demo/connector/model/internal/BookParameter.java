package com.endava.demo.connector.model.internal;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BookParameter {
    private Hotel hotel;
    private String roomCode;
    private String rateCode;
    private String internalBookId;
    private String wishes;

    @Override
    public String toString() {
        return "BookParameter{" +
                "hotel=" + hotel +
                ", roomCode='" + roomCode + '\'' +
                ", rateCode='" + rateCode + '\'' +
                ", internalBookId='" + internalBookId + '\'' +
                ", wishes='" + wishes + '\'' +
                '}';
    }
}
