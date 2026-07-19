package com.cryptopal.ai.service;

import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.market.entity.PriceSnapshot;
import com.cryptopal.market.model.CryptoPrice;
import com.cryptopal.market.repository.PriceSnapshotRepository;
import com.cryptopal.trading.dto.HoldingView;
import com.cryptopal.trading.dto.PortfolioResponse;
import com.cryptopal.trading.entity.Transaction;
import com.cryptopal.trading.repository.TransactionRepository;
import com.cryptopal.trading.service.TradingService;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AiContextBuilder {

    private static final int MAX_TRANSACTIONS = 10;
    private static final int MAX_TREND_POINTS = 8;

    private final TradingService tradingService;
    private final TransactionRepository transactionRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final MarketPriceCache marketPriceCache;

    public AiContextBuilder(TradingService tradingService,
                            TransactionRepository transactionRepository,
                            PriceSnapshotRepository priceSnapshotRepository,
                            MarketPriceCache marketPriceCache) {
        this.tradingService = tradingService;
        this.transactionRepository = transactionRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.marketPriceCache = marketPriceCache;
    }

    @Transactional(readOnly = true)
    public String build(String username, Long userId, String question) {
        StringBuilder context = new StringBuilder();
        context.append("You are CryptoPal's market assistant. Answer using ONLY the data below. ")
                .append("Be concise and use Markdown. If the data does not contain the answer, say so. ")
                .append("You are not a licensed financial advisor: do not give personalised investment advice.\n\n");
        context.append("This is a simulated trading app. If (and only if) the user explicitly asks to ")
                .append("place, set, or create a buy or sell order — including conditional orders like ")
                .append("'buy AVAX if it drops to 33.44' or 'sell 2 BTC if it rises to 70000' — reply with ONLY ")
                .append("a raw JSON object and no other text, in exactly this shape:\n")
                .append("{\"action\":\"order\",\"orderType\":\"instant|conditional\",\"symbol\":\"AVAX\",")
                .append("\"side\":\"BUY|SELL\",\"targetPrice\":33.44,\"volume\":2}\n")
                .append("Omit targetPrice for instant orders. Compute volume in coins; if the user gives a ")
                .append("dollar amount, divide it by the relevant price. For every other message, answer ")
                .append("normally in the user's language.\n\n");

        context.append("## User\n").append(username).append("\n\n");
        appendPortfolio(context, userId);
        appendLivePrices(context);
        appendTransactions(context, userId);
        appendTrends(context);

        context.append("## Question\n").append(question).append('\n');
        return context.toString();
    }

    private void appendPortfolio(StringBuilder context, Long userId) {
        PortfolioResponse portfolio = tradingService.getPortfolio(userId);
        context.append("## Portfolio\n")
                .append("Cash balance: ").append(portfolio.cashBalance()).append('\n')
                .append("Holdings value: ").append(portfolio.holdingsValue()).append('\n');
        if (portfolio.holdings().isEmpty()) {
            context.append("No crypto holdings.\n");
        } else {
            for (HoldingView holding : portfolio.holdings()) {
                context.append("- ").append(holding.symbol())
                        .append(": volume ").append(holding.volume())
                        .append(", price ").append(holding.currentPrice())
                        .append(", value ").append(holding.value()).append('\n');
            }
        }
        context.append('\n');
    }

    private void appendLivePrices(StringBuilder context) {
        context.append("## Live prices\n");
        for (CryptoPrice price : marketPriceCache.getAll()) {
            context.append("- ").append(price.symbol()).append(": ").append(price.price()).append('\n');
        }
        context.append('\n');
    }

    private void appendTransactions(StringBuilder context, Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        context.append("## Recent transactions\n");
        if (transactions.isEmpty()) {
            context.append("No transactions yet.\n\n");
            return;
        }
        transactions.stream().limit(MAX_TRANSACTIONS).forEach(transaction ->
                context.append("- ").append(transaction.getType())
                        .append(' ').append(transaction.getVolume())
                        .append(' ').append(transaction.getAssetSymbol())
                        .append(" at ").append(transaction.getPrice())
                        .append(" total ").append(transaction.getTotalValue())
                        .append(" on ").append(transaction.getCreatedAt()).append('\n'));
        context.append('\n');
    }

    private void appendTrends(StringBuilder context) {
        context.append("## Recent price trend\n");
        for (CryptoPrice price : marketPriceCache.getAll()) {
            List<PriceSnapshot> history =
                    priceSnapshotRepository.findTop20ByAssetSymbolOrderByRecordedAtDesc(price.symbol());
            if (history.isEmpty()) {
                continue;
            }
            context.append("- ").append(price.symbol()).append(": ");
            history.stream().limit(MAX_TREND_POINTS)
                    .forEach(snapshot -> context.append(snapshot.getPrice()).append(' '));
            context.append('\n');
        }
        context.append('\n');
    }
}
