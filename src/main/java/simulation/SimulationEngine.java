package simulation;

import java.util.*;

public class SimulationEngine implements Runnable, IPositionChangeObserver {
    private List<Animal> A;
    private final AbstractWorldMap map;
    private final int startEnergy;
    private final int startingAnimalsNo;
    private boolean running;
    private IPositionChangeObserver observer;
    private final int delay;
    private final int chartValueNo;

    private ArrayList<ArrayList<Double>> chartsInfo = new ArrayList<>();
    private Double[] chartsSummarize;
    private ArrayList<String> chartsOrder = new ArrayList<>();

    public SimulationEngine(AbstractWorldMap map, int startingAnimalsNo, int startEnergy, int chartValueNo, int delay) {
        this.map = map;
        this.map.setObserver(this);
        this.startingAnimalsNo = startingAnimalsNo;
        this.startEnergy = startEnergy;
        addAnimals();
        this.A = map.getA();
        this.running = true;
        this.delay = delay;
        this.chartValueNo = chartValueNo;
    }

    public void setChartsInfo(ArrayList<String> chartsOrder){
        this.chartsOrder = chartsOrder;
        chartsSummarize = new Double[chartsOrder.size()];
        for(int k=0;k<chartsOrder.size();k++) {
            chartsSummarize[k] = 0.0;
            chartsInfo.add(new ArrayList<>());
            for (int i = 0; i < chartValueNo; i++) {
                chartsInfo.get(k).add(0.0);
            }
        }
    }
    public ArrayList<ArrayList<Double>> getChartsInfo(){return chartsInfo;}
    public Double[] getChartsSummarize(){
        int days = map.getDays();
        for(int i=0;i<chartsInfo.size();i++){
            chartsSummarize[i] /= days;
        }
        return chartsSummarize;
    }
    public ArrayList<String> getChartsOrder(){return  chartsOrder;}
    public void setRunning(boolean bool){this.running = bool;}
    public boolean getRunning(){return this.running;}

    public void setObserver(IPositionChangeObserver observer){this.observer = observer;}

    public void addAnimals() {
        Vector2d[] positions = new Vector2d[startingAnimalsNo];
        int width = map.width;
        int height = map.height;
        ArrayList<ArrayList<Integer>> gens = new ArrayList<>();
        ArrayList<Integer> freePositions = new ArrayList<>();
        for(int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                freePositions.add(j*width+i);
            }
        }
        for (int j = 0; j < startingAnimalsNo; j++) {
            Random rand = new Random();
            ArrayList<Integer> gensi = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                int g = rand.nextInt(8);
                gensi.add(g);
            }
            Collections.sort(gensi);
            gens.add(gensi);

            int id = rand.nextInt(freePositions.size());
            int x = freePositions.get(id);
            freePositions.remove(id);
            positions[j] = new Vector2d(x%width, x/width);
        }
        for (int i = 0; i < startingAnimalsNo; i++) {
            map.place(new Animal(map, positions[i], gens.get(i), startEnergy, false), true);
        }
    }

    void refreshData(){
        double e=0;
        double c=0;
        for(Animal a:map.getA()) {
            e += a.getEnergy();
            c += a.getChildren();
        }
        if(A.size()>0) {
            e /= A.size();
            c /= A.size();
        }
        else{
            e = chartsInfo.get(2).get(chartsInfo.get(2).size()-1);
            c = chartsInfo.get(4).get(chartsInfo.get(4).size()-1);
        }
        for(int i=0;i<chartsOrder.size();i++){
            switch (chartsOrder.get(i)) {
                case "Number of animals" -> chartsInfo.get(i).add((double) A.size());
                case "Number of grass" -> chartsInfo.get(i).add((double) map.getG().size());
                case "Average energy of living animals" -> chartsInfo.get(i).add(e);
                case "Average age of dead animals" -> chartsInfo.get(i).add(map.avgAge());
                case "Average children no. of living animals" -> chartsInfo.get(i).add(c);
            }
            chartsSummarize[i] += chartsInfo.get(i).get(chartsInfo.get(i).size()-1);
        }
    }

    @Override
    public void run() {
        try{
            while(true){
                if(running){
                    A = map.getA();
                    map.remove();       //        removeDead();
                    for (Animal a : A) {    //        move();
                        a.move();
                    }
                    map.eat();          //        eating();
                    map.copulate();     //        copulating();
                    map.newGrass();     //        newGrass();
                    for (Animal a : A) {
                        a.oldering();
                    }
                    map.nextDay();
                    refreshData();
                    observer.dayEnded(this, this.map);
                }
                Thread.sleep(delay);
            }
        }catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void positionChanged(Vector2d oldPosition, Vector2d newPosition, AbstractWorldMap map) {
        this.observer.positionChanged(oldPosition, newPosition, map);
    }

    @Override
    public void magicHappened(int howManyTimes, AbstractWorldMap map){
        observer.magicHappened(howManyTimes, map);
    }
}

