///[Error:m|3]
interface I1<T>{ void m(T x); }
interface J2 extends I1<String>{ void m(Object x); }
class Init{ static void main(){} }
