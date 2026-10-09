///[Error:m|4]
class Caja<T>{}
class A1{ Caja<String> m(){} }
class B2 extends A1{ Caja<Object> m(){} }
class Init{ static void main(){} }
