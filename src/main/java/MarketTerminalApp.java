import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketTerminalApp extends Application {
    //for linking the market data to JavaFX table
    FakeMarketDataProvider provider = new FakeMarketDataProvider();
    StripedEventDispatcher stripes = new StripedEventDispatcher(4);
    MarketProcessor processor = new MarketProcessor();
    //symbols
    List<String> symbols = List.of("AAPL", "MSFT", "NVDA");


    //method for table generation and data importing to table
    @Override
    public void start(Stage stage) {
        //loop through each symbol, subscribe to that symbol
        for (String s : symbols) {
            provider.subscribe(
                    s,
                    //Consumer (receives MarketEvent without returning a result) LAMBDA 1
                    event -> stripes.dispatch(
                            event.symbol(),
                            //Runnable lambda (takes no inputs and returns nothing) LAMBDA 2
                            //already receives the variable from the surrounding lambda (variable capture)
                            () -> processor.process(event))
                    );
        }

        //create table
        TableView<MarketViewRow> table = new TableView<>();

        //label table
        Label title = new Label("MARKET TERMINAL");

        //dimensions
        VBox root = new VBox(title, table);
        Scene scene = new Scene(root, 1000, 650);

        //title and headers
        stage.setTitle("Market Terminal");
        TableColumn<MarketViewRow, String> symbolColumn = new TableColumn<>("SYMBOL");
        TableColumn<MarketViewRow, Double> priceColumn = new TableColumn<>("PRICE");
        TableColumn<MarketViewRow, String> trendColumn = new TableColumn<>("TREND");
        //get values
        symbolColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().state().symbol())
        );
        priceColumn.setCellValueFactory(
                data -> new ReadOnlyObjectWrapper<>(data.getValue().state().price())
        );
        trendColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().trend().toString())
        );
        //add columns to table
        table.getColumns().addAll(
                symbolColumn,
                priceColumn,
                trendColumn
        );
        //temp object to display prices
        MarketState testState = new MarketState(
                "AAPL",
                200.50,
                199.50,
                1.00,
                0.50,
                198.00,
                202.00,
                197.00,
                2.50,
                1.26,
                System.currentTimeMillis(),
                MarketStatus.LIVE
        );
        //temp test view
        MarketViewRow testRow =
                new MarketViewRow(testState, TrendDirection.UP);
        table.getItems().add(testRow);
        stage.setScene(scene);
        stage.show();


        //timeline for refresh
        //create object (timeline)
        Timeline timeline = new Timeline(

                new KeyFrame(
                        //execute KeyFrame after 1s interval
                        Duration.seconds(1),
                        //lambda to print per every 1s
                        event -> {
                            //get snapshot
                            Map<String, MarketState> snapshot = processor.getSnapshot();
                        List<MarketViewRow> rows = new ArrayList<>();
                        //loop through snapshots
                        for (MarketState s : snapshot.values()) {
                            //call processor to get trend for that symbol
                            TrendDirection trend = processor.getTrend(s.symbol());
                            MarketViewRow row = new MarketViewRow(s, trend);
                            rows.add(row);
                        }
                        table.getItems().setAll(rows);
                        }

                ));
        //repeat indefinitely
        timeline.setCycleCount(timeline.INDEFINITE);
        //start timer
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}