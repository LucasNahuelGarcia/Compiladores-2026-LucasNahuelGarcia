// Las llamadas recursivas consultan firmas, sin re-chequear cuerpos.
class Recursiva {
    int ping(int n) { return pong(n - 1); }
    int pong(int n) { return ping(n - 1); }
    int bucle(int n) { return bucle(bucle(n)); }
}
