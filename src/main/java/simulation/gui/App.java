package simulation.gui;

import simulation.*;
import com.sun.jdi.IntegerValue;
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
    AbstractWorldMap map1;
    SimulationEngine engine1;
    AbstractWorldMap map2;
    SimulationEngine engine2;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    boolean engine1Running;
    boolean engine2Running;

    HashMap<MapDirection, Image> imageAnimals = new HashMap<>();
    Image imageGrass;
    Image imageNothing;

    ArrayList<XYChart.Series> allChartSeries;
    ArrayList<XYChart.Series> allChartSeries2;
    GridPane grid1;
    GridPane grid2;

    GuiElementBox[][] matrix1;
    GuiElementBox[][] matrix2;

    Label magic1;
    Label magic2;

    Label labelGens1 = new Label();
    Label labelGens2 = new Label();

    String genotype1 = "";
    String genotype2 = "";


    Label labelStats1 = new Label();
    Label labelStats2 = new Label();

    void stats(SimulationEngine engine, AbstractWorldMap map){
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
                Count.put(s, x);
            }
            else {
                x = Count.get(s) + 1;
                Count.put(s, x);
            }
            if(max<x){
                max = x;
                S = s;
            }
        }

        if(map == map1) {
            if(all == 0){
                labelGens1.setText("All animals are dead :(");
            }
            else {
                genotype1 = S;
                for (int i = 0; i < chartsOrder.size(); i++) {
                    allChartSeries.get(i).getData().clear();
                    for (int k = chartsInfo.get(i).size() - 1; k >= chartsInfo.get(i).size() - chartValueNo; k--) {
                        allChartSeries.get(i).getData().add(new XYChart.Data((k), chartsInfo.get(i).get(k)));
                    }
                }
                labelGens1.setText(S + "; " + df.format(max * 100 / all) + "%");
            }
        }
        else{
            if(all == 0){
                labelGens2.setText("All animals are dead :(");
            }
            else {
                genotype2 = S;
                for (int i = 0; i < chartsOrder.size(); i++) {
                    allChartSeries2.get(i).getData().clear();
                    for (int k = chartsInfo.get(i).size() - 1; k >= chartsInfo.get(i).size() - chartValueNo; k--) {
                        allChartSeries2.get(i).getData().add(new XYChart.Data((k), chartsInfo.get(i).get(k)));
                    }
                }
                labelGens2.setText(S + "; " + df.format(max * 100 / all) + "%");
            }
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

    void updateGrid(AbstractWorldMap map, GuiElementBox[][] matrix, Vector2d position){
        Object object = map.objectAt(position);
        if(object instanceof Animal a) {
            matrix[position.x][position.y].update(imageAnimals.get(a.getDirection()), a.getEnergy());
        }
        else if(object instanceof Grass)
            matrix[position.x][position.y].update(imageGrass, -1);
        else
            matrix[position.x][position.y].update(imageNothing, -1);
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
        imageGrass = new Image("file:src/main/resources/grass1.png");
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
                add("Width: ");
                add("Height: ");
                add("Animal starting energy: ");
                add("Animal move energy: ");
                add("Grass energy: ");
                add("Jungle ratio[%]: ");
                add("How many values are shown in charts: ");
                add("Delay between moves [ms]: ");
            }
        };
        ArrayList<TextField> textFields = new ArrayList<>(){
            {
                add(new TextField("20"));
                add(new TextField("15"));
                add(new TextField("15"));
                add(new TextField("50"));
                add(new TextField("3"));
                add(new TextField("100"));
                add(new TextField("50"));
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

        CheckBox checkBox1 = new CheckBox("If first map ('with edges') is magic");
        vbox.getChildren().add(checkBox1);
        CheckBox checkBox2 = new CheckBox("If second map ('without edges') is magic");
        vbox.getChildren().add(checkBox2);

        Button S = new Button("Start");
        S.setDefaultButton(true);
        vbox.getChildren().add(S);

        vbox.setSpacing(20);
        VBox.setMargin(vbox, new Insets(25, 25, 25, 25));
        Scene scene = new Scene(root, 550, 650);

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

            this.fieldSize = Math.min((1500-50)/(2*this.width+2), (1000-500)/(this.height+1));
            boolean magic1 = checkBox1.isSelected();
            int magicUses;
            if(magic1)
                magicUses = 3;
            else
                magicUses = 0;
            map1 = new EdgedMap(this.width, this.height, this.startEnergy, this.moveEnergy, this.plantEnergy, this.jungleRatio, magicUses);
            engine1 = new SimulationEngine(map1, this.animalNO, this.startEnergy, this.chartValueNo, this.delay);
            engine1.setObserver(this);

            boolean magic2 = checkBox2.isSelected();
            if(magic2)
                magicUses = 3;
            else
                magicUses = 0;
            map2 = new NoEdgedMap(this.width, this.height, this.startEnergy, this.moveEnergy, this.plantEnergy, this.jungleRatio, magicUses);
            engine2 = new SimulationEngine(map2, this.animalNO, this.startEnergy, this.chartValueNo, this.delay);
            engine2.setObserver(this);

            grid1 = new GridPane();
            grid1.setAlignment(Pos.CENTER);
            grid2 = new GridPane();
            grid2.setAlignment(Pos.CENTER);

            matrix1 = new GuiElementBox[width][height];
            matrix2 = new GuiElementBox[width][height];
            createGrid(grid1, matrix1);
            createGrid(grid2, matrix2);
            prepareGrid(matrix1, map1);
            prepareGrid(matrix2, map2);
            System.out.println(map1.getA().size());
            primaryStage.close();
            simulate(primaryStage);
        });
    }

    void simulate(Stage primaryStage){
        primaryStage.setTitle("World");

        Thread engineThread1;
        Thread engineThread2;
        engineThread1 = new Thread(engine1);
        engineThread2 = new Thread(engine2);

        ToggleButton startStop1 = new ToggleButton("Stop");
        startStop1.setSelected(false);
        ToggleButton startStop2 = new ToggleButton("Stop");
        startStop2.setSelected(false);
        startStop1.setOnAction(e -> {
            if(startStop1.isSelected()){
                engine1.setRunning(false);
                startStop1.setText("Start");
                engine1Running = false;
            }
            else{
                engine1.setRunning(true);
                startStop1.setText("Stop");
                engine1Running = true;
            }
        });
        startStop2.setOnAction(e -> {
            if(startStop2.isSelected()){
                engine2.setRunning(false);
                startStop2.setText("Start");
                engine2Running = false;
            }
            else{
                engine2.setRunning(true);
                startStop2.setText("Stop");
                engine2Running = true;
            }
        });

        Button stats1 = new Button("Stats -> CSV");
        stats1.setDefaultButton(true);
        Button stats2 = new Button("Stats -> CSV");
        stats2.setDefaultButton(true);

        stats1.setOnAction(e -> {
            if(engine1Running) {
                labelStats1.setText("First stop the simulation!");
            }
            else {
                labelStats1.setText("Saved!");
                try {
                    new Statistics().saveToCSV(engine1, 1, chartValueNo);
//                saveToCSV(engine1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        stats2.setOnAction(e -> {
            if(engine2Running) {
                labelStats2.setText("First stop the simulation!");
            }
            else {
                labelStats2.setText("Saved!");
                try {
                    new Statistics().saveToCSV(engine2, 2, chartValueNo);
//                saveToCSV(engine2);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        HBox maps = new HBox();
        maps.setAlignment(Pos.CENTER);
        VBox first = new VBox();
        VBox second = new VBox();
        HBox gensInfo1 = new HBox();
        HBox gensInfo2 = new HBox();
        HBox buttonMagic1 = new HBox();
        HBox buttonMagic2 = new HBox();

        Button buttonGenotype1 = new Button("Show animals with this genotype");
        Button buttonGenotype2 = new Button("Show animals with this genotype");

        first.getChildren().add(grid1);

        gensInfo1.getChildren().add(labelGens1);
        gensInfo1.getChildren().add(buttonGenotype1);
        gensInfo1.setAlignment(Pos.CENTER);

        buttonMagic1.getChildren().add(startStop1);
        buttonMagic1.getChildren().add(stats1);
        buttonMagic1.getChildren().add(labelStats1);
        magic1 = new Label();
        buttonMagic1.setAlignment(Pos.CENTER);
//        buttonMagic1.getChildren().add(magic1);

        first.getChildren().add(gensInfo1);
        first.getChildren().add(buttonMagic1);
        first.getChildren().add(magic1);
        first.setAlignment(Pos.CENTER);

        ArrayList<String> chartsOrder = new ArrayList<>() {
            {
                add("animals");
                add("grass");
                add("avgEnergy");
                add("avgAge");
                add("avgChildren");
            }
        };
        engine1.setChartsInfo(chartsOrder);
        engine2.setChartsInfo(chartsOrder);

        ArrayList<NumberAxis> allxAxis = new ArrayList<>();
        ArrayList<NumberAxis> allyAxis = new ArrayList<>();
        ArrayList<LineChart<Number,Number>> allCharts = new ArrayList<>();
        allChartSeries = new ArrayList<>();

        for(int i=0;i<4;i++) {
            allxAxis.add(new NumberAxis());
            allxAxis.get(i).setForceZeroInRange(false);
            allyAxis.add(new NumberAxis());
            allCharts.add(new LineChart<Number, Number>(allxAxis.get(i), allyAxis.get(i)));
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
        HBox chartsSecondRow = new HBox();
        for(int i=0;i<2;i++)
            chartsFirstRow.getChildren().add(allCharts.get(i));
        for(int i=2;i<4;i++)
            chartsSecondRow.getChildren().add(allCharts.get(i));

        first.getChildren().add(chartsFirstRow);
        first.getChildren().add(chartsSecondRow);

        second.getChildren().add(grid2);

        gensInfo2.getChildren().add(labelGens2);
        gensInfo2.getChildren().add(buttonGenotype2);
        gensInfo2.setAlignment(Pos.CENTER);

        buttonMagic2.getChildren().add(startStop2);
        buttonMagic2.getChildren().add(stats2);
        buttonMagic2.getChildren().add(labelStats2);
        magic2 = new Label();
        buttonMagic2.setAlignment(Pos.CENTER);
//        buttonMagic2.getChildren().add(magic2);

        second.getChildren().add(gensInfo2);
        second.getChildren().add(buttonMagic2);
        second.getChildren().add(magic2);
        second.setAlignment(Pos.CENTER);

        buttonGenotype1.setOnAction(e -> {
            if(engine1Running) {
                labelStats1.setText("First stop the simulation!");
            }
            else
                showGenotypes(genotype1, map1, matrix1);
//                labelStats1.setText("Saved!");
        });
        buttonGenotype2.setOnAction(e -> {
            if(engine2Running) {
                labelStats2.setText("First stop the simulation!");
            }
            else
                showGenotypes(genotype2, map2, matrix2);
//                labelStats2.setText("Saved!");
        });


        ArrayList<NumberAxis> allxAxis2 = new ArrayList<>();
        ArrayList<NumberAxis> allyAxis2 = new ArrayList<>();
        ArrayList<LineChart<Number,Number>> allCharts2 = new ArrayList<>();
        allChartSeries2 = new ArrayList<>();

        for(int i=0;i<4;i++) {
            allxAxis2.add(new NumberAxis());
            allxAxis2.get(i).setForceZeroInRange(false);
            allyAxis2.add(new NumberAxis());
            allCharts2.add(new LineChart<Number, Number>(allxAxis2.get(i), allyAxis2.get(i)));
            allCharts2.get(i).setCreateSymbols(false);
        }
        for(int i=0;i<2;i++) {
            allChartSeries2.add(new XYChart.Series());
            allCharts2.get(0).getData().add(allChartSeries2.get(i));
            allCharts2.get(0).setAnimated(false);
        }
        for(int i=2;i<5;i++) {
            allChartSeries2.add(new XYChart.Series());
            allCharts2.get(i-1).getData().add(allChartSeries2.get(i));
            allCharts2.get(i-1).setAnimated(false);
        }

        for(int i=0;i<5;i++){
            allChartSeries2.get(i).setName(chartsOrder.get(i));
        }


        HBox chartsFirstRow2 = new HBox();
        HBox chartsSecondRow2 = new HBox();
        for(int i=0;i<2;i++)
            chartsFirstRow2.getChildren().add(allCharts2.get(i));
        for(int i=2;i<4;i++)
            chartsSecondRow2.getChildren().add(allCharts2.get(i));

        second.getChildren().add(chartsFirstRow2);
        second.getChildren().add(chartsSecondRow2);

        maps.getChildren().add(first);
        maps.getChildren().add(second);

        Scene scene1 = new Scene(maps, 1500, 1000);
        primaryStage.setScene(scene1);
        primaryStage.show();
        engineThread1.start();
        engine1Running = true;
        engineThread2.start();
        engine2Running = true;
    }

    void showGenotypes(String genotype, AbstractWorldMap map, GuiElementBox[][] matrix){
        CopyOnWriteArrayList<Animal> A = map.getA();
        for(Animal a:A){
//            System.out.println(a.getStringGens());
//            System.out.println(genotype);
            if(a.getStringGens().equals(genotype)){
                Platform.runLater(() -> {
                    matrix[a.getPosition().x][a.getPosition().y].update(imageAnimals.get(a.getDirection()), -2);
                });
//                System.out.println("XX");
            }
        }
    }

    public void dayEnded(SimulationEngine engine, AbstractWorldMap map){
        Platform.runLater(() -> stats(engine, map));
    }

    @Override
    public void positionChanged(Vector2d oldPosition, Vector2d newPosition, AbstractWorldMap map) {
        if(map == map1) {
            Platform.runLater(() -> {
                updateGrid(map1, matrix1, oldPosition);
                updateGrid(map1, matrix1, newPosition);
            });
        }
        if(map == map2){
            Platform.runLater(() -> {
                updateGrid(map2, matrix2, oldPosition);
                updateGrid(map2, matrix2, newPosition);
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
