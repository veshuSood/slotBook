package com.example.slotBook.Service;

import com.example.slotBook.Repository.IdempotencyKeyRepository;
import com.example.slotBook.entity.*;
import com.example.slotBook.Repository.BookingRepository;
import com.example.slotBook.Repository.SlotRepository;
import com.example.slotBook.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final Clock clock;

    public SlotService(
            SlotRepository slotRepository,
            BookingRepository bookingRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            Clock clock
    ) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.clock = clock;
    }

    public Slot createSlot(Slot slot) {

        if (slot.getStatus() == null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            System.out.println("🔥 CREATE SLOT CONTROLLER CALLED");
            System.out.println("Slot received: " + slot);
        }

        return slotRepository.save(slot);
    }

    public List<Slot> getAllSlots() {

        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        return slotRepository.findFutureSlots(today, now);
    }

    @Transactional
    public Booking bookSlot(Long slotId, Long userId,String idempotencyKey) {
        var existingKey =
                idempotencyKeyRepository
                        .findByUserIdAndIdempotencyKey(userId, idempotencyKey);

        if (existingKey.isPresent()) {

            IdempotencyKey existing = existingKey.get();

            if (!existing.getSlotId().equals(slotId)) {
                throw new RuntimeException(
                        "Idempotency-Key was already used for a different slot"
                );
            }

            Long bookingId = existing.getBookingId();

            return bookingRepository.findById(bookingId)
                    .orElseThrow(() ->
                            new RuntimeException("Booking not found"));
        }

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ConflictException(
                    "Idempotency-Key was already used for a different slot"
            );
        }

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.HELD);
        booking.setCreatedAt(LocalDateTime.now(clock));
        booking.setHoldExpiry(LocalDateTime.now(clock).plusMinutes(5));

        Booking savedBooking = bookingRepository.save(booking);
        IdempotencyKey key = new IdempotencyKey();

        key.setUserId(userId);
        key.setIdempotencyKey(idempotencyKey);
        key.setSlotId(slotId);
        key.setBookingId(savedBooking.getId());
        key.setCreatedAt(LocalDateTime.now(clock));

        idempotencyKeyRepository.save(key);

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        return savedBooking;
    }
    @Transactional
    public Booking confirmBooking(Long slotId, Long userId) {
        Booking booking=bookingRepository.findBySlotIdAndUserIdAndStatus(slotId,
                userId,BookingStatus.HELD).orElseThrow(()->new ConflictException("no active hold found")
        );
        LocalDateTime now=LocalDateTime.now(clock);
        if(!now.isBefore(booking.getHoldExpiry())){
            throw new ConflictException("hold expired");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }
    public Slot findSlotById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
    }
    @Transactional
    public void deleteSlot(Long slotId) {

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() ->
                        new ConflictException("Slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ConflictException(
                    "Cannot delete a booked or held slot"
            );
        }

        slotRepository.delete(slot);
    }
}