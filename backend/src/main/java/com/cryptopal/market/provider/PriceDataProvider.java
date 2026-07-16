package com.cryptopal.market.provider;

import com.cryptopal.market.model.CryptoPrice;
import java.util.List;

public interface PriceDataProvider {

    List<CryptoPrice> currentPrices();
}
