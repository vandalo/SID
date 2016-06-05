package rio;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public class Main {
    public static void main(String[] args) throws FileNotFoundException{
        String JENA = "./";
        String File = "resources/ontoprac.owl";
        String NamingContext = "http://www.semanticweb.org/luisoliva/ontologies/2016/4/ontoprac#";
        
        System.out.println("----------------Starting program -------------");

        ComunicacionOnto comunicator = new ComunicacionOnto(JENA,File,NamingContext);

        comunicator.loadOntology();        

        int choise = 2; //1 = merge water en depuradora general, 2 = proceso industrial
        Method method = null;
        Processes p = new Processes();
        String nameF = comunicator.executeQuery(choise);
        System.out.println(nameF);
        
        if (choise == 1 || choise == 2){
	        comunicator.getWater();
	        Watermass w2 = comunicator.reifyWater(NamingContext+"water_mass_2");
	        Watermass w1 = comunicator.reifyWater(NamingContext+"water_mass_1");
	        //System.out.println(w1.toString());
	        //System.out.println(w2.toString());
	        if (choise == 2){
	        	String industria = "Polleria_Loli";
	        	Industria ind = comunicator.reifyIndustry(NamingContext+industria);
	        }
	        
	        try {
	          method = p.getClass().getMethod(nameF, Watermass.class, Watermass.class);
	        } catch (SecurityException e) {
	        } catch (NoSuchMethodException e) {         
	        }
	        Watermass w3 = null;
	        try {
	        	w3 = (Watermass) method.invoke(p, w1, w2);
	        	} catch (IllegalArgumentException e) {        		
	        	} catch (IllegalAccessException e) {         		
	        	} catch (InvocationTargetException e) {        		
	        	}
	
	        //Watermass w3 = Processes.mergeWater(w1, w2);
	        if (w3!=null){
	        	comunicator.addWatermass(w3);
	        	System.out.println(w3.toString());
	        }
        }
        
        
        
        comunicator.releaseOntology();
     
        System.out.println("--------- Program terminated --------------------");
    }
}