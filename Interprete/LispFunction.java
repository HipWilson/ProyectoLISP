
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
        EntornoLisp entornoLocal = new EntornoLisp(env);
        for (int i = 0; i < parametros.size(); i++) {
            entornoLocal.asignarVariable(parametros.get(i), args.get(i));
        }
        return new LispEvaluator().evaluate(cuerpo, entornoLocal);
    }
}