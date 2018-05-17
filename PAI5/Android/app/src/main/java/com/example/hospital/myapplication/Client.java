package com.example.hospital.myapplication;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String respuesta;
    String message = "";
    String firmaMensaje;
    TextView resultado;

    Client(String addr, int port, String message, String firmaMensaje, TextView resultado) {
        dstAddress = addr;
        dstPort = port;

        this.message = message;
        this.firmaMensaje = firmaMensaje;
        this.resultado = resultado;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        Socket socket = null;
        DataOutputStream os = null;
        BufferedReader is = null;

        try {
            //Crear socket
            socket = new Socket(dstAddress, dstPort);

            //Crear canales de entrada y salida de datos
            os = new DataOutputStream(socket.getOutputStream());
            //El mensaje se manda en dos pasos: primero el mensaje y luego la firma
            os.writeBytes( message + "\n");
            os.writeBytes(firmaMensaje + "\n");
            //Inidicar al servidor que ya acabamos de enviar los datos
            os.writeBytes("&END\n");

            //Obtener la respuesta del servidor
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            respuesta = is.readLine();
            is.close();
            os.close();
            //Cerrar socket
            socket.close();
        } catch (Exception e) {
            respuesta = "Petición incorrecta";
        } finally {

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Imprimir respuesta del servidor por pantalla
        resultado.setText(respuesta);
        super.onPostExecute(result);
    }
}
