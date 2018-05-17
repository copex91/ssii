import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class GenerateKeyPair {
	public static void main(String[] args) {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			KeyPair pair = gen.generateKeyPair();

			String pubKey = savePublicKey(pair.getPublic());
			PrintWriter writer1 = new PrintWriter("pubKey.txt", "UTF-8");
			writer1.print(pubKey);
			writer1.close();
			PublicKey pubSaved = loadPublicKey(pubKey);
			System.out.println("Clave pública: "+ pubKey);

			String privKey = savePrivateKey(pair.getPrivate());
			PrintWriter writer2 = new PrintWriter("privKey.txt", "UTF-8");
			writer2.print(privKey);
			writer2.close();
			PrivateKey privSaved = loadPrivateKey(privKey);
			System.out.println("Clave privada: " + privKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
