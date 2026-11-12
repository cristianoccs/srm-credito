package com.srm.creditengine.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<SettlementEntity, UUID> {

    Optional<SettlementEntity> findByIdempotencyKey(String idempotencyKey);

    @Query(
            value = """
                    select s.id as id,
                           s.receivable_id as "receivableId",
                           r.assignor as assignor,
                           s.face_value_brl as "faceValueBrl",
                           s.present_value_brl as "presentValueBrl",
                           s.discount_brl as "discountBrl",
                           s.final_amount as "finalAmount",
                           s.payment_currency as "paymentCurrency",
                           s.exchange_rate_value as exchangeRate,
                           s.settled_at as settledAt
                    from settlements s
                    join receivables r on r.id = s.receivable_id
                    where (cast(:from as timestamp with time zone) is null
                           or s.settled_at >= cast(:from as timestamp with time zone))
                      and (cast(:to as timestamp with time zone) is null
                           or s.settled_at < cast(:to as timestamp with time zone))
                      and (cast(:assignor as varchar) is null
                           or lower(r.assignor) like lower(concat('%', cast(:assignor as varchar), '%')))
                      and (cast(:currency as varchar) is null
                           or s.payment_currency = cast(:currency as varchar))
                    """,
            countQuery = """
                    select count(*)
                    from settlements s
                    join receivables r on r.id = s.receivable_id
                    where (cast(:from as timestamp with time zone) is null
                           or s.settled_at >= cast(:from as timestamp with time zone))
                      and (cast(:to as timestamp with time zone) is null
                           or s.settled_at < cast(:to as timestamp with time zone))
                      and (cast(:assignor as varchar) is null
                           or lower(r.assignor) like lower(concat('%', cast(:assignor as varchar), '%')))
                      and (cast(:currency as varchar) is null
                           or s.payment_currency = cast(:currency as varchar))
                    """,
            nativeQuery = true
    )
    Page<SettlementStatementView> search(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("assignor") String assignor,
            @Param("currency") String currency,
            Pageable pageable
    );
}
