CREATE TABLE slots (
                       id BIGSERIAL PRIMARY KEY,
                       date DATE NOT NULL,
                       start_time TIME NOT NULL,
                       end_time TIME NOT NULL,
                       status VARCHAR(20) NOT NULL
);

CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          slot_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_booking_slot
                              FOREIGN KEY (slot_id)
                                  REFERENCES slots(id)
);