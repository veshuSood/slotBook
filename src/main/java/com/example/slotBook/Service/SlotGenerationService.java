package com.example.slotBook.Service;

import com.example.slotBook.entity.Slot;
import com.example.slotBook.entity.SlotStatus;
import com.example.slotBook.Repository.SlotRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class SlotGenerationService {

    private final SlotRepository slotRepository;
    private final Clock clock;

    public SlotGenerationService(
            SlotRepository slotRepository,
            Clock clock
    ) {
        this.slotRepository = slotRepository;
        this.clock = clock;
    }

    @Transactional
    public void generateUpcomingSlots() {

        LocalDate today = LocalDate.now(clock);

        LocalTime openingTime = LocalTime.of(9, 0);
        LocalTime closingTime = LocalTime.of(17, 0);

        for (int day = 0; day < 7; day++) {

            LocalDate date = today.plusDays(day);

            LocalTime startTime = openingTime;

            while (startTime.isBefore(closingTime)) {

                LocalTime endTime = startTime.plusMinutes(30);

                if (!slotRepository.existsByDateAndStartTimeAndEndTime(
                        date,
                        startTime,
                        endTime
                )) {

                    Slot slot = new Slot();

                    slot.setDate(date);
                    slot.setStartTime(startTime);
                    slot.setEndTime(endTime);
                    slot.setStatus(SlotStatus.AVAILABLE);

                    slotRepository.save(slot);
                }

                startTime = endTime;
            }
        }
    }
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void generateSlotsAutomatically() {
        generateUpcomingSlots();
    }
}