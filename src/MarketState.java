public record MarketState(
        String symbol,
        double price,
        double previousPrice,
        double change,
        double changePercent,
        long timestamp,
        MarketStatus status
) {



}
