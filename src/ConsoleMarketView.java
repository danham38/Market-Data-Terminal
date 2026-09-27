import java.util.Comparator;
import java.util.Map;

public class ConsoleMarketView {
    //table width
    private static final int TABLE_WIDTH = 87;

    //makes the table for the terminal view
    public void render(Map<String, MarketState> snapshot) {

       System.out.println("-".repeat(TABLE_WIDTH));

        //lays out table nicely in columns
        System.out.printf(
                "%-8s %10s %10s %10s %10s %10s %12s %10s%n",
                "SYMBOL",
                "PRICE",
                "TICK %",
                "OPEN",
                "HIGH",
                "LOW",
                "SESSION %",
                "STATUS"
        );

        System.out.println("-".repeat(TABLE_WIDTH));

        //orders the symbols in a predictable output (ASC)
        snapshot.values().stream()
                .sorted(Comparator.comparing(MarketState::symbol))
                .forEach(state -> {
                    System.out.printf(
                            "%-8s %10.2f %+9.2f%% %10.2f %10.2f %10.2f %+11.2f%% %10s%n",
                            state.symbol(),
                            state.price(),
                            state.changePercent(),
                            state.openPrice(),
                            state.highPrice(),
                            state.lowPrice(),
                            state.sessionChangePercent(),
                            state.status()
                    );
                });
    }
}
