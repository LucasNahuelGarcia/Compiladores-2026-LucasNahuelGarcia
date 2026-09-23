class Sistema <T> extends Base {
    
    public static void inicializar() {}
    
    private T[] elementos;
    protected int cantidad;
    public boolean activo;
    
    public Sistema(int capacidad) {
        var temp = capacidad;
    }
    
    private Sistema() {}
    
    public T obtener(int indice) {
        return null;
    }
    
    protected void procesar(T elemento) {
        if (activo) {
            var x = elemento;
        } else {
            return;
        }
    }
}