// Sustitución reflexiva de T con el mismo lexema en cada nivel.
class Base<T> { T x; T m(T a) { return a; } }
class Intermedia<T> extends Base<T> { }
class Final<T> extends Intermedia<T> {
    T prueba(T param) {
        this.x = param;
        return this.m(this.x);
    }
}
