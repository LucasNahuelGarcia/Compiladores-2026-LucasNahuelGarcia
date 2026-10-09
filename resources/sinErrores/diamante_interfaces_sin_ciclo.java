///[SinErrores]
interface Base1{}
interface Left2 extends Base1{}
interface Right3 extends Base1{}
class Join4 implements Left2,Right3{}
class Init{ static void main(){} }
