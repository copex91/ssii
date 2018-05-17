import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ServerSocketFactory;

import com.google.gson.Gson;

public class AndroidServer {
	private ServerSocket serverSocket;

	// Constructor del Servidor
	public AndroidServer() throws Exception {
		// ServerSocketFactory para construir los ServerSockets
		ServerSocketFactory socketFactory = (ServerSocketFactory) ServerSocketFactory.getDefault();
		// Creaci�n de un objeto ServerSocket escuchando peticiones en el puerto
		// 8000
		serverSocket = (ServerSocket) socketFactory.createServerSocket(8000);
	}

	// Ejecuci�n del servidor para escuchar peticiones de los clientes
	void runServer() {
		while (true) {
			try {
				System.out.println("Esperando conexiones de clientes...");
				Socket socket = (Socket) serverSocket.accept();
				System.out.println("Conexi�n entrante...");
				Reader reader = new FileReader(new File(".\\src\\db.txt"));
				Gson gson = new Gson();
				DB database = gson.fromJson(reader, DB.class);

				// Abre un BufferedReader para leer los datos del cliente
				BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// Abre un PrintWriter para enviar datos al cliente
				DataOutputStream os = new DataOutputStream(socket.getOutputStream());
				// Se lee del cliente el mensaje y el macdelMensajeEnviado
				// Mensaje: IDobjeto:N�mero;IDobjeto:N�mero...&Usuario&Firma
				String mensaje = is.readLine();
				String linea;
				String firmaMensaje = "";
				while ((linea = is.readLine()) != null) {
					firmaMensaje = firmaMensaje + linea;
				}
				System.out.println("Mensaje recibido: " + mensaje);
				System.out.println("Firma recibida: " + firmaMensaje);

				String[] parts = mensaje.split("&");
				String message = parts[0];
				String usuario = parts[1];
				
				Map<String, String> usuarios = database.getUsuarios();
				String pubKey = "";
				
				//Comprueba si el usuario existe en la DB
				if (usuarios.containsKey(usuario)) {
					pubKey = usuarios.get(usuario);

					// Verificaci�n de la firma
					Signature sg = Signature.getInstance("SHA256WITHRSA");

					// String pubKey =
					// "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5SLfGfUzCxuK4OyaCoaLgTe5XdW+ns16gwEYB/4oUXb+DvuP/D+mrKno3MiJKcDLcwdj0oLvYfPyJEvTzYftwA//Kfs3ED+AIe/+Hy6+RqfeLp7hWvEiepZili4RTg4D5E/b0YoLVtGGE9Hh/v2ieir5aOKVbYGKN4xLtPyFB2TdPNCs7pN1Sh1O48V8CrWKxNg/9zGeGdwt4eXvR4hXLB2BJJK+chPWhcSKlp6QPMcvo4CMvgH+UKuzrFcMjB0LwJ3h9xEtiaKkm3NL7Si1Pp29iIsV6UPo1Ncy5+oCP2wQ9VMFsbSfufj5lMLLj8H4M+LYnurECuSoBFgbCS18KwIDAQAB";
					PublicKey pub_recovered = loadPublicKey(pubKey);
					sg.initVerify(pub_recovered);
					sg.update((message + "&" + usuario).getBytes());
					if (sg.verify(base64Decode(firmaMensaje))) {
						
						//Array con todos los pedidos
						String[] pedidos = message.split(";");
						//Por cada pedido, se añade uno a la base de datos
						for (int i=0; pedidos.length > i; i++){
							
							//Se obtiene el producto y cantidad de éste en parts
							parts = pedidos[i].split(":");
							Map<String, Integer> pedido =  new HashMap<String, Integer>();
							pedido.put(parts[0], Integer.parseInt(parts[1]));
							
							Pedido p = new Pedido (usuario,pedido);
							database.setPedido(p);
						}
						
						System.out.println("Pedido a�adido");
						os.writeChars("Petición OK");
					} else {
						System.out.println("Error de firma");
						os.writeChars("Petición incorrecta");
					}
				} else {
					System.out.println("Usuario no encontrado");
					os.writeChars("Petición incorrecta");
				}
				is.close();
				os.close();
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		PKCS8EncodedKeySpec spec = fact.getKeySpec(priv, PKCS8EncodedKeySpec.class);
		byte[] packed = spec.getEncoded();
		String key64 = base64Encode(packed);
		Arrays.fill(packed, (byte) 0);
		return key64;
	}

	public static String savePublicKey(PublicKey publ) throws GeneralSecurityException {
		KeyFactory fact = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec spec = fact.getKeySpec(publ, X509EncodedKeySpec.class);
		return base64Encode(spec.getEncoded());
	}

	public static byte[] base64Decode(String key64) {
		return Base64.getDecoder().decode(key64);
	}

	public static String base64Encode(byte[] packed) {
		return new String(Base64.getEncoder().encode(packed));
	}
}
