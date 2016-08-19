/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.test;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.Messages;
import fr.inria.edelweiss.kgdqp.core.ProviderImplCostMonitoring;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.XMLLayout;

/**
 *
 * @author macina
 */
public class TestDQP {

    private Logger logger = Logger.getLogger(TestDQP.class);

    static final String host = "localhost";

    private HashMap<String, String> queries = new HashMap<String, String>();
    private boolean modeBGP = false;
    private int round = 0;
    private int numberWantedSibqueries = 0;

    public TestDQP(boolean modeBGP) {

        this.modeBGP = modeBGP;
        //cross domain
        String cd1 = "SELECT ?predicate ?object WHERE { "
                + "{ <http://dbpedia.org/resource/Barack_Obama> ?predicate ?object } "
                + "UNION "
                + "{ ?subject <http://www.w3.org/2002/07/owl#sameAs> <http://dbpedia.org/resource/Barack_Obama> . "
                + "?subject ?predicate ?object } "
                + "}";
        
        String cd2 = "SELECT ?party ?page WHERE { "
                + "<http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/ontology/party> ?party . "
                + "?x <http://data.nytimes.com/elements/topicPage> ?page . "
                + "?x <http://www.w3.org/2002/07/owl#sameAs> <http://dbpedia.org/resource/Barack_Obama> ."
                + "}";
        
        String cd3 = "SELECT ?president ?party ?page WHERE { "
                + "?president <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/President> . "
                + "?president <http://dbpedia.org/ontology/nationality> <http://dbpedia.org/resource/United_States> . "
                + "?president <http://dbpedia.org/ontology/party> ?party . ?x <http://data.nytimes.com/elements/topicPage> ?page . "
                + "?x <http://www.w3.org/2002/07/owl#sameAs> ?president ."
                + "}";
        
        String cd4 = "SELECT ?actor ?news WHERE { "
                + "?film <http://purl.org/dc/terms/title> 'Tarzan' . "
                + "?film <http://data.linkedmdb.org/resource/movie/actor> ?actor . "
                + "?actor <http://www.w3.org/2002/07/owl#sameAs> ?x. "
                + "?y <http://www.w3.org/2002/07/owl#sameAs> ?x . "
                + "?y <http://data.nytimes.com/elements/topicPage> ?news ."
                + "}";
        
        String cd5 = "SELECT ?film ?director ?genre WHERE { "
                + "?film <http://dbpedia.org/ontology/director> ?director . "
                + "?director <http://dbpedia.org/ontology/nationality> <http://dbpedia.org/resource/Italy> . "
                + "?x <http://www.w3.org/2002/07/owl#sameAs> ?film . "
                + "?x <http://data.linkedmdb.org/resource/movie/genre> ?genre ."
                + "}";
        
        String cd6 = "SELECT ?name ?location ?news WHERE { "
                + "?artist <http://xmlns.com/foaf/0.1/name> ?name . "
                + "?artist <http://xmlns.com/foaf/0.1/based_near> ?location . "
                + "?location <http://www.geonames.org/ontology#parentFeature> ?germany . "
                + "?germany <http://www.geonames.org/ontology#name> 'Federal Republic of Germany'."
                + "}";
        
        String cd7 = "SELECT ?location ?news WHERE { "
                + "?location <http://www.geonames.org/ontology#parentFeature> ?parent . "
                + "?parent <http://www.geonames.org/ontology#name> 'California' . "
                + "?y <http://www.geonames.org/ontology#name> ?location . "
                + "?y <http://data.nytimes.com/elements/topicPage> ?news ."
                + " }";
        
        
        
        //life science
         String ls1 = "SELECT ?drug ?melt WHERE { "
                 + "{ ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/meltingPoint> ?melt. } "
                 + " UNION "
                 + "{ ?drug <http://dbpedia.org/ontology/Drug/meltingPoint> ?melt . }"
                 + "}";
         
        String ls2 = "SELECT ?predicate ?object WHERE { \n" +
                " { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }\n" +
                "  UNION \n" +
                " { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.w3.org/2002/07/owl#sameAs> ?caff .\n" +
                "  ?caff ?predicate ?object . } \n" +
                " }";
        
         String ls2bis = "SELECT ?predicate ?object WHERE  \n" +
                " { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }\n" ;
        
        String ls3 = "SELECT ?Drug ?IntDrug ?IntEffect WHERE {\n" +
                    "    ?Drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Drug> .\n" +
                    "    ?y <http://www.w3.org/2002/07/owl#sameAs> ?Drug .\n" +
                    "    ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug1> ?y .\n" +
                    "    ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug2> ?IntDrug .\n" +
                    "    ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/text> ?IntEffect . \n" +
                    "}";
        
         String ls33 = "select ?Drug ?IntDrug ?IntEffect \n" +
                "where\n" +
                "{\n" +
                "service <http://localhost:8892/sparql> {?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug1> ?y . \n" +
                "?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug2> ?IntDrug . \n" +
                "?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/text> ?IntEffect . }\n" +
                "?y <http://www.w3.org/2002/07/owl#sameAs> ?Drug . \n" +
                "?Drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Drug> . \n" +
                "}";
         
         
        String ls4 = "SELECT ?drugDesc ?cpd ?equation WHERE { "
                + "?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/cathartics> ."
                + " ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?cpd ."
                + " ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/description> ?drugDesc ."
                + " ?enzyme <http://bio2rdf.org/ns/kegg#xSubstrate> ?cpd ."
                + " ?enzyme <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Enzyme> ."
                + " ?reaction <http://bio2rdf.org/ns/kegg#xEnzyme> ?enzyme ."
                + " ?reaction <http://bio2rdf.org/ns/kegg#equation> ?equation . "
                + "}";
       
        String ls44="select ?drugDesc ?cpd ?equation \n" +
                "where\n" +
                "{service <http://localhost:8892/sparql> {?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/cathartics> . \n" +
                "?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?cpd . \n" +
                "?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/description> ?drugDesc . }\n" +
                "service <http://localhost:8895/sparql> {?enzyme <http://bio2rdf.org/ns/kegg#xSubstrate> ?cpd . \n" +
                "?reaction <http://bio2rdf.org/ns/kegg#xEnzyme> ?enzyme . \n" +
                "?reaction <http://bio2rdf.org/ns/kegg#equation> ?equation . }\n" +
                "?enzyme <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Enzyme> . \n" +
                "}";
        
        String ls5="SELECT ?drug ?keggUrl ?chebiImage WHERE { "
                + "?drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugs> ."
                + " ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?keggDrug ."
                + " ?keggDrug <http://bio2rdf.org/ns/bio2rdf#url> ?keggUrl . "
                + " ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/genericName> ?drugBankName . "
                + " ?chebiDrug <http://purl.org/dc/elements/1.1/title> ?drugBankName . "
                + " ?chebiDrug <http://bio2rdf.org/ns/bio2rdf#image> ?chebiImage ."
                + "}";
        
        
        String ls55 ="select ?drug ?keggUrl ?chebiImage " +
                    "where" +
                    "{ " +
                    "?drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugs> . " +
                    "service <http://localhost:8892/sparql> {?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?keggDrug . " +
                    "?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/genericName> ?drugBankName . }" +
                    "?keggDrug <http://bio2rdf.org/ns/bio2rdf#url> ?keggUrl ." +
                    "?chebiDrug <http://purl.org/dc/elements/1.1/title> ?drugBankName . " +
                    "service <http://localhost:8890/sparql> {?chebiDrug <http://bio2rdf.org/ns/bio2rdf#image> ?chebiImage . }" +
                    "}";
        
        
        String ls6="SELECT ?drug ?title WHERE { "
                + " ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/micronutrient> . "
                + " ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/casRegistryNumber> ?id . "
                + " ?keggDrug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Drug> . "
                + " ?keggDrug <http://bio2rdf.org/ns/bio2rdf#xRef> ?id . "
                + " ?keggDrug <http://purl.org/dc/elements/1.1/title> ?title ."
                + "}";
        
        String ls66 = "select ?drug ?title \n" +
                        "where\n" +
                        "{ \n" +
                        "service <http://localhost:8892/sparql> {?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/micronutrient> . \n" +
                        "?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/casRegistryNumber> ?id . }\n" +
                        "service <http://localhost:8895/sparql> {?keggDrug <http://bio2rdf.org/ns/bio2rdf#xRef> ?id . \n" +
                        "?keggDrug <http://purl.org/dc/elements/1.1/title> ?title . }\n" +
                        "?keggDrug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Drug> .\n" +
                        "}";
        
        String ls7="SELECT ?drug ?transform ?mass WHERE { "
                + "{ ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/affectedOrganism> \"Humans and other mammals\". "
                + " ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/casRegistryNumber> ?cas . "
                + " ?keggDrug <http://bio2rdf.org/ns/bio2rdf#xRef> ?cas . "
                + " ?keggDrug <http://bio2rdf.org/ns/bio2rdf#mass> ?mass "
                + " FILTER ( ?mass > \"5\" ) } "
                + " OPTIONAL { ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/biotransformation> ?transform . } "
                + "}";
        
        String simple = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?nom ?popTotale  WHERE { "
                + "    ?region igeo:codeRegion ?v ."
                + "    ?region igeo:subdivisionDirecte ?departement ."
                + "     ?departement igeo:nom ?nom .  "
                + "    ?departement idemo:population ?popLeg ."
                + "    ?popLeg idemo:populationTotale ?popTotale ."
                + "} ORDER BY ?popTotale";

        String optional = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT * WHERE { "
                + "    ?region igeo:codeRegion ?v ."
                + "    ?region igeo:subdivisionDirecte ?departement ."
                + "    ?departement igeo:nom ?nom . "
                + "    OPTIONAL { ?departement igeo:subdivisionDirecte ?arrondisement . }"
                + "} ORDER BY ?region ?departement ?nom ?arrondisement";

        String union = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?arrondisement ?popTotale  WHERE { "
                + "    { ?region igeo:codeRegion ?v ."
                + "       ?region igeo:subdivisionDirecte ?departement ."
                + "       ?departement igeo:nom ?nom . "
                + "       ?departement igeo:subdivisionDirecte ?arrondisement . "
                + "       FILTER (?v <= \"42\")"
                + "    } "
                + "       UNION "
                + "    { "
                + "       ?region igeo:codeRegion ?v ."
                + "       ?region igeo:subdivisionDirecte ?departement ."
                + "       ?departement igeo:nom ?nom . "
                + "       ?departement igeo:subdivisionDirecte ?arrondisement . "
                + "       FILTER (?v > \"42\")"
                + "    } "
                + "    ?arrondisement idemo:population ?popLeg ."
                + "    ?popLeg idemo:populationTotale ?popTotale . "
                + "} ORDER BY ?popTotale";

//        String filterbis = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
//                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
//                + "SELECT ?arrondisement2 ?arrondisement1  WHERE { "
//                + "       ?region igeo:codeRegion ?v1 ."
//                + "       ?region igeo:subdivisionDirecte ?departement1 ."
//                + "       ?departement1 igeo:nom ?nom . "
//                + "       ?departement1 igeo:subdivisionDirecte ?arrondisement1 . "
//                + "       ?region igeo:subdivisionDirecte ?departement2 ."
//                + "       ?departement2 igeo:subdivisionDirecte ?arrondisement2 . "
//                + "FILTER (?arrondisement1 != ?arrondisement2)"
//                + "} ";
//
//        String subQuery = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
//                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
//                + "SELECT ?nom ?popTotale  WHERE { "
//                + "    ?region igeo:codeRegion ?v ."
//                + "    ?region igeo:subdivisionDirecte ?departement ."
//                + "    ?departement igeo:nom ?nom . "
//                + "     { SELECT ?region "
//                + "       { ?region igeo:codeRegion \"31\" . } "
//                + "     }"
//                + "    ?departement idemo:population ?popLeg ."
//                + "    ?popLeg idemo:populationTotale ?popTotale . "
//                + "} ORDER BY ?popTotale";

        String minus = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?nom ?popTotale  WHERE { "
                + "   ?region igeo:codeRegion ?v ."
                + "   ?region igeo:subdivisionDirecte ?departement ."
                + "   ?departement igeo:nom ?nom .  "
                + "   ?departement igeo:subdivisionDirecte ?arrondissement . "
                + "   minus { "
                + "       ?region igeo:codeRegion \"24\" ."
                + "       ?departement igeo:subdivisionDirecte <http://id.insee.fr/geo/arrondissement/751> . "
                + "        }  "
                + "   ?arrondissement idemo:population ?popLeg ."
                + "   ?popLeg idemo:populationTotale ?popTotale . "
                + "} ORDER BY ?popTotale";

        String filters = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?arrondissement ?cantonNom  WHERE { "
                + "   ?region igeo:codeRegion ?v ."
                + "    ?region igeo:subdivisionDirecte ?departement . "
                + "    ?departement igeo:nom ?nom .   "
                + "   ?departement igeo:subdivisionDirecte ?arrondissement . "
                + "   ?arrondissement igeo:subdivisionDirecte ?canton . "
                + "    ?canton igeo:nom ?cantonNom . "
                + " FILTER (?v = \"11\") "
                + " FILTER (?cantonNom = \"Paris 14e  canton\") "
                + "} ORDER BY ?arrondissement ?cantonNom";

        String all = "PREFIX idemo:<http://rdf.insee.fr/def/demo#> "
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?nom ?popTotale  WHERE {  "
                + "{ ?region ?p \"24\". "
                + "?region igeo:subdivisionDirecte ?departement . "
                + "?departement igeo:subdivisionDirecte ?arrondissement . "
                + " OPTIONAL { "
                + " ?arrondissement igeo:nom ?nom .  "
                + " }"
                + "}  "
                + " UNION "
                + "{   "
                + "   ?region igeo:codeRegion ?v ."
                + "   ?region igeo:subdivisionDirecte ?departement ."
                + "   ?departement igeo:subdivisionDirecte ?arrondissement . "
                + "   ?arrondissement igeo:nom ?nom .  "
                + "   minus { "
                + "        ?departement igeo:subdivisionDirecte <http://id.insee.fr/geo/arrondissement/751> . "
                + "        ?arrondissement igeo:subdivisionDirecte <http://id.insee.fr/geo/canton/6448> . "
                + "         FILTER (?v = \"24\")"
                + "        }  "
                + "  }  "
                + "    ?arrondissement idemo:population ?popLeg .  "
                + "    ?popLeg idemo:populationTotale ?popTotale .   "
                + " } ORDER BY ?popTotale";

        //name queries and queries
        //life science
//        queries.put("simple", ls1);//OK
//          queries.put("simple", ls2);//NOK: 319 if DrugBank 0 if more Endpoints ??????  OK: issue with ASK cache  and predicate as variable
//        queries.put("simple", ls3);//OK but too much time
//        queries.put("simple", ls44);//NOK Service grouping (H+D): timeout OK without SG (due clause services splitting ??) ok with ls44
//        queries.put("simple", ls55);//NOK Service grouping (H+D): timeout OK without SG (due clause services splitting ??)+ OK with ls55
//        queries.put("simple", ls66);////NOK Service grouping (H+D): timeout OK without SG (due clause services splitting ??) + OK withh ls66
//        queries.put("simple", ls7);//OK : thnaks to filer clause and 
         
         //cross domain
//        queries.put("simple", cd1);//NOK H, D & no grouping  shared unbound predicate???
//        queries.put("simple", cd2);//OK for H & D
//        queries.put("simple", cd3);//NOK : timeout  OK with no grouping
//        queries.put("simple", cd4);
//        queries.put("simple", cd5);
//        queries.put("simple", cd6);
//        queries.put("simple", cd7);
         
//        queries.put("simple", simple);
//        queries.put("minus",minus);
//        queries.put("union",union);
//        queries.put("filters",filters);
//        queries.put("optional",optional);
        queries.put("all", all);
//
//        queries.put("subQuery",subQuery);//?? but processed as AND by default  because EDGES + SUBQUERY is not an AND BGP-able
//     //when method putFreeEdgesInBGP is used => duplicated result TO FIX
    }

    public void testLocal() throws EngineException, MalformedURLException, IOException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcessDQP.create(graph);

        StopWatch sw = new StopWatch();
        sw.start();
        logger.info("Initializing GraphEngine, entailments: " + graph.getEntailment());
        Load ld = Load.create(graph);
        logger.info("Initialized GraphEngine: " + sw.getTime() + " ms");

        sw.reset();
        sw.start();
        try {
            ld.parseDir(TestDQP.class.getClassLoader().getResource("demographie").getPath() + "/cog-2012.ttl");
            ld.parseDir(TestDQP.class.getClassLoader().getResource("demographie").getPath() + "/popleg-2010.ttl");
        } catch (LoadException ex) {
            java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, ex);
        }

        logger.info("Graph size: " + graph.size());

        for (String q : queries.values()) {
            logger.info("Querying with : \n" + q);
            for (int i = 0; i < 10; i++) {
                sw.reset();
                sw.start();
                Mappings results = exec.query(q);
                logger.info(results.size() + " results: " + sw.getTime() + " ms");
            }
        }
    }

    public void testDQP(String testCase) throws EngineException, MalformedURLException {
        Graph graph = Graph.create(false);
        ProviderImplCostMonitoring sProv = ProviderImplCostMonitoring.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, true);
        execDQP.setGroupingEnabled(true);

//      Mode BGP or not
        if (modeBGP) {
            execDQP.setPlanProfile(Query.QP_BGP);
        }

//      DUPLICATED DATA
        if (testCase.equals("d")) {
            execDQP.addRemote(new URL("http://" + host + ":8081/sparql"), WSImplem.REST);
            execDQP.addRemote(new URL("http://" + host + ":8082/sparql"), WSImplem.REST);
           //Demographic
            execDQP.addRemote(new URL("http://" + host + ":8088/sparql"), WSImplem.REST);
        }

//      GLOBAL BGP
        if (testCase.equals("g")) {
            execDQP.addRemote(new URL("http://" + host + ":8083/sparql"), WSImplem.REST);
            execDQP.addRemote(new URL("http://" + host + ":8084/sparql"), WSImplem.REST);
            //Demographic
            execDQP.addRemote(new URL("http://" + host + ":8088/sparql"), WSImplem.REST);
        }

//      Partial BGP and AND Lock
        if (testCase.equals("p")) {
            execDQP.addRemote(new URL("http://" + host + ":8085/sparql"), WSImplem.REST);
            execDQP.addRemote(new URL("http://" + host + ":8086/sparql"), WSImplem.REST);
            execDQP.addRemote(new URL("http://" + host + ":8087/sparql"), WSImplem.REST);
            //Demographic
            execDQP.addRemote(new URL("http://" + host + ":8088/sparql"), WSImplem.REST);
        }
        
        if (testCase.equals("ls")) {
            execDQP.addRemote(new URL("http://" + host + ":8890/sparql"), WSImplem.REST);//ChEBI
//            execDQP.addRemote(new URL("http://" + host + ":8891/sparql"), WSImplem.REST);//DBPedia subset
            execDQP.addRemote(new URL("http://" + host + ":8892/sparql"), WSImplem.REST);//DrugBAnk
            execDQP.addRemote(new URL("http://" + host + ":8895/sparql"), WSImplem.REST);//KEGG
            
//            execDQP.addRemote(new URL("http://dbpedia.org/sparql"), WSImplem.REST);//DBPedia online
        }

        if (testCase.equals("cd")) {
            execDQP.addRemote(new URL("http://" + host + ":8891/sparql"), WSImplem.REST);//DBPedia Subset
            execDQP.addRemote(new URL("http://" + host + ":8893/sparql"), WSImplem.REST);//Geonames
            execDQP.addRemote(new URL("http://" + host + ":8894/sparql"), WSImplem.REST);//Jamendo
            execDQP.addRemote(new URL("http://" + host + ":8896/sparql"), WSImplem.REST);//LinkedDB
            execDQP.addRemote(new URL("http://" + host + ":8897/sparql"), WSImplem.REST);//NewYorkTimes
//            execDQP.addRemote(new URL("http://" + host + ":8898/sparql"), WSImplem.REST);//SW Dog Food
            
//            execDQP.addRemote(new URL("http://dbpedia.org/sparql"), WSImplem.REST);//DBPedia online
        }
        
        for (Map.Entry<String, String> query : queries.entrySet()) {
//            try {
//                String resultFileName = "/home/macina/NetBeansProjects/corese/kg-dqp/src/main/resources/" + query.getKey() + "/" + query.getKey() + "Result";
//                String valuesFileName = "/home/macina/NetBeansProjects/corese/kg-dqp/src/main/resources/" + query.getKey() + "/" + query.getKey() + "Values";
//                File resultFile = new File(resultFileName);
//                File valuesFile = new File(valuesFileName);
//
//                if (modeBGP) {
////                    resultFileName += "H" + round + ".txt";
////                    valuesFileName += "H" + round + ".csv";
//                    resultFileName += "H.txt";
//                    valuesFileName += "H.csv";
//                    resultFile = new File(resultFileName);
//                    valuesFile = new File(valuesFileName);
//
//                    //To put values (execution time , final results, etc. => in a .csv file)
//                    FileWriter writeValuesFile;
//                    try {
//                        writeValuesFile = new FileWriter(valuesFile,true);
//                        BufferedWriter bufferValuesFile = new BufferedWriter(writeValuesFile);
////                        if(!valuesFile.exists())
//                            bufferValuesFile.write("BGPs, Edges, Query, Results, Execution, Remote, Intermediate\n");
//                        bufferValuesFile.flush();
//                        writeValuesFile.close();
//                    } catch (IOException ex) {
//                        java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                } else {
////                    resultFileName += "D" + round + ".txt";
////                    valuesFileName += "D" + round + ".csv";
//                    
//                    resultFileName += "D.txt";
//                    valuesFileName += "D.csv";
//                    
//                    resultFile = new File(resultFileName);
//                    valuesFile = new File(valuesFileName);
//
//                    //To put values (execution time , final results, etc. => in a .csv file)
//                    FileWriter writeValuesFile;
//                    try {
//                        writeValuesFile = new FileWriter(valuesFile,true);
//                        BufferedWriter bufferValuesFile = new BufferedWriter(writeValuesFile);
////                        if(!valuesFile.exists())
//                            bufferValuesFile.write("Edges, Query, Results, Execution, Remote, Intermediate\n");
//                        bufferValuesFile.flush();
//                        writeValuesFile.close();
//                    } catch (IOException ex) {
//                        java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
//                
////            for (int i = 0; i < 1; i++) {
//                logger.setAdditivity(false);
//                try {
////                    TO FIX
////                    String path = TestDQP.class.getClassLoader().getResource("test").getPath()+query.getKey()+"/log"+i+".html";
////                    System.out.println("?? "+path);
//                    String logName = "/home/macina/NetBeansProjects/corese/kg-dqp/src/main/resources/" + query.getKey() + "/" + query.getKey();
//                    if (modeBGP) {
//                        logName += "H" + round + ".xml";
//                    } else {
//                        logName += "D" + round + ".xml";
//                    }
//                    FileAppender fa = new FileAppender(new XMLLayout(), logName, false);
//
//                    logger.addAppender(fa);
//
//                } catch (IOException ex) {
//                    java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, ex);
//                }

                StopWatch sw = new StopWatch();
                sw.start();
                Mappings map;
                if(numberWantedSibqueries == 0){
                    map = execDQP.query(query.getValue());
                }
                else{
                     map = execDQP.query(query.getValue(), numberWantedSibqueries);
                }
                sw.stop();
                logger.info(map.size() + " results in " + sw.getTime() + " ms");
                if(logger.isDebugEnabled())
                    logger.debug("\n" + map.toString());
                logger.info(Messages.countQueries);
                logger.info(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
                logger.info(Messages.countTransferredResults);
                logger.info(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
                logger.info(Messages.countDS);
                logger.info(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));
                logger.info(Messages.countTransferredResultsPerSource);
                logger.info(Util.prettyPrintCounter(QueryProcessDQP.sourceVolumeCounter));

//                try {
//                    //To put results (mappings values) in a .txt file
//                    FileWriter writeResultFile = new FileWriter(resultFile, false);
//                    BufferedWriter bufferResultFile = new BufferedWriter(writeResultFile);
//                    bufferResultFile.write(map.toString());
//                    bufferResultFile.flush();
//                    bufferResultFile.close();
//                    writeResultFile.close();
//
//                    //To put values (execution time , final results, etc. => in a .csv file)
//                    FileWriter writeValuesFile = new FileWriter(valuesFile, true);
//                    BufferedWriter bufferValuesFile = new BufferedWriter(writeValuesFile);
//                    bufferValuesFile.write(round + "," + map.size() + "," + sw.getTime() + "," + Util.sum(QueryProcessDQP.queryCounter) + "," + Util.sum(QueryProcessDQP.queryVolumeCounter) + "," + "\n");
//                    bufferValuesFile.flush();
//                    writeValuesFile.close();
//                } catch (IOException e) {
//                    java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, e);
//                }

//            } catch (InterruptedException ex) {
//                java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }

    }

    public void setRound(int round) {
        this.round = round;
    }

    public static void main(String[] args) throws EngineException, MalformedURLException {

        Options options = new Options();
        Option bgpOpt = new Option("bgp", "modeBGP", false, "specify the evaluation strategy");
        Option helpOpt = new Option("h", "help", false, "print this message");
        Option centralizeOpt = new Option("c", "centralize", false, "to evualuate in a centralized context");
        Option testCaseOpt = new Option("tc", "testCase", true, "chose the test case ( d, g or p)");
        Option roundOpt = new Option("r", "round", true, "the roound of the test ( 0, 1 ..., n)");
        Option numberWantedSubqueries = new Option("nws", "numberWantedSuqueries", true, "the number of wanted subqueries ( 0, 1 ..., n)");

        options.addOption(bgpOpt);
        options.addOption(helpOpt);
        options.addOption(centralizeOpt);
        options.addOption(testCaseOpt);
        options.addOption(roundOpt);
        options.addOption(numberWantedSubqueries);

        String header = "blabla";
        String footer = "\nPlease report any issue to macina@i3s.unice.fr";

        TestDQP test = new TestDQP(false);

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("kgdqp", header, options, footer, true);
                System.exit(0);
            }

            if (cmd.hasOption("bgp")) {
                test = new TestDQP(true);
            }

            if (cmd.hasOption("r")) {
                test.setRound(Integer.parseInt(cmd.getOptionValue("r")));
            }
            
            if (cmd.hasOption("nws")) {
                test.setNumberWantedSibqueries(Integer.parseInt(cmd.getOptionValue("nws")));
            }
            
            if (cmd.hasOption("c")) {
                test.testLocal();
            } else {

                if (cmd.hasOption("tc")) {
                    String tetsCase = cmd.getOptionValue("tc");
                    test.testDQP(tetsCase);
                }
            }

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setNumberWantedSibqueries(int numberWantedSibqueries) {
        this.numberWantedSibqueries = numberWantedSibqueries;
    }

}
