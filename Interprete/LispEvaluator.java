package lisp.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lisp.environment.LispEnvironment;
import lisp.environment.LispEnvironment.FunctionDefinition;

/**
 * Evaluador de expresiones LISP.
 * Evalúa expresiones aritméticas, operaciones lógicas y funciones definidas por el usuario.
 * También maneja formas especiales como quote, setq, defun y cond.
 * @author Fatima Navarro 24044
 */
public class LispEvaluator {
    
    @FunctionalInterface
    private interface LispOperator {
        Object apply(List<?> list, LispEnvironment env);
    }
    
    private final Map<String, LispOperator> operators = new HashMap<>();
    
    /**
     * Constructor que inicializa el mapa de operadores usando hashmaps.
     */
    public LispEvaluator() {
        // Formas especiales
        operators.put("quote", this::handleQuote);
        operators.put("QUOTE", this::handleQuote); 
        operators.put("setq", this::handleSetq);
        operators.put("SETQ", this::handleSetq);
        operators.put("defun", this::handleDefun);
        operators.put("DEFUN", this::handleDefun);  
        operators.put("cond", this::handleCond);
        operators.put("COND", this::handleCond);
        // para la t y nil
        operators.put("t", (list, env) -> "t");
        operators.put("nil", (list, env) -> "nil");
        operators.put("T", (list, env) -> "t");
        operators.put("NIL", (list, env) -> "nil");
        
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
    /**
     * Evalúa una expresión LISP en el entorno proporcionado.
     */
    public Object evaluate(Object expr, LispEnvironment env) {
        // Si es un número, se evalúa a sí mismo
        if (expr instanceof Number) {
            return expr;
        }
        
        // Si es un símbolo (String), buscar su valor en el entorno
        if (expr instanceof String) {
            String symbol = (String) expr;
            
            // Verificar si es una variable
            if (env.hasVariable(symbol)) {
                return env.getVariable(symbol);
            }
            
            // Si no es una variable, retorna el símbolo mismo
            return symbol;
        }
        
        // Si no es una lista, no se puede evaluar más
        if (!(expr instanceof List)) {
            return expr;
        }
        
        // Obtener la lista
        List<?> list = (List<?>) expr;
        
        // Lista vacía se evalúa a sí misma
        if (list.isEmpty()) {
            return list;
        }
        
        // Obtener el operador o nombre de función
        String operator = list.get(0).toString();
        
        // Buscar en el mapa de operadores
        LispOperator op = operators.get(operator);
        if (op != null) {
            return op.apply(list, env);
        }
        
        // Si no es un operador conocido, intentar como función de usuario
        return applyUserFunction(operator, list, env);
    }
    
    private Object handleQuote(List<?> list, LispEnvironment env) {
        if (list.size() != 2) {
            throw new LispException("Error: quote requiere exactamente un argumento");
        }
        return list.get(1);
    }
    
    private Object handleSetq(List<?> list, LispEnvironment env) {
        if (list.size() != 3) {
            throw new LispException("Error: setq requiere exactamente dos argumentos");
        }
        
        if (!(list.get(1) instanceof String)) {
            throw new LispException("Error: el primer argumento de setq debe ser un símbolo");
        }
        
        String variable = list.get(1).toString();
        Object value = evaluate(list.get(2), env);
        
        return env.setVariable(variable, value);
    }
    
    private Object handleDefun(List<?> list, LispEnvironment env) {
        if (list.size() != 4) {
            throw new LispException("Error: defun requiere exactamente tres argumentos");
        }
        
        String functionName = list.get(1).toString();
        
        // Parámetros
        if (!(list.get(2) instanceof List)) {
            throw new LispException("Error: los parámetros de defun deben ser una lista");
        }
        
        List<?> paramsList = (List<?>) list.get(2);
        List<String> params = new ArrayList<>();
        
        for (Object param : paramsList) {
            params.add(param.toString());
        }
        
        // Cuerpo de la función
        Object body = list.get(3);
        
        return env.defineFunction(functionName, params, body);
    }
    
    private Object handleCond(List<?> list, LispEnvironment env) {
        for (int i = 1; i < list.size(); i++) {
            if (!(list.get(i) instanceof List)) {
                throw new LispException("Error: cláusula de cond debe ser una lista");
            }
            
            List<?> clause = (List<?>) list.get(i);
            if (clause.size() != 2) {
                throw new LispException("Error: cláusula de cond debe tener exactamente dos elementos");
            }
            
            Object condition = clause.get(0);
            Object result = clause.get(1);
            
            // Si la condición es 't' o evalúa a verdadero
            if (condition.equals("t") || isTrue(evaluate(condition, env))) {
                Object evaluated = evaluate(result, env);
                return evaluated;
            }
        }
        
        // Si ninguna condición se cumple
        return "nil";
    }
    
    private Object evaluateAdd(List<?> list, LispEnvironment env) {
        List<Object> args = evaluateArguments(list, env);
        
        if (args.isEmpty()) return 0;
        
        double result = 0;
        boolean allIntegers = true;
        
        for (Object arg : args) {
            if (!(arg instanceof Number)) {
                throw new LispException("Error: + requiere argumentos numéricos");
            }
            
            Number num = (Number) arg;
            result += num.doubleValue();
            
            // Si algún operando es double, el resultado también lo será
            if (num instanceof Double || num instanceof Float) {
                allIntegers = false;
            }
        }
        
        // Retorna int o double según corresponda
        if (allIntegers && result == Math.floor(result) && !Double.isInfinite(result)) {
            return (int) result;
        } else {
            return result;
        }
    }
    
    private Object evaluateSubtract(List<?> list, LispEnvironment env) {
        List<Object> args = evaluateArguments(list, env);
        
        if (args.isEmpty()) {
            throw new LispException("Error: - requiere al menos un argumento");
        }
        
        if (!(args.get(0) instanceof Number)) {
            throw new LispException("Error: - requiere argumentos numéricos");
        }
        
        Number firstArg = (Number) args.get(0);
        double result = firstArg.doubleValue();
        boolean isInteger = firstArg instanceof Integer || firstArg instanceof Long;
        
        // Si solo hay un argumento, negar
        if (args.size() == 1) {
            if (isInteger) {
                return -firstArg.intValue();
            } else {
                return -result;
            }
        }
        
        // Restar el resto de argumentos
        for (int i = 1; i < args.size(); i++) {
            if (!(args.get(i) instanceof Number)) {
                throw new LispException("Error: - requiere argumentos numéricos");
            }
            
            Number num = (Number) args.get(i);
            result -= num.doubleValue();
            
            // Si algún operando es double, el resultado también lo será
            if (num instanceof Double || num instanceof Float) {
                isInteger = false;
            }
        }
        
        // Retorna int o double según corresponda
        if (isInteger && result == Math.floor(result) && !Double.isInfinite(result)) {
            return (int) result;
        } else {
            return result;
        }
    }
    
    private Object evaluateMultiply(List<?> list, LispEnvironment env) {
        List<Object> args = evaluateArguments(list, env);
        
        if (args.isEmpty()) return 1;
        
        double result = 1;
        boolean allIntegers = true;
        
        for (Object arg : args) {
            if (!(arg instanceof Number)) {
                throw new LispException("Error: * requiere argumentos numéricos");
            }
            
            Number num = (Number) arg;
            result *= num.doubleValue();
            
            // Si algún operando es double, el resultado también lo será
            if (num instanceof Double || num instanceof Float) {
                allIntegers = false;
            }
        }
        
        // Retorna int o double según corresponda
        if (allIntegers && result == Math.floor(result) && !Double.isInfinite(result)) {
            return (int) result;
        } else {
            return result;
        }
    }
    
    private Object evaluateDivide(List<?> list, LispEnvironment env) {
        List<Object> args = evaluateArguments(list, env);
        
        if (args.isEmpty()) {
            throw new LispException("Error: / requiere al menos un argumento");
        }
        
        if (!(args.get(0) instanceof Number)) {
            throw new LispException("Error: / requiere argumentos numéricos");
        }
        
        Number firstArg = (Number) args.get(0);
        double result = firstArg.doubleValue();
        
        // Si solo hay un argumento, invertir
        if (args.size() == 1) {
            return 1.0 / result;
        }
        
        // Dividir por el resto de argumentos
        for (int i = 1; i < args.size(); i++) {
            if (!(args.get(i) instanceof Number)) {
                throw new LispException("Error: / requiere argumentos numéricos");
            }
            
            Number num = (Number) args.get(i);
            double divisor = num.doubleValue();
            
            if (divisor == 0) {
                throw new LispException("Error: división por cero");
            }
            
            result /= divisor;
        }
        
        // Retorna int o double según corresponda
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return (int) result;
        } else {
            return result;
        }
    }
    
