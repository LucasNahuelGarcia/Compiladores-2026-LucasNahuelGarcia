package analizadorSemantico;

import analizadorLexico.Token;
import analizadorSemantico.ast.*;
import java.util.*;

/** Type checks method and constructor bodies after declarations and inheritance are complete. */
final class AnalizadorCuerpos {
    private final TablaSimbolos tabla;
    private ContextoAnalisis contexto;

    AnalizadorCuerpos(TablaSimbolos tabla) { this.tabla = tabla; }

    void analizar() {
        for (EntradaClase clase : tabla.getClases().values()) {
            for (EntradaMetodo metodo : clase.getMetodos().values())
                if (metodo.getClaseDeclaracion() == clase && metodo.getCuerpo() != null)
                    analizarUnidad(clase, metodo);
            for (EntradaMetodo ctor : clase.getConstructores().values())
                if (ctor.getCuerpo() != null) analizarUnidad(clase, ctor);
        }
    }

    private void analizarUnidad(EntradaClase clase, EntradaMetodo metodo) {
        contexto = new ContextoAnalisis(tabla, clase, metodo);
        for (EntradaParametro p : metodo.getParametros().values())
            p.getTipo().estaBienDeclarado(tabla, clase.getParametroGenerico());
        analizarBloque(metodo.getCuerpo(), false);
    }

    private void analizarBloque(BloqueAst bloque, boolean nuevoAmbito) {
        if (nuevoAmbito) contexto.abrirBloque();
        try { for (SentenciaAst s : bloque.getSentencias()) analizarSentencia(s); }
        finally { if (nuevoAmbito) contexto.cerrarBloque(); }
    }

    private void analizarSentencia(SentenciaAst s) {
        if (s instanceof BloqueAst) { analizarBloque((BloqueAst)s, true); return; }
        if (s instanceof CicloForAst) { analizarFor((CicloForAst)s); return; }
        NodoSentenciaAst n = (NodoSentenciaAst)s;
        List<ExpresionAst> es = n.getExpresiones();
        switch (n.getClase()) {
            case VACIA: return;
            case EXPRESION:
                tipo(es.get(0));
                if (!es.get(0).esLlamadaValida()) error(es.get(0).getToken(), "la expresion no puede usarse como sentencia");
                return;
            case VARIABLE_LOCAL: {
                Tipo t = tipo(es.get(0));
                if (t == null || esVoid(t)) error(es.get(0).getToken(), "una variable local no puede tener tipo void");
                if (t instanceof TipoNull) error(es.get(0).getToken(), "var no puede inferir el tipo null");
                contexto.declararLocal(n.getNombre(), n.getNombre().getLexema(), t); return;
            }
            case RETURN:
                Tipo esperado = contexto.getRetornoEsperado();
                if (es.isEmpty()) {
                    if (esperado != null && !esVoid(esperado)) error(s.getToken(), "falta el valor de retorno");
                } else {
                    Tipo actual = tipo(es.get(0), esperado);
                    if (esperado == null || esVoid(esperado) || !ConformidadTipos.conforma(actual, esperado, tabla))
                        error(es.get(0).getToken(), "el valor retornado no coincide con el tipo declarado");
                }
                return;
            case IF:
                exigirBooleano(es.get(0));
                for (SentenciaAst rama : n.getSentencias()) {
                    if (esDeclaracionLocalDirecta(rama)) error(rama.getToken(), "var no puede ser el cuerpo directo de if o else");
                    analizarSentencia(rama);
                }
                return;
            case WHILE:
                exigirBooleano(es.get(0));
                if (esDeclaracionLocalDirecta(n.getSentencias().get(0))) error(n.getSentencias().get(0).getToken(), "var no puede ser el cuerpo directo de while");
                analizarSentencia(n.getSentencias().get(0)); return;
            default: return;
        }
    }

