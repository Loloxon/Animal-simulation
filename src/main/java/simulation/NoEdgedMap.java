package simulation;

public class NoEdgedMap extends AbstractWorldMap implements IPositionChangeObserver{
    public NoEdgedMap(int width, int height, int startEnergy, int moveEnergy, int plantEnergy, int jungleRatio, int magicUses) {
        super(width, height, startEnergy, moveEnergy, plantEnergy, jungleRatio, magicUses);
    }

    @Override
    public boolean canMoveTo(Vector2d position)  {
        return true;
    }
}
