class PruebaAtributos <T> {
    public int x;
    private char y;
    protected boolean z;
    
    public String cadena;
    private Object objeto;
    
    protected T atributoGenerico;
    
    // Visibilidad por defecto (vacía) debe seguir funcionando
    int sinVisibilidad;
    T genericoSinVisibilidad;
}