class PruebaFor {
    public void probarBucles() {
        // 1. For completo estandar (asumiendo que i ya fue declarada como var local)
        for (i = 0; i < 10; i = i + 1) {
            var x = i;
        }
        
        // 2. For sin inicialización
        for (; actual != null; actual = actual.siguiente) {
            hacerAlgo();
        }
        
        // 3. For infinito (todas las expresiones opcionales vacías)
        for (;;) {
            if (condicion) return;
        }
        
        // 4. For de una sola línea (sin llaves)
        for (j = 0; j < 5; j = j + 1) 
            llamarMetodo(j);
    }
}