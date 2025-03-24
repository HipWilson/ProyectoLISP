package Interprete;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LispProcessorTest {

    @Test
    public void testProcessSimpleExpression() {
        LispProcessor processor = new LispProcessor();
        Object result = processor.procesar("(+ 1 2)");
        assertEquals(3, result);
    }

    @Test
    public void testProcessUserDefinedFunction() {
        LispProcessor processor = new LispProcessor();
        processor.procesar("(defun suma (a b) (+ a b))");
        Object result = processor.procesar("(suma 3 4)");
        assertEquals(7, result);
    }
}