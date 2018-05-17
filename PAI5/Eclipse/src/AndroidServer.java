import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
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
import com.google.gson.GsonBuilder;

public class AndroidServer {
	private ServerSocket serverSocket;

	// Constructor del Servidor
	public AndroidServer() throws Exception {
		// ServerSocketFactory para construir los ServerSockets
		ServerSocketFactory socketFactory = (ServerSocketFactory) ServerSocketFactory.getDefault();
		// Creación de un objeto ServerSocket escuchando peticiones en el puerto
		// 8000
		serverSocket = (ServerSocket) socketFactory.createServerSocket(8000);
	}

	// Ejecución del servidor para escuchar peticiones de los clientes
	void runServer() {
		while (true) {
			BufferedReader is;
			DataOutputStream os;
			Socket socket;
			try {
				System.out.println("Esperando conexiones de clientes...");
				socket = (Socket) serverSocket.accept();
				System.out.println("Conexión entrante...");
				Reader reader = new FileReader(new File(".\\src\\db.txt"));
				Gson gson = new Gson();
				DB database = gson.fromJson(reader, DB.class);
				
				// Abre un BufferedReader para leer los datos del cliente
				is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// Abre un PrintWriter para enviar datos al cliente
				os = new DataOutputStream(socket.getOutputStream());
				// Se lee del cliente el mensaje y el macdelMensajeEnviado
				// Mensaje: IDobjeto:Número;IDobjeto:Número...&Usuario&Firma
				String mensaje = is.readLine();
				String linea;
				String firmaMensaje = "";
				while ((linea = is.readLine()) != null) {
					if (!linea.equals("&END")) {
						firmaMensaje = firmaMensaje + linea;
					}else{
						//Ya ha finalizado el mensaje
						break;
					}
				}
				System.out.println("Mensaje recibido: " + mensaje);
				System.out.println("Firma recibida: " + firmaMensaje);

				String[] parts = mensaje.split("&");
				String message = parts[0];
				String usuario = parts[1];
				
				Map<String, String> usuarios = database.usuarios;
				String pubKey = "";
				
				//Comprueba si el usuario existe en la DB
				if (usuarios.containsKey(usuario)) {
					pubKey = usuarios.get(usuario);

					// Verificación de la firma
					Signature sg = Signature.getInstance("SHA256WITHRSA");

					// String pubKey =
					// "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5SLfGfUzCxuK4OyaCoaLgTe5XdW+ns16gwEYB/4oUXb+DvuP/D+mrKno3MiJKcDLcwdj0oLvYfPyJEvTzYftwA//Kfs3ED+AIe/+Hy6+RqfeLp7hWvEiepZili4RTg4D5E/b0YoLVtGGE9Hh/v2ieir5aOKVbYGKN4xLtPyFB2TdPNCs7pN1Sh1O48V8CrWKxNg/9zGeGdwt4eXvR4hXLB2BJJK+chPWhcSKlp6QPMcvo4CMvgH+UKuzrFcMjB0LwJ3h9xEtiaKkm3NL7Si1Pp29iIsV6UPo1Ncy5+oCP2wQ9VMFsbSfufj5lMLLj8H4M+LYnurECuSoBFgbCS18KwIDAQAB";
					PublicKey pub_recovered = loadPublicKey(pubKey);
					sg.initVerify(pub_recovered);
					sg.update((message + "&" + usuario).getBytes());
					if (sg.verify(base64Decode(firmaMensaje))) {
						
						//Array con todos los pedidos
						String[] productos = message.split(";");
						Map<String, Integer> pedido =  new HashMap<String, Integer>();
						//Por cada pedido, se añade uno a la base de datos
						for (int i=0; productos.length > i; i++){
							//Se obtiene el producto y cantidad de éste en parts
							parts = productos[i].split(":");
							pedido.put(parts[0], Integer.parseInt(parts[1]));
						}
						
						Pedido p = new Pedido(usuario, pedido);
						database.pedidos.add(p);
//						database.setPedido(p);
						Writer writer = new FileWriter(".\\src\\db.txt");
						Gson w_gson = new GsonBuilder().setPrettyPrinting().create();
					    w_gson.toJson(database, writer);
					    writer.flush();
				        writer.close();
						
						System.out.println("Pedido añadido");
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
