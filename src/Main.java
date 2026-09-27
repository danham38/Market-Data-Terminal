import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        //all symbols to subscribe to
        List<String> symbols = List.of(
                "AAPL",
                "MSFT",
                "NVDA"
        );

        ScheduledExecutorService displayScheduler = Executors.newSingleThreadScheduledExecutor();

        FakeMarketDataProvider provider = new FakeMarketDataProvider();
        StripedEventDispatcher dispatcher = new StripedEventDispatcher(4);
        MarketProcessor processor = new MarketProcessor();
        ConsoleMarketView view = new ConsoleMarketView();

        for (String symbol : symbols) {
            provider.subscribe(
                    symbol,
                    event -> dispatcher.dispatch(
                            event.symbol(),
                            () -> processor.process(event)
                    )
            );
        }

        displayScheduler.scheduleAtFixedRate(
                () -> {
                    view.render(processor.getSnapshot());
                },
                1,
                2,
                TimeUnit.SECONDS
        );

    }
}