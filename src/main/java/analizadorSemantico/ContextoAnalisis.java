package analizadorSemantico;

import analizadorLexico.Token;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/** State shared while checking one method or constructor body. */
public final class ContextoAnalisis {
    private final TablaSimbolos tablaSimbolos;
    private final EntradaClase claseActual;
    private final EntradaMetodo unidadActual;
    private final Deque<Ambito> pilaDeBloques = new ArrayDeque<>();

    public ContextoAnalisis(TablaSimbolos tablaSimbolos, EntradaClase claseActual,
                            EntradaMetodo unidadActual) {
        this.tablaSimbolos = tablaSimbolos;
        this.claseActual = claseActual;
        this.unidadActual = unidadActual;
        abrirBloque();
    }

    public TablaSimbolos getTablaSimbolos() { return tablaSimbolos; }
    public EntradaClase getClaseActual() { return claseActual; }
    public EntradaMetodo getUnidadActual() { return unidadActual; }
    public Tipo getRetornoEsperado() { return unidadActual.getTipoRetorno(); }
    public boolean esConstructor() { return unidadActual.getTipoRetorno() == null; }
    public boolean esEstatico() { return unidadActual.isEstatico(); }

    public void abrirBloque() {
        pilaDeBloques.push(new Ambito());
    }

    public void cerrarBloque() {
        if (pilaDeBloques.isEmpty())
            throw new IllegalStateException("No hay un bloque de analisis para cerrar");
        pilaDeBloques.pop();
    }

    public VariableLocal declararLocal(Token token, String nombre, Tipo tipo) {
        if (buscarParametro(nombre) != null || buscarLocalVisible(nombre) != null)
            throw new ExcepcionSemantica(token,
                    "el nombre de variable local o parametro '" + nombre + "' ya esta visible");
        VariableLocal local = new VariableLocal(token, nombre, tipo);
        pilaDeBloques.peek().agregar(local);
        return local;
    }

    public VariableLocal buscarLocalVisible(String nombre) {
        for (Ambito ambito : pilaDeBloques) {
            VariableLocal variable = ambito.get(nombre);
            if (variable != null)
                return variable;
        }
        return null;
    }

    public EntradaParametro buscarParametro(String nombre) {
        return unidadActual.getParametros().get(nombre);
    }

    public VariableVisible resolverVariable(String nombre) {
        VariableLocal local = buscarLocalVisible(nombre);
        if (local != null)
            return new VariableVisible(local.getToken(), local.getTipo(), false, false, local);
        EntradaParametro parametro = buscarParametro(nombre);
        if (parametro != null)
            return new VariableVisible(parametro.getToken(), parametro.getTipo(), false, false, parametro);
        EntradaAtributo atributo = claseActual.getAtributos().get(nombre);
        if (atributo != null) {
            Tipo receptor = claseActual.getParametroGenerico() == null
                    ? new TipoReferencia(claseActual.getToken(), claseActual.getNombre())
                    : new TipoReferencia(claseActual.getToken(), claseActual.getNombre(),
                        new TipoParametro(claseActual.getSimboloParametroGenerico().getToken(),
                                claseActual.getSimboloParametroGenerico()));
            Tipo tipo = TipoVistas.vista(atributo.getDeclaracionOriginal().getTipo(), receptor,
                    atributo.getClaseDeclaracion(), tablaSimbolos);
            return new VariableVisible(atributo.getToken(), tipo, true, atributo.isEstatico(),
                    atributo.getDeclaracionOriginal(), atributo);
        }
        return null;
    }

    public static final class VariableVisible {
        private final Token token;
        private final Tipo tipo;
        private final boolean atributo;
        private final boolean estatico;
        private final EntidadDeclarativa entidad;
        private final EntradaAtributo referenciaAtributo;

        private VariableVisible(Token token, Tipo tipo, boolean atributo, boolean estatico,
                                EntidadDeclarativa entidad) {
            this(token, tipo, atributo, estatico, entidad, null);
        }

        private VariableVisible(Token token, Tipo tipo, boolean atributo, boolean estatico,
                                EntidadDeclarativa entidad, EntradaAtributo referenciaAtributo) {
            this.token = token;
            this.tipo = tipo;
            this.atributo = atributo;
            this.estatico = estatico;
            this.entidad = entidad;
            this.referenciaAtributo = referenciaAtributo;
        }

        public Token getToken() { return token; }
        public Tipo getTipo() { return tipo; }
        public boolean esAtributo() { return atributo; }
        public boolean esEstatico() { return estatico; }
        public EntidadDeclarativa getEntidad() { return entidad; }
        public EntradaAtributo getReferenciaAtributo() { return referenciaAtributo; }
    }
}
