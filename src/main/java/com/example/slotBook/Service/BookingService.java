package com.example.slotBook.Service;

import org.springframework.stereotype.Service;

@Service
public class BookingService {
    public String book(Long slotId){
        return "booking requested for slot id: "+slotId;
    }
}
