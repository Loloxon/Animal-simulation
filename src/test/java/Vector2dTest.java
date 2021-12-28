import simulation.Vector2d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Vector2dTest {
    Vector2d a = new Vector2d(1,2);
    Vector2d b = new Vector2d(-1,-2);
    Vector2d c = new Vector2d(5,1);
    Vector2d d = new Vector2d(6,3);
    @Test
    public void equalsTest(){
        assertEquals(a, a);
        Assertions.assertNotEquals(a, b);
    }
    @Test
    public void toStringTest(){
        assertEquals("(1,2)", a.toString());
    }
    @Test
    public void precedesTest(){
        Assertions.assertTrue(b.precedes(a));
        Assertions.assertFalse(c.precedes(a));
    }
    @Test
    public void followsTest(){
        Assertions.assertTrue(a.follows(b));
        Assertions.assertFalse(c.follows(a));
    }
    @Test
    public void addTest(){
        assertEquals(d, a.add(c));
        assertEquals(d, c.add(a));
    }
    @Test
    public void subtractTest(){
        assertEquals(a, d.subtract(c));
        assertEquals(c, d.subtract(a));
    }
}