    private void analizarFor(CicloForAst f) {
        contexto.abrirBloque();
        try {
            if (f.getForma() == CicloForAst.Forma.CLASICO) {
                if (f.getInicializacion() != null) {
                    tipo(f.getInicializacion());
                    if (!f.getInicializacion().esLlamadaValida())
                        error(f.getInicializacion().getToken(), "la inicializacion del for no es una sentencia valida");
                }
                if (f.getCondicion() != null) exigirBooleano(f.getCondicion());
                if (f.getActualizacion() != null) {
                    tipo(f.getActualizacion());
                    if (!f.getActualizacion().esLlamadaValida())
                        error(f.getActualizacion().getToken(), "la actualizacion del for no es una sentencia valida");
                }
            } else {
                validarTipoEscrito(f.getTipoVariable(), f.getNombreVariable());
                Tipo iterable = tipo(f.getIterable());
                if (!(iterable instanceof TipoArreglo)) error(f.getIterable().getToken(), "el for mejorado requiere un arreglo iterable");
                Tipo elemento = ((TipoArreglo) iterable).getTipoElemento();
                if (!ConformidadTipos.conforma(elemento, f.getTipoVariable(), tabla))
                    error(f.getNombreVariable(), "el tipo de la variable del for no coincide con el elemento");
                contexto.declararLocal(f.getNombreVariable(), f.getNombreVariable().getLexema(), f.getTipoVariable());
            }
            analizarSentencia(f.getCuerpo());
        } finally { contexto.cerrarBloque(); }
    }

    private Tipo tipo(ExpresionAst e) { return tipo(e, null); }

