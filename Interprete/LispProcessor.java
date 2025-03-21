package Interprete;

import java.util.List;

public class LispProcessor {

    private final TokenizadorLisp lexicAnalyzer;
    private final LispParser syntaxAnalyzer;
    private final LispEvaluator evaluator;
    private final EntornoLisp globalContext;

    public LispProcessor() {
        this.lexicAnalyzer = new TokenizadorLisp();
        this.syntaxAnalyzer = new LispParser();
        this.evaluator = new LispEvaluator();
        this.globalContext = new EntornoLisp();

        configurarContextoInicial();
    }

    private void configurarContextoInicial() {
        globalContext.asignarVariable("verdadero", "t");
        globalContext.asignarVariable("falso", "nil");
        
        // Añadir una implementación iterativa de factorial
        // Esta versión evita la recursión profunda que causa desbordamiento de pila
        try {
            String factorialIterativo = 
                "(defun factorial (n) " +
                    "(cond " +
                        "((< n 0) 0) " +
                        "((equal n 0) 1) " +
                        "(t (setq resultado 1) " +
                           "(setq i 1) " +
                           "(while (<= i n) " +
                              "(setq resultado (* resultado i)) " +
                              "(setq i (+ i 1))) " +
                           "resultado)))";
            procesar(factorialIterativo);
        } catch (Exception e) {
            System.err.println("Error al configurar factorial: " + e.getMessage());
        }
    }

    public Object procesar(String expresion) {
        try {
            List<String> tokens = lexicAnalyzer.analizar(expresion);

            if (tokens.isEmpty()) {
                return null;
            }

            Object estructura = syntaxAnalyzer.parse(tokens);
            return evaluator.evaluate(estructura, globalContext);
        } catch (RuntimeException e) {
            throw new ErrorLisp("Error en la evaluación: " + e.getMessage(), e);
        }
    }
}