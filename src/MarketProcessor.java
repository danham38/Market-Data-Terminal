import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MarketProcessor {
    //thread safe mapping for MarketStates
    private final Map<String, MarketState> states = new ConcurrentHashMap<>();

    //thread safe mapping for history of prices (last 60) as 1 per sec
    private final ConcurrentHashMap<String, Deque<Double>> history = new ConcurrentHashMap<>();

    //stale timeframe
    private static final long STALE_THRESHOLD_MS = 3000;
    private static final int PRICE_HISTORY_LIMIT = 60;
    private static final double TREND_TOLERANCE_PERCENT = 0.05;

    public void process(MarketEvent event) {
        //get the previous state
        MarketState previousState = states.get(event.symbol());
        Deque<Double> prices = history.computeIfAbsent(event.symbol(), key -> new ConcurrentLinkedDeque<>());
        prices.addLast(event.price());

        if (prices.size() > PRICE_HISTORY_LIMIT) {
            prices.removeFirst();
        }

        //if no previous events, previous price = current price & change = 0 & changePerc = 0.0
        if (previousState == null) {
            MarketState newState = new MarketState(
                    event.symbol(),
                    event.price(),
                    event.price(),
                    0.0,
                    0.0,
                    event.price(),
                    event.price(),
                    event.price(),
                    0.0,
                    0.0,
                    event.timestamp(),
                    MarketStatus.LIVE
            );
            states.put(event.symbol(), newState);
            return;
        }
        //else calc change and use prev price from prevState
        double change = event.price() - previousState.price();
        double changePercent;

        //session calcs
        double openPrice = previousState.openPrice();
        double highPrice = Math.max(previousState.highPrice(), event.price());
        double lowPrice = Math.min(previousState.lowPrice(), event.price());
        double sessionChange = previousState.openPrice() - event.price();
        double sessionChangePercent;
        if (previousState.openPrice() == 0.0) {
            sessionChangePercent = 0.0;
        } else {
            sessionChangePercent = ((event.price() - previousState.openPrice()) / previousState.openPrice()) * 100;
        }
        //if prevState price is 0, div by 0 = error so set changePerc to 0.0
        if (previousState.price() == 0.0) {
            changePercent = 0.0;
            //else calc changePerc
        } else {
            changePercent = (change / previousState.price()) * 100;
        }

        //create new MarketState
        MarketState newState = new MarketState(
                event.symbol(),
                event.price(),
                previousState.price(),
                change,
                changePercent,
                openPrice,
                highPrice,
                lowPrice,
                sessionChange,
                sessionChangePercent,
                event.timestamp(),
                MarketStatus.LIVE
        );
        states.put(event.symbol(), newState);
    }

    //method to get the MarketState according to symbol
    public MarketState getState(String symbol) {
        return states.get(symbol);
    }

    //method returns immutable snapshot of current state
    public Map<String, MarketState> getSnapshot() {
        //create new snapshot
        Map<String, MarketState> snapshot = new HashMap<>();
        //get current time
        long now = System.currentTimeMillis();
        //for each entry in states
        for (Map.Entry<String, MarketState> entry : states.entrySet()) {
            MarketState state = entry.getValue();
            //calc how long its been since the event arrived
            long age = now - state.timestamp();

            //if too long since arrived then mark as stale, else keep as live
            MarketStatus status = age > STALE_THRESHOLD_MS ? MarketStatus.STALE : MarketStatus.LIVE;

            //return it back into the snapshot
            snapshot.put(entry.getKey(), state.withStatus(status));
        }
        //return copy of the snapshot
        return Map.copyOf(snapshot);
    }

    //gets price history as a List as is read only so doesn't need Deque operations
    public List<Double> getPriceHistory(String symbol) {
        //create new Deque for each symbol
        Deque<Double> prices = history.get(symbol);

        //if no previous prices return empty list
        if (prices == null) {
            return List.of();
        }
        //else return a snapshot of the prices
        return List.copyOf(prices);
    }

    public TrendDirection getTrend(String symbol) {
        List<Double> prices = getPriceHistory(symbol);

        if (prices.size() < 2) {
            return TrendDirection.FLAT;
        }

        double oldestPrice = prices.getFirst();
        if (oldestPrice == 0.0) {
            return TrendDirection.FLAT;
        }

        double newestPrice = prices.getLast();

        double trendPercentageChange = ((newestPrice - oldestPrice) / oldestPrice) * 100;

        if (trendPercentageChange > +TREND_TOLERANCE_PERCENT) {
            return TrendDirection.UP;
        } else if (trendPercentageChange < -TREND_TOLERANCE_PERCENT) {
            return TrendDirection.DOWN;
        } else {
            return TrendDirection.FLAT;
        }

    }


}
