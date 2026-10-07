ALTER TABLE bookings
    ADD CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
            REFERENCES users(id);