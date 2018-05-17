import java.util.Map;

public class Pedido {
	String usuario;
	Map<String, Integer> productos;
	
	public Pedido (String usuario, Map<String, Integer> productos){
		this.usuario = usuario;
		this.productos = productos;
	}
}
