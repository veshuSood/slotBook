package com.example.slotBook.Repository;

import com.example.slotBook.entity.Booking;
import com.example.slotBook.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findBySlotIdAndUserIdAndStatus(
            Long slotId,
            Long userId,
            BookingStatus status
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Booking> findByStatusAndHoldExpiryLessThanEqual(
            BookingStatus status,
            LocalDateTime now
    );
}
