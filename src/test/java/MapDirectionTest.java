import simulation.MapDirection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapDirectionTest {
    @Test
    public void nextTest(){
        assertEquals(MapDirection.NORTH, MapDirection.NWEST.next());
        assertEquals(MapDirection.NEAST, MapDirection.NORTH.next());
        assertEquals(MapDirection.EAST, MapDirection.NEAST.next());
        assertEquals(MapDirection.SEAST, MapDirection.EAST.next());
        assertEquals(MapDirection.SOUTH, MapDirection.SEAST.next());
        assertEquals(MapDirection.SWEST, MapDirection.SOUTH.next());
        assertEquals(MapDirection.WEST, MapDirection.SWEST.next());
        assertEquals(MapDirection.NWEST, MapDirection.WEST.next());
    }
    @Test
    public void getImageTest(){
        assertEquals("file:src/main/resources/n.png", MapDirection.NORTH.getImage());
        assertEquals("file:src/main/resources/ne.png", MapDirection.NEAST.getImage());
        assertEquals("file:src/main/resources/e.png", MapDirection.EAST.getImage());
        assertEquals("file:src/main/resources/se.png", MapDirection.SEAST.getImage());
        assertEquals("file:src/main/resources/s.png", MapDirection.SOUTH.getImage());
        assertEquals("file:src/main/resources/sw.png", MapDirection.SWEST.getImage());
        assertEquals("file:src/main/resources/w.png", MapDirection.WEST.getImage());
        assertEquals("file:src/main/resources/nw.png", MapDirection.NWEST.getImage());
    }
}