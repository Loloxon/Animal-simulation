package simulation.gui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import simulation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class App extends Application implements IPositionChangeObserver {
    private int animalNO;
    private int width;
    private int height;
    private int startEnergy;
    private int moveEnergy;
    private int plantEnergy;
    private int jungleRatio;
    private int fieldSize;
    private int chartValueNo;
    private int delay;
    private AbstractWorldMap map1;
    private SimulationEngine engine1;
    private AbstractWorldMap map2;
    private SimulationEngine engine2;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    private final HashMap<MapDirection, Image> imageAnimals = new HashMap<>();
    private Image imageGrass;
    private Image imageNothing;

    private final ArrayList<XYChart.Series> allChartSeries1 = new ArrayList<>();
    private final ArrayList<XYChart.Series> allChartSeries2 = new ArrayList<>();
    private final GridPane grid1 = new GridPane();
    private final GridPane grid2 = new GridPane();
    private final GridPane trackingGrid1 = new GridPane();
    private final GridPane trackingGrid2 = new GridPane();

    private GuiElementBox[][] matrix1;
    private GuiElementBox[][] matrix2;

    private final Label magic1 = new Label();
    private final Label magic2 = new Label();

    private final Label labelGens1 = new Label();
    private final Label labelGens2 = new Label();

    private String genotype1 = "";
    private String genotype2 = "";

    void stats(SimulationEngine engine, AbstractWorldMap map, Label labelGens, ArrayList<XYChart.Series> allChartSeries){
        ArrayList<String> chartsOrder = engine.getChartsOrder();
        ArrayList<ArrayList<Double>> chartsInfo = engine.getChartsInfo();

        CopyOnWriteArrayList<String> genotypesToCheck = map.getGenotypes();
        HashMap<String,Integer> Count = new HashMap<>();
        String S = "";
        double all = map.getA().size();
        int x;
        double max = 0;
        for(String s:genotypesToCheck){
            if(!Count.containsKey(s)) {
                x = 1;
            }
            else {
                x = Count.get(s) + 1;
            }
            Count.put(s, x);
            if(max<x){
                max = x;
                S = s;
            }
        }
        if(all == 0){
            labelGens.setText("All animals are dead :(");
        }
        else {
            if(map == map1)
                genotype1 = S;
            else
                genotype2 = S;
            for (int i = 0; i < chartsOrder.size(); i++) {
                allChartSeries.get(i).getData().clear();
                for (int k = chartsInfo.get(i).size() - 1; k >= chartsInfo.get(i).size() - chartValueNo; k--) {
                    allChartSeries.get(i).getData().add(new XYChart.Data((k), chartsInfo.get(i).get(k)));
                }
            }
            labelGens.setText(S + "; " + df.format(max * 100 / all) + "%");
        }
    }

    void createGrid(GridPane grid, GuiElementBox[][] matrix){
        grid.setGridLinesVisible(true);
        for(int i=0;i<width;i++){
            grid.getColumnConstraints().add(new ColumnConstraints(fieldSize));
        }
        for(int i=0;i<height;i++){
            grid.getRowConstraints().add(new RowConstraints(fieldSize));
        }
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                matrix[i][j] = new GuiElementBox(fieldSize,imageNothing, -1, startEnergy);
                GridPane.setConstraints(matrix[i][j].view, i, j);
                grid.getChildren().add(matrix[i][j].view);
            }
        }
    }

    void prepareGrid(GuiElementBox[][] matrix, AbstractWorldMap map){
        CopyOnWriteArrayList<Animal> A = map.getA();
        for(Animal a:A){
            int x = a.getPosition().x;
            int y = a.getPosition().y;
            matrix[x][y].update(imageAnimals.get(a.getDirection()), a.getEnergy());
        }
    }

    void updateGrid(AbstractWorldMap map, GuiElementBox[][] matrix, Vector2d position, SimulationEngine engine, GridPane trackingGrid){
        int x = position.x;
        int y = position.y;
        Object object = map.objectAt(position);
        if(object instanceof Animal a) {
               matrix[x][y].update(imageAnimals.get(a.getDirection()), a.getEnergy());
            matrix[x][y].view.setOnMouseClicked(event -> {
                if (!engine.getRunning()) {
                    map.setTrackedAnimal(a);
                    updateTracking(map, trackingGrid);
                }
            });
        }
        else if(object instanceof Grass g) {
            if(g.getPosition().follows(map.jungleLL) && g.getPosition().precedes(map.jungleUR))
                matrix[x][y].update(imageGrass, -3);
            else
                matrix[x][y].update(imageGrass, -1);
        }
        else
            matrix[x][y].update(imageNothing, -1);
    }
    @Override
    public void start(Stage primaryStage){
        MapDirection[] directions = new MapDirection[8];
        for(int i=0;i<8;i++) {
            directions[i] = MapDirection.NORTH;
            for(int k=0;k<i;k++)
                directions[i] = directions[i].next();
            imageAnimals.put(directions[i], new Image(directions[i].getImage()));
        }
        imageGrass = new Image("file:src/main/resources/grass.png");
        imageNothing = new Image("file:src/main/resources/nothing.png");

        primaryStage.setTitle("Starting screen");
        VBox root = new VBox();
        VBox vbox = new VBox();
        Label scenetitle = new Label("Enter initial data"); //animals no, width, height, startEnergy, moveEnergy, plantEnergy, jungleRatio
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 25));
        vbox.getChildren().add(scenetitle);
        root.getChildren().add(vbox);

        ArrayList<String> labels = new ArrayList<>(){
            {
                add("Number of animals: ");
                add("Map width: ");
                add("Map height: ");
                add("Animal starting energy: ");
                add("Animal move energy: ");
                add("Grass energy: ");
                add("Jungle ratio [%]: ");
                add("How many values are shown in charts: ");
                add("Delay between moves [ms]: ");
            }
        };
        ArrayList<TextField> textFields = new ArrayList<>(){
            {
                add(new TextField("20"));
                add(new TextField("20"));
                add(new TextField("10"));
                add(new TextField("50"));
                add(new TextField("3"));
                add(new TextField("100"));
                add(new TextField("36"));
                add(new TextField("100"));
                add(new TextField("50"));
            }
        };

        ArrayList<HBox> hboxes = new ArrayList<>(){
            {
                for(int i=0;i<labels.size();i++)
                    add(new HBox());
            }
        };

        for(int i=0;i<labels.size();i++){
            hboxes.get(i).getChildren().add(new Label(labels.get(i)));
            hboxes.get(i).getChildren().add(textFields.get(i));
            vbox.getChildren().add(hboxes.get(i));
            hboxes.get(i).setAlignment(Pos.CENTER);
        }

        CheckBox checkBox1 = new CheckBox("If first map ('folded') is magic");
        vbox.getChildren().add(checkBox1);
        CheckBox checkBox2 = new CheckBox("If second map ('with edges') is magic");
        vbox.getChildren().add(checkBox2);

        Button S = new Button("Start");
        S.setDefaultButton(true);
        vbox.getChildren().add(S);

        vbox.setSpacing(20);
        VBox.setMargin(vbox, new Insets(25, 25, 25, 25));
        Scene scene = new Scene(root, 550, 600);

        primaryStage.setScene(scene);
        primaryStage.show();

        S.setOnAction(e -> {
            ArrayList<Integer> values = new ArrayList<>();
            for(TextField t:textFields){
                values.add(Integer.parseInt(t.getText()));
            }
            this.animalNO = values.get(0);
            this.width = values.get(1);
            this.height = values.get(2);
            this.startEnergy = values.get(3);
            this.moveEnergy = values.get(4);
            this.plantEnergy = values.get(5);
            this.jungleRatio = values.get(6);
            this.chartValueNo = values.get(7);
            this.delay = values.get(8);

            this.fieldSize = Math.min((1500-50)/(2*this.width+2), (800/2)/(this.height+1));
            boolean magic1 = checkBox1.isSelected();
            int magicUses;
            if(magic1)
                magicUses = 3;
            else
                magicUses = 0;
            map1 = new NoEdgedMap(this.width, this.height, this.startEnergy, this.moveEnergy, this.plantEnergy, this.jungleRatio, magicUses);
            engine1 = new SimulationEngine(map1, this.animalNO, this.startEnergy, this.chartValueNo, this.delay);
            engine1.setObserver(this);

            boolean magic2 = checkBox2.isSelected();
            if(magic2)
                magicUses = 3;
            else
                magicUses = 0;
            map2 = new EdgedMap(this.width, this.height, this.startEnergy, this.moveEnergy, this.plantEnergy, this.jungleRatio, magicUses);
            engine2 = new SimulationEngine(map2, this.animalNO, this.startEnergy, this.chartValueNo, this.delay);
            engine2.setObserver(this);

            grid1.setAlignment(Pos.CENTER);
            grid2.setAlignment(Pos.CENTER);

            matrix1 = new GuiElementBox[width][height];
            matrix2 = new GuiElementBox[width][height];
            createGrid(grid1, matrix1);
            createGrid(grid2, matrix2);
            prepareGrid(matrix1, map1);
            prepareGrid(matrix2, map2);
            primaryStage.close();
            simulate(primaryStage);
        });
    }

    VBox createCharts(ArrayList<String> chartsOrder, ArrayList<XYChart.Series> allChartSeries){

        VBox boxCharts = new VBox();

        ArrayList<NumberAxis> allxAxis = new ArrayList<>();
        ArrayList<NumberAxis> allyAxis = new ArrayList<>();
        ArrayList<LineChart<Number,Number>> allCharts = new ArrayList<>();

        for(int i=0;i<4;i++) {
            allxAxis.add(new NumberAxis());
            allxAxis.get(i).setForceZeroInRange(false);
            allyAxis.add(new NumberAxis());
            allCharts.add(new LineChart<>(allxAxis.get(i), allyAxis.get(i)));
            allCharts.get(i).setCreateSymbols(false);
        }
        for(int i=0;i<2;i++) {
            allChartSeries.add(new XYChart.Series());
            allCharts.get(0).getData().add(allChartSeries.get(i));
            allCharts.get(0).setAnimated(false);
        }
        for(int i=2;i<5;i++) {
            allChartSeries.add(new XYChart.Series());
            allCharts.get(i-1).getData().add(allChartSeries.get(i));
            allCharts.get(i-1).setAnimated(false);
        }

        for(int i=0;i<5;i++){
            allChartSeries.get(i).setName(chartsOrder.get(i));
        }
        HBox chartsFirstRow = new HBox();
        for(int i=0;i<2;i++)
            chartsFirstRow.getChildren().add(allCharts.get(i));
        HBox chartsSecondRow = new HBox();
        for(int i=2;i<4;i++)
            chartsSecondRow.getChildren().add(allCharts.get(i));
        boxCharts.getChildren().add(chartsFirstRow);
        boxCharts.getChildren().add(chartsSecondRow);
        return boxCharts;
    }

    VBox createInfo(Label name, GridPane grid, GridPane trackingGrid, SimulationEngine engine, Label labelStats, AbstractWorldMap map,
                    GuiElementBox[][] matrix, Label labelGens, Label magic, int mapNo){
        VBox boxInfo = new VBox();

        boxInfo.getChildren().add(name);
        boxInfo.getChildren().add(grid);
        boxInfo.getChildren().add(trackingGrid);

        ToggleButton startStop = new ToggleButton("Stop");
        startStop.setSelected(false);
        startStop.setOnAction(e -> {
            if(startStop.isSelected()){
                engine.setRunning(false);
                startStop.setText("Start");
            }
            else{
                engine.setRunning(true);
                startStop.setText("Stop");
            }
        });

        Button stats = new Button("Statistics -> CSV");
        stats.setDefaultButton(true);
        stats.setOnAction(e -> {
            if(engine.getRunning()) {
                labelStats.setText("First stop the simulation!");
            }
            else {
                labelStats.setText("Saved!");
                try {
                    new Statistics().saveToCSV(engine, mapNo, chartValueNo);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button buttonGenotype = new Button("Show animals with this genotype");
        stats.setDefaultButton(true);
        buttonGenotype.setOnAction(e -> {
            if(engine.getRunning()) {
                labelStats.setText("First stop the simulation!");
            }
            else {
                showGenotypes(map, mapNo, matrix);
                labelStats.setText("Animals showed");
            }
        });

        HBox gensInfo = new HBox();
        gensInfo.getChildren().add(labelGens);
        gensInfo.getChildren().add(buttonGenotype);
        gensInfo.setAlignment(Pos.CENTER);
        boxInfo.getChildren().add(gensInfo);

        HBox buttonMagic = new HBox();
        buttonMagic.getChildren().add(startStop);
        buttonMagic.getChildren().add(stats);
        buttonMagic.getChildren().add(labelStats);
        buttonMagic.setAlignment(Pos.CENTER);
        boxInfo.getChildren().add(buttonMagic);

        boxInfo.getChildren().add(magic);
        boxInfo.setAlignment(Pos.CENTER);
        return boxInfo;
    }

    void simulate(Stage primaryStage){
        primaryStage.setTitle("World");
        Button exit = new Button("Exit");
        exit.setMinSize(50,25);
        HBox maps = new HBox();
        maps.setAlignment(Pos.CENTER);

        ArrayList<String> chartsOrder = new ArrayList<>() {
            {
                add("Number of animals");
                add("Number of grass");
                add("Average energy of living animals");
                add("Average age of dead animals");
                add("Average children no. of living animals");
            }
        };
        Thread engineThread1;
        engineThread1 = new Thread(engine1);
        engine1.setChartsInfo(chartsOrder);

        VBox first = new VBox();
        Label name1 = new Label("Folded map");
        first.getChildren().add(createInfo(name1, grid1, trackingGrid1, engine1, new Label(), map1, matrix1, labelGens1, magic1, 1));
        first.getChildren().add(createCharts(chartsOrder, allChartSeries1));
        maps.getChildren().add(first);

        maps.getChildren().add(exit);

        Thread engineThread2;
        engineThread2 = new Thread(engine2);
        engine2.setChartsInfo(chartsOrder);

        VBox second = new VBox();
        Label name2 = new Label("Map with edges");
        second.getChildren().add(createInfo(name2, grid2, trackingGrid2, engine2, new Label(), map2, matrix2, labelGens2, magic2, 2));
        second.getChildren().add(createCharts(chartsOrder, allChartSeries2));
        maps.getChildren().add(second);

        Scene scene = new Scene(maps, 1500, 900);
        primaryStage.setScene(scene);
        primaryStage.show();
        engineThread1.start();
        engineThread2.start();

        exit.setOnAction(e -> {
            engineThread1.stop();
            engineThread2.stop();
            primaryStage.close();
        });
    }

    void showGenotypes(AbstractWorldMap map, int mapNo, GuiElementBox[][] matrix){
        String genotype;
        if(mapNo==1)
            genotype = genotype1;
        else
            genotype = genotype2;
        CopyOnWriteArrayList<Animal> A = map.getA();
        for(Animal a:A){
            if(a.getStringGens().equals(genotype)){
                Platform.runLater(() -> matrix[a.getPosition().x][a.getPosition().y].update(imageAnimals.get(a.getDirection()), -2));
            }
        }
    }

    
    void updateTracking(AbstractWorldMap map, GridPane trackingGrid){
        trackingGrid.setGridLinesVisible(false);
        trackingGrid.getChildren().clear();
        trackingGrid.getColumnConstraints().clear();
        trackingGrid.getRowConstraints().clear();
        trackingGrid.setGridLinesVisible(true);
        for(int i=0;i<2;i++)
            trackingGrid.getRowConstraints().add(new RowConstraints(30));
        for(int i=0;i<4;i++)
            trackingGrid.getColumnConstraints().add(new ColumnConstraints(100));
        trackingGrid.getColumnConstraints().add(new ColumnConstraints(280));
        trackingGrid.add(new Label("Children no.:"),0,0);
        trackingGrid.add(new Label("Offsprings no.:"),1,0);
        trackingGrid.add(new Label("Day of death:"),2,0);
        trackingGrid.add(new Label("Age:"),3,0);
        trackingGrid.add(new Label("Genotype:"),4,0);
        int[] info = map.getTrackedInfo();
        if(map.getTrackedAnimal() == null){
            for(int i=0;i<5;i++)
                trackingGrid.add(new Label("-"),i,1);
        }
        else {
            for (int i = 0; i < 4; i++) {
                if(info[i]==-1)
                    trackingGrid.add(new Label("It's still alive!"), i, 1);
                else
                    trackingGrid.add(new Label(Integer.toString(info[i])), i, 1);
            }
            trackingGrid.add(new Label(map.getTrackedAnimal().getStringGens()), 4, 1);
        }
        trackingGrid.setAlignment(Pos.CENTER);
    }
    
    public void dayEnded(SimulationEngine engine, AbstractWorldMap map){
        if(map == map1) {
            Platform.runLater(() -> stats(engine, map1, labelGens1, allChartSeries1));
            Platform.runLater(() -> updateTracking(map, trackingGrid1));
        }
        else {
            Platform.runLater(() -> stats(engine, map2, labelGens2, allChartSeries2));
            Platform.runLater(() -> updateTracking(map, trackingGrid2));
        }
    }

    @Override
    public void positionChanged(Vector2d oldPosition, Vector2d newPosition, AbstractWorldMap map) {
        if(map == map1) {
            Platform.runLater(() -> {
                updateGrid(map1, matrix1, oldPosition, engine1, trackingGrid1);
                updateGrid(map1, matrix1, newPosition, engine1, trackingGrid1);
            });
        }
        if(map == map2){
            Platform.runLater(() -> {
                updateGrid(map2, matrix2, oldPosition, engine2, trackingGrid2);
                updateGrid(map2, matrix2, newPosition, engine2, trackingGrid2);
            });
        }
    }

    @Override
    public void magicHappened(int howManyTimes, AbstractWorldMap map){
        if(map == map1) {
            Platform.runLater(() -> {
                switch (howManyTimes) {
                    case 1 -> magic1.setText("Wow, was it magic?");
                    case 2 -> magic1.setText("Hey! Magic happened second time!");
                    case 3 -> magic1.setText("Third time?? Amazing!!!");
                }
            });
        }
        if(map == map2){
            Platform.runLater(() -> {
                switch (howManyTimes) {
                    case 1 -> magic2.setText("Wow, was it magic?");
                    case 2 -> magic2.setText("Hey! Magic happened second time!");
                    case 3 -> magic2.setText("Third time?? Amazing!!!");
                }
            });
        }
    }
}
