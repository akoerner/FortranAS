
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HelloWorldTest {

    @Test
    public void testHelloWorld() {
        String message = HelloWorld.sayHello();
        assertEquals("Hello, World!", message);
        assertNotEquals("Gello, World!", message);
    }
}
