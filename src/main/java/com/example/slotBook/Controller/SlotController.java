package com.example.slotBook.Controller;

import com.example.slotBook.Security.UserDetailsImpl;
import com.example.slotBook.Service.SlotService;
import com.example.slotBook.entity.Booking;
import com.example.slotBook.entity.Slot;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping("/slots/{id}")
    public Slot slot(@PathVariable Long id) {
        return slotService.findSlotById(id);
    }

    @GetMapping("/slots")
    public List<Slot> allSlots() {
        return slotService.getAllSlots();
    }

    @PostMapping("/slots")
    public Slot createSlot(@RequestBody Slot slot) {
        return slotService.createSlot(slot);
    }


    @PostMapping("/slots/{id}/book")
    public Booking bookSlot(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        Long userId = userDetails.getUser().getId();

        return slotService.bookSlot(id, userId, idempotencyKey);
    }
    @PostMapping("/slots/{id}/confirm")
    public Booking confirmBooking(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        Long userId = userDetails.getUser().getId();

        return slotService.confirmBooking(id, userId);
    }
    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long slotId
    ) {
        slotService.deleteSlot(slotId);

        return ResponseEntity.noContent().build();
    }
}