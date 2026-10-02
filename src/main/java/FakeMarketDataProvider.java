

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FakeMarketDataProvider implements MarketDataProvider {
    //set for paused symbols - ConcurrentHashMap as two different threads will interact and needs to be thread safe
    private final Set<String> pausedSymbols = ConcurrentHashMap.newKeySet();
    //ScheduledExecutorService = run this code repeatedly on another thread
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    //lookup map of starting prices
    private final Map<String, Double> startingPrices = Map.of(
            "AAPL", 200.0,
            "MSFT", 420.0,
            "NVDA", 180.0
            );

    @Override
    public void subscribe(String symbol, Consumer<MarketEvent> consumer) {
        //normalise strings
        String normalisedSymbol = symbol.toUpperCase(Locale.ROOT);
        //find if starting price exists in map. if yes use it, if no use default value 100.0
        double startingPrice = startingPrices.getOrDefault(normalisedSymbol, 100.0);
        MarketSubscription subscription = new MarketSubscription(
                normalisedSymbol,
                consumer,
                startingPrice,
                pausedSymbols);

        scheduler.scheduleAtFixedRate(
                subscription,
                0,
                1,
                TimeUnit.SECONDS
        );
    }




    //inner class

    private static class MarketSubscription implements Runnable{
        private final String symbol;
        private final Consumer<MarketEvent> consumer;
        private final Set<String> pausedSymbols;
        private double currentPrice;

        //constructor
        public MarketSubscription(
                String symbol,
                Consumer<MarketEvent> consumer,
                double startingPrice,
                Set<String> pausedSymbols) {
            this.symbol = symbol;
            this.consumer = consumer;
            this.currentPrice = startingPrice;
            this.pausedSymbols = pausedSymbols;
        }

        //each time run() runs:
        //change = x
        //update currentPrice
        //create MarketEvent
        //send event to subscriber
        @Override
        public void run() {

            //Fake fault injection to simulate stopped receiving market states
            if (pausedSymbols.contains(symbol)) {
                return;
            }
            //makes a percentage -0.25% -> +0.25%
            double percentageMove = (Math.random() - 0.5) * 0.005;
            double change = currentPrice * percentageMove;
            this.currentPrice += change;

            MarketEvent event = new MarketEvent(
                    symbol,
                    currentPrice,
                    System.currentTimeMillis()
            );
            consumer.accept(event);
        }


    }

    public void pauseSymbol(String symbol) {
        String normalisedSymbol = symbol.toUpperCase(Locale.ROOT);
        pausedSymbols.add(normalisedSymbol);
    }
    public void resumeSymbol(String symbol) {
        String normalisedSymbol = symbol.toUpperCase(Locale.ROOT);
        pausedSymbols.remove(normalisedSymbol);

    }
}
