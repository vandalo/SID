package rio;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;
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
        System.out.println("1 = merge water en depuradora general");
        System.out.println("2 = proceso industrial");
        System.out.println("3 = Verter Aguas");
        int choise = scan.nextInt();
        Method method = null;
        Processes p = new Processes();
        String nameF = comunicator.executeQuery(choise);
        System.out.println(nameF);
        
        if (choise == 1 || choise == 2 || choise == 3){
        	String industria = "";
	        Object w1, w2;
	        if (choise == 1){
		        w2 = comunicator.reifyWater(NamingContext+"water_mass_2");
		        w1 = comunicator.reifyWater(NamingContext+"water_mass_1");
		        System.out.println(w1.toString());
		        System.out.println(w2.toString());
	        }
	        else if (choise == 2){
	        	//industria = "Polleria_Loli";
	        	System.out.println("Escribe el nombre de la industria (case sensitive)");
	        	industria = scan.next();
	        	w1 = comunicator.reifyIndustry(NamingContext+industria);
	        	w2 = comunicator.reifyWaterWithPrefix("pre");
	        	System.out.println("Industria Proceso: " + w1.toString());
	        	System.out.println("Water Proceso: " + w2.toString());
	        }
	        else {
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
	        	System.out.println(w3.toString());
	        }
        }
        
        
        
        comunicator.releaseOntology();
     
        System.out.println("--------- Program terminated --------------------");
        
        /*
         * String clsName = "Ex";  // use fully qualified name
		 * Class cls = Class.forName(clsName);
		 * Object clsInstance = (Object) cls.newInstance();
		*/
    }
}