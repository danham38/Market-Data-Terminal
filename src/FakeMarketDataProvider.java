import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FakeMarketDataProvider implements MarketDataProvider {
    //ScheduledExecutorService = run this code repeatedly on another thread
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Override
    public void subscribe(String symbol, Consumer<MarketEvent> consumer) {
        MarketSubscription subscription = new MarketSubscription(symbol, consumer, 200.0);

        scheduler.scheduleAtFixedRate(
                //not sure,
                subscription,
                0,
                1,
                TimeUnit.SECONDS
        );
    }




    //inner object

    private static class MarketSubscription implements Runnable{
        private final String symbol;
        private final Consumer<MarketEvent> consumer;
        private double currentPrice;

        //constructor
        public MarketSubscription(
                String symbol,
                Consumer<MarketEvent> consumer,
                double startingPrice) {
            this.symbol = symbol;
            this.consumer = consumer;
            this.currentPrice = startingPrice;
        }

        //each time run() runs:
        //change = x
        //update currentPrice
        //create MarketEvent
        //send event to subscriber
        @Override
        public void run() {
            double change = (Math.random() - 0.5);
            this.currentPrice += change;

            MarketEvent event = new MarketEvent(
                    symbol,
                    currentPrice,
                    System.currentTimeMillis()
            );
            consumer.accept(event);
        }


    }
}
