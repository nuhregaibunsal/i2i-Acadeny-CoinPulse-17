package com.cryptopal.trading.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings")
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Column(nullable = false)
    private BigDecimal volume;

    protected Holding() {
    }

    public Holding(Long userId, String assetSymbol, BigDecimal volume) {
        this.userId = userId;
        this.assetSymbol = assetSymbol;
        this.volume = volume;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }
}
