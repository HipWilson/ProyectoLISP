package Interprete;

import java.util.ArrayList;
import java.util.List;

public class TokenizadorLisp {

    public static class Simbolo {
        private final String contenido;
        private final int fila;
        private final int columna;

        public Simbolo(String contenido, int fila, int columna) {
            this.contenido = contenido;
            this.fila = fila;
            this.columna = columna;
        }

        public String obtenerContenido() {
            return contenido;
        }

        public int obtenerFila() {
            return fila;
        }

        public int obtenerColumna() {
            return columna;
        }

        @Override
        public String toString() {
            return contenido;
        }
    }

    public List<String> analizar(String entrada) {
        List<String> tokens = new ArrayList<>();
        List<Simbolo> listaTokens = analizarConUbicacion(entrada);
        for (Simbolo token : listaTokens) {
            tokens.add(token.obtenerContenido());
        }
        return tokens;
    }

    public List<Simbolo> analizarConUbicacion(String entrada) {
        List<Simbolo> tokens = new ArrayList<>();
        int filaActual = 1;
        int columnaActual = 1;

        StringBuilder tokenTemporal = new StringBuilder();
        int inicioColumna = 1;

        for (int i = 0; i < entrada.length(); i++) {
            char caracter = entrada.charAt(i);

            if (caracter == '\n') {
                if (tokenTemporal.length() > 0) {
                    tokens.add(new Simbolo(tokenTemporal.toString(), filaActual, inicioColumna));
                    tokenTemporal.setLength(0);
                }
                filaActual++;
                columnaActual = 1;
                continue;
            }

            if (caracter == '(' || caracter == ')' || caracter == '\'') {
                if (tokenTemporal.length() > 0) {
                    tokens.add(new Simbolo(tokenTemporal.toString(), filaActual, inicioColumna));
                    tokenTemporal.setLength(0);
                }
                tokens.add(new Simbolo(String.valueOf(caracter), filaActual, columnaActual));
                columnaActual++;
                continue;
            }

            if (Character.isWhitespace(caracter)) {
                if (tokenTemporal.length() > 0) {
                    tokens.add(new Simbolo(tokenTemporal.toString(), filaActual, inicioColumna));
                    tokenTemporal.setLength(0);
                }
                columnaActual++;
                continue;
            }

            if (tokenTemporal.length() == 0) {
                inicioColumna = columnaActual;
            }

            tokenTemporal.append(caracter);
            columnaActual++;
        }

        if (tokenTemporal.length() > 0) {
            tokens.add(new Simbolo(tokenTemporal.toString(), filaActual, inicioColumna));
        }

        return tokens;
    }
}