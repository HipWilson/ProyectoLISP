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

    public LispEvaluator() {
        // Formas especiales
        operators.put("quote", this::handleQuote);
        operators.put("setq", this::handleSetq);
        operators.put("defun", this::handleDefun);
        operators.put("cond", this::handleCond);

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
        operators.put("atom", this::evaluateAtom);
        operators.put("list", this::evaluateList);
    }

    public Object evaluate(Object expr, EntornoLisp env) {
        if (expr instanceof Number) return expr;
        if (expr instanceof String) {
            String symbol = (String) expr;
            return env.existeVariable(symbol) ? env.obtenerVariable(symbol) : symbol;
        }
        if (!(expr instanceof List)) return expr;

        List<?> list = (List<?>) expr;
        if (list.isEmpty()) return list;

        String operator = list.get(0).toString();
        LispOperator op = operators.get(operator);
        return (op != null) ? op.apply(list, env) : applyUserFunction(operator, list, env);
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

    private Object evaluateAdd(List<?> list, EntornoLisp env) {
        return evaluateNumericOperation(list, env, 0, (a, b) -> a + b);
    }

    private Object evaluateSubtract(List<?> list, EntornoLisp env) {
        return evaluateNumericOperation(list, env, 0, (a, b) -> a - b);
    }

    private Object evaluateMultiply(List<?> list, EntornoLisp env) {
        return evaluateNumericOperation(list, env, 1, (a, b) -> a * b);
    }

    private Object evaluateDivide(List<?> list, EntornoLisp env) {
        return evaluateNumericOperation(list, env, 1, (a, b) -> a / b);
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

    private Object applyUserFunction(String functionName, List<?> list, EntornoLisp env) {
        if (!env.existeFuncion(functionName)) {
            throw new ErrorLisp("Error: función " + functionName + " no definida");
        }
        
        EntornoLisp.DefinicionFuncion function = env.obtenerFuncion(functionName);
        List<Object> evaluatedArgs = new ArrayList<>();
        
        // Primero evaluar todos los argumentos
        for (int i = 1; i < list.size(); i++) {
            evaluatedArgs.add(evaluate(list.get(i), env));
        }
        
        // Crear una lista con el nombre de la función como primer elemento
        // seguido por los argumentos evaluados
        List<Object> functionCallList = new ArrayList<>();
        functionCallList.add(functionName);
        functionCallList.addAll(evaluatedArgs);
        
        return new LispFunction(function.getParametros(), function.getCuerpo()).apply(functionCallList, env);
    }
}