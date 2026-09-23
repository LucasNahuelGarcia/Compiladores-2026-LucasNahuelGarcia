class PruebaMetodos {
    
    // Constructores
    public PruebaMetodos() {}
    private PruebaMetodos(int semilla) {}
    protected PruebaMetodos(char a, char b) {}
    PruebaMetodos(boolean bandera) {} // Sin visibilidad
    
    // Método estático
    public static void main(String[] args) {}
    private static int calcular() { return 0; }
    
    // Métodos void
    public void hacerAlgo() {}
    protected void hacerOtraCosa(int x) { var y = x; }
    
    // Métodos que devuelven objetos (Pone a prueba el if de RestoMiembroIdClase)
    public PruebaMetodos obtenerInstancia() { return this; }
    private String obtenerNombre() { return null; }
}