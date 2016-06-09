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
import org.apache.jena.reasoner.rulesys.builtins.Remove;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
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
		//System.out.println(resid);
		return new Industria(resid);
	}
	
    
    public List<Individual> getWater(){
    	List<Individual> result = new ArrayList<Individual>();
    	OntClass watermassClass = model.getOntClass(NamingContext+"Water_mass");
    	for (Iterator<Individual> i = model.listIndividuals(watermassClass); i.hasNext();) {
    		Individual ind = i.next();
    		result.add(ind);
            System.out.println("    · " + ind.toString());
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
        		prefix + "water_mass_" + w.dbo * 50 +"-"+ new Date());
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
    
    public void modifyVolRio(float vol, Individual particularWatermass){
    	float old = particularWatermass.getProperty(model.getProperty(NamingContext+"hasVolume")).getFloat();
    	particularWatermass.getProperty(model.getProperty(NamingContext+"hasVolume")).changeLiteralObject(old - vol);
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
    	return nameFunction;
     }
    
   
    
     
}