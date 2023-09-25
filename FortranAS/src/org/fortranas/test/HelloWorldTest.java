package org.fortranas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HelloWorldTest {

    @Test
    public void testHelloWorld() {
        String message = "Hello, World!";
        assertEquals("Hello, World!", message);
    }
}
