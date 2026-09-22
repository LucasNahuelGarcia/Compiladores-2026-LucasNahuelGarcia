package main;

import java.io.IOException;

import analizadorLexico.AnalizadorLexico;
import analizadorLexico.ExcepcionLexica;
import analizadorSintactico.Inicial;
import analizadorSintactico.ExcepcionSintactica;
import sourcemanager.SourceManager;
import sourcemanager.SourceManagerImpl;

public class MainSint {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("[Error:archivo|0]");
            return;
        }

        SourceManager sourceManager = new SourceManagerImpl();

        try {
            sourceManager.open(args[0]);
            AnalizadorLexico analizadorLexico = new AnalizadorLexico(sourceManager);
            new Inicial().parse(analizadorLexico);
            System.out.println("Compilacion Exitosa");
            System.out.println("[SinErrores]");
        } catch (ExcepcionLexica | ExcepcionSintactica exception) {
            System.out.println(exception.getMessage());
        } catch (IOException exception) {
            System.out.println("[Error:archivo|0]");
        } finally {
            try {
                sourceManager.close();
            } catch (IOException exception) {
            }
        }
    }

}
