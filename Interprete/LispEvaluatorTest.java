package Interprete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class LispEvaluatorTest {

    @Test
    public void testEvaluateArithmetic() {
        LispEvaluator evaluator = new LispEvaluator();
        EntornoLisp env = new EntornoLisp();
        
        Object result = evaluator.evaluate(List.of("+", 1, 2), env);
        assertEquals(3, result);
    }

    @Test
    public void testEvaluateSetq() {
        LispEvaluator evaluator = new LispEvaluator();
        EntornoLisp env = new EntornoLisp();
        
        evaluator.evaluate(List.of("setq", "x", 10), env);
        assertEquals(10, env.obtenerVariable("x"));
    }

    @Test
    public void testEvaluateDefun() {
        LispEvaluator evaluator = new LispEvaluator();
        EntornoLisp env = new EntornoLisp();
        
        evaluator.evaluate(
            List.of("defun", "suma", List.of("a", "b"), List.of("+", "a", "b")),
            env
        );
        assertTrue(env.existeFuncion("suma"));
    }
}