    private Tipo tipo(ExpresionAst e, Tipo contextoEsperado) {
        if (!(e instanceof NodoExpresionAst)) return e.getTipo();
        NodoExpresionAst n = (NodoExpresionAst)e;
        List<ExpresionAst> a = n.getOperandos();
        Tipo t = null;
        switch (n.getClase()) {
            case LITERAL_INT: t = prim(TipoPrimitivo.Primitivo.INT, e.getToken()); break;
            case LITERAL_CHAR: t = prim(TipoPrimitivo.Primitivo.CHAR, e.getToken()); break;
            case LITERAL_BOOLEAN: t = prim(TipoPrimitivo.Primitivo.BOOLEAN, e.getToken()); break;
            case LITERAL_STRING: t = new TipoReferencia(e.getToken(), "String"); break;
            case LITERAL_NULL: t = new TipoNull(e.getToken()); break;
            case THIS:
                if (contexto.esEstatico()) error(e.getToken(), "this no puede usarse en un contexto estatico");
                t = tipoClaseActual(e.getToken()); break;
            case VARIABLE: {
                ContextoAnalisis.VariableVisible v = contexto.resolverVariable(n.getNombre().getLexema());
                if (v == null) error(n.getNombre(), "la variable no fue declarada");
                if (contexto.esEstatico() && v.esAtributo() && !v.esEstatico()) error(n.getNombre(), "un atributo de instancia no puede usarse en un contexto estatico");
                if (v.esAtributo() && "private".equals(v.getReferenciaAtributo().getVisibilidad())
                        && v.getReferenciaAtributo().getClaseDeclaracion() != contexto.getClaseActual())
                    error(n.getNombre(), "el atributo no es accesible");
                t = v.getTipo(); e.setAsignable(true); break;
            }
            case PARENTESIS: t = tipo(a.get(0)); break;
            case NUEVO_ARREGLO:
                validarTipoEscrito(n.getTipoEscrito(), e.getToken());
                for (ExpresionAst dim : a) exigirTipo(dim, TipoPrimitivo.Primitivo.INT);
                t = n.getTipoEscrito(); break;
            case NUEVO_OBJETO: {
                TipoReferencia clas = comoRef(n.getTipoEscrito(), e.getToken());
                validarTipoEscrito(clas, e.getToken());
                EntradaClase c = tabla.getClase(clas.getNombreClase());
                if (c == null || c.isInterfaz()) error(e.getToken(), "el tipo no es instanciable");
                if (c.getSimboloParametroGenerico() != null && clas.getArgumentoGenerico() == null)
                    error(e.getToken(), "no se puede instanciar una clase generica sin argumento");
                EntradaMetodo ctor = c.getConstructores().get(String.valueOf(a.size()));
                if (ctor == null) error(e.getToken(), "no existe un constructor compatible");
                if ("private".equals(ctor.getVisibilidad()) && c != contexto.getClaseActual())
                    error(e.getToken(), "el constructor no es accesible");
                validarArgumentos(a, ctor, clas, e.getToken(), null); t = clas; e.setLlamadaValida(true); break;
            }
            case ACCESO_INDICE: {
                Tipo base = tipo(a.get(0)); exigirTipo(a.get(1), TipoPrimitivo.Primitivo.INT);
                if (!(base instanceof TipoArreglo)) error(e.getToken(), "solo se puede indexar un arreglo");
                t = ((TipoArreglo)base).getTipoElemento(); e.setAsignable(!esParentesis(a.get(0))); break;
            }
            case ACCESO_ATRIBUTO: {
                Tipo receptor = tipo(a.get(0));
                if (receptor instanceof TipoArreglo) {
                    if (!"length".equals(n.getNombre().getLexema())) error(n.getNombre(), "los arreglos solo exponen el atributo length");
                    t = prim(TipoPrimitivo.Primitivo.INT, e.getToken());
                    break;
                }
                EntradaClase c = claseDe(receptor, e.getToken());
                if (c.isInterfaz()) error(n.getNombre(), "las interfaces no exponen atributos de instancia");
                EntradaAtributo attr = c.getAtributos().get(n.getNombre().getLexema());
                if (attr == null) error(n.getNombre(), "el atributo no fue declarado");
                if (attr.getVisibilidad().equals("private") && attr.getClaseDeclaracion() != contexto.getClaseActual()) error(n.getNombre(), "el atributo no es accesible");
                if (contexto.esEstatico() && attr.getClaseDeclaracion() == contexto.getClaseActual() && !attr.isEstatico()) error(n.getNombre(), "un atributo de instancia no puede usarse en un contexto estatico");
                t = TipoVistas.vista(attr.getDeclaracionOriginal().getTipo(), receptor, attr.getClaseDeclaracion(), tabla);
                e.setAsignable(!esParentesis(a.get(0))); break;
            }
            case LLAMADA_IMPLICITA:
            case LLAMADA_ESTATICA:
            case LLAMADA_MIEMBRO: {
                Tipo receptor;
                EntradaClase c;
                int desde;
                if (n.getClase() == NodoExpresionAst.Clase.LLAMADA_MIEMBRO) {
                    receptor = tipo(a.get(0)); desde = 1;
                    if (receptor instanceof TipoArreglo) {
                        if (!"toString".equals(n.getNombre().getLexema()) || !argsVacias(a, desde))
                            error(n.getNombre(), "el arreglo solo admite toString() sin argumentos");
                        EntradaClase object = tabla.getClase("Object");
                        EntradaMetodo toString = object.getMetodo("toString", 0);
                        t = toString.getTipoRetorno(); e.setLlamadaValida(true); break;
                    }
                    c = claseDe(receptor, e.getToken());
                }
                else if (n.getClase() == NodoExpresionAst.Clase.LLAMADA_ESTATICA) {
                    receptor = n.getTipoEscrito();
                    desde = 0;
                    c = claseDe(receptor, receptor.getToken());
                }
                else { receptor = tipoClaseActual(e.getToken()); desde = 0; c = contexto.getClaseActual(); }
                List<ExpresionAst> args = a.subList(desde, a.size());
                EntradaMetodo m = c.getMetodo(n.getNombre().getLexema(), args.size());
                if (m == null) error(n.getNombre(), "no existe un metodo compatible");
                if (n.getClase() == NodoExpresionAst.Clase.LLAMADA_ESTATICA && !m.isEstatico()) error(n.getNombre(), "el metodo no es estatico");
                if ("private".equals(m.getVisibilidad()) && m.getClaseDeclaracion() != contexto.getClaseActual())
                    error(n.getNombre(), "el metodo no es accesible");
                Map<ParametroTipo, Tipo> inferencias = validarArgumentos(
                        args, m, receptor, n.getNombre(), contextoEsperado);
                t = ConformidadTipos.vista(TipoVistas.vista(
                        m.getDeclaracionOriginal().getTipoRetorno(), receptor,
                        m.getClaseDeclaracion(), tabla), inferencias);
                if (n.getClase() == NodoExpresionAst.Clase.LLAMADA_IMPLICITA
                        && contexto.esEstatico() && !m.isEstatico())
                    error(n.getNombre(), "un metodo de instancia no puede invocarse implicitamente en un contexto estatico");
                e.setLlamadaValida(n.getClase() != NodoExpresionAst.Clase.LLAMADA_MIEMBRO || !esParentesis(a.get(0))); break;
            }
            case ASIGNACION: {
                Tipo izq = tipo(a.get(0)), der = tipo(a.get(1), izq);
                if (!a.get(0).esAsignable()) error(e.getToken(), "el destino de asignacion no es asignable");
                if (!ConformidadTipos.conforma(der, izq, tabla)) error(e.getToken(), "tipos incompatibles en asignacion");
                t = izq; e.setLlamadaValida(true); break;
            }
            case INCREMENTO: {
                Tipo op = tipo(a.get(0)); if (!a.get(0).esAsignable()) error(e.getToken(), "el destino no es asignable");
                if (!(op instanceof TipoPrimitivo)
                        || !Arrays.asList(TipoPrimitivo.Primitivo.INT, TipoPrimitivo.Primitivo.CHAR)
                                .contains(((TipoPrimitivo)op).getPrimitivo()))
                    error(e.getToken(), "incremento requiere una variable numerica");
                t = op; e.setLlamadaValida(true); break;
            }
            case UNARIO: {
                Tipo op = tipo(a.get(0)); String lex = n.getValor();
                if (esVoid(op)) error(e.getToken(), "void no puede usarse como operando");
                exigirPrimitivo(op, lex.equals("!") ? TipoPrimitivo.Primitivo.BOOLEAN : TipoPrimitivo.Primitivo.INT, e.getToken());
                t = op; break;
            }
            case BINARIO: {
                Tipo x = tipo(a.get(0)), y = tipo(a.get(1)); String op = n.getValor();
                if (esVoid(x) || esVoid(y)) error(e.getToken(), "void no puede usarse como operando");
                if (op.equals("&&") || op.equals("||")) { exigirTipo(x, TipoPrimitivo.Primitivo.BOOLEAN, e.getToken()); exigirTipo(y, TipoPrimitivo.Primitivo.BOOLEAN, e.getToken()); t = prim(TipoPrimitivo.Primitivo.BOOLEAN, e.getToken()); }
                else if (Arrays.asList("==", "!=").contains(op)) { if (!ConformidadTipos.conforma(x,y,tabla) && !ConformidadTipos.conforma(y,x,tabla)) error(e.getToken(), "operandos incompatibles"); t = prim(TipoPrimitivo.Primitivo.BOOLEAN,e.getToken()); }
                else if (Arrays.asList("<", ">", "<=", ">=").contains(op)) { exigirTipo(x,TipoPrimitivo.Primitivo.INT,e.getToken()); exigirTipo(y,TipoPrimitivo.Primitivo.INT,e.getToken()); t=prim(TipoPrimitivo.Primitivo.BOOLEAN,e.getToken()); }
                else { exigirTipo(x,TipoPrimitivo.Primitivo.INT,e.getToken()); exigirTipo(y,TipoPrimitivo.Primitivo.INT,e.getToken()); t=prim(TipoPrimitivo.Primitivo.INT,e.getToken()); }
                break;
            }
            default: error(e.getToken(), "expresion no soportada");
        }
        e.setTipo(t); return t;
    }

