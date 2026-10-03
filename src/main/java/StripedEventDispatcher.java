

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//threads
public class StripedEventDispatcher {
    //create the service that executes them
    private final ExecutorService[] stripes;
    public StripedEventDispatcher(int stripeCount) {
        //create new service with as many threads as we pass in
        this.stripes = new ExecutorService[stripeCount];
        //iterate over that many
        for (int i = 0; i < stripeCount; i++) {
            //for each i, add a worker thread at position i of stripes
            stripes[i] = Executors.newSingleThreadExecutor();
        }
    }
    public void dispatch(String symbol, Runnable task) {
        //computes the stripe index
        int stripeIndex = Math.floorMod(symbol.hashCode(), stripes.length);

        //at the position of the stripe index in stripes:
        // lambda to try/catch execute the thread
        stripes[stripeIndex].execute(() -> {
            try {
                //run it
                task.run();
            } catch (Exception e) {
                //handle/log failure
                System.err.println("Failed to process task for " + symbol + ": " + e.getMessage());
            }
        });
    }

    //resource management (shutdown gracefully)
    public void shutdown() {
        for (ExecutorService stripe : stripes) {
            stripe.shutdown();
        }
    }

}
