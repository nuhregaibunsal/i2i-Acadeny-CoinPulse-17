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
@Table(name = "pending_orders")
public class PendingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 4)
    private String type;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Column(nullable = false, length = 5)
    private String direction;

    @Column(name = "target_price", nullable = false)
    private BigDecimal targetPrice;

    @Column(nullable = false)
    private BigDecimal volume;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected PendingOrder() {
    }

    public PendingOrder(Long userId, String type, String assetSymbol, String direction,
                        BigDecimal targetPrice, BigDecimal volume, String status) {
        this.userId = userId;
        this.type = type;
        this.assetSymbol = assetSymbol;
        this.direction = direction;
        this.targetPrice = targetPrice;
        this.volume = volume;
        this.status = status;
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

    public String getDirection() {
        return direction;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
