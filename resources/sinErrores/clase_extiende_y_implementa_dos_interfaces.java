///[SinErrores]
class P1{}
interface I1{ void m(); }
interface I2{ void n(); }
class C3 extends P1 implements I1,I2{ void m(){} void n(){} }
class Init{ static void main(){} }