    private Object evaluateEqual(List<?> list, LispEnvironment env) {
        if (list.size() != 3) {
            throw new LispException("Error: equal requiere exactamente dos argumentos");
        }
        
        Object arg1 = evaluate(list.get(1), env);
        Object arg2 = evaluate(list.get(2), env);
        
        return arg1.equals(arg2) ? "t" : "nil";
    }
    
    private Object evaluateLessThan(List<?> list, LispEnvironment env) {
        if (list.size() != 3) {
            throw new LispException("Error: < requiere exactamente dos argumentos");
        }
        
        Object arg1 = evaluate(list.get(1), env);
        Object arg2 = evaluate(list.get(2), env);
        
        if (!(arg1 instanceof Number) || !(arg2 instanceof Number)) {
            throw new LispException("Error: < requiere argumentos numéricos");
        }
        
        double num1 = ((Number) arg1).doubleValue();
        double num2 = ((Number) arg2).doubleValue();
        
        return num1 < num2 ? "t" : "nil";
    }
    
    private Object evaluateGreaterThan(List<?> list, LispEnvironment env) {
        if (list.size() != 3) {
            throw new LispException("Error: > requiere exactamente dos argumentos");
        }
        
        Object arg1 = evaluate(list.get(1), env);
        Object arg2 = evaluate(list.get(2), env);
        
        if (!(arg1 instanceof Number) || !(arg2 instanceof Number)) {
            throw new LispException("Error: > requiere argumentos numéricos");
        }
        
        double num1 = ((Number) arg1).doubleValue();
        double num2 = ((Number) arg2).doubleValue();
        
        return num1 > num2 ? "t" : "nil";
    }
    
