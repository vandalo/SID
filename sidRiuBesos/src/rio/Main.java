package rio;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.ontology.Individual;
public class Main {
    public static void main(String[] args) throws FileNotFoundException{
        String JENA = "./";
        String File = "resources/ontoprac.owl";
        String NamingContext = "http://www.semanticweb.org/luisoliva/ontologies/2016/4/ontoprac#";
        Scanner scan = new Scanner(System.in);
        
        System.out.println("----------------Starting program -------------");

        ComunicacionOnto comunicator = new ComunicacionOnto(JENA,File,NamingContext);

        comunicator.loadOntology();        

        System.out.println("Choose option: ");
        System.out.println("1 = merge water");
        System.out.println("2 = proceso industrial");
        System.out.println("3 = Verter Aguas");
        System.out.println("4 = Depurar Aguas De la Depuradora");
        System.out.println("5 = Calcular tiempo necesario para depurar una masa de agua en una depuradora");
        System.out.println("6 = Eficiencia/Velocidad de una depuradora");
        int choise = scan.nextInt();
        Method method = null;
        Processes p = new Processes();
        String nameF = comunicator.executeQuery(choise);
        System.out.println(nameF);
        
        if (choise == 1 || choise == 2 || choise == 3){
        	int numDep = 1;
        	String industria = "";
	        Object w1, w2;
	        if (choise == 1){
	        	String wm1, wm2;
	        	System.out.println("Entra el nombre de la primera masa de agua");
	        	wm1 = scan.next();
	        	System.out.println("Entra el nombre de la primera masa de agua");
	        	wm2 = scan.next();
		        w2 = comunicator.reifyWater(NamingContext+wm1);
		        w1 = comunicator.reifyWater(NamingContext+wm2);
		        System.out.println(w1.toString());
		        System.out.println(w2.toString());
	        }
	        else if (choise == 2){
	        	//industria = "Polleria_Loli";
	        	System.out.println("Escribe el nombre de la industria (case sensitive)");
	        	industria = scan.next();
	        	float volumen = comunicator.getPropertyNeedsVolumen(NamingContext+industria);
	        	System.out.println("Volumen probar: " + volumen);
	        	w1 = comunicator.reifyIndustry(NamingContext+industria);
	        	w2 = comunicator.reifyWaterWithPrefixAndDelete("rio", volumen);
	        	System.out.println("Industria Proceso: " + w1.toString());
	        	System.out.println("Water Proceso: " + w2.toString());
	        }
	        else {
	        	System.out.println("Escribe el numero de la depuradora donde verter");
	        	numDep = scan.nextInt();
	        	System.out.println("Escribe el numero de industrias que van a verter");
	        	int aux = scan.nextInt();
	        	Industria vecInds[] = new Industria[aux];
	        	Watermass vecWater[] = new Watermass[aux];
	        	System.out.println("Escribe: \nNmbre de una industria para verter (case sensitive)");
	        	industria = scan.next();
	        	for (; aux > 0; aux--){
	        		vecInds[aux-1] = comunicator.reifyIndustry(NamingContext+industria);
	        		vecWater[aux-1] = comunicator.reifyWater(NamingContext+"post_"+industria);
	        		//reiniciamos el agua almacenada de la industria ya que la ha vertido
	        		comunicator.editWatermass(new Watermass(0,0), NamingContext+"post_"+industria);
		        	System.out.println("Industria Proceso: " + vecInds[aux-1].toString());
		        	System.out.println("Water Proceso: " + vecWater[aux-1].toString());
		        	if (aux > 1) {
		        		System.out.println("Escribe: \nNmbre de una industria para verter (case sensitive)");
		        		industria = scan.next();
		        	}
	        	}
	        	w1 = vecInds;
	        	w2 = vecWater;
	        }
	        
	        try {
	          if (choise == 1) method = p.getClass().getMethod(nameF, Watermass.class, Watermass.class);
	          else if (choise == 2) method = p.getClass().getMethod(nameF, Industria.class, Watermass.class);
	          else method = p.getClass().getMethod(nameF, Industria[].class, Watermass[].class);
	        } catch (SecurityException e) {
	        } catch (NoSuchMethodException e) {         
	        }
	        Watermass w3 = null;
	        try {
	        	w3 = (Watermass) method.invoke(p, (Object)w1, (Object)w2);
	        	} catch (IllegalArgumentException e) {        		
	        	} catch (IllegalAccessException e) {         		
	        	} catch (InvocationTargetException e) {        		
	        	}
	
	        //Watermass w3 = Processes.mergeWater(w1, w2);
	        if (w3!=null){
	        	if (choise == 1) comunicator.addWatermass(w3);
	        	else if (choise == 2) {//ACUTALIZAMOS EL WATERMASS DE SALIDA DE LA EMPRESA, DEL ALMACEN, Y SI ESTA LLENO SOLTAREMOS CON PROCESO
	        		//DE VERTIDO, LO QUE SOBRESALGA
	        		Watermass w4 = comunicator.reifyWater(NamingContext+"post_"+industria);
	        		w3 = Processes.efficientMergeWater(w3, w4);
	        		comunicator.editWatermass(w3, NamingContext+"post_"+industria);
	        	}
	        	else if (choise == 3) comunicator.addWatermass(w3, "depuradora"+numDep+"_");
	        	System.out.println("Agua de salida: " + w3.toString());
	        }
        }
        
        
        else if (choise == 4){
        	int horas, numDepuradora;
        	Depuradora dep; 
        	System.out.println("Entra el n�mero de horas que van a transcurrir");
        	horas = scan.nextInt();
        	System.out.println("Entra el n�mero de depuradora");
        	numDepuradora = scan.nextInt();
        	dep = comunicator.reifyDepuradora(String.valueOf(numDepuradora));
        	List<Watermass> aguasLimpiar = comunicator.getWaterListWithPrefix("depuradora"+numDepuradora);
        	System.out.println("Aguas antes de depurar: " + aguasLimpiar);
        	List<Individual> aguasLimpiarIndividuals = comunicator.getWaterIndividualsPrefix("depuradora"+numDepuradora);
        	Pair<Float, List<Watermass>> ret = Processes.CalcularDBOLimpiado(aguasLimpiar, dep.tiempoVida, horas);
        	aguasLimpiar = ret.getRight();
        	float ef = ret.getLeft();
        	comunicator.editOrDeleteWatermass(aguasLimpiar, aguasLimpiarIndividuals, dep.posicion);
        	comunicator.updateDepuradora(dep,horas,String.valueOf(numDepuradora), ef);
        	System.out.println("Aguas despues de depurar: " + aguasLimpiar);
        }
        
        else if (choise == 5){
        	int numDepuradora; 
        	float volumen, dboTope, dboActual, numHoras;
        	Depuradora dep; 
        	System.out.println("Entra el n�mero de depuradora");
        	numDepuradora = scan.nextInt();
        	System.out.println("Entra el tamano de la masa de agua");
        	volumen = scan.nextFloat();
        	System.out.println("Indica hasta que DBO quieres limpiar");
        	dboTope = scan.nextFloat();
        	System.out.println("Indica que DBO tiene actualmente la masa de agua");
        	dboActual = scan.nextFloat();
        	
        	dep = comunicator.reifyDepuradora(String.valueOf(numDepuradora));
        	float dboHora = ((Processes.maxK / volumen) +0.01f)*dep.eficiencia;
        	numHoras = (dboActual - dboTope) / dboHora;
        	System.out.println("La depuradora " + numDepuradora + " con eficiencia del " +
        			dep.eficiencia*100 + " % tardara " + numHoras + " horas en limpiar el agua.");
        }
        
        else if (choise == 6){
        	int numDepuradora;
        	Depuradora dep; 
        	System.out.println("Entra el n�mero de depuradora");
        	numDepuradora = scan.nextInt();
        	dep = comunicator.reifyDepuradora(String.valueOf(numDepuradora));
        	System.out.println("La depuradora " + numDepuradora + " tiene una eficiencia del " + dep.eficiencia*100 +
        			" %.\nEsta depuradora tiene un tiempo de vida de " + dep.tiempoVida);
        }
        
        
        scan.close();
        comunicator.releaseOntology();
     
        System.out.println("--------- Program terminated --------------------");
        
        /*
         * String clsName = "Ex";  // use fully qualified name
		 * Class cls = Class.forName(clsName);
		 * Object clsInstance = (Object) cls.newInstance();
		*/
    }
}