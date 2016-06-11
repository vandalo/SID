package rio;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

public class ComunicacionOnto{
    OntModel model;
    String JENAPath;
    String OntologyFile;
    String NamingContext;
    OntDocumentManager dm;

    public ComunicacionOnto(String _JENA_PATH, String _File,String _NamingContext) {
        this.JENAPath = _JENA_PATH;
        this.OntologyFile = _File;
        this.NamingContext =  _NamingContext;
    }

    public void loadOntology() {
        System.out.println("Loading Ontology");
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
        dm = model.getDocumentManager();
        dm.addAltEntry(NamingContext, "file:" + JENAPath + OntologyFile );
        model.read(NamingContext);
    }

    public void releaseOntology() throws FileNotFoundException {
        System.out.println("Releasing Ontology");
        if (!model.isClosed()){
            model.write(new FileOutputStream(OntologyFile));
            model.close();
        }
    }

	public Watermass reifyWater(String URI) {
		Individual water = model.getIndividual(URI);
		//ExtendedIterator<Individual> individuals = model.listIndividuals();
		Property volume = model.getProperty(NamingContext+"hasVolume");
		RDFNode nodeVolume = water.getPropertyValue(volume);
		float v = nodeVolume.asLiteral().getFloat();
		
		Property dbo = model.getProperty(NamingContext+"hasDBO");
		RDFNode nodeDBO = water.getPropertyValue(dbo);
		float d = nodeDBO.asLiteral().getFloat();
		
		return new Watermass(v, d);
	}
	
	
	//crea una instancia de la clase industria con los atributos que tiene en la ontologia
	public Industria reifyIndustry(String URI) {
		Individual industria = model.getIndividual(URI);
		Property residuos = model.getProperty(NamingContext+"hasResiduo");
		NodeIterator it = industria.listPropertyValues(residuos);
		Vector<Residuo> resid = new Vector<Residuo>();
		Individual indiv;
		while (it.hasNext()){
			indiv = it.next().as(Individual.class);
			resid.add(new Residuo(indiv.getLocalName(), 
					indiv.getProperty(model.getProperty(NamingContext+"hasConcentracion")).getFloat()));
		}
		return new Industria(resid);
	}
	
    
    public List<Individual> getWaterIndividualsPrefix(String prefix){
    	List<Individual> result = new ArrayList<Individual>();
    	OntClass watermassClass = model.getOntClass(NamingContext+"Water_mass");
    	for (Iterator<Individual> i = model.listIndividuals(watermassClass); i.hasNext();) {
    		Individual ind = i.next();
    		if (ind.getLocalName().startsWith(prefix)) result.add(ind);
        }
		return result;
    }
    
    
    public List<Watermass> getWaterListWithPrefix(String prefix){
    	List<Watermass> result = new ArrayList<Watermass>();
    	OntClass watermassClass = model.getOntClass(NamingContext+"Water_mass");
    	for (Iterator<Individual> i = model.listIndividuals(watermassClass); i.hasNext();) {
    		Individual ind = i.next();
    		if (ind.getLocalName().startsWith(prefix)){
    			result.add(instanceWatermass(ind));
    		}

        }
		return result;
    }
    
    public Watermass reifyWaterWithPrefix(String prefix){
    	OntClass watermassClass = model.getOntClass(NamingContext+"Clean_water_mass");
    	for (Iterator<Individual> i = model.listIndividuals(watermassClass); i.hasNext();) {
    		Individual ind = i.next();
    		if (ind.getLocalName().startsWith(prefix)){
    			return instanceWatermass(ind);
    		}
        }
    	return null;
    }
    
    
    public Watermass reifyWaterWithPrefixAndDelete(String prefix, float volumen){
    	OntClass watermassClass = model.getOntClass(NamingContext+"Water_mass");
    	for (Iterator<Individual> i = model.listIndividuals(watermassClass); i.hasNext();) {
    		Individual ind = i.next();
    		if (ind.getLocalName().startsWith(prefix)){
    			Watermass wm = instanceWatermass(ind);
    			wm.volume = volumen;
    			modifyVolRio(volumen, ind);
    			//deleteWatermass(ind);
    			return wm;
    		}
        }
    	return null;
    }
    
