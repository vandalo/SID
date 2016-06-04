package tutorial;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public class Main {
    public static void main(String[] args) throws FileNotFoundException{
        String JENA = "./";
        String File = "resources/ontoprac.owl";
        String NamingContext = "http://www.semanticweb.org/luisoliva/ontologies/2016/4/ontoprac#";
        
        System.out.println("----------------Starting program -------------");

        EjemploPractica tester = new EjemploPractica(JENA,File,NamingContext);

        tester.loadOntology();        

        String nameF = tester.executeQuery();
        System.out.println(nameF);
        tester.getWater();
        Watermass w2 = tester.reifyWater(NamingContext+"water_mass_2");
        Watermass w1 = tester.reifyWater(NamingContext+"water_mass_1");
        System.out.println(w1.toString());
        System.out.println(w2.toString());
        
        Method method = null;
        Processes p = new Processes();
        try {
          method = p.getClass().getMethod(nameF, Watermass.class, Watermass.class);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {         
        }
        Watermass w3 = null;
        try {
        	w3 = (Watermass) method.invoke(null, w1, w2);
        	} catch (IllegalArgumentException e) {        		
        	} catch (IllegalAccessException e) {         		
        	} catch (InvocationTargetException e) {        		
        	}

        //Watermass w3 = Processes.mergeWater(w1, w2);
        if (w3!=null){
        	tester.addWatermass(w3);
        	System.out.println(w3.toString());
        }
        
        tester.releaseOntology();
     
        System.out.println("--------- Program terminated --------------------");
    }
}