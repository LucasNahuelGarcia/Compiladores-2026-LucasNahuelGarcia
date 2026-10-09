///[SinErrores]
interface I1{ void m(); }
interface I2{ void n(); }
interface J3 extends I1,I2{}
class C4 implements J3{ void m(){} void n(){} }
class Init{ static void main(){} }
