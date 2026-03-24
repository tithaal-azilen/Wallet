package com.Tithaal.Wallet.redis;

import com.Tithaal.Wallet.entity.IdempotencyRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRecordRepository extends CrudRepository<IdempotencyRecord, String> {
}