    private Watermass instanceWatermass(Individual water) {
		Property volume = model.getProperty(NamingContext+"hasVolume");
		RDFNode nodeVolume = water.getPropertyValue(volume);
		float v = nodeVolume.asLiteral().getFloat();
		
		Property dbo = model.getProperty(NamingContext+"hasDBO");
		RDFNode nodeDBO = water.getPropertyValue(dbo);
		float d = nodeDBO.asLiteral().getFloat();
		return new Watermass(v, d);
	}
    
    
    public Depuradora reifyDepuradora(String sufix){
    	OntClass depuradoras = model.getOntClass(NamingContext+"Depuradora");
    	for (Iterator<Individual> i = model.listIndividuals(depuradoras); i.hasNext();) {
    		Individual ind = i.next();
    		if (ind.getLocalName().endsWith(sufix)){
    			Depuradora dp = instanceDepuradora(ind);
    			return dp;
    		}
        }
    	return null;
    }
    
    
    public void updateDepuradora(Depuradora dep, int horas, String sufix){
    	OntClass depuradoras = model.getOntClass(NamingContext+"Depuradora");
    	for (Iterator<Individual> i = model.listIndividuals(depuradoras); i.hasNext();) {
    		Individual ind = i.next();
    		if (ind.getLocalName().endsWith(sufix)){
    			ind.getProperty(model.getProperty(NamingContext+"hasTiempoVida")).changeLiteralObject(dep.tiempoVida+horas);
    			//TODO UPDATE EFICIENCIA
    		}
        }
    }
    
    
    private Depuradora instanceDepuradora(Individual depur) {
		Property volume = model.getProperty(NamingContext+"hasEficiencia");
		RDFNode nodeVolume = depur.getPropertyValue(volume);
		float v = nodeVolume.asLiteral().getFloat();
		
		Property dbo = model.getProperty(NamingContext+"hasTiempoVida");
		RDFNode nodeDBO = depur.getPropertyValue(dbo);
		int d = nodeDBO.asLiteral().getInt();
		
		int pos = depur.getProperty(model.getProperty(NamingContext+"hasPosicion")).getInt();
		return new Depuradora(v, d, pos);
	}
    
    
    public void deleteWatermass(Individual ind){
    	ind.remove();
    }
    
    
    public float getPropertyNeedsVolumen(String URI){
    	Individual industria = model.getIndividual(URI);
    	Property needsVol = model.getProperty(NamingContext+"needsVolumen");
    	RDFNode nodeDBO = industria.getPropertyValue(needsVol);
		float vol = nodeDBO.asLiteral().getFloat();
		return vol;
    }
    
    
    public void addWatermass(Watermass w){
    	OntClass watermassClass = model.getOntClass(NamingContext+"Water_mass");
        Individual particularWatermass = watermassClass.createIndividual(NamingContext+"water_mass_"+ UUID.randomUUID());
        Property volume = model.getProperty(NamingContext+"hasVolume");
        Property dbo = model.getProperty(NamingContext+"hasDBO");
        Literal vol = model.createTypedLiteral(new Float(w.volume));
        Literal d = model.createTypedLiteral(new Float(w.dbo));
        particularWatermass.addLiteral(volume, vol);
        particularWatermass.addLiteral(dbo, d);
    }
    
