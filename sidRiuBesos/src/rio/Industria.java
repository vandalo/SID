package rio;

import java.util.Vector;

public class Industria {
	public Vector<Residuo> residuos;

	@SuppressWarnings("unchecked")
	public Industria(Vector<Residuo> residuos){
		this.residuos = (Vector<Residuo>) residuos.clone();
	}
	
	public String toString(){
		return "Industry with "+ residuos;
	}
}
