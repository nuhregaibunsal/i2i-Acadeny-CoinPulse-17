package com.cryptopal.trading.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 4)
    private String type;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Column(nullable = false)
    private BigDecimal volume;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Transaction() {
    }

    public Transaction(Long userId, String type, String assetSymbol,
                       BigDecimal volume, BigDecimal price, BigDecimal totalValue) {
        this.userId = userId;
        this.type = type;
        this.assetSymbol = assetSymbol;
        this.volume = volume;
        this.price = price;
        this.totalValue = totalValue;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
