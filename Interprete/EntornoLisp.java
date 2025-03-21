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

    // Método optimizado para evitar recursión excesiva
    public Object obtenerVariable(String nombre) {
        EntornoLisp entornoActual = this;
        while (entornoActual != null) {
            if (entornoActual.tablaVariables.containsKey(nombre)) {
                return entornoActual.tablaVariables.get(nombre);
            }
            entornoActual = entornoActual.entornoPadre;
        }
        throw new RuntimeException("Variable no encontrada: " + nombre);
    }

    public Object asignarVariable(String nombre, Object valor) {
        tablaVariables.put(nombre, valor);
        return valor;
    }

    // Método optimizado para evitar recursión excesiva
    public boolean existeVariable(String nombre) {
        EntornoLisp entornoActual = this;
        while (entornoActual != null) {
            if (entornoActual.tablaVariables.containsKey(nombre)) {
                return true;
            }
            entornoActual = entornoActual.entornoPadre;
        }
        return false;
    }

    public String registrarFuncion(String nombre, List<String> parametros, Object cuerpo) {
        tablaFunciones.put(nombre, new DefinicionFuncion(parametros, cuerpo));
        return nombre;
    }

    // Método optimizado para evitar recursión excesiva
    public DefinicionFuncion obtenerFuncion(String nombre) {
        EntornoLisp entornoActual = this;
        while (entornoActual != null) {
            if (entornoActual.tablaFunciones.containsKey(nombre)) {
                return entornoActual.tablaFunciones.get(nombre);
            }
            entornoActual = entornoActual.entornoPadre;
        }
        throw new RuntimeException("Función no encontrada: " + nombre);
    }

    // Método optimizado para evitar recursión excesiva
    public boolean existeFuncion(String nombre) {
        EntornoLisp entornoActual = this;
        while (entornoActual != null) {
            if (entornoActual.tablaFunciones.containsKey(nombre)) {
                return true;
            }
            entornoActual = entornoActual.entornoPadre;
        }
        return false;
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