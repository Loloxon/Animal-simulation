package simulation;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

abstract public class AbstractWorldMap implements IPositionChangeObserver {
    protected Map<Vector2d, Grass> grasses = new HashMap<>();
    protected CopyOnWriteArrayList<Animal> A = new CopyOnWriteArrayList<>();
    protected CopyOnWriteArrayList<Grass> G = new CopyOnWriteArrayList<>();
    protected ArrayList<Vector2d> noGrassJungle = new ArrayList<>();
    protected ArrayList<Vector2d> noGrassStep = new ArrayList<>();
    private final CopyOnWriteArrayList<String> genotypes = new CopyOnWriteArrayList<>();
    protected int width;
    protected int height;
    private final int startEnergy;
    protected int moveEnergy;
    private final int plantEnergy;
    private float deadA;
    private float sumAge;
    private int magicUses;
    private int days;
    public Vector2d jungleLL;
    public Vector2d jungleUR;
    private IPositionChangeObserver observer;

    private Animal trackedAnimal;
    int[] trackedInfo = new int[4];//children, offsprings, when died, age

    public AbstractWorldMap(int width, int height, int startEnergy, int moveEnergy, int plantEnergy, int jungleRatio, int magicUses){
        this.deadA = 0;
        this.sumAge = 0;
        this.width = width;
        this.height = height;
        this.startEnergy = startEnergy;
        this.moveEnergy = moveEnergy;
        this.plantEnergy = plantEnergy;
        this.magicUses = magicUses;
        this.days = 0;
        jungleLL = new Vector2d((int) Math.floor((10-Math.sqrt(jungleRatio))*width/20),(int) Math.floor((10-Math.sqrt(jungleRatio))*height/20));
        jungleUR = new Vector2d((int) Math.floor((10+Math.sqrt(jungleRatio))*width/20)-1,(int) Math.floor((10+Math.sqrt(jungleRatio))*height/20)-1);
        for(int j=0;j<height;j++)
            for(int i=0;i<width;i++) {
                Vector2d pos = new Vector2d(i, j);
                if (pos.precedes(jungleUR) && pos.follows(jungleLL))
                    this.noGrassJungle.add(pos);
                else
                    this.noGrassStep.add(pos);
            }
    }

    public CopyOnWriteArrayList<Animal> getA(){
        return A;
    }
    public CopyOnWriteArrayList<Grass> getG(){
        return G;
    }
    public int getDays(){return days;}
    public CopyOnWriteArrayList<String> getGenotypes(){
        return genotypes;
    }
    public int[] getTrackedInfo(){ //children, offsprings, when died, how old
        return trackedInfo;
    }
    public Animal getTrackedAnimal(){
        return this.trackedAnimal;
    }
    public void setTrackedAnimal(Animal a){
        for(Animal tmp:A){
            tmp.setTracked(false);
        }
        a.setTracked(true);
        this.trackedAnimal = a;
        trackedInfo[0] = 0;
        trackedInfo[1] = 0;
        trackedInfo[2] = -1;
        trackedInfo[3] = a.getAge();
    }

    public double avgAge(){
        if(deadA == 0)
            return 0;
        return sumAge/deadA;
    }

    public void place(Animal animal, boolean beginning) {
        ArrayList<Integer> newGens = animal.getGens();
        StringBuilder S = new StringBuilder();
        for(int i=0;i<32;i++){
            S.append(newGens.get(i));
        }
        this.genotypes.add(S.toString());
        Vector2d pos = animal.getPosition();
        for(int i=0;i<G.size();i++){
            if(G.get(i).getPosition().equals(pos)){
                if (G.get(i).getPosition().precedes(jungleUR) && G.get(i).getPosition().follows(jungleLL))
                    noGrassJungle.add(G.get(i).getPosition());
                else
                    noGrassStep.add(G.get(i).getPosition());
                G.remove(i);
                break;
            }
        }
        A.add(animal);
        animal.addObserver(this);
        if(!beginning)
            observer.positionChanged(animal.getPosition(), animal.getPosition(), this);
    }

