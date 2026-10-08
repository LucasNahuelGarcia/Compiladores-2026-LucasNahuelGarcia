package main;

import java.io.IOException;

import analizadorLexico.AnalizadorLexico;
import analizadorLexico.ExcepcionLexica;
import analizadorSemantico.ExcepcionSemantica;
import analizadorSemantico.TablaSimbolos;
import analizadorSintactico.ExcepcionSintactica;
import analizadorSintactico.Inicial;
import sourcemanager.SourceManager;
import sourcemanager.SourceManagerImpl;

public final class MainSemantico {
	private MainSemantico() {
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("[Error:archivo|0]");
			return;
		}

		SourceManager sourceManager = new SourceManagerImpl();

		try {
			sourceManager.open(args[0]);
			AnalizadorLexico analizadorLexico = new AnalizadorLexico(sourceManager);
			// El parser construye la TS; estas llamadas son las tres etapas semanticas.
			TablaSimbolos tablaSimbolos = new Inicial().parse(analizadorLexico);
			tablaSimbolos.estaBienDeclarado();
			tablaSimbolos.consolidar();
			// Esta linea es parte del contrato de salida y no debe modificarse.
			System.out.println("[SinErrores]");
		} catch (ExcepcionSemantica exception) {
			System.out.println(exception.getMessage());
			// Debe ser exactamente la ultima linea del reporte semantico.
			System.out.println("[Error:" + exception.getToken().getLexema()
					+ "|" + exception.getToken().getNroLinea() + "]");
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
