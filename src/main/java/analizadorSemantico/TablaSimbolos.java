package analizadorSemantico;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, EntradaClase> clases;

    public TablaSimbolos() {
        clases = new LinkedHashMap<>();
    }

    public void agregarClase(EntradaClase clase) {
        clases.put(clase.getNombre(), clase);
    }

    public EntradaClase getClase(String nombre) {
        return clases.get(nombre);
    }

    public Map<String, EntradaClase> getClases() {
        return Collections.unmodifiableMap(clases);
    }

    public void setClases(Map<String, EntradaClase> clases) {
        this.clases = new LinkedHashMap<>(clases);
    }
}