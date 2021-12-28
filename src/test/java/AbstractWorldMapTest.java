import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import simulation.AbstractWorldMap;
import simulation.Vector2d;


public class AbstractWorldMapTest {
    int width = 10, height = 10;
    AbstractWorldMap map = new AbstractWorldMap(width,height,100,5,50,25,0) {
        @Override
        public boolean canMoveTo(Vector2d position) {
            return false;
        }
    };
    @Test
    public void jungleAreaTest(){
        Assertions.assertFalse(map.jungleLL.precedes(new Vector2d(0,0)));
        Assertions.assertFalse(map.jungleUR.follows(new Vector2d(width,height)));
        Assertions.assertFalse(map.jungleUR.precedes(map.jungleLL));
    }
}
