package com.example.hospital.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    // Setup Server information
    protected static String server = "10.0.2.2";
    protected static int port = 8000;
    private PKCS8EncodedKeySpec keySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Capturamos el boton de Enviar
        View button = findViewById(R.id.button_send);

       // Llama al listener del boton Enviar
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    showDialog();
            }
        });
    }

    // Creación de un cuadro de dialogo para confirmar pedido
    private void showDialog() throws Resources.NotFoundException {
            final EditText usuario = (EditText) findViewById(R.id.et_usuario);
            final EditText sabanas = (EditText) findViewById(R.id.et_sab);
            final String usuario_str = usuario.getText().toString();
            final String sabanas_str = sabanas.getText().toString();

            if (usuario.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Indique su nombre de usuario", Toast.LENGTH_SHORT).show();
            }else if (sabanas_str.equals("")) {
                // Mostramos un mensaje emergente;
                Toast.makeText(getApplicationContext(), "Selecciona al menos un elemento", Toast.LENGTH_SHORT).show();
            } else {
            new AlertDialog.Builder(this)
                    .setTitle("Enviar")
                    .setMessage("Se va a proceder al envio")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                // Catch ok button and send information
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    // 1. Extraer los datos de la vista
                                    String mensaje;
                                    mensaje = "1:" + sabanas_str + "&" + usuario_str;

                                    Signature sg = null;
                                    KeyFactory kf = null;
                                    try {
                                        sg = Signature.getInstance("SHA256WITHRSA");
                                        String pvk = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDlIt8Z9TMLG4rg7JoKhouBN7ld1b6ezXqDARgH/ihRdv4O+4/8P6asqejcyIkpwMtzB2PSgu9h8/IkS9PNh+3AD/8p+zcQP4Ah7/4fLr5Gp94unuFa8SJ6lmKWLhFODgPkT9vRigtW0YYT0eH+/aJ6Kvlo4pVtgYo3jEu0/IUHZN080Kzuk3VKHU7jxXwKtYrE2D/3MZ4Z3C3h5e9HiFcsHYEkkr5yE9aFxIqWnpA8xy+jgIy+Af5Qq7OsVwyMHQvAneH3ES2JoqSbc0vtKLU+nb2IixXpQ+jU1zLn6gI/bBD1UwWxtJ+5+PmUwsuPwfgz4tie6sQK5KgEWBsJLXwrAgMBAAECggEAcjv1YDqXTQVZMpOipHa5XB2M45QpaYBlgKRt96YlMaASPyP5f7e3/8Lhnhi4EUHV7C4V/SBb+cilwqSvHnuS8zrGaoacyBPwbHr6hU9He3A7W6DIFw+6scUBt3+WDwT7ubp6i7e3uXvRzVXIxthqRV/hYgH8n1CCuPjP0ZZHOAyGbS2pX2x1j8UkrwIM7KLesRH0N1WTLCnwOcDNHqYxZkeIgrRwCn3fEIFqD/v/7O0Xmzi0FWPotsGNwdP/rj20xu54yg36yJyrsEaJkVIGlheNSVfi72kvAKS3NRoW5b7VfWt8XMtNnGUsuCTAhiSZrBZE+cTkXE+rcdE4exN50QKBgQD8vtZ6B0MbsO/1LQjfZj6ChyEC/pgd1raR8UrGlQcGWGJpjP+5LDazAJf/RaHLmxpzZfhWU5tCVe/sS43U2lLiC2uLXvsv8PuxCbGTU8FSFy2iZo4RU90gqWZ47bt7YKNayymvPg1oe3IH7lLzgXg1mKChmJ/ikc3/fi30p5h0MwKBgQDoFjUBcm/xEyR2NZVJl3PJLQeQA6ggGk0Scy4D8lQPJAjTtLbATNB5Ad4h/AOT3aEynMJE5PW14uGKYL+tmNDaozyCs8T/zxx2Ls3T5fMC99B76AWmbu39sYLLr7Clcc5d5CNqffeKDJZO0vPwwtWCsnOLguukh3NBvOdyR1GgKQKBgQCZtbIYeqwsfhohQKdBvhvMJERGXvHCS9+yuE1ioiWojT4ktTScuC/4Aydtfzqb6hNXFS/HyIcG+96zFWwHhFOd15YrJ7OZ/3QCwkN1tx0+QIxnVPmXvioggAWrC+HWcfpG8IHEaveakGDUQ/O81gN2jQE75edu0n+2n1Vxki+ckwKBgBjd+EP1bQUZlfiMeThvX9qYVo0ZtzPpXYSyjqWhm1wb8k4suMAV+uhcSN6/T+rR/mmb3jzfg2w/qQbYovEIxKgIgX1Hob3/BP+suCUSKF2TC+Wa0LAqhpl+IZONeZHghcoNnbXVVWaXPquncrfDSHk+gZ7bIkB4uuK6SNo3xgkxAoGAcFqAC2BJMfimKykJGuYZyzwZy+qgF/Pdp1JmHFEAIjlfh3fAdEdAR18rYqhAKdRqXWBOZsO4FKdv5Wd9YT/F+b5AF8a+SBmpHqH0wD2ieH3LIjFoGFeSUCIUI9BNumOjcZUkKMzUzZi5elbTGUbU7cuOCjfhq7BUW/GYnaz/X6I=";
                                        PrivateKey prv_recovered = loadPrivateKey(pvk);
                                        sg.initSign(prv_recovered);
                                        sg.update(mensaje.getBytes());
                                        byte[] firma = sg.sign();
                                        Log.e("Firma", firma.toString());
                                        String firmaMensaje = base64Encode(firma);
                                        Log.e("Firma", firmaMensaje);
//                                        mensaje = mensaje + "&" + firmaMensaje;
                                        Client myClient = new Client(server, port, mensaje, firmaMensaje);
                                        myClient.execute();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        Toast.makeText(MainActivity.this, "Petición enviada correctamente", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                    )
                    .setNegativeButton(android.R.string.no, null).show();
            }
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
}
