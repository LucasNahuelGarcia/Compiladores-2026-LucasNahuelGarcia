// Un atributo de instancia puede usarse mediante un receptor explicito en un metodo estatico.
class Aa {
    int a1;
    Bb x2;

    static void m1(Aa p1) {
        var v1 = new Aa();
        v1.a1 = 10;
    }
}

class Bb {
}
