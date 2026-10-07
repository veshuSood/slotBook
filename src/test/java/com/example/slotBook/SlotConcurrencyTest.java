package com.example.slotBook;

import com.example.slotBook.Service.SlotService;
import com.example.slotBook.entity.Booking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SlotConcurrencyTest {

    @Autowired
    private SlotService slotService;

    @Test
    void multipleUsersCanBookSameSlot() throws Exception {

        int threadCount = 3;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready =
                new CountDownLatch(threadCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Booking> successfulBookings =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            Long userId = 301L + i;

            executor.submit(() -> {

                // Tell the main thread:
                // "I am ready."
                ready.countDown();

                try {
                    // Wait until ALL threads are ready.
                    start.await();

                    // Everyone starts booking at approximately
                    // the same time.
                    Booking booking =
                            slotService.bookSlot(6L, userId, "test-" + userId);

                    synchronized (successfulBookings) {
                        successfulBookings.add(booking);
                    }

                } catch (Exception e) {

                    System.out.println(
                            "Request failed: " + e.getMessage()
                    );
                }
            });
        }

        // Don't start the race until all 3 threads
        // have reached the starting line.
        ready.await();

        System.out.println("All threads ready!");

        // RELEASE!
        start.countDown();

        executor.shutdown();

        while (!executor.isTerminated()) {
            Thread.sleep(10);
        }

        System.out.println(
                "Successful bookings: "
                        + successfulBookings.size()
        );
        assertEquals(1, successfulBookings.size());

        for (Booking booking : successfulBookings) {
            System.out.println(
                    "Booking ID: "
                            + booking.getId()
                            + " | User: "
                            + booking.getUserId()
            );
        }
    }
}