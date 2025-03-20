package Interprete;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        LispProcessor interpreter = new LispProcessor();

        if (args.length > 0) {
            try {
                executeFile(args[0], interpreter);
                return;
            } catch (IOException e) {
                System.err.println("Error al leer el archivo: " + e.getMessage());
                System.exit(1);
            }
        }

        startREPL(interpreter);
    }

    private static void executeFile(String filename, LispProcessor interpreter) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(";")) {
                    continue;
                }

                buffer.append(line).append(" ");

                int openParens = countOccurrences(buffer.toString(), '(');
                int closedParens = countOccurrences(buffer.toString(), ')');

                if (openParens > 0 && openParens == closedParens) {
                    try {
                        Object result = interpreter.procesar(buffer.toString());

                        if (result != null) {
                            System.out.println(result);
                        }
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                    }

                    buffer.setLength(0);
                }
            }

            if (buffer.length() > 0) {
                try {
                    Object result = interpreter.procesar(buffer.toString());

                    if (result != null) {
                        System.out.println(result);
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private static void startREPL(LispProcessor interpreter) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Intérprete LISP - Ingrese expresiones LISP");
        System.out.println("Ejemplos: (+ 2 3), (defun factorial (n) (cond ((equal n 0) 1) (t (* n (factorial (- n 1))))))");
        System.out.println("Escriba 'salir' para terminar");
        System.out.println();

        StringBuilder buffer = new StringBuilder();

        while (true) {
            System.out.print(buffer.length() == 0 ? "lisp> " : "...> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("salir")) {
                System.out.println("Adios, gracias por usar el programa");
                break;
            }

            if (input.isEmpty() || input.startsWith(";")) {
                continue;
            }

            buffer.append(input).append(" ");

            int openParens = countOccurrences(buffer.toString(), '(');
            int closedParens = countOccurrences(buffer.toString(), ')');

            if (openParens > 0 && openParens == closedParens) {
                try {
                    Object result = interpreter.procesar(buffer.toString());

                    if (result != null) {
                        System.out.println("=> " + result);
                    }
                } catch (ErrorLisp e) {
                    System.err.println("Error: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Causa: " + e.getCause().getMessage());
                    }
                } catch (Exception e) {
                    System.err.println("Error inesperado: " + e.getMessage());
                    e.printStackTrace();
                }

                buffer.setLength(0);
                System.out.println();
            }
        }

        scanner.close();
    }

    private static int countOccurrences(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }
}