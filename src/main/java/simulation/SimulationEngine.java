package simulation;

import java.util.*;

public class SimulationEngine implements Runnable, IPositionChangeObserver {
    List<Animal> A;
    List<Grass> G;
    Map<Vector2d, Animal> animals;
    Map<Vector2d, Grass> grasses;
    AbstractWorldMap map;
    int startEnergy;
    int animalNO;
    boolean running;
    IPositionChangeObserver observer;
    int days;
    int delay;
    int chartValueNo;

    ArrayList<ArrayList<Double>> chartsInfo = new ArrayList<>();
    Double[] chartsSummarize;
    ArrayList<String> chartsOrder = new ArrayList<>();

//    public List<Animal> getA() {
//        return A;
//    }
//    public List<Grass> getG() {
//        return G;
//    }
//    public Map<Vector2d, Animal> getAnimals(){
//        return animals;
//    }
//    public Map<Vector2d, Grass> getGrasses(){return grasses;}

    public SimulationEngine(AbstractWorldMap map, int animalNO, int startEnergy, int chartValueNo, int delay) {
        this.map = map;
        this.map.setObserver(this);
        this.animalNO = animalNO;
        this.startEnergy = startEnergy;
        addAnimals();
        this.A = map.getA();
        this.G = map.getG();
        this.animals = map.getAnimals();
        this.grasses = map.getGrasses();
        this.running = true;
        this.days = 0;
        this.delay = delay;
        this.chartValueNo = chartValueNo;
    }

    public void setChartsInfo(ArrayList<String> chartsOrder){
        this.chartsOrder = chartsOrder;
//        chartsInfo = new ArrayList[chartsOrder.size()];
//        ArrayList<Double>[] chartsInfo = new ArrayList[];
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
        for(int i=0;i<chartsInfo.size();i++){
            chartsSummarize[i] /= days;
        }
        return chartsSummarize;
    }
    public ArrayList<String> getChartsOrder(){return  chartsOrder;}
    public void setRunning(boolean bool){this.running = bool;}
    public void setObserver(IPositionChangeObserver observer){this.observer = observer;}

    public void addAnimals() {
        Vector2d[] positions = new Vector2d[animalNO];
        int width = map.width;
        int height = map.height;
        ArrayList<ArrayList<Integer>> gens = new ArrayList<>();
        ArrayList<Integer> freePositions = new ArrayList<>();
        for(int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                freePositions.add(j*width+i);
            }
        }
        for (int j = 0; j < animalNO; j++) {
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
//            positions[j] = new Vector2d(rand.nextInt(width), rand.nextInt(height));
        }
        for (int i = 0; i < animalNO; i++) {
            map.place(new Animal(map, positions[i], gens.get(i), startEnergy, i+1), true);
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
                case "animals" -> chartsInfo.get(i).add((double) A.size());
                case "grass" -> chartsInfo.get(i).add((double) map.getG().size());
                case "avgEnergy" -> chartsInfo.get(i).add(e);
                case "avgAge" -> chartsInfo.get(i).add(map.avgAge());
                case "avgChildren" -> chartsInfo.get(i).add(c);
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
                    G = map.getG();
                    grasses = map.getGrasses();
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
                    this.days+=1;
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

