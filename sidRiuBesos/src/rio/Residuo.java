package rio;

public class Residuo {
	public float concentracion;
	public String name;

	public Residuo(String name, float conc){
		this.concentracion = conc;
		this.name = name;
	}
	
	public String toString(){
		return "Residuo " + this.name + " with "+this.concentracion+ " concentracion";
	}
}
