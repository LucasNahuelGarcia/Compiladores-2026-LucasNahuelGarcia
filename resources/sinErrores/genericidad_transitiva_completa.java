///[SinErrores]
class A1<T>{ T valor; T m(T x){} }
class B2<U> extends A1<U>{ U otro; }
class C3 extends B2<String>{ String m(String x){} }
class Init{ static void main(){} }
