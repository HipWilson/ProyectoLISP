
package Interprete;

public class ErrorLisp extends RuntimeException {

    private final int fila;
    private final int columna;

    public ErrorLisp(String mensaje) {
        super(mensaje);
        this.fila = -1;
        this.columna = -1;
    }

    public ErrorLisp(String mensaje, int fila, int columna) {
        super(String.format("Fallo en la línea %d, posición %d: %s", fila, columna, mensaje));
        this.fila = fila;
        this.columna = columna;
    }

    public ErrorLisp(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.fila = -1;
        this.columna = -1;
    }

    public int obtenerFila() {
        return fila;
    }

    public int obtenerColumna() {
        return columna;
    }
}
