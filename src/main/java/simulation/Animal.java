package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Animal implements IMapElement {
    private final AbstractWorldMap map;
    private MapDirection direction;
    private Vector2d position;
    private final ArrayList<Integer> gens;
    private final int ID;
    private int age = 0;
    private int energy;
    private int children;
    private IPositionChangeObserver observer;
    private boolean tracked;

    public Animal(AbstractWorldMap map, Vector2d initialPosition, ArrayList<Integer> gens, int energy, int ID, boolean tracked){
        this.map = map;
        this.position = initialPosition;
        this.gens = gens;
        this.energy = energy;
        this.ID = ID;
        this.children = 0;
        this.tracked = tracked;
        Random rand = new Random();
        int chance = rand.nextInt(8);
        direction = MapDirection.NORTH;
        for(int i=0;i<chance;i++){
            direction = direction.next();
        }
    }
    @Override
    public Vector2d getPosition(){return position;}
    public void setPosition(Vector2d pos){this.position = pos;}
    public MapDirection getDirection(){return this.direction;}
    public ArrayList<Integer> getGens(){return gens;}
    public String getStringGens(){
        StringBuilder S = new StringBuilder();
        for(int g:gens){
            S.append(g);
        }
        return S.toString();
    }
    public int getAge(){return age;}
    //    public int getStartEnergy(){return map.startEnergy;}
    public int getEnergy(){return energy;}
    public void setEnergy(int energy){this.energy = energy;}
    public boolean getTracked(){return this.tracked;}
    public void setTracked(boolean bool){this.tracked = bool;}
    public void feed(int food){
        this.energy += food;
    }
    public int getChildren(){return children;}          //ilosc dzieci
    public void makeChildren(){this.children+=1;}       //zwiekszenie ilosci dzieci
    public int[] parseGens(){       //zliczenie krotnosci kazdego z genóœ
        int[] noGens = new int[8];
        for(Integer g:gens){
            noGens[g]+=1;
        }
        return noGens;
    }
    public String toString(){
        return switch (this.direction) {
            case NORTH -> "N";
            case EAST -> "E";
            case SOUTH -> "S";
            case WEST -> "W";
            case SEAST -> "SE";
            case SWEST -> "SW";
            case NEAST -> "NE";
            case NWEST -> "NW";
        };
    }

    public void oldering(){
        this.age+=1;
        this.energy = Math.max(this.energy-map.moveEnergy, 0);
    }

    public void move(){ //losowanie kierunku, przod/tył sie porusza
        Vector2d oldPosition = position;
        Random rand = new Random();
        int newDirection = gens.get(rand.nextInt(gens.size()));
        if(newDirection==0){
            if(map.canMoveTo(position.add(direction.toUnitVector())))
                position = position.add(direction.toUnitVector());
            this.setPosition(new Vector2d((position.x+map.width)%(map.width), (position.y+map.height)%(map.height)));
        }
        else
        if(newDirection==4){
            if(map.canMoveTo(position.subtract(direction.toUnitVector())))
                position = position.subtract(direction.toUnitVector());
            this.setPosition(new Vector2d((position.x+map.width)%(map.width), (position.y+map.height)%(map.height)));
        }
        else
            for(int i=0;i<newDirection;i++){
                this.direction = this.direction.next();
            }
        observer.positionChanged(oldPosition, this.position, this.map);
    }

    public Animal copulate(Animal ani2, int newID, boolean tracked){ //obecne zwierze + argument = nowe zwracane zwierze
        int energy = ani2.getEnergy();
        ArrayList<Integer> gens = ani2.getGens();
        Random rand = new Random();
        int side = rand.nextInt(2);
        ArrayList<Integer> newGens = new ArrayList<>();
        int div = energy/(energy+this.energy);
        for(int i=0;i<32;i++){
            if((energy>this.energy && side==0)||(energy<=this.energy && side==1)){
                if(i<div)
                    newGens.add(gens.get(i));
                else
                    newGens.add(this.gens.get(i));
            }
            else{
                if(i>=div)
                    newGens.add(gens.get(i));
                else
                    newGens.add(this.gens.get(i));
            }
        }
        Collections.sort(newGens);
        int newEnergy = this.energy/4 + energy/4;
        this.energy = this.energy*3/4;              //zmiejszenie energii
        ani2.setEnergy(energy*3/4);
        this.children+=1;                           //dodanie dzieci
        ani2.makeChildren();
        return new Animal(this.map, this.position, newGens, newEnergy, newID, tracked);
    }
    public void addObserver(IPositionChangeObserver observer){this.observer=observer;}
}
