import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketProcessor {
    //thread safe mapping for MarketStates
    private final Map<String, MarketState> states = new ConcurrentHashMap<>();

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
                    event.timestamp(),
                    MarketStatus.LIVE
            );
            states.put(event.symbol(), newState);
            return;
        }
        //else calc change and use prev price from prevState
        double change = event.price() - previousState.price();
        double changePercent;
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
                event.timestamp(),
                MarketStatus.LIVE
        );
        states.put(event.symbol(), newState);
    }

    //method to get the MarketState according to symbol
    public MarketState getState(String symbol) {
        return states.get(symbol);
    }
}
