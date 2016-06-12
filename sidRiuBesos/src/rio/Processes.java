package rio;

import java.util.List;

import org.apache.jena.atlas.lib.Pair;

public class Processes {
	private static float k = 15;//random del archivo entre 1-50 TODO
	public static float maxK = 50; //el caso para el 100% de eficiencia
	public static float verterAguaCalidad = 0.1f; //CON QUE DBO VERTEMOS AGUA
	
	
	public static float addVolume(float v1, float v2){
		return v1+v2;
	}
	
	public static float getAmount (float c, float v){
		return c*v;
	}
	
	public static float mergeAmounts (float c1, float v1, float c2, float v2){
		return getAmount(c1,v1)+getAmount(c2,v2);
	}
	
	public static float mergeConcentration(float c1, float v1, float c2, float v2){
		return mergeAmounts(c1,v1,c2,v2)/addVolume(v1,v2);
	}

	public static Watermass mergeWater(Watermass w1, Watermass w2){
		float v3 = addVolume(w1.volume, w2.volume);
		float dbo3 = mergeConcentration(w1.volume, w1.dbo, w2.volume, w2.dbo);
		return new Watermass(v3, dbo3);
	}
	
	public static Watermass efficientMergeWater(Watermass w1, Watermass w2){
		return new Watermass(w1.volume+w2.volume, (w1.dbo*w1.volume+w2.dbo*w2.volume)/(w1.volume+w2.volume));
	}
	
	public static Watermass Ejecutar_PI(Industria ind, Watermass w2){
		float dboTotal = 0;;
		for (Residuo res : ind.residuos){
			dboTotal += res.concentracion*2;
		}
		return new Watermass(w2.volume, dboTotal);
	}
	
	//actualizar el agua de la depuradora con la masa de agua que devolvemos, la cual es la mezcla de las aguas
	public static Watermass Verter_Aguas(Industria ind[], Watermass w2[]){
		float volumenTotal = 0, DBO = 0;
		for (Watermass wm : w2){
			volumenTotal += wm.volume;
			DBO += (wm.dbo * wm.volume);
		}
		DBO = DBO/volumenTotal;
		return new Watermass(volumenTotal,DBO);
	}
	
	//LLAMADAS CONSTANTES A LIMPIAR 1H PARA HACER BIEN EL CALCULO
	//Y NO TRATAR EN LA ECUACION, AGUAS QUE ESTAN LIMPIAS (PORQUE
	//PRESUPONEMOS QUE YA SE HABRAN VERTIDO AL RIO)
	public static Pair<Float, List<Watermass>> CalcularDBOLimpiado(List<Watermass> aguas, int horasDeVida, int horasProceso){
		Pair<Float, List<Watermass>> p;
		float eficienciaSumada = 0;
		float aux = (float) horasProceso;
		float efResult;
		while (horasProceso > 0){
			efResult = CalcularLimpiadoHora(aguas, horasDeVida);
			eficienciaSumada += efResult;
			horasProceso -= 1;
			if (efResult > 0) horasDeVida += 1;
			else aux -= 1;
		}
		p = new Pair<Float, List<Watermass>>(eficienciaSumada/aux, aguas);
		return p;
	}
	
	//LIMPIAR 1H EN LA DEPURADORA
	private static float CalcularLimpiadoHora(List<Watermass> aguas, int horaActual){
		float limpiado = 0, totalVolumen = 0;
		for (Watermass wm : aguas){
			if (wm.dbo > verterAguaCalidad) totalVolumen += wm.volume;
		}
		limpiado = (k/totalVolumen) + 0.01f; //TODO CAMBIAR K POR VALOR DEL FICHERO CON HORA ACTUAL
		for (Watermass wm : aguas){
			if (wm.dbo > verterAguaCalidad && (limpiado < wm.dbo)) wm.dbo -= limpiado;
			else if (wm.dbo > verterAguaCalidad) wm.dbo = verterAguaCalidad;
		}
		if (totalVolumen > 0 ) return ((limpiado - 0.01f)/(maxK/totalVolumen));
		else return 0;
	}
}