    private Map<ParametroTipo, Tipo> validarArgumentos(List<ExpresionAst> args, EntradaMetodo metodo,
            Tipo receptor, Token token, Tipo contextoEsperado) {
        EntradaMetodo original = metodo.getDeclaracionOriginal();
        List<EntradaParametro> formales = new ArrayList<>(original.getParametros().values());
        List<Tipo> reales = new ArrayList<>();
        List<Tipo> esperados = new ArrayList<>();
        Map<ParametroTipo, Tipo> inferencias = new IdentityHashMap<>();
        for (int i = 0; i < args.size(); i++) {
            Tipo formal = TipoVistas.vista(formales.get(i).getTipo(), receptor,
                    metodo.getClaseDeclaracion(), tabla);
            Tipo real = tipo(args.get(i), formal);
            reales.add(real);
            esperados.add(formal);
            inferirParametrosDeMetodo(formal, real, original.getSimbolosParametrosGenericos(), inferencias);
        }
        if (contextoEsperado != null) {
            Tipo retornoDeclarado = TipoVistas.vista(original.getTipoRetorno(), receptor,
                    metodo.getClaseDeclaracion(), tabla);
            inferirParametrosDeMetodo(retornoDeclarado, contextoEsperado,
                    original.getSimbolosParametrosGenericos(), inferencias);
        }
        for (Map.Entry<String, ParametroTipo> entrada : original.getSimbolosParametrosGenericos().entrySet()) {
            ParametroTipo parametro = entrada.getValue();
            if (!inferencias.containsKey(parametro)) {
                List<Tipo> bounds = original.getParametrosGenericos().get(entrada.getKey());
                Tipo inferido = bounds == null || bounds.isEmpty()
                        ? new TipoReferencia(token, "Object")
                        : TipoVistas.vista(bounds.get(0), receptor, metodo.getClaseDeclaracion(), tabla);
                inferencias.put(parametro, inferido);
            }
        }
        for (Map.Entry<String, List<Tipo>> entrada : original.getParametrosGenericos().entrySet()) {
            ParametroTipo parametro = original.getSimbolosParametrosGenericos().get(entrada.getKey());
            Tipo inferido = inferencias.get(parametro);
            for (Tipo bound : entrada.getValue()) {
                Tipo vistaBound = TipoVistas.vista(bound, receptor, metodo.getClaseDeclaracion(), tabla);
                vistaBound = ConformidadTipos.vista(vistaBound, inferencias);
                if (!ConformidadTipos.conforma(inferido, vistaBound, tabla))
                    error(token, "el argumento inferido no satisface el limite generico");
            }
        }
        for (int i = 0; i < args.size(); i++) {
            Tipo esperado = ConformidadTipos.vista(esperados.get(i), inferencias);
            if (!ConformidadTipos.conforma(reales.get(i), esperado, tabla))
                error(token, "tipo de argumento incompatible");
        }
        return inferencias;
    }

