import java.security.KeyPairGenerator;

public class Main {
	public static void main(String[] args) {
		KeyPairGenerator kpg;
		
		try {

			AndroidServer server = new AndroidServer();
			server.runServer();
			
//			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
//		    KeyPair pair = gen.generateKeyPair();
//
//		    String pubKey = savePublicKey(pair.getPublic());
//		    PrintWriter writer1 = new PrintWriter("pubKey.txt", "UTF-8");
//		    writer1.print(pubKey);
//		    writer1.close();
//		    PublicKey pubSaved = loadPublicKey(pubKey);
//		    System.out.println(pair.getPublic()+"\n"+pubKey);
//
//		    String privKey = savePrivateKey(pair.getPrivate());
//		    PrintWriter writer2 = new PrintWriter("privKey.txt", "UTF-8");
//		    writer2.print(privKey);
//		    writer2.close();
//		    PrivateKey privSaved = loadPrivateKey(privKey);
//		    System.out.println(pair.getPrivate()+"\n"+privKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
