package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "wallet_logs")
@Getter
@Setter
public class WalletLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "transaction_type", nullable = false)
    private Byte transactionType;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "related_type")
    private Byte relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "merchant_trade_no", unique = true, length = 100)
    private String merchantTradeNo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;
}
