package rio;

public class Depuradora {
	public float eficiencia;
	public int tiempoVida;
	public int posicion;

	public Depuradora(float ef, int t, int posicion){
		this.eficiencia = ef;
		this.tiempoVida = t;
		this.posicion = posicion;
	}
	
	public String toString(){
		return "Depuradora con eficiencia "+this.eficiencia*100+"% y : "+this.tiempoVida+" horas de vida en la posicion: " + this.posicion;
	}
}
