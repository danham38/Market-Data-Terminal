

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StripedEventDispatcher {
    private final ExecutorService[] stripes;
    public StripedEventDispatcher(int stripeCount) {
        this.stripes = new ExecutorService[stripeCount];
        for (int i = 0; i < stripeCount; i++) {
            stripes[i] = Executors.newSingleThreadExecutor();
        }
    }
    public void dispatch(String symbol, Runnable task) {
        int stripeIndex = Math.floorMod(symbol.hashCode(), stripes.length);

        stripes[stripeIndex].execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                //handle/log failure
                System.err.println("Failed to process task for " + symbol + ": " + e.getMessage());
            }
        });
    }

}
