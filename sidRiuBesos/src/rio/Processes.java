package rio;

import java.util.List;

public class Processes {
	private static float k = 15;//random del archivo entre 1-50 TODO
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
		return new Watermass(w1.volume+w2.volume, (w1.dbo*w1.volume+w2.dbo*w2.volume)/w1.volume+w2.volume);
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
		//TODO HACER LA MEZCLA DE AGUAS
		return new Watermass(0,0);
	}
	
	//LLAMADAS CONSTANTES A LIMPIAR 1H PARA HACER BIEN EL CALCULO
	//Y NO TRATAR EN LA ECUACION, AGUAS QUE ESTAN LIMPIAS (PORQUE
	//PRESUPONEMOS QUE YA SE HABRAN VERTIDO AL RIO)
	public static List<Watermass> CalcularDBOLimpiado(List<Watermass> aguas, int horasDeVida, int horasProceso){
		while (horasProceso > 0){
			CalcularLimpiadoHora(aguas, horasDeVida);
			horasDeVida += 1;
			horasProceso -= 1;
		}
		return aguas;
	}
	
	//LIMPIAR 1H EN LA DEPURADORA
	private static List<Watermass> CalcularLimpiadoHora(List<Watermass> aguas, int horaActual){
		float limpiado = 0, totalVolumen = 0;
		for (Watermass wm : aguas){
			if (wm.dbo > verterAguaCalidad) totalVolumen += wm.volume;
		}
		limpiado = (k/totalVolumen) + 0.01f; //TODO CAMBIAR K POR VALOR DEL FICHERO CON HORA ACTUAL
		for (Watermass wm : aguas){
			if (wm.dbo > verterAguaCalidad && (limpiado < wm.dbo)) wm.dbo -= limpiado;
			else if (wm.dbo > verterAguaCalidad) wm.dbo = verterAguaCalidad;
		}
		return aguas;
	}
}









