CREATE UNIQUE INDEX ux_slots_datetime
    ON slots(date, start_time, end_time);