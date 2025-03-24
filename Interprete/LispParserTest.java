package Interprete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class LispParserTest {

    @Test
    public void testParseSimpleExpression() {
        LispParser parser = new LispParser();
        Object result = parser.parse("(+ 1 2)");
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals("+", list.get(0));
        assertEquals(1, list.get(1));
        assertEquals(2, list.get(2));
    }

    @Test
    public void testParseNestedExpression() {
        LispParser parser = new LispParser();
        Object result = parser.parse("(* (+ 1 2) 3)");
        assertTrue(result instanceof List);
        List<?> outerList = (List<?>) result;
        assertEquals("*", outerList.get(0));
        
        List<?> innerList = (List<?>) outerList.get(1);
        assertEquals("+", innerList.get(0));
        assertEquals(1, innerList.get(1));
        assertEquals(2, innerList.get(2));
        
        assertEquals(3, outerList.get(2));
    }

    @Test
    public void testParseQuote() {
        LispParser parser = new LispParser();
        Object result = parser.parse("'(1 2 3)");
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals("quote", list.get(0));
        assertTrue(list.get(1) instanceof List);
    }
}