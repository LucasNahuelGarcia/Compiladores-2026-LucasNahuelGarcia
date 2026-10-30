// Los primarios parentizados conservan valor; sus cadenas no son lvalues.
class PrimarioParentizado {
    int x;
    PrimarioParentizado siguiente;
    int darNumero() { return 1; }
    PrimarioParentizado darObjeto() { return this; }
    int[] darArreglo() { return new int[3]; }
    void validos(PrimarioParentizado obj) {
        var a = (obj).darNumero();
        var b = (obj).x + 1;
        var c = (this.x = 3);
        this.x = (obj.x = 5);
        darObjeto().darArreglo();
    }
}