    public void addWatermass(Watermass w, String prefix){
    	OntClass watermassClass = model.getOntClass(NamingContext+"Water_mass");
        Individual particularWatermass = watermassClass.createIndividual(NamingContext+ 
        		prefix + "water_mass_" + w.dbo +"-"+ Calendar.getInstance().get(Calendar.MINUTE)
        		+ Calendar.getInstance().get(Calendar.SECOND));
        Property volume = model.getProperty(NamingContext+"hasVolume");
        Property dbo = model.getProperty(NamingContext+"hasDBO");
        Literal vol = model.createTypedLiteral(new Float(w.volume));
        Literal d = model.createTypedLiteral(new Float(w.dbo));
        particularWatermass.addLiteral(volume, vol);
        particularWatermass.addLiteral(dbo, d);
    }
    
    
    public void editWatermass(Watermass w, String URI){
    	Individual particularWatermass = model.getIndividual(URI);
    	particularWatermass.getProperty(model.getProperty(NamingContext+"hasVolume")).changeLiteralObject(w.volume);
    	particularWatermass.getProperty(model.getProperty(NamingContext+"hasDBO")).changeLiteralObject(w.dbo);
    }
    
    
    public void editOrDeleteWatermass(List<Watermass> wl, List<Individual> indivs, int pos){
    	//int pos = indivs.get(0).getProperty(model.getProperty(NamingContext+"hasPosicion")).getInt();
    	//cogemos el rio de la posicion que estamos o mas abajo
    	Individual rio = reifyRioIndiv(pos);
    	for (int i = 0; i < indivs.size(); i++){
    		if (wl.get(i).dbo > Processes.verterAguaCalidad){
	    		indivs.get(i).getProperty(model.getProperty(NamingContext+"hasVolume")).changeLiteralObject(wl.get(i).volume);
	    		indivs.get(i).getProperty(model.getProperty(NamingContext+"hasDBO")).changeLiteralObject(wl.get(i).dbo);
    		}
    		else {
    			//actualizamos el rio que toca
    			modifyVolDboRio(indivs.get(i).getProperty(model.getProperty(NamingContext+"hasVolume")).getFloat(), 
    					indivs.get(i).getProperty(model.getProperty(NamingContext+"hasDBO")).getFloat(), rio);
    			
    			deleteWatermass(indivs.get(i));
    		}
    	}
    }
    
    //coge el rio de su posicion o mas abajo
    private Individual reifyRioIndiv(int posicion){
    	Individual rio = model.getIndividual(NamingContext+"Rio_Besos");
    	Property aguas = model.getProperty(NamingContext+"hasWatermass");
    	NodeIterator it = rio.listPropertyValues(aguas);
    	RDFNode rd;
    	while (it.hasNext()){
    		rd = it.next();
			if ((rd.as(Individual.class)).getProperty(model.getProperty(NamingContext+"hasPosicion")).getInt() > posicion)
				return rd.as(Individual.class);
		}
    	return null;
    }

    
    
    public void modifyVolRio(float vol, Individual particularWatermass){
    	float old = particularWatermass.getProperty(model.getProperty(NamingContext+"hasVolume")).getFloat();
    	particularWatermass.getProperty(model.getProperty(NamingContext+"hasVolume")).changeLiteralObject(old - vol);
    }
    
    
    public void modifyVolDboRio(float vol, float dbo, Individual rio){
    	float oldVol = rio.getProperty(model.getProperty(NamingContext+"hasVolume")).getFloat();
    	rio.getProperty(model.getProperty(NamingContext+"hasVolume")).changeLiteralObject(oldVol + vol);
    	float oldDbo = rio.getProperty(model.getProperty(NamingContext+"hasDBO")).getFloat();
    	float dboNuevo = (vol*dbo + oldVol * oldDbo) / (oldVol + vol);
    	rio.getProperty(model.getProperty(NamingContext+"hasDBO")).changeLiteralObject(dboNuevo);
    }
    
    
    public String executeQuery(int choise){
    	String aux = "";
    	if (choise == 1) aux = "?s a prac:Merge_water .";
    	else if (choise == 2) aux = "?s a prac:Proceso_Industrial .";
    	else if (choise == 3) aux = "?s a prac:Verter_Aguas .";
    	
    	String queryString ="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    	   					"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
    	   					"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "+
    	   					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
    	   					"PREFIX prac: <http://www.semanticweb.org/luisoliva/ontologies/2016/4/ontoprac#> "+
    	   					"SELECT ?functionName "+
    	   					"WHERE { "+
    	   					aux + 
    	   					 "?s prac:hasCode ?functionName }";
    	Query query = QueryFactory.create(queryString);
    	String nameFunction = "";
    	try (QueryExecution qe = QueryExecutionFactory.create(query, model)){
    		ResultSet results = qe.execSelect();
    	   	results = ResultSetFactory.copyResults(results);
    	   	while(results.hasNext()){
    	   		QuerySolution sol = results.next();
    	   		RDFNode n = sol.get("functionName");
    	   		if (n.isLiteral()) {
    	   			nameFunction = n.asLiteral().toString();
    	   			break;
    	   		}
    	   	}
    	   	qe.close();
    	}
    	if (choise > 3) return "";
    	return nameFunction;
     }
    
   
    
     
}