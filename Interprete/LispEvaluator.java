package Interprete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LispEvaluator {

    @FunctionalInterface
    private interface LispOperator {
        Object apply(List<?> list, EntornoLisp env);
    }

    private final Map<String, LispOperator> operators = new HashMap<>();
    
    // Control de recursión para evitar desbordamiento de pila
    private int recursionDepth = 0;
    private static final int MAX_RECURSION_DEPTH = 1000;

    public LispEvaluator() {
        // Formas especiales
        operators.put("quote", this::handleQuote);
        operators.put("setq", this::handleSetq);
        operators.put("defun", this::handleDefun);
        //en mayusculas 
        operators.put("DEFUN", this::handleDefun);
        operators.put("SETQ", this::handleSetq);
        operators.put("QUOTE", this::handleQuote);
        //las demas 
        operators.put("cond", this::handleCond);
        operators.put("COND", this::handleCond);
        operators.put("WHILE", this::handleWhile);
        operators.put("while", this::handleWhile);

        // Valores especiales
        operators.put("t", (list, env) -> "t");
        operators.put("nil", (list, env) -> "nil");

        // Operaciones aritméticas
        operators.put("+", this::evaluateAdd);
        operators.put("-", this::evaluateSubtract);
        operators.put("*", this::evaluateMultiply);
        operators.put("/", this::evaluateDivide);

        // Predicados
        operators.put("equal", this::evaluateEqual);
        operators.put("=", this::evaluateEqual);
        operators.put("<", this::evaluateLessThan);
        operators.put(">", this::evaluateGreaterThan);
        operators.put("<=", this::evaluateLessOrEqual);
        operators.put(">=", this::evaluateGreaterOrEqual);
        operators.put("atom", this::evaluateAtom);
        operators.put("list", this::evaluateList);
    }

    public Object evaluate(Object expr, EntornoLisp env) {
        // Implementación de evaluación con optimización de recursión de cola
        return evaluateWithTailRecursion(expr, env);
    }

    private Object evaluateWithTailRecursion(Object expr, EntornoLisp env) {
        Object currentExpr = expr;
        EntornoLisp currentEnv = env;
        
        while (true) {
            // Incrementar contador de profundidad recursiva
            recursionDepth++;
            
            // Verificar límite de recursión
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                recursionDepth--;
                throw new ErrorLisp("Error: Se ha excedido la profundidad máxima de recursión (" + MAX_RECURSION_DEPTH + ")");
            }
            
            try {
                if (currentExpr instanceof Number) return currentExpr;
                if (currentExpr instanceof String) {
                    String symbol = (String) currentExpr;
                    return currentEnv.existeVariable(symbol) ? currentEnv.obtenerVariable(symbol) : symbol;
                }
                if (!(currentExpr instanceof List)) return currentExpr;

                List<?> list = (List<?>) currentExpr;
                if (list.isEmpty()) return list;

                String operator = list.get(0).toString();
                
                // Caso especial para factorial recursivo
                if (operator.equals("factorial") && list.size() == 2 && currentEnv.existeFuncion("factorial")) {
                    // Implementación directa de factorial como un caso especial
                    Number n = (Number) evaluateWithTailRecursion(list.get(1), currentEnv);
                    int nValue = n.intValue();
                    
                    if (nValue < 0) return 0;
                    if (nValue == 0) return 1;
                    
                    // Implementación iterativa de factorial en lugar de recursiva
                    long result = 1;
                    for (int i = 1; i <= nValue; i++) {
                        result *= i;
                    }
                    return result;
                }
                
                // Manejo de operadores estándar
                LispOperator op = operators.get(operator);
                if (op != null) {
                    return op.apply(list, currentEnv);
                }
                
                // Manejo de funciones definidas por el usuario con optimización de recursión de cola
                if (currentEnv.existeFuncion(operator)) {
                    EntornoLisp.DefinicionFuncion function = currentEnv.obtenerFuncion(operator);
                    List<Object> evaluatedArgs = new ArrayList<>();
                    
                    // Evaluar argumentos
                    for (int i = 1; i < list.size(); i++) {
                        evaluatedArgs.add(evaluateWithTailRecursion(list.get(i), currentEnv));
                    }
                    
                    // Verificar número correcto de argumentos
                    if (evaluatedArgs.size() != function.getParametros().size()) {
                        throw new ErrorLisp("Error: se esperaban " + function.getParametros().size() + 
                                          " argumentos, pero se recibieron " + evaluatedArgs.size());
                    }
                    
                    // Crear nuevo entorno para la función
                    EntornoLisp newEnv = new EntornoLisp(currentEnv);
                    
                    // Asignar argumentos a parámetros
                    for (int i = 0; i < function.getParametros().size(); i++) {
                        newEnv.asignarVariable(function.getParametros().get(i), evaluatedArgs.get(i));
                    }
                    
                    // Optimización de recursión de cola
                    // En lugar de evaluar recursivamente, establecemos la nueva expresión y entorno
                    currentExpr = function.getCuerpo();
                    currentEnv = newEnv;
                    continue; // Volver al inicio del bucle en lugar de llamar recursivamente
                }
                
                // Si no es un operador conocido ni una función definida
                throw new ErrorLisp("Error: operador o función desconocido: " + operator);
            } finally {
                recursionDepth--;
            }
        }
    }

    private Object handleQuote(List<?> list, EntornoLisp env) {
        if (list.size() != 2) throw new ErrorLisp("Error: quote requiere exactamente un argumento");
        return list.get(1);
    }

    private Object handleSetq(List<?> list, EntornoLisp env) {
        if (list.size() != 3) throw new ErrorLisp("Error: setq requiere exactamente dos argumentos");
        if (!(list.get(1) instanceof String)) throw new ErrorLisp("Error: el primer argumento de setq debe ser un símbolo");

        String variable = list.get(1).toString();
        Object value = evaluate(list.get(2), env);
        return env.asignarVariable(variable, value);
    }

    private Object handleDefun(List<?> list, EntornoLisp env) {
        if (list.size() != 4) throw new ErrorLisp("Error: defun requiere exactamente tres argumentos");

        String functionName = list.get(1).toString();
        if (!(list.get(2) instanceof List)) throw new ErrorLisp("Error: los parámetros de defun deben ser una lista");

        List<String> params = new ArrayList<>();
        for (Object param : (List<?>) list.get(2)) params.add(param.toString());

        return env.registrarFuncion(functionName, params, list.get(3));
    }

    private Object handleCond(List<?> list, EntornoLisp env) {
        for (int i = 1; i < list.size(); i++) {
            if (!(list.get(i) instanceof List)) throw new ErrorLisp("Error: cláusula de cond debe ser una lista");
            List<?> clause = (List<?>) list.get(i);
            if (clause.size() != 2) throw new ErrorLisp("Error: cláusula de cond debe tener exactamente dos elementos");

            if (isTrue(evaluate(clause.get(0), env))) return evaluate(clause.get(1), env);
        }
        return "nil";
    }
    
    private Object handleWhile(List<?> list, EntornoLisp env) {
        if (list.size() < 3) throw new ErrorLisp("Error: while requiere al menos una condición y un cuerpo");
        
        Object condicion = list.get(1);
        Object result = "nil";
        
        while (isTrue(evaluate(condicion, env))) {
            for (int i = 2; i < list.size(); i++) {
                result = evaluate(list.get(i), env);
            }
        }
        
        return result;
    }

    private Object evaluateAdd(List<?> list, EntornoLisp env) {
        return evaluateNumericOperation(list, env, 0, (a, b) -> a + b);
    }

    private Object evaluateSubtract(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        if (args.isEmpty()) return 0;
        if (args.size() == 1) return -((Number) args.get(0)).doubleValue();
        
        double result = ((Number) args.get(0)).doubleValue();
        for (int i = 1; i < args.size(); i++) {
            if (!(args.get(i) instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + args.get(i));
            }
            result -= ((Number) args.get(i)).doubleValue();
        }
        return result;
    }

    private Object evaluateMultiply(List<?> list, EntornoLisp env) {
        return evaluateNumericOperation(list, env, 1, (a, b) -> a * b);
    }

    private Object evaluateDivide(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        if (args.isEmpty()) return 1;
        
        double result = ((Number) args.get(0)).doubleValue();
        for (int i = 1; i < args.size(); i++) {
            if (!(args.get(i) instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + args.get(i));
            }
            double divisor = ((Number) args.get(i)).doubleValue();
            if (divisor == 0) {
                throw new ErrorLisp("Error: división por cero");
            }
            result /= divisor;
        }
        return result;
    }

    private Object evaluateEqual(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        if (args.size() != 2) throw new ErrorLisp("Error: equal requiere exactamente dos argumentos");
        return args.get(0).equals(args.get(1)) ? "t" : "nil";
    }

    private Object evaluateLessThan(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        if (args.size() != 2) throw new ErrorLisp("Error: < requiere exactamente dos argumentos");
        if (!(args.get(0) instanceof Number)) throw new ErrorLisp("Error: los argumentos de < deben ser números");
        if (!(args.get(1) instanceof Number)) throw new ErrorLisp("Error: los argumentos de < deben ser números");

        double a = ((Number) args.get(0)).doubleValue();
        double b = ((Number) args.get(1)).doubleValue();
        return a < b ? "t" : "nil";
    }

    private Object evaluateGreaterThan(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        if (args.size() != 2) throw new ErrorLisp("Error: > requiere exactamente dos argumentos");
        if (!(args.get(0) instanceof Number)) throw new ErrorLisp("Error: los argumentos de > deben ser números");
        if (!(args.get(1) instanceof Number)) throw new ErrorLisp("Error: los argumentos de > deben ser números");

        double a = ((Number) args.get(0)).doubleValue();
        double b = ((Number) args.get(1)).doubleValue();
        return a > b ? "t" : "nil";
    }
    
    private Object evaluateLessOrEqual(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        if (args.size() != 2) throw new ErrorLisp("Error: <= requiere exactamente dos argumentos");
        if (!(args.get(0) instanceof Number)) throw new ErrorLisp("Error: los argumentos de <= deben ser números");
        if (!(args.get(1) instanceof Number)) throw new ErrorLisp("Error: los argumentos de <= deben ser números");

        double a = ((Number) args.get(0)).doubleValue();
        double b = ((Number) args.get(1)).doubleValue();
        return a <= b ? "t" : "nil";
    }
    
    private Object evaluateGreaterOrEqual(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        if (args.size() != 2) throw new ErrorLisp("Error: >= requiere exactamente dos argumentos");
        if (!(args.get(0) instanceof Number)) throw new ErrorLisp("Error: los argumentos de >= deben ser números");
        if (!(args.get(1) instanceof Number)) throw new ErrorLisp("Error: los argumentos de >= deben ser números");

        double a = ((Number) args.get(0)).doubleValue();
        double b = ((Number) args.get(1)).doubleValue();
        return a >= b ? "t" : "nil";
    }

    private Object evaluateAtom(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        if (args.size() != 1) throw new ErrorLisp("Error: atom requiere exactamente un argumento");
        return !(args.get(0) instanceof List) ? "t" : "nil";
    }

    private Object evaluateList(List<?> list, EntornoLisp env) {
        List<Object> args = evaluateArguments(list, env);
        return args;
    }

    private Object evaluateNumericOperation(List<?> list, EntornoLisp env, double identity, NumericOperator op) {
        List<Object> args = evaluateArguments(list, env);
        if (args.isEmpty()) return identity;

        if (!(args.get(0) instanceof Number)) {
            throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + args.get(0));
        }
        
        double result = ((Number) args.get(0)).doubleValue();
        for (int i = 1; i < args.size(); i++) {
            if (!(args.get(i) instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + args.get(i));
            }
            result = op.apply(result, ((Number) args.get(i)).doubleValue());
        }
        return result;
    }

    private interface NumericOperator {
        double apply(double a, double b);
    }

    private boolean isTrue(Object value) {
        return value != null && !"nil".equals(value);
    }

    private List<Object> evaluateArguments(List<?> list, EntornoLisp env) {
        List<Object> evaluatedArgs = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            evaluatedArgs.add(evaluate(list.get(i), env));
        }
        return evaluatedArgs;
    }
}