package simulation;

public interface IPositionChangeObserver {
    void positionChanged(Vector2d oldPosition, Vector2d newPosition, AbstractWorldMap map);

    default void magicHappened(int howManyTimes, AbstractWorldMap map){

    }

    default void dayEnded(SimulationEngine engine, AbstractWorldMap map){

    }
}