    public ArrayList<Animal> getStrongest(Vector2d v, boolean if2nd){
        ArrayList<Animal> animals = new ArrayList<>();
        ArrayList<Animal> animalsFinal = new ArrayList<>();
        for(Animal a:A){
            if(a.getPosition().equals(v))
                animals.add(a);
        }
        if(animals.size()>0) {
            int firstE = animals.get(0).getEnergy();
            int secondE = -1;
            if(animals.size()>1)
                secondE = animals.get(0).getEnergy();
            for (Animal a : animals) {
                if (a.getEnergy() >= firstE)
                    firstE = a.getEnergy();
                else if (a.getEnergy() > secondE)
                    secondE = a.getEnergy();
            }
            for (Animal a : animals) {
                if (a.getEnergy() == firstE)
                    animalsFinal.add(a);
            }
            if (if2nd) {
                for (Animal a : animals) {
                    if (a.getEnergy() == secondE)
                        animalsFinal.add(a);
                }
            }
        }
        return animalsFinal;
    }

    public void remove(){
        boolean flag = true;
        while(flag) {
            flag = false;
            for (int i = 0; i < A.size(); i++) {
                if (A.get(i).getEnergy() <= 0) {
                    ArrayList<Integer> deadGens;
                    deadGens = A.get(i).getGens();
                    StringBuilder S = new StringBuilder();
                    for(int k=0;k<32;k++){
                        S.append(deadGens.get(k));
                    }
                    for(int k=0;k<genotypes.size();k++){
                        if(genotypes.get(k).equals(S.toString())){
                            genotypes.remove(k);
                            break;
                        }
                    }
                    if(A.get(i).equals(trackedAnimal)){
                        trackedInfo[2] = days;
                    }
                    this.deadA+=1;
                    this.sumAge+=A.get(i).getAge();
                    observer.positionChanged(A.get(i).getPosition(),A.get(i).getPosition(), this);
                    A.remove(i);
                    flag = true;
                    break;
                }
            }
        }
    }

    public void eat(){
        if(A.size()>0) {
            List<Pair<Vector2d, Integer>> places = new ArrayList<>();
            Pair<Vector2d, Integer> P = new Pair<>(A.get(0).getPosition(), 1);
            places.add(P);
            for (int i = 1; i < A.size(); i++) {
                for (int j=0;j<places.size();j++){
                    if(places.get(j).first.equals(A.get(i).getPosition())){
                        break;
                    }
                    if(j == places.size()-1){
                        Pair<Vector2d, Integer> P1 = new Pair<>(A.get(i).getPosition(), 1);
                        places.add((P1));
                    }
                }
            }
            for (Pair<Vector2d, Integer> p : places) {
                if(grasses.containsKey(p.first)){
                    List<Animal> animals = getStrongest(p.first,false);
                    int saturate = this.plantEnergy / animals.size();
                    for (Animal a : animals) {
                        a.feed(saturate);
                    }
                    if (p.first.precedes(jungleUR) && p.first.follows(jungleLL))
                        noGrassJungle.add(p.first);
                    else
                        noGrassStep.add(p.first);
                    for(int i=0;i<G.size();i++){
                        if(G.get(i).getPosition().equals(p.first)) {
                            G.remove(i);
                            break;
                        }
                    }
                    grasses.remove(p.first);
                }
            }
        }
    }

