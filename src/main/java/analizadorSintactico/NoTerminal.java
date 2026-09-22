package analizadorSintactico;

public interface NoTerminal {
    void parse(ContextoSintactico c) throws ExcepcionSintactica;
}
