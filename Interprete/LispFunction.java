package Interprete;

import java.util.List;

public class LispFunction {
    private final List<String> parametros;
    private final Object cuerpo;

    public LispFunction(List<String> parametros, Object cuerpo) {
        this.parametros = parametros;
        this.cuerpo = cuerpo;
    }

    public Object apply(List<?> args, EntornoLisp env) {
        // El primer elemento de args es el nombre de la función,
        // así que debemos omitirlo al asociar argumentos con parámetros
        if (args.size() - 1 != parametros.size()) {
            throw new ErrorLisp("Error: se esperaban " + parametros.size() + 
                              " argumentos, pero se recibieron " + (args.size() - 1));
        }
        
        EntornoLisp entornoLocal = new EntornoLisp(env);
        
        // Asignar cada argumento a su correspondiente parámetro
        for (int i = 0; i < parametros.size(); i++) {
            entornoLocal.asignarVariable(parametros.get(i), args.get(i + 1));
        }
        
        return new LispEvaluator().evaluate(cuerpo, entornoLocal);
    }
}