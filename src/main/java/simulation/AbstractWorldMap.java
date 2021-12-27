package simulation;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

abstract public class AbstractWorldMap implements IPositionChangeObserver {
    protected Map<Vector2d, Animal> animals = new HashMap<>();
    protected Map<Vector2d, Grass> grasses = new HashMap<>();
    protected CopyOnWriteArrayList<Animal> A = new CopyOnWriteArrayList<>();
    protected CopyOnWriteArrayList<Grass> G = new CopyOnWriteArrayList<>();
    protected List<Vector2d> noGrassJungle = new ArrayList<>();
    protected List<Vector2d> noGrassStep = new ArrayList<>();
    int width;
    int height;
    int startEnergy;
    int moveEnergy;
    int plantEnergy;
    int jungleRatio;
    int maxID;
    float deadA;
    float sumAge;
    int magicUses;
    int whichMap;
    //    public boolean magicHappened;
    double[] noGens = new double[8];
    CopyOnWriteArrayList<String> genotypes = new CopyOnWriteArrayList<>();
    Vector2d jungleLL;
    Vector2d jungleUR;
    IPositionChangeObserver observer;

    public AbstractWorldMap(int width, int height, int startEnergy, int moveEnergy, int plantEnergy, int jungleRatio, int magicUses, int whichMap){
        this.maxID = 0;
        this.deadA = 0;
        this.sumAge = 0;
        this.width = width;
        this.height = height;
        this.startEnergy = startEnergy;
        this.moveEnergy = moveEnergy;
        this.plantEnergy = plantEnergy;
        this.jungleRatio = jungleRatio;
        this.magicUses = magicUses;
        this.whichMap = whichMap;
//        this.magicHappened = false;
        jungleLL = new Vector2d((width-1)*(100-jungleRatio)/200,(height-1)*(100-jungleRatio)/200);
        jungleUR = new Vector2d((width-1)*(100+jungleRatio)/200,(height-1)*(100+jungleRatio)/200);
        for(int j=0;j<height;j++)
            for(int i=0;i<width;i++) {
                Vector2d pos = new Vector2d(i, j);
                if (pos.precedes(jungleUR) && pos.follows(jungleLL))
                    this.noGrassJungle.add(pos);
                else
                    this.noGrassStep.add(pos);
            }
        for(int i=0;i<8;i++)
            noGens[i]=0;
    }

    public CopyOnWriteArrayList<Animal> getA(){
        return A;
    }
    public CopyOnWriteArrayList<Grass> getG(){
        return G;
    }
    public Map<Vector2d, Animal> getAnimals(){
        return animals;
    }
    public Map<Vector2d, Grass> getGrasses(){return grasses;}
    public String toString(){return super.toString();}
    public double avgAge(){
        if(deadA == 0)
            return 0;
        return sumAge/deadA;
    }
//    public double[] avgGens(){
//        double[] avGens = new double[8];
//        for(int i=0;i<8;i++){
//            avGens[i]=noGens[i]/maxID;
//        }
//        return avGens;
//    }

    public CopyOnWriteArrayList<String> getGenotypes(){
        return genotypes;
    }

    public List<Animal> getStrongest(Vector2d v, boolean if2nd){
        List<Animal> animals1 = new CopyOnWriteArrayList<>();
        List<Animal> animals2 = new CopyOnWriteArrayList<>();
        for(Animal a:A){
            if(a.getPosition().equals(v))
                animals1.add(a);
        }
        if(animals1.size()>0) {
            int firstE = animals1.get(0).getEnergy();
            int secondE = -1;
            if(animals1.size()>1)
                secondE = animals1.get(0).getEnergy();
            for (Animal a : animals1) {
                if (a.getEnergy() >= firstE)
                    firstE = a.getEnergy();
                else if (a.getEnergy() > secondE)
                    secondE = a.getEnergy();
            }
            for (Animal a : animals1) {
                if (a.getEnergy() == firstE)
                    animals2.add(a);
            }
            if (if2nd) {
                for (Animal a : animals1) {
                    if (a.getEnergy() == secondE)
                        animals2.add(a);
                }
            }
        }
        return animals2;
    }

