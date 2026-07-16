package com.cryptopal.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "price_history")
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "recorded_at", insertable = false, updatable = false)
    private OffsetDateTime recordedAt;

    protected PriceSnapshot() {
    }

    public PriceSnapshot(String assetSymbol, BigDecimal price) {
        this.assetSymbol = assetSymbol;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }
}
