package Interprete;

import java.util.ArrayList;
import java.util.List;

public class LispParser {

    private int position;

    public Object parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        TokenizadorLisp tokenizer = new TokenizadorLisp();
        List<String> tokens = tokenizer.analizar(input);

        if (tokens.isEmpty()) {
            return null;
        }

        position = 0;
        Object result = parseExpression(tokens);

        if (position < tokens.size()) {
            throw new RuntimeException("Error: paréntesis extra detectado");
        }

        return result;
    }

    public Object parse(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        position = 0;
        return parseExpression(tokens);
    }

    private Object parseExpression(List<String> tokens) {
        if (position >= tokens.size()) {
            throw new RuntimeException("Fin inesperado de entrada");
        }

        String token = tokens.get(position++);

        if (token.equals("(")) {
            return parseList(tokens);
        }

        if (token.equals("'")) {
            List<Object> quoteList = new ArrayList<>();
            quoteList.add("quote");
            quoteList.add(parseExpression(tokens));
            return quoteList;
        }

        if (isNumeric(token)) {
            return parseNumber(token);
        }

        return token;
    }

    private List<Object> parseList(List<String> tokens) {
        List<Object> elements = new ArrayList<>();

        while (position < tokens.size()) {
            String currentToken = tokens.get(position);

            if (currentToken.equals(")")) {
                position++;
                return elements;
            }

            elements.add(parseExpression(tokens));
        }

        throw new RuntimeException("Se esperaba un paréntesis de cierre");
    }

    private Number parseNumber(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(token);
            } catch (NumberFormatException e2) {
                throw new RuntimeException("No se puede convertir '" + token + "' a un número");
            }
        }
    }

    private boolean isNumeric(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String printResult(Object expr) {
        if (expr instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) expr;
            StringBuilder sb = new StringBuilder("[");

            for (int i = 0; i < list.size(); i++) {
                sb.append(printResult(list.get(i)));
                if (i < list.size() - 1) {
                    sb.append(" ");
                }
            }

            sb.append("]");
            return sb.toString();
        } else {
            return expr.toString();
        }
    }
}