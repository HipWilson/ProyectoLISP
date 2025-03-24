package Interprete;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TokenizadorLispTest {

    @Test
    public void testTokenizeSimpleExpression() {
        TokenizadorLisp tokenizer = new TokenizadorLisp();
        List<String> tokens = tokenizer.analizar("(+ 1 2)");
        assertEquals(5, tokens.size());
        assertEquals("(", tokens.get(0));
        assertEquals("+", tokens.get(1));
        assertEquals("1", tokens.get(2));
        assertEquals("2", tokens.get(3));
        assertEquals(")", tokens.get(4));
    }

    @Test
    public void testTokenizeWithComments() {
        TokenizadorLisp tokenizer = new TokenizadorLisp();
        List<String> tokens = tokenizer.analizar("; Esto es un comentario\n(+ 1 2)");
        assertEquals(5, tokens.size()); // El comentario debe ignorarse
    }
}