package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class EntradaClase {
    private Token token;
    private String nombre;
    private TipoReferencia claseBase;
    private Map<String, EntradaAtributo> atributos;
    private Map<String, EntradaMetodo> metodos;
    private Map<String, EntradaMetodo> constructores;
    private String parametroGenerico;
    private boolean estaConsolidada;
    private List<TipoReferencia> interfaces;
    private boolean interfaz;
    private boolean finalClase;
    private boolean sealed;
    private boolean nonSealed;
    private List<Token> permisos;

    public EntradaClase(Token token, String nombre) {
        this.token = Objects.requireNonNull(token, "El token de la clase no puede ser null");
        this.nombre = nombre;
        this.atributos = new LinkedHashMap<>();
        this.metodos = new LinkedHashMap<>();
        this.constructores = new LinkedHashMap<>();
        this.estaConsolidada = false;
        this.interfaces = new ArrayList<>();
        this.permisos = new ArrayList<>();
    }

    public EntradaClase(Token token) {
        this(token, token.getLexema());
    }

    public EntradaClase(Token token, boolean interfaz) {
        this(token);
        this.interfaz = interfaz;
    }

    public boolean isInterfaz() {
        return interfaz;
    }

    public void setInterfaz(boolean interfaz) {
        this.interfaz = interfaz;
    }

    public boolean isFinalClase() { return finalClase; }
    public void setFinalClase(boolean finalClase) { this.finalClase = finalClase; }
    public boolean isSealed() { return sealed; }
    public void setSealed(boolean sealed) { this.sealed = sealed; }
    public boolean isNonSealed() { return nonSealed; }
    public void setNonSealed(boolean nonSealed) { this.nonSealed = nonSealed; }
    public List<Token> getPermisos() { return Collections.unmodifiableList(permisos); }
    public void agregarPermiso(Token permiso) { permisos.add(permiso); }

    public void agregarAtributo(EntradaAtributo atributo) {
        if (atributos.containsKey(atributo.getNombre()))
            throw new ExcepcionSemantica(atributo.getToken(),
                    "el atributo '" + atributo.getNombre() + "' ya fue declarado en la clase '" + nombre + "'");
        atributos.put(atributo.getNombre(), atributo);
    }

    public void agregarMetodo(EntradaMetodo metodo) {
        if (metodos.containsKey(metodo.getClaveFirma()))
            throw new ExcepcionSemantica(metodo.getToken(),
                    "el metodo '" + metodo.getNombre() + "' con aridad "
                            + metodo.getParametros().size() + " ya fue declarado en la clase '" + nombre + "'");
        metodos.put(metodo.getClaveFirma(), metodo);
    }

    public void agregarConstructor(EntradaMetodo constructor) {
        if (constructores.containsKey(constructor.getClaveConstructor()))
            throw new ExcepcionSemantica(constructor.getToken(),
                    "el constructor '" + constructor.getNombre() + "' con aridad "
                            + constructor.getParametros().size() + " ya fue declarado en la clase '" + nombre + "'");
        constructores.put(constructor.getClaveConstructor(), constructor);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = Objects.requireNonNull(token, "El token de la clase no puede ser null");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoReferencia getClaseBase() {
        return claseBase;
    }

    public void setClaseBase(TipoReferencia claseBase) {
        this.claseBase = claseBase;
        this.estaConsolidada = false;
    }

    public String getParametroGenerico() {
        return parametroGenerico;
    }

    public void setParametroGenerico(String parametroGenerico) {
        this.parametroGenerico = parametroGenerico;
    }

    public boolean isEstaConsolidada() {
        return estaConsolidada;
    }

    public void agregarInterfaz(TipoReferencia interfaz) {
        interfaces.add(interfaz);
        estaConsolidada = false;
    }

    public List<TipoReferencia> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    public Map<String, EntradaAtributo> getAtributos() {
        return Collections.unmodifiableMap(atributos);
    }

    public void setAtributos(Map<String, EntradaAtributo> atributos) {
        this.atributos = new LinkedHashMap<>(atributos);
    }

    public Map<String, EntradaMetodo> getMetodos() {
        return Collections.unmodifiableMap(metodos);
    }

    public EntradaMetodo getMetodo(String nombre, int aridad) {
        return metodos.get(nombre + "/" + aridad);
    }

    public void setMetodos(Map<String, EntradaMetodo> metodos) {
        this.metodos = new LinkedHashMap<>(metodos);
    }

    public Map<String, EntradaMetodo> getConstructores() {
        return Collections.unmodifiableMap(constructores);
    }

    public void setConstructores(Map<String, EntradaMetodo> constructores) {
        this.constructores = new LinkedHashMap<>(constructores);
    }

    public void estaBienDeclarado(TablaSimbolos tablaSimbolos) {
        chequearCircularidad(tablaSimbolos, new ArrayList<>());
        if (claseBase != null)
            claseBase.estaBienDeclarado(tablaSimbolos, parametroGenerico);
        for (TipoReferencia interfaz : interfaces)
            interfaz.estaBienDeclarado(tablaSimbolos, parametroGenerico);
        for (EntradaAtributo atributo : atributos.values())
            atributo.estaBienDeclarado(tablaSimbolos, parametroGenerico);
        for (EntradaMetodo metodo : metodos.values())
            metodo.estaBienDeclarado(tablaSimbolos, parametroGenerico);
        for (EntradaMetodo constructor : constructores.values())
            constructor.estaBienDeclarado(tablaSimbolos, parametroGenerico);
    }

    private void chequearCircularidad(TablaSimbolos tablaSimbolos, List<String> rutaActual) {
        if (rutaActual.contains(nombre))
            throw new ExcepcionSemantica(token,
                    "herencia circular detectada: la entidad '" + nombre + "' esta en un ciclo de herencia");

        rutaActual.add(nombre);

        if (claseBase != null) {
            EntradaClase padre = tablaSimbolos.getClase(claseBase.getNombreClase());
            if (padre != null)
                padre.chequearCircularidad(tablaSimbolos, rutaActual);
        }

        for (TipoReferencia referenciaInterfaz : interfaces) {
            EntradaClase interfaz = tablaSimbolos.getClase(referenciaInterfaz.getNombreClase());
            if (interfaz != null)
                interfaz.chequearCircularidad(tablaSimbolos, rutaActual);
        }

        rutaActual.remove(nombre);
    }

    public void consolidar(TablaSimbolos tablaSimbolos) {
        if (estaConsolidada)
            return;

        // El ancestro siempre se consolida antes de copiar sus miembros.
        EntradaClase padre = claseBase == null ? null : tablaSimbolos.getClase(claseBase.getNombreClase());
        if (padre != null)
            padre.consolidar(tablaSimbolos);

        for (TipoReferencia interfaz : interfaces) {
            EntradaClase entradaInterfaz = tablaSimbolos.getClase(interfaz.getNombreClase());
            if (entradaInterfaz != null)
                entradaInterfaz.consolidar(tablaSimbolos);
        }

        Map<String, Tipo> sustituciones = obtenerSustituciones(padre, claseBase);
        if (padre != null)
            absorberMiembros(padre, sustituciones);
        for (TipoReferencia interfaz : interfaces) {
            EntradaClase entradaInterfaz = tablaSimbolos.getClase(interfaz.getNombreClase());
            if (entradaInterfaz != null && this.interfaz)
                absorberMiembros(entradaInterfaz, obtenerSustituciones(entradaInterfaz, interfaz));
        }
        estaConsolidada = true;
    }

    void validarContratosDeInterfaces(TablaSimbolos tablaSimbolos) {
        if (interfaz)
            return;

        EntradaClase padre = claseBase == null ? null : tablaSimbolos.getClase(claseBase.getNombreClase());
        if (padre != null)
            padre.consolidar(tablaSimbolos);

        for (TipoReferencia referenciaInterfaz : interfaces) {
            EntradaClase entradaInterfaz = tablaSimbolos.getClase(referenciaInterfaz.getNombreClase());
            if (entradaInterfaz == null)
                continue;
            entradaInterfaz.consolidar(tablaSimbolos);
            Map<String, Tipo> sustituciones = obtenerSustituciones(entradaInterfaz, referenciaInterfaz);
            for (EntradaMetodo requerido : entradaInterfaz.metodos.values()) {
                EntradaMetodo implementacion = metodos.get(requerido.getClaveFirma());
                if (implementacion != null && implementacion.getToken() == requerido.getToken())
                    implementacion = null;
                if (implementacion == null && padre != null)
                    implementacion = padre.metodos.get(requerido.getClaveFirma());
                if (implementacion != null && implementacion.getToken() == requerido.getToken())
                    implementacion = null;
                if (implementacion == null || implementacion.isEstatico()
                    || nivelVisibilidad(implementacion.getVisibilidad()) < 2
                        || !mismaFirma(requerido, implementacion, sustituciones))
                    throw new ExcepcionSemantica(token,
                            "la clase '" + nombre + "' no implementa correctamente el metodo '"
                                    + requerido.getNombre() + "'");
            }
        }
    }

    private void absorberMiembros(EntradaClase padre, Map<String, Tipo> sustituciones) {
        for (EntradaAtributo atributoPadre : padre.atributos.values()) {
            if (atributos.containsKey(atributoPadre.getNombre()))
                throw new ExcepcionSemantica(atributos.get(atributoPadre.getNombre()).getToken(),
                        "el atributo '" + atributoPadre.getNombre() + "' oculta un atributo heredado");
            atributos.put(atributoPadre.getNombre(), atributoPadre.copiar(sustituciones));
        }

        for (EntradaMetodo metodoPadre : padre.metodos.values()) {
            EntradaMetodo metodoHijo = metodos.get(metodoPadre.getClaveFirma());
            if (metodoHijo == null) {
                metodos.put(metodoPadre.getClaveFirma(), metodoPadre.copiar(sustituciones));
            } else if (metodoPadre.getVisibilidad().equals("private")) {
                continue;
            } else if (metodoPadre.isEstatico() || metodoHijo.isEstatico()) {
                throw new ExcepcionSemantica(metodoHijo.getToken(),
                        "no se puede redefinir el metodo estatico '" + metodoHijo.getNombre() + "'");
            } else if (metodoPadre.isFinalMethod()) {
                throw new ExcepcionSemantica(metodoHijo.getToken(),
                        "no se puede redefinir el metodo final '" + metodoHijo.getNombre() + "'");
            } else if (nivelVisibilidad(metodoHijo.getVisibilidad())
                    < nivelVisibilidad(metodoPadre.getVisibilidad())) {
                throw new ExcepcionSemantica(metodoHijo.getToken(),
                        "el metodo hijo no puede reducir la visibilidad de '" + metodoHijo.getNombre() + "'");
            } else if (!mismaFirma(metodoPadre, metodoHijo, sustituciones)) {
                throw new ExcepcionSemantica(metodoHijo.getToken(),
                        "la redefinicion del metodo '" + metodoHijo.getNombre() + "' no coincide con su ancestro");
            }
        }
    }

    private int nivelVisibilidad(String visibilidad) {
        if ("private".equals(visibilidad))
            return 0;
        if ("protected".equals(visibilidad))
            return 1;
        return 2;
    }

    private Map<String, Tipo> obtenerSustituciones(EntradaClase padre, TipoReferencia relacion) {
        Map<String, Tipo> sustituciones = new HashMap<>();
        if (padre != null && padre.parametroGenerico != null && relacion != null
                && relacion.getArgumentoGenerico() != null)
            sustituciones.put(padre.parametroGenerico, relacion.getArgumentoGenerico());
        return sustituciones;
    }

    private boolean mismaFirma(EntradaMetodo padre, EntradaMetodo hijo, Map<String, Tipo> sustituciones) {
        if (!mismoTipo(padre.getTipoRetorno(), hijo.getTipoRetorno(), sustituciones))
            return false;
        EntradaParametro[] parametrosPadre = padre.getParametros().values().toArray(new EntradaParametro[0]);
        EntradaParametro[] parametrosHijo = hijo.getParametros().values().toArray(new EntradaParametro[0]);
        if (parametrosPadre.length != parametrosHijo.length)
            return false;
        for (int i = 0; i < parametrosPadre.length; i++)
            if (!mismoTipo(parametrosPadre[i].getTipo(), parametrosHijo[i].getTipo(), sustituciones))
                return false;
        return true;
    }

    private boolean mismoTipo(Tipo primero, Tipo segundo, Map<String, Tipo> sustituciones) {
        if (primero == null || segundo == null)
            return primero == segundo;
        if (primero instanceof TipoArreglo && segundo instanceof TipoArreglo)
            return mismoTipo(((TipoArreglo) primero).getTipoElemento(),
                    ((TipoArreglo) segundo).getTipoElemento(), sustituciones);
        if (primero instanceof TipoPrimitivo && segundo instanceof TipoPrimitivo)
            return ((TipoPrimitivo) primero).getPrimitivo() == ((TipoPrimitivo) segundo).getPrimitivo();
        if (primero instanceof TipoReferencia && segundo instanceof TipoReferencia) {
            TipoReferencia referenciaPrimera = (TipoReferencia) primero;
            TipoReferencia referenciaSegunda = (TipoReferencia) segundo;
            Tipo sustituto = sustituciones.get(referenciaPrimera.getNombreClase());
            if (sustituto != null)
                return mismoTipo(sustituto, segundo, sustituciones);
            if (!referenciaPrimera.getNombreClase().equals(referenciaSegunda.getNombreClase()))
                return false;
            return mismoTipo(referenciaPrimera.getArgumentoGenerico(),
                    referenciaSegunda.getArgumentoGenerico(), sustituciones);
        }
        return false;
    }
}