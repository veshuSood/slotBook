package com.example.slotBook.Controller;

import com.example.slotBook.Service.BookingService;
import com.example.slotBook.dto.BookingRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    @PostMapping("/bookings")
    public String booking(@RequestBody BookingRequest request){
        return bookingService.book(request.getSlotId());
    }
}