    public void newGrass(){
        Random rand = new Random();
        if(noGrassJungle.size()>0) {
            int x = rand.nextInt(noGrassJungle.size());
            boolean flag = true;
            for(Animal a:A){
                if (a.getPosition().equals(noGrassJungle.get(x))) {
                    flag = false;
                    break;
                }
            }
            if(flag) {
                Grass g1 = new Grass(noGrassJungle.get(x));
                G.add(g1);
                observer.positionChanged(g1.getPosition(), g1.getPosition(), this);
                grasses.put(noGrassJungle.get(x), g1);
                noGrassJungle.remove(x);
            }
        }
        if(noGrassStep.size()>0) {
            int x = rand.nextInt(noGrassStep.size());
            boolean flag = true;
            for(Animal a:A){
                if (a.getPosition().equals(noGrassStep.get(x))) {
                    flag = false;
                    break;
                }
            }
            if(flag) {
                Grass g2 = new Grass(noGrassStep.get(x));
                G.add(g2);
                observer.positionChanged(g2.getPosition(), g2.getPosition(), this);
                grasses.put(noGrassStep.get(x), g2);
                noGrassStep.remove(x);
            }
        }
        if(magicUses>0)
            checkMagic();
    }
    public void remove(){
        boolean flag = true;
        while(flag) {
            flag = false;
            for (int i = 0; i < A.size(); i++) {
                if (A.get(i).getEnergy() <= 0) {
                    ArrayList<Integer> deadGens;
                    deadGens = A.get(i).getGens();
                    for(int k=0;k<8;k++){
                        noGens[k]-=deadGens.get(k);
                    }
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
                    if(places.get(j).p1.equals(A.get(i).getPosition())){
                        break;
                    }
                    if(j == places.size()-1){
                        Pair<Vector2d, Integer> P1 = new Pair<>(A.get(i).getPosition(), 1);
                        places.add((P1));
                    }
                }
            }
            for (Pair<Vector2d, Integer> p : places) {
                if(grasses.containsKey(p.p1)){
//                    int saturate = this.plantEnergy / p.p2;
                    List<Animal> animals = getStrongest(p.p1,false);
                    int saturate = this.plantEnergy / animals.size();
                    for (Animal a : animals) {
                        a.feed(saturate);
                    }
                    if (p.p1.precedes(jungleUR) && p.p1.follows(jungleLL))
                        noGrassJungle.add(p.p1);
                    else
                        noGrassStep.add(p.p1);
                    for(int i=0;i<G.size();i++){
                        if(G.get(i).getPosition().equals(p.p1)) {
                            G.remove(i);
                            break;
                        }
                    }
                    grasses.remove(p.p1);
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
                if (animals1.get(0).getEnergy() >= startEnergy / 2 && animals1.get(1).getEnergy() >= startEnergy / 2)
                    place(animals1.get(0).copulate(animals1.get(1), maxID + 1), false);
            }
        }
    }

    void checkMagic(){
        if(A.size()==5){
            ArrayList<Vector2d> pos = new ArrayList<>();
            Random rand = new Random();
            int x;
//            int y;
//            Vector2d newPos = new Vector2d(0,0);
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
//            for(int i=0;i<5;i++) {
//                boolean found = false;
//                while(!found) {
//                    x = rand.nextInt(width);
//                    y = rand.nextInt(height);
//                    newPos = new Vector2d(x,y);
//                    found = true;
//                    for (Animal a : A) {
//                        if (a.getPosition().equals(newPos)) {
//                            found = false;
//                            break;
//                        }
//                    }
//                    for (Vector2d v : pos) {
//                        if (v.equals(newPos)) {
//                            found = false;
//                            break;
//                        }
//                    }
//                }
//                pos.add(newPos);
//            }
            for(int i=0;i<5;i++){
                this.place(new Animal(this, pos.get(i), A.get(i).getGens(), this.startEnergy, this.maxID+1), false);
            }
            magicUses-=1;
            observer.magicHappened(3-magicUses, this);
        }
    }

    public abstract boolean canMoveTo(Vector2d position);

    public IMapElement objectAt(Vector2d position){
        for(Animal a:A){
            if(a.getPosition().equals(position))
                return a;
        }
        for(Grass g:G){
            if(g.getPosition().equals(position))
                return g;
        }
        return new Nothing();
    }

    public void place(Animal animal, boolean beginning) {
        maxID +=1;
        int[] newParsedGens = animal.parseGens();
        ArrayList<Integer> newGens = animal.getGens();
        StringBuilder S = new StringBuilder();
        for(int i=0;i<8;i++) {
            this.noGens[i] += newParsedGens[i];
        }
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
        animals.put(pos, animal);
        A.add(animal);
        animal.addObserver(this);
        if(!beginning)
            observer.positionChanged(animal.getPosition(), animal.getPosition(), this);
    }

    public void setObserver(IPositionChangeObserver observer){this.observer=observer;}
    public void positionChanged(Vector2d oldPosition, Vector2d newPosition, AbstractWorldMap map) {
        observer.positionChanged(oldPosition, newPosition, this);
    }
}
