import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Priority;
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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.chart.XYChart;

public class MarketTerminalApp extends Application {
    //vars for timestamp checking
    private long lastChartTimeStamp = -1;
    private int chartTick = 0;
    //starting symbol for price chart
    private String selectedSymbol = "AAPL";
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
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        //scaling as deltas are small; if axis scaled to 0 then line looks flat
        yAxis.setForceZeroInRange(false);
        LineChart<Number, Number> priceChart = new LineChart<>(xAxis, yAxis);
        //price series chart
        XYChart.Series<Number, Number> priceSeries = new XYChart.Series<>();
        //stop animations
        priceChart.setAnimated(false);
        priceChart.setTitle(selectedSymbol + " Rolling Price");
        //stops dots being drawn at each discrete interval
        priceChart.setCreateSymbols(false);
        priceSeries.setName("AAPL");
        priceChart.getData().add(priceSeries);
        //dimensions
        VBox root = new VBox(title, table, priceChart);
        Scene scene = new Scene(root, 1000, 650);

        //setting height for aesthetics
        table.setPrefHeight(160);
        table.setMaxHeight(160);
        VBox.setVgrow(priceChart, Priority.ALWAYS);
        root.setSpacing(8);

        //title and headers
        stage.setTitle("Market Terminal");
        TableColumn<MarketViewRow, String> symbolColumn = new TableColumn<>("SYMBOL");
        TableColumn<MarketViewRow, Double> priceColumn = new TableColumn<>("PRICE");
        TableColumn<MarketViewRow, String> trendColumn = new TableColumn<>("TREND");
        TableColumn<MarketViewRow, Double> tickPercentageColumn = new TableColumn<>("TICK %");
        TableColumn<MarketViewRow, Double> sessionPercentageColumn = new TableColumn<>("SESSION %");
        TableColumn<MarketViewRow, String> statusColumn = new TableColumn<>("STATUS");
        //get values
        symbolColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().state().symbol())
        );
        priceColumn.setCellValueFactory(
                data -> new ReadOnlyObjectWrapper<>(
                        data.getValue().state().price()
                )
        );
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", price));
                }
            }
        });
        trendColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().trend().toString())
        );
        tickPercentageColumn.setCellValueFactory(
                data -> new ReadOnlyObjectWrapper<>(
                        data.getValue().state().changePercent()
                )
        );
        tickPercentageColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double percentageTick, boolean empty) {
                super.updateItem(percentageTick, empty);
                if (empty || percentageTick == null) {
                    setText(null);
                } else {
                    setText(String.format("%+.2f%%", percentageTick));
                }
            }
        });
        sessionPercentageColumn.setCellValueFactory(
                data -> new ReadOnlyObjectWrapper<>(
                        data.getValue().state().sessionChangePercent()
                )
        );
        sessionPercentageColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double spc, boolean empty) {
                //spc = sessionChangePercentage
                super.updateItem(spc, empty);
                if (empty || spc == null) {

                    setText(null);
                } else {
                    setText(String.format("%+.2f%%", spc));
                }
            }
        });

        statusColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().state().status().toString())
        );
        //add columns to table
        table.getColumns().addAll(
                symbolColumn,
                priceColumn,
                trendColumn,
                tickPercentageColumn,
                sessionPercentageColumn,
                statusColumn
        );
        //show the frame
        stage.setScene(scene);
        stage.show();
        PauseTransition pauseTest = new PauseTransition(Duration.seconds(5));

        pauseTest.setOnFinished(event -> {
            provider.pauseSymbol("MSFT");
        });

        pauseTest.play();
        //listen for table selection
        table.getSelectionModel().selectedItemProperty().
                addListener((observable, oldRow, newRow) -> {
            if (newRow != null) {
                //get clicked on symbol
                selectedSymbol = newRow.state().symbol();
                //display new symbol in title
                priceChart.setTitle(selectedSymbol + " Rolling Price");
                //clear previous data
                priceSeries.getData().clear();
                //get price history
                List<Double> history = processor.getPriceHistory(selectedSymbol);
                //display price history
                for (int i = 0; i < history.size(); i++) {
                    priceSeries.getData().add(new XYChart.Data<>(i, history.get(i)));
                }
                chartTick = history.size();
                //latest state currently displayed is already accounted for
                lastChartTimeStamp = newRow.state().timestamp();
                priceSeries.setName(selectedSymbol);
            }
        });


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
                        //get aapl snapshot
                            MarketState selectedState = snapshot.get(selectedSymbol);
                            //if exists and timestamp hasn't been seen before
                            if (selectedState != null && selectedState.timestamp() != lastChartTimeStamp) {
                                //add to chart
                                priceSeries.getData().add(new XYChart.Data<>(chartTick, selectedState.price()));
                                //increment chartTick
                                chartTick++;
                                //remember timestamp
                                lastChartTimeStamp = selectedState.timestamp();
                                //if size gets too big, remove first point
                                if (priceSeries.getData().size() > 60) {
                                    priceSeries.getData().removeFirst();
                                }
                            }
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
        timeline.setCycleCount(Timeline.INDEFINITE);
        //start timer
        timeline.play();
    }



    public static void main(String[] args) {
        launch(args);
    }
}