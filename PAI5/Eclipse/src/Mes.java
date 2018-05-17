
public class Mes {
	public String mes;

	public void setMes(String mes) {
		this.mes = mes;
	}

	public void setPorcentaje(Float porcentaje) {
		this.porcentaje = porcentaje;
	}

	public void setTendencia(String tendencia) {
		this.tendencia = tendencia;
	}

	public String getMes() {
		return mes;
	}

	public Mes(String mes, Float porcentaje, String tendencia) {
		super();
		this.mes = mes;
		this.porcentaje = porcentaje;
		this.tendencia = tendencia;
	}

	public Float getPorcentaje() {
		return porcentaje;
	}

	public String getTendencia() {
		return tendencia;
	}

	public Float porcentaje;
	public String tendencia;
}
