CREATE TABLE idempotency_keys (
                                  id BIGSERIAL PRIMARY KEY,

                                  user_id BIGINT NOT NULL,

                                  idempotency_key VARCHAR(100) NOT NULL,

                                  booking_id BIGINT,

                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT uq_idempotency_user_key
                                      UNIQUE (user_id, idempotency_key)
);