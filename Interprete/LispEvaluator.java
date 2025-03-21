package Interprete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LispEvaluator {

    @FunctionalInterface
    private interface LispOperator {
        Object apply(List<?> list, EntornoLisp env, int depth);
    }

    private final Map<String, LispOperator> operators = new HashMap<>();

    public LispEvaluator() {
        // Formas especiales
        operators.put("quote", this::handleQuote);
        operators.put("setq", this::handleSetq);
        operators.put("defun", this::handleDefun);
        operators.put("cond", this::handleCond);

        // Valores especiales
        operators.put("t", (list, env, depth) -> "t");
        operators.put("nil", (list, env, depth) -> "nil");

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
        return evaluate(expr, env, 0); // Iniciar con profundidad 0
    }

    private Object evaluate(Object expr, EntornoLisp env, int depth) {
        if (depth > 1000) {
            throw new ErrorLisp("Se ha excedido la profundidad máxima de recursión (1000)");
        }

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
        return (op != null) ? op.apply(list, env, depth + 1) : applyUserFunction(operator, list, env, depth + 1);
    }

    private Object handleQuote(List<?> list, EntornoLisp env, int depth) {
        if (list.size() != 2) throw new ErrorLisp("Error: quote requiere exactamente un argumento");
        return list.get(1);
    }

    private Object handleSetq(List<?> list, EntornoLisp env, int depth) {
        if (list.size() != 3) throw new ErrorLisp("Error: setq requiere exactamente dos argumentos");
        if (!(list.get(1) instanceof String)) throw new ErrorLisp("Error: el primer argumento de setq debe ser un símbolo");

        String variable = list.get(1).toString();
        Object value = evaluate(list.get(2), env, depth + 1);
        return env.asignarVariable(variable, value);
    }

    private Object handleDefun(List<?> list, EntornoLisp env, int depth) {
        if (list.size() != 4) throw new ErrorLisp("Error: defun requiere exactamente tres argumentos");

        String functionName = list.get(1).toString();
        if (!(list.get(2) instanceof List)) throw new ErrorLisp("Error: los parámetros de defun deben ser una lista");

        List<String> params = new ArrayList<>();
        for (Object param : (List<?>) list.get(2)) params.add(param.toString());

        return env.registrarFuncion(functionName, params, list.get(3));
    }

    private Object handleCond(List<?> list, EntornoLisp env, int depth) {
        for (int i = 1; i < list.size(); i++) {
            if (!(list.get(i) instanceof List)) throw new ErrorLisp("Error: cláusula de cond debe ser una lista");
            List<?> clause = (List<?>) list.get(i);
            if (clause.size() != 2) throw new ErrorLisp("Error: cláusula de cond debe tener exactamente dos elementos");

            Object condition = evaluate(clause.get(0), env, depth + 1);
            if (isTrue(condition)) return evaluate(clause.get(1), env, depth + 1);
        }
        return "nil";
    }

    private Object evaluateAdd(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        if (args.isEmpty()) return 0.0;

        double result = 0.0;
        for (Object arg : args) {
            if (!(arg instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + arg);
            }
            result += ((Number) arg).doubleValue();
        }
        return simplifyNumber(result);
    }

    private Object evaluateSubtract(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        if (args.isEmpty()) throw new ErrorLisp("Error: - requiere al menos un argumento");

        if (args.size() == 1) {
            if (!(args.get(0) instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + args.get(0));
            }
            return simplifyNumber(-((Number) args.get(0)).doubleValue());
        }

        double result = ((Number) args.get(0)).doubleValue();
        for (int i = 1; i < args.size(); i++) {
            if (!(args.get(i) instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + args.get(i));
            }
            result -= ((Number) args.get(i)).doubleValue();
        }
        return simplifyNumber(result);
    }

    private Object evaluateMultiply(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        if (args.isEmpty()) return 1.0;

        double result = 1.0;
        for (Object arg : args) {
            if (!(arg instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + arg);
            }
            result *= ((Number) arg).doubleValue();
        }
        return simplifyNumber(result);
    }

    private Object evaluateDivide(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        if (args.isEmpty()) throw new ErrorLisp("Error: / requiere al menos un argumento");

        if (args.size() == 1) {
            if (!(args.get(0) instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + args.get(0));
            }
            double value = ((Number) args.get(0)).doubleValue();
            if (value == 0) throw new ErrorLisp("Error: división por cero");
            return simplifyNumber(1.0 / value);
        }

        double result = ((Number) args.get(0)).doubleValue();
        for (int i = 1; i < args.size(); i++) {
            if (!(args.get(i) instanceof Number)) {
                throw new ErrorLisp("Error: se esperaba un número pero se encontró: " + args.get(i));
            }
            double divisor = ((Number) args.get(i)).doubleValue();
            if (divisor == 0) throw new ErrorLisp("Error: división por cero");
            result /= divisor;
        }
        return simplifyNumber(result);
    }

    private Object evaluateEqual(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        if (args.size() != 2) throw new ErrorLisp("Error: equal requiere exactamente dos argumentos");
        return args.get(0).equals(args.get(1)) ? "t" : "nil";
    }

    private Object evaluateLessThan(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        if (args.size() != 2) throw new ErrorLisp("Error: < requiere exactamente dos argumentos");
        if (!(args.get(0) instanceof Number)) throw new ErrorLisp("Error: los argumentos de < deben ser números");
        if (!(args.get(1) instanceof Number)) throw new ErrorLisp("Error: los argumentos de < deben ser números");

        double a = ((Number) args.get(0)).doubleValue();
        double b = ((Number) args.get(1)).doubleValue();
        return a < b ? "t" : "nil";
    }

    private Object evaluateGreaterThan(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        if (args.size() != 2) throw new ErrorLisp("Error: > requiere exactamente dos argumentos");
        if (!(args.get(0) instanceof Number)) throw new ErrorLisp("Error: los argumentos de > deben ser números");
        if (!(args.get(1) instanceof Number)) throw new ErrorLisp("Error: los argumentos de > deben ser números");

        double a = ((Number) args.get(0)).doubleValue();
        double b = ((Number) args.get(1)).doubleValue();
        return a > b ? "t" : "nil";
    }

    private Object evaluateAtom(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        if (args.size() != 1) throw new ErrorLisp("Error: atom requiere exactamente un argumento");
        return !(args.get(0) instanceof List) ? "t" : "nil";
    }

    private Object evaluateList(List<?> list, EntornoLisp env, int depth) {
        List<Object> args = evaluateArguments(list, env, depth + 1);
        return args;
    }

    private Number simplifyNumber(double value) {
        if (value == Math.floor(value)) {
            return (int) value;
        }
        return value;
    }

    private boolean isTrue(Object value) {
        return value != null && !"nil".equals(value);
    }

    private List<Object> evaluateArguments(List<?> list, EntornoLisp env, int depth) {
        List<Object> evaluatedArgs = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            evaluatedArgs.add(evaluate(list.get(i), env, depth + 1));
        }
        return evaluatedArgs;
    }

    private Object applyUserFunction(String functionName, List<?> list, EntornoLisp env, int depth) {
        if (!env.existeFuncion(functionName)) {
            throw new ErrorLisp("Error: función " + functionName + " no definida");
        }

        EntornoLisp.DefinicionFuncion function = env.obtenerFuncion(functionName);
        List<String> params = function.getParametros();
        Object body = function.getCuerpo();

        if (list.size() - 1 != params.size()) {
            throw new ErrorLisp("Error: se esperaban " + params.size() +
                              " argumentos, pero se recibieron " + (list.size() - 1));
        }

        // Crear un nuevo entorno para la ejecución de la función
        EntornoLisp localEnv = new EntornoLisp(env);

        // Evaluar argumentos y asignarlos a los parámetros
        for (int i = 0; i < params.size(); i++) {
            Object argValue = evaluate(list.get(i + 1), env, depth + 1);
            localEnv.asignarVariable(params.get(i), argValue);
        }

        // Evaluar el cuerpo de la función en el entorno local
        return evaluate(body, localEnv, depth + 1);
    }
}