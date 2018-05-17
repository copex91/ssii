package com.example.hospital.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    // Dirección del servidor
    protected static String server = "10.0.2.2";
    protected static int port = 8000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText usuario = (EditText) findViewById(R.id.et_usuario);
        final EditText sabanas = (EditText) findViewById(R.id.et_sab);
        final EditText toallas = (EditText) findViewById(R.id.et_toa);
        final EditText jabones = (EditText) findViewById(R.id.et_jab);
        final TextView resultado = (TextView) findViewById(R.id.tv_res);

        // Capturamos el boton de Enviar
        View button = findViewById(R.id.button_send);

       // Llama al listener del boton Enviar
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String usuario_str = usuario.getText().toString();
                final String sabanas_str = sabanas.getText().toString();
                final String toallas_str = toallas.getText().toString();
                final String jabones_str = jabones.getText().toString();
                //Comprobación de nombre de usuario relleno
                if (usuario.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Indique su nombre de usuario", Toast.LENGTH_SHORT).show();

                    // Se debe de rellenar al menos un artículo
                }else if (sabanas_str.equals("") && toallas_str.equals("") && jabones_str.equals("")) {
                    // Mostramos un mensaje emergente;
                    Toast.makeText(getApplicationContext(), "Rellene al menos un elemento", Toast.LENGTH_SHORT).show();

                    // Para los artículos rellenos, sólo se puede pedir una cantidad de 1 a 300
                }else if (!sabanas_str.equals("") && (getInt(sabanas_str)>300 || getInt(sabanas_str)<1) ||
                        !toallas_str.equals("") && (getInt(toallas_str)>300 || getInt(toallas_str)<1) ||
                        !jabones_str.equals("") && (getInt(jabones_str)>300 || getInt(jabones_str)<1)) {
                    // Mostramos un mensaje emergente;
                    Toast.makeText(getApplicationContext(), "La cantidad debe estar entre 1 y 300", Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Enviar")
                            .setMessage("Se va a proceder al envio")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    resultado.setText("");
                                    String pvk = "";
                                    // Extraer los datos de la vista y formar el mensaje a enviar
                                    String mensaje = "";

                                    // Cantidad de sábanas
                                    if(sabanas_str.equals("")) {
                                        mensaje = mensaje + "sabanas:0;";
                                    }else {
                                        mensaje = mensaje + "sabanas:" + sabanas_str + ";";
                                    }

                                    // Cantidad de toallas
                                    if(toallas_str.equals("")) {
                                        mensaje = mensaje + "toallas:0;";
                                    }else {
                                        mensaje = mensaje + "toallas:" + toallas_str + ";";
                                    }

                                    // Cantidad de jabones
                                    if(jabones_str.equals("")) {
                                        mensaje = mensaje + "jabon:0";
                                    }else {
                                        mensaje = mensaje + "jabon:" + jabones_str;
                                    }

                                    // Usuario que realiza la petición
                                    mensaje = mensaje + "&" + usuario_str;

                                    // Obtener clave privada de la DB
                                    try {
                                        InputStream is = getResources().openRawResource(R.raw.db);
                                        String db = readTextFile(is);
                                        JSONObject reader = new JSONObject(db);
                                        pvk = reader.getJSONObject("usuarios").getJSONObject(usuario_str).getString("priv");
                                    }catch (Exception e){
                                        resultado.setText("Petición incorrecta");
                                    }
                                    if (!pvk.equals("")){
                                        // Firmar y lanzar el pedido al servidor
                                        try {
                                            //Firmar
                                            Signature sg = Signature.getInstance("SHA256WITHRSA");
                                            PrivateKey prv_recovered = loadPrivateKey(pvk);
                                            sg.initSign(prv_recovered);
                                            sg.update(mensaje.getBytes());
                                            byte[] firma = sg.sign();
                                            String firmaMensaje = base64Encode(firma);

                                            //Lanzar pedido al servidor de forma asíncrona
                                            Client myClient = new Client(server, port, mensaje, firmaMensaje, resultado);
                                            myClient.execute();
                                        } catch (Exception e) {
                                            resultado.setText("Petición incorrecta");
                                        }
//                                        finally {
//                                            Toast.makeText(MainActivity.this, "Petición enviada correctamente", Toast.LENGTH_SHORT).show();
//                                        }
                                    }
                                }
                            }).setNegativeButton(android.R.string.no, null).show();
                }
            }
        });
    }

    // Creación de un cuadro de dialogo para confirmar pedido
    private void showDialog() throws Resources.NotFoundException {

        }

    public static PrivateKey loadPrivateKey(String key64) throws GeneralSecurityException {
        byte[] clear = base64Decode(key64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }


    public static PublicKey loadPublicKey(String stored) throws GeneralSecurityException {
        byte[] data = base64Decode(stored);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePublic(spec);
    }

    public static String savePrivateKey(PrivateKey priv) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec spec = fact.getKeySpec(priv,
                PKCS8EncodedKeySpec.class);
        byte[] packed = spec.getEncoded();
        String key64 = base64Encode(packed);

        Arrays.fill(packed, (byte) 0);
        return key64;
    }

    public static String savePublicKey(PublicKey publ) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = fact.getKeySpec(publ,
                X509EncodedKeySpec.class);
        return base64Encode(spec.getEncoded());
    }

    public static byte[] base64Decode(String key64){
        return Base64.decode(key64, Base64.DEFAULT);
    }

    public static String base64Encode(byte[] packed){
        return Base64.encodeToString(packed, Base64.DEFAULT);
    }

    public String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    public int getInt(String s){
        if (s.equals("")){
            return 0;
        }else{
            return Integer.parseInt(s.replaceAll("[\\D]", ""));
        }
    }
}
