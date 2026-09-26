//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        FakeMarketDataProvider provider = new FakeMarketDataProvider();
        StripedEventDispatcher dispatcher = new StripedEventDispatcher(4);
        MarketProcessor processor = new MarketProcessor();
        provider.subscribe(
                "AAPL",

                // whenever the provider produces a MarketEvent
                event -> {

                    // send this work to the stripe associated with this symbol
                    dispatcher.dispatch(
                            event.symbol(),
                            // actual work to be executed
                            () -> {
                                processor.process(event);
                                System.out.println(
                                        processor.getState(event.symbol())
                                );
                            }
                    );
                }
        );
//        provider.subscribe(
//                "MSFT",
//                event -> System.out.println(event)
//        );

    }
}