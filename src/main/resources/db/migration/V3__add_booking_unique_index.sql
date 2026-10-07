CREATE UNIQUE INDEX ux_bookings_active_slot
    ON bookings(slot_id)
    WHERE status IN ('HELD', 'CONFIRMED');