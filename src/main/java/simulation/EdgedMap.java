package simulation;

public class EdgedMap extends AbstractWorldMap implements IPositionChangeObserver {
    public EdgedMap(int width, int height, int startEnergy, int moveEnergy, int plantEnergy, int jungleRatio, int magicUses) {
        super(width, height, startEnergy, moveEnergy, plantEnergy, jungleRatio, magicUses);
    }

    @Override
    public boolean canMoveTo(Vector2d position)  {
        return position.precedes(new Vector2d(this.width - 1, this.height - 1)) && position.follows(new Vector2d(0, 0));
    }
}
