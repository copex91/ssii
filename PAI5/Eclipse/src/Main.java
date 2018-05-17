import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.security.KeyPairGenerator;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {
	public static void main(String[] args) {

		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());

			if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
				String mes = String.valueOf(cal.get(Calendar.MONTH)+1);
				if (cal.get(Calendar.MONTH) < 10) {
					mes = "0" + mes;
				}
				//calcula mes+año
				mes = mes + String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4);;
				
				//Lee indicador.txt
				Reader reader = new FileReader(new File("src/indicador.txt"));
				Gson gson = new Gson();
				Indicador indicador = gson.fromJson(reader, Indicador.class);
				//calcula el porcentaje
				Float porcentaje = (float) 0;
				if (indicador.total != 0) {
					porcentaje = (float) indicador.correctas / indicador.total * 100;
				}

				//calcula la tendencia
				String tendencia = "0"; // Tendencia nula

				if (indicador.meses.size() >= 2) {
					Mes primero = indicador.meses.get(indicador.meses.size() - 1); //Mes anterior
					Mes segundo = indicador.meses.get(indicador.meses.size() - 2); //Mes anterior del anterior

					if (primero.porcentaje <= porcentaje && segundo.porcentaje <= porcentaje) {
						tendencia = "+";
					} else if (primero.porcentaje > porcentaje && segundo.porcentaje > porcentaje) {
						tendencia = "-";
					}
				}
				
				//Se crea un nuevo mes y se resetea el total y las correctas
				
				Mes m = new Mes(mes, porcentaje, tendencia);
				indicador.meses.add(m);
				indicador.setCorrectas(0);
				indicador.setTotal(0);

				//Se vuelve a escribir en el indicador.txt
				Writer writer = new FileWriter("src/indicador.txt");
				Gson w_gson = new GsonBuilder().setPrettyPrinting().create();
				w_gson.toJson(indicador, writer);
				writer.flush();
				writer.close();

			}
			 AndroidServer server = new AndroidServer();
			 server.runServer();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
