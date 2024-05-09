package com.endava.demo.thirdparty.controller;

import com.endava.demo.thirdparty.model.BookingCommit;
import com.endava.demo.thirdparty.model.BookingInit;
import com.endava.demo.thirdparty.model.BookingResponse;
import com.endava.demo.thirdparty.model.ErrorResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@Log4j2
@RequestMapping("/booking")
public class BookingController {
    @PostMapping("/init")
    public ResponseEntity<Object> createBooking(@RequestHeader(value = "scenario", required = false) String scenario, @RequestBody @NonNull BookingInit bookingInit) {
        log.info("Received booking init request: " + bookingInit);
        Random random = new Random();

        scenario = getNotSoRandomScenario(bookingInit, scenario, random);

        return switch (scenario) {
            case "1" -> {
                // Successful response
                Integer confirmationId = random.nextInt();
                log.info("BookInit Scenario 1: Successful booking: " + confirmationId);
                yield ResponseEntity.ok(BookingResponse.builder().message("Booking created successfully").confirmationNumber(confirmationId).build());
            }
            case "2" -> {
                // Return 500 with exception message "Sold out"
                log.info("BookInit Scenario 2: Room already sold out.");
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().message("Sold out.").status(5874).build());
            }
            case "3" -> {
                try {
                    // Sleep for 10 seconds
                    log.info("BookInit Scenario 3: Long running task");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    log.error("Whatever we want to do with it. Not important now.");
                }
                // Return successful response after sleep
                Integer confirmationId = random.nextInt();
                log.info("BookInit Scenario 3: Successful booking: " + confirmationId);
                yield ResponseEntity.ok(BookingResponse.builder().message("Booking created successfully after delay").confirmationNumber(confirmationId).build());
            }
            case "4" -> {
                // Return 500 with exception message "Wish is too long"
                log.info("BookInit Scenario 4: Too long wish");
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ErrorResponse.builder().message("Wish is too long").status(4003).build());
            }
            default ->
                // Invalid scenario
                ResponseEntity.badRequest().body("Invalid scenario");
        };
    }

    private String getNotSoRandomScenario(BookingInit bookingInit, String scenario, Random random) {
        // If scenario header is not provided, generate a random scenario
        if (scenario == null) {
            //If wishes are longer than some max length, lets say that we are in scenario 4
            if (bookingInit != null && bookingInit.getWishes() != null && bookingInit.getWishes().length() > 10) {
                return "4";
            }
            return String.valueOf(random.nextInt(3) + 1); // Generate random scenario between 1 and 3
        }
        return "1";
    }

    @PostMapping("/commit")
    public ResponseEntity<Object> commitBooking(@RequestHeader(value = "scenario", required = false) String scenario, @RequestBody @NonNull BookingCommit bookingCommit) {
        log.info("Received booking commit request: " + bookingCommit);
        Random random = new Random();
        if (scenario == null) {
            scenario = String.valueOf(random.nextInt(2) + 1); // Generate random scenario between 1 and 2
        }

        return switch (scenario) {
            case "1" -> {
                // Successful response
                Integer confirmationId = random.nextInt();
                log.info("BookCommit: Scenario 1: Successful booking: " + confirmationId);
                yield ResponseEntity.ok(BookingResponse.builder().message("Booking created successfully").confirmationNumber(confirmationId).build());
            }
            case "2" -> {
                // Return 500 with exception message "We are not ready"
                log.info("BookCommit Scenario 2: We are not ready.");
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().message("We are not ready.").status(6043).build());
            }
            default ->
                // Invalid scenario
                ResponseEntity.badRequest().body("Invalid scenario");
        };
    }
}
