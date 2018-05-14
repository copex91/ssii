package com.example.hospital.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String message = "";
    String response;
    String firmaMensaje;

    Client(String addr, int port, String message, String firmaMensaje) {
        dstAddress = addr;
        dstPort = port;
        this.message = message;
        this.firmaMensaje = firmaMensaje;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        Socket socket = null;
        DataOutputStream os = null;
        BufferedReader is = null;

        try {
            socket = new Socket(dstAddress, dstPort);

            os = new DataOutputStream(socket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    os.writeBytes( message + "\n");
                    os.writeBytes(firmaMensaje + "\n");

//            String responseLine = is.readLine();
                    os.close();
                    is.close();
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
//        Toast.makeText(, response, Toast.LENGTH_SHORT).show();
        super.onPostExecute(result);
    }
}
