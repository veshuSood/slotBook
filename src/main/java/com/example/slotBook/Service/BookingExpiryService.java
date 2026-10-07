package com.example.slotBook.Service;

import com.example.slotBook.entity.Booking;
import com.example.slotBook.entity.BookingStatus;
import com.example.slotBook.Repository.BookingRepository;
import com.example.slotBook.entity.SlotStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingExpiryService {

    private final BookingRepository bookingRepository;
    private final Clock clock;

    public BookingExpiryService(
            BookingRepository bookingRepository,
            Clock clock
    ) {
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void expireBookings() {

        LocalDateTime now = LocalDateTime.now(clock);

        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndHoldExpiryLessThanEqual(
                        BookingStatus.HELD,
                        now
                );

        for (Booking booking : expiredBookings) {

            if (booking.getStatus() != BookingStatus.HELD) {
                continue;
            }

            booking.setStatus(BookingStatus.EXPIRED);

            booking.getSlot().setStatus(SlotStatus.AVAILABLE);

        }
    }
}