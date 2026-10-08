package com.example.slotBook.Repository;

import com.example.slotBook.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot,Long> {
    boolean existsByDateAndStartTimeAndEndTime(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );
    @Query("""
        SELECT s
        FROM Slot s
        WHERE s.date > :date
           OR (s.date = :date AND s.startTime > :time)
        ORDER BY s.date, s.startTime
        """)
    List<Slot> findFutureSlots(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );

}
