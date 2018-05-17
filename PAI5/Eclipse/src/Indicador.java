import java.util.List;

public class Indicador {
	public Integer total;
	public void setTotal(Integer total) {
		this.total = total;
	}
	public void setCorrectas(Integer correctas) {
		this.correctas = correctas;
	}
	public void setMeses(List<Mes> meses) {
		this.meses = meses;
	}
	public Integer correctas;
	public List<Mes> meses;
}