    private void inferirParametrosDeMetodo(Tipo formal, Tipo real,
            Map<String, ParametroTipo> parametros, Map<ParametroTipo, Tipo> inferencias) {
        if (formal instanceof TipoArreglo && real instanceof TipoArreglo) {
            inferirParametrosDeMetodo(((TipoArreglo)formal).getTipoElemento(),
                    ((TipoArreglo)real).getTipoElemento(), parametros, inferencias);
            return;
        }
        if (!(formal instanceof TipoReferencia) || !(real instanceof TipoReferencia)) return;
        TipoReferencia f = (TipoReferencia)formal;
        TipoReferencia r = (TipoReferencia)real;
        if (f.getParametroTipo() != null && parametros.containsValue(f.getParametroTipo())) {
            Tipo anterior = inferencias.putIfAbsent(f.getParametroTipo(), real);
            if (anterior != null && !ConformidadTipos.iguales(anterior, real))
                inferencias.put(f.getParametroTipo(), null);
            return;
        }
        if (!f.esParametroTipo() && !r.esParametroTipo()
                && f.getNombreClase().equals(r.getNombreClase()))
            inferirParametrosDeMetodo(f.getArgumentoGenerico(), r.getArgumentoGenerico(), parametros, inferencias);
    }
    private void exigirBooleano(ExpresionAst e) { exigirTipo(e, TipoPrimitivo.Primitivo.BOOLEAN); }
    private void exigirTipo(ExpresionAst e, TipoPrimitivo.Primitivo tipo) { exigirTipo(tipo(e), tipo, e.getToken()); }
    private void exigirTipo(Tipo t, TipoPrimitivo.Primitivo esperado, Token token) { exigirPrimitivo(t, esperado, token); }
    private void exigirPrimitivo(Tipo t, TipoPrimitivo.Primitivo esperado, Token token) { if (!(t instanceof TipoPrimitivo) || ((TipoPrimitivo)t).getPrimitivo()!=esperado) error(token,"tipo de expresion incompatible"); }
    private TipoPrimitivo prim(TipoPrimitivo.Primitivo p, Token t) { return new TipoPrimitivo(t,p); }
    private Tipo tipoClaseActual(Token t) { EntradaClase c=contexto.getClaseActual(); return c.getParametroGenerico()==null ? new TipoReferencia(t,c.getNombre()) : new TipoReferencia(t,c.getNombre(),new TipoParametro(c.getSimboloParametroGenerico().getToken(),c.getSimboloParametroGenerico())); }
    private EntradaClase claseDe(Tipo t, Token token) { if (!(t instanceof TipoReferencia) || ((TipoReferencia)t).esParametroTipo()) error(token,"se esperaba un objeto para acceder al miembro"); EntradaClase c=tabla.getClase(((TipoReferencia)t).getNombreClase()); if(c==null) error(token,"tipo no declarado"); return c; }
    private TipoReferencia comoRef(Tipo t,Token token) { if(!(t instanceof TipoReferencia)) error(token,"se esperaba un tipo de referencia"); return (TipoReferencia)t; }
    private boolean esParentesis(ExpresionAst e) { return e instanceof NodoExpresionAst && ((NodoExpresionAst)e).getClase()==NodoExpresionAst.Clase.PARENTESIS; }
    private boolean argsVacias(List<ExpresionAst> args, int desde) { return args.size() == desde; }
    private boolean esVoid(Tipo t) { return t instanceof TipoVoid || t instanceof TipoPrimitivo && ((TipoPrimitivo)t).getPrimitivo()==TipoPrimitivo.Primitivo.VOID; }
    private boolean esDeclaracionLocalDirecta(SentenciaAst s) { return s instanceof NodoSentenciaAst && ((NodoSentenciaAst)s).getClase()==NodoSentenciaAst.Clase.VARIABLE_LOCAL; }
    private void validarTipoEscrito(Tipo t, Token token) {
        if (contexto.esEstatico() && contieneParametro(t, contexto.getClaseActual().getSimboloParametroGenerico()))
            error(token, "el parametro generico de clase no puede usarse en un contexto estatico");
        t.estaBienDeclarado(tabla, contexto.getClaseActual().getParametroGenerico());
        validarArgumentosGenericos(t, token);
    }
    private boolean contieneParametro(Tipo t, ParametroTipo simbolo) {
        if (t == null || simbolo == null) return false;
        if (t instanceof TipoArreglo) return contieneParametro(((TipoArreglo)t).getTipoElemento(), simbolo);
        if (!(t instanceof TipoReferencia)) return false;
        TipoReferencia ref = (TipoReferencia)t;
        return ref.getParametroTipo() == simbolo || contieneParametro(ref.getArgumentoGenerico(), simbolo);
    }
    private void validarArgumentosGenericos(Tipo t, Token token) {
        if (t instanceof TipoArreglo) { validarArgumentosGenericos(((TipoArreglo)t).getTipoElemento(), token); return; }
        if (!(t instanceof TipoReferencia)) return;
        TipoReferencia ref = (TipoReferencia)t;
        if (ref.esParametroTipo()) return;
        EntradaClase clase = tabla.getClase(ref.getNombreClase());
        if (clase == null) error(token, "el tipo no fue declarado");
        Tipo arg = ref.getArgumentoGenerico();
        if (clase.getSimboloParametroGenerico() != null && arg == null)
            error(token, "una clase generica requiere un argumento de tipo");
        if (arg != null) {
            if (!(arg instanceof TipoReferencia) || ((TipoReferencia)arg).esParametroTipo() == false
                    && tabla.getClase(arg.getNombre()) != null
                    && tabla.getClase(arg.getNombre()).getSimboloParametroGenerico() != null)
                error(token, "el argumento generico debe ser un tipo de referencia no generico o un parametro");
            validarArgumentosGenericos(arg, token);
        }
    }
    private void error(Token t,String msg) { throw new ExcepcionSemantica(t,msg); }
}
