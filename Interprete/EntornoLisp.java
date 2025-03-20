package Interprete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntornoLisp {

    private final Map<String, Object> tablaVariables;
    private final Map<String, DefinicionFuncion> tablaFunciones;
    private final EntornoLisp entornoPadre;

    public EntornoLisp() {
        this(null);
    }

    public EntornoLisp(EntornoLisp entornoPadre) {
        this.tablaVariables = new HashMap<>();
        this.tablaFunciones = new HashMap<>();
        this.entornoPadre = entornoPadre;
    }

    public Object obtenerVariable(String nombre) {
        if (tablaVariables.containsKey(nombre)) {
            return tablaVariables.get(nombre);
        }
        if (entornoPadre != null) {
            return entornoPadre.obtenerVariable(nombre);
        }
        throw new RuntimeException("Variable no encontrada: " + nombre);
    }

    public Object asignarVariable(String nombre, Object valor) {
        tablaVariables.put(nombre, valor);
        return valor;
    }

    public boolean existeVariable(String nombre) {
        return tablaVariables.containsKey(nombre) || 
               (entornoPadre != null && entornoPadre.existeVariable(nombre));
    }

    public String registrarFuncion(String nombre, List<String> parametros, Object cuerpo) {
        tablaFunciones.put(nombre, new DefinicionFuncion(parametros, cuerpo));
        return nombre;
    }

    public DefinicionFuncion obtenerFuncion(String nombre) {
        if (tablaFunciones.containsKey(nombre)) {
            return tablaFunciones.get(nombre);
        }
        if (entornoPadre != null) {
            return entornoPadre.obtenerFuncion(nombre);
        }
        throw new RuntimeException("Función no encontrada: " + nombre);
    }

    public boolean existeFuncion(String nombre) {
        return tablaFunciones.containsKey(nombre) || 
               (entornoPadre != null && entornoPadre.existeFuncion(nombre));
    }

    public static class DefinicionFuncion {
        private final List<String> parametros;
        private final Object cuerpo;

        public DefinicionFuncion(List<String> parametros, Object cuerpo) {
            this.parametros = parametros;
            this.cuerpo = cuerpo;
        }

        public List<String> getParametros() {
            return parametros;
        }

        public Object getCuerpo() {
            return cuerpo;
        }
    }
}