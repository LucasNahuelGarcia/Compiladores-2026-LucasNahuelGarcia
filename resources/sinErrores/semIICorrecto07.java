// Arreglos y las dos formas de ciclo aceptadas por la gramatica.
class A1 {
    int[][] matriz;
    int longitud;
    String texto;

    void recorrer(int[] valores) {
        longitud = valores.length;
        texto = valores.toString();
        matriz[0][0] = longitud;
        for (int valor : valores) {
            var copia = valor;
        }
        for (; true; ) {
            ;
        }
    }
}

class Init { static void main() { } }