    private Object evaluateAtom(List<?> list, LispEnvironment env) {
        if (list.size() != 2) {
            throw new LispException("Error: atom requiere exactamente un argumento");
        }
        
        Object arg = evaluate(list.get(1), env);
        boolean isAtom = !(arg instanceof List) || ((List<?>) arg).isEmpty();
        return isAtom ? "t" : "nil";
    }
    
    private Object evaluateList(List<?> list, LispEnvironment env) {
        List<Object> result = new ArrayList<>();
        
        for (int i = 1; i < list.size(); i++) {
            result.add(evaluate(list.get(i), env));
        }
        
        return result;
    }
    
    private Object applyUserFunction(String functionName, List<?> list, LispEnvironment env) {
        if (!env.hasFunction(functionName)) {
            throw new LispException("Error: función no definida: " + functionName);
        }
        
        // Obtener la definición de la función
        FunctionDefinition function = env.getFunction(functionName);
        List<String> params = function.getParameters();
        
        // Evaluar argumentos
        List<Object> args = evaluateArguments(list, env);
        
        if (params.size() != args.size()) {
            throw new LispException("Error: la función " + functionName + 
                                   " espera " + params.size() + " argumentos, pero recibió " + args.size());
        }
        
        // Crear entorno para la función
        LispEnvironment functionEnv = new LispEnvironment(env);
        
        // Asignar argumentos a parámetros
        for (int i = 0; i < params.size(); i++) {
            functionEnv.setVariable(params.get(i), args.get(i));
        }
        
        // Evaluar el cuerpo
        return evaluate(function.getBody(), functionEnv);
    }
    
    private List<Object> evaluateArguments(List<?> list, LispEnvironment env) {
        List<Object> evaluatedArgs = new ArrayList<>();
        
        for (int i = 1; i < list.size(); i++) {
            evaluatedArgs.add(evaluate(list.get(i), env));
        }
        
        return evaluatedArgs;
    }
    
    private boolean isTrue(Object value) {
        return !"nil".equals(value);
    }
}