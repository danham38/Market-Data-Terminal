import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketProcessor {
    //thread safe mapping for MarketStates
    private final Map<String, MarketState> states = new ConcurrentHashMap<>();

    //stale timeframe
    private static final long STALE_THRESHOLD_MS = 3000;

    public void process(MarketEvent event) {
        //get the previous state
        MarketState previousState = states.get(event.symbol());

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
            //DEBUG
            //System.out.println(state.symbol() + " calculated status = " + status);

            //return it back into the snapshot
            snapshot.put(entry.getKey(), state.withStatus(status));
            //DEBUG
            /*System.out.println(
                    state.symbol() +
                            " age=" + age +
                            " timestamp= " + state.timestamp() +
                            " now= " + now
            );*/
        }
        //return copy of the snapshot
        return Map.copyOf(snapshot);
    }
}
