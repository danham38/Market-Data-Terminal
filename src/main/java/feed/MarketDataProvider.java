

import model.MarketEvent;

import java.util.function.Consumer;

public interface MarketDataProvider {
    //Consumer<model.MarketEvent> is the destination for the new event (price)
    void subscribe(String symbol, Consumer<MarketEvent> consumer);
}
