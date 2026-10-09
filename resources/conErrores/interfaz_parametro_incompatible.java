///[Error:C3|3]
interface I1{ void m(int x); }
class C3 implements I1{ void m(char x){} }
class Init{ static void main(){} }
