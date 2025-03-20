package Interprete;

/**
 * Excepción específica para errores en el intérprete LISP.
 * Proporciona información contextual como línea y columna.
 * 
 * @author Equipo LISP
 */
public class LispException extends RuntimeException {
    
    private final int line;
    private final int column;
    
    /**
     * Constructor simple con solo mensaje.
     * 
     * @param message Mensaje de error
     */
    public LispException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }
    
    /**
     * Constructor con información de posición.
     * 
     * @param message Mensaje de error
     * @param line Línea donde se produjo el error
     * @param column Columna donde se produjo el error
     */
    public LispException(String message, int line, int column) {
        super(String.format("Error en línea %d, columna %d: %s", line, column, message));
        this.line = line;
        this.column = column;
    }
    
    /**
     * Constructor con causa.
     * 
     * @param message Mensaje de error
     * @param cause Causa del error
     */
    public LispException(String message, Throwable cause) {
        super(message, cause);
        this.line = -1;
        this.column = -1;
    }
    
    /**
     * Obtiene la línea donde se produjo el error.
     * 
     * @return Número de línea o -1 si no está disponible
     */
    public int getLine() {
        return line;
    }
    
    /**
     * Obtiene la columna donde se produjo el error.
     * 
     * @return Número de columna o -1 si no está disponible
     */
    public int getColumn() {
        return column;
    }
}