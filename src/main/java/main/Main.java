package main;

import java.io.IOException;

import analizadorLexico.AnalizadorLexico;
import analizadorLexico.Token;
import sourcemanager.SourceManager;
import sourcemanager.SourceManagerImpl;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("[Error:archivo|0]");
            return;
        }

        SourceManager sourceManager = new SourceManagerImpl();

        try {
            sourceManager.open(args[0]);
            AnalizadorLexico analizadorLexico = new AnalizadorLexico(sourceManager);
            Token token;

            do {
                token = analizadorLexico.proximoToken();
                System.out.println(token);
            } while (!token.isEOF());

            if (analizadorLexico.tieneErrores()) {
                analizadorLexico.imprimirErrores();
            } else {
                System.out.println("[SinErrores]");
            }
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
