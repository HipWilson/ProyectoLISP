package Interprete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class EntornoLispTest {

    @Test
    public void testVariableAssignment() {
        EntornoLisp env = new EntornoLisp();
        env.asignarVariable("x", 10);
        assertEquals(10, env.obtenerVariable("x"));
    }

    @Test
    public void testNestedEnvironments() {
        EntornoLisp global = new EntornoLisp();
        global.asignarVariable("x", 10);
        
        EntornoLisp local = new EntornoLisp(global);
        local.asignarVariable("y", 20);
        
        assertEquals(10, local.obtenerVariable("x")); // Variable del entorno padre
        assertEquals(20, local.obtenerVariable("y")); // Variable local
    }

    @Test
    public void testFunctionRegistration() {
        EntornoLisp env = new EntornoLisp();
        List<String> params = List.of("a", "b");
        env.registrarFuncion("suma", params, "(+ a b)");
        assertTrue(env.existeFuncion("suma"));
    }
}