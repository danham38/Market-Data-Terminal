public record MarketState(
        String symbol,
        double price,
        double previousPrice,
        double change,
        double changePercent,
        double openPrice,
        double highPrice,
        double lowPrice,
        double sessionChange,
        double sessionChangePercent,
        long timestamp,
        MarketStatus status
) {

    //allows us to change status by creating new snapshot of MarketState
    public MarketState withStatus(MarketStatus newStatus) {
        return new MarketState(
                symbol,
                price,
                previousPrice,
                change,
                changePercent,
                openPrice,
                highPrice,
                lowPrice,
                sessionChange,
                sessionChangePercent,
                timestamp,
                newStatus
        );
    }



}