    public void copulate(){
        Set<Vector2d> cords = new LinkedHashSet<>();
        for(Animal a:A){
            cords.add(a.getPosition());
        }
        for(Vector2d v:cords){
            List<Animal> animals1= getStrongest(v,true);
            if(animals1.size()>1) {
                if (animals1.get(0).getEnergy() >= startEnergy / 2 && animals1.get(1).getEnergy() >= startEnergy / 2) {
                    if(animals1.get(0).getTracked() || animals1.get(1).getTracked()) {
                        trackedInfo[1] += 1;
                        if (animals1.get(0).equals(trackedAnimal) || animals1.get(1).equals(trackedAnimal)) {
                            trackedInfo[0] += 1;
                        }
                        place(animals1.get(0).copulate(animals1.get(1), true), false);
                    }
                    else
                        place(animals1.get(0).copulate(animals1.get(1), false), false);
                }
            }
        }
    }

    void newGrassInArea(ArrayList<Vector2d> noGrass){
        ArrayList<Vector2d> trueNoGrass = new ArrayList<>(noGrass);
        for(int i=trueNoGrass.size()-1;i>=0;i--){
            for(Animal a:A){
                if(a.getPosition().equals(trueNoGrass.get(i))){
                    trueNoGrass.remove(i);
                    break;
                }
            }
        }
        Random rand = new Random();
        if(trueNoGrass.size()>0) {
            int x = 0;
            boolean flag = true;
            while(flag) {
                x = rand.nextInt(trueNoGrass.size());
                flag = false;
                for (Animal a : A) {
                    if (a.getPosition().equals(trueNoGrass.get(x))) {
                        flag = true;
                        break;
                    }
                }
            }
            Grass g1 = new Grass(trueNoGrass.get(x));
            G.add(g1);
            observer.positionChanged(g1.getPosition(), g1.getPosition(), this);
            grasses.put(trueNoGrass.get(x), g1);
            for(int i=0;i<noGrass.size();i++)
                if(noGrass.get(i).equals(trueNoGrass.get(x))) {
                    noGrass.remove(i);
                    break;
                }
        }
    }

    public void newGrass(){
        newGrassInArea(noGrassJungle);
        newGrassInArea(noGrassStep);
    }

    public void nextDay(){
        this.days+=1;
        if(trackedInfo[2]==-1)
            trackedInfo[3]+=1;
        if(magicUses>0)
            checkMagic();
    }

    void checkMagic(){
        if(A.size()==5){
            ArrayList<Vector2d> pos = new ArrayList<>();
            Random rand = new Random();
            int x;
            ArrayList<Integer> freePositions = new ArrayList<>();
            for(int j=0;j<height;j++){
                for(int i=0;i<width;i++){
                    freePositions.add(j*width+i);
                }
            }
            ArrayList<Integer> notFreePositions = new ArrayList<>();
            for(Animal a:A){
                notFreePositions.add(a.getPosition().y*width+a.getPosition().x);
            }
            notFreePositions.sort(Comparator.reverseOrder());
            for(int i=1;i<notFreePositions.size();i++){
                if(!Objects.equals(notFreePositions.get(i), notFreePositions.get(i - 1))){
                    freePositions.remove(notFreePositions.get(i-1));
                }
            }
            for(int i=0;i<5;i++){
                int id = rand.nextInt(freePositions.size());
                x = freePositions.get(id);
                freePositions.remove(id);
                pos.add(new Vector2d(x%width, x/width));
            }
            for(int i=0;i<5;i++){
                this.place(new Animal(this, pos.get(i), A.get(i).getGens(), this.startEnergy, false), false);
            }
            magicUses-=1;
            observer.magicHappened(3-magicUses, this);
        }
    }

    public abstract boolean canMoveTo(Vector2d position);

    public IMapElement objectAt(Vector2d position){
        ArrayList<Animal> animalsOnPosition = getStrongest(position,false);
        if(animalsOnPosition.size()>0)
            return animalsOnPosition.get(0);
        for(Grass g:G){
            if(g.getPosition().equals(position))
                return g;
        }
        return new Nothing();
    }

    public void setObserver(IPositionChangeObserver observer){this.observer=observer;}
    public void positionChanged(Vector2d oldPosition, Vector2d newPosition, AbstractWorldMap map) {
        observer.positionChanged(oldPosition, newPosition, this);
    }
}
