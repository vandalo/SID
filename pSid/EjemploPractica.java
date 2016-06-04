package tutorial;

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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EjemploPractica{
    OntModel model;
    String JENAPath;
    String OntologyFile;
    String NamingContext;
    OntDocumentManager dm;

    public EjemploPractica(String _JENA_PATH, String _File,String _NamingContext) {
        this.JENAPath = _JENA_PATH;
        this.OntologyFile = _File;
        this.NamingContext =  _NamingContext;
    }

    public void loadOntology() {
        System.out.println("· Loading Ontology");
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
        dm = model.getDocumentManager();
        dm.addAltEntry(NamingContext, "file:" + JENAPath + OntologyFile );
        model.read(NamingContext);
    }

    public void releaseOntology() throws FileNotFoundException {
        System.out.println("· Releasing Ontology");
        if (!model.isClosed()){
            model.write(new FileOutputStream(OntologyFile));
            model.close();
        }
    }

	public Watermass reifyWater(String URI) {
		Individual water = model.getIndividual(URI);
		
		Property volume = model.getProperty(NamingContext+"hasVolume");
		RDFNode nodeVolume = water.getPropertyValue(volume);
		float v = nodeVolume.asLiteral().getFloat();
		
		Property dbo = model.getProperty(NamingContext+"hasDBO");
		RDFNode nodeDBO = water.getPropertyValue(dbo);
		float d = nodeDBO.asLiteral().getFloat();
		
		return new Watermass(v, d);
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
    
    public String executeQuery(){
    	String queryString ="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
    	   					"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
    	   					"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "+
    	   					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
    	   					"PREFIX prac: <http://www.semanticweb.org/luisoliva/ontologies/2016/4/ontoprac#> "+
    	   					"SELECT ?functionName "+
    	   					"WHERE { "+
    	   					 "?s a prac:Merge_water ." +
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