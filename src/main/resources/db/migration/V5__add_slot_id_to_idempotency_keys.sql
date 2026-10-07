ALTER TABLE idempotency_keys
    ADD COLUMN slot_id BIGINT;

UPDATE idempotency_keys ik
SET slot_id = b.slot_id
    FROM bookings b
WHERE ik.booking_id = b.id;

ALTER TABLE idempotency_keys
    ALTER COLUMN slot_id SET NOT NULL;

ALTER TABLE idempotency_keys
    ADD CONSTRAINT fk_idempotency_slot
        FOREIGN KEY (slot_id)
            REFERENCES slots(id);