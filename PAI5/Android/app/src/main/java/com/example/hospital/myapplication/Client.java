package com.example.hospital.myapplication;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

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
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (Exception e) {
            resultado.setText("Petición incorrecta");
        } finally {
            if (socket != null) {
                String response = "";
                try {
                    //El mensaje se manda en dos pasos: primero el mensaje y luego la firma
                    os.writeBytes( message + "\n");
                    os.writeBytes(firmaMensaje + "\n");
                    os.close();
                    //Obtener la respuesta del servidor
                    respuesta = is.readLine();

                    //Cerrar canales
                    os.close();
                    is.close();
                    socket.close();
                } catch (IOException e) {
                    resultado.setText("Petición incorrecta");
                }
            }else{
                resultado.setText("Petición incorrecta");
            }
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
