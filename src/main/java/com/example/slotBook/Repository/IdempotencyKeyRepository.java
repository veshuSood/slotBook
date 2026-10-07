package com.example.slotBook.Repository;

import com.example.slotBook.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );// as we are using userId and idempotencyKey as unique
}
