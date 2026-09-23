interface MiInterfaz <Generico> {
    public void metodoPublico();
    private int metodoPrivado(char c);
    protected MiInterfaz metodoProtegido();
    
    // Las interfaces deben seguir soportando métodos sin visibilidad explícita
    void metodoPorDefecto();
    Generico obtenerGenerico();
}