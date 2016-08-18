/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * An optimizer that combines filter and binding optimizations.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 * @author Abdoul Macina, macina@i3s.unice.fr
 */
public class RemoteQueryOptimizerFull implements RemoteQueryOptimizer {

    private static Logger logger = Logger.getLogger(RemoteQueryOptimizerFull.class);

    @Override
    public String getSparqlQuery(Node gNode, List<Node> from, Edge edge, Environment env) {

        // IF (gNode == null) && (from.size <= 0) THEN nothing to do
        // IF (gNode == null) && (from.size > 0) THEN include from clauses ==> FROM
        // IF (gNode != null) && (from.size <= 0) THEN include graph pattern
        // IF (gNode != null) && (from.size > 0) THEN include from clauses  ==> FROM NAMED
        //get Prefixes
        String sparqlPrefixes = getPrefixes(env);

        //binding handling
        Edge reqEdge = getBindings(env, edge);

        //filter handling
        List<Filter> filters = Util.getApplicableFilter(env, reqEdge);

        //from handling
        String fromClauses = getFroms(from, gNode);

        String queryBody = getEdgeBody(gNode, env, reqEdge, fromClauses);

        if (filters.size() > 0) {
            queryBody += "\t  FILTER (\n";
            int i = 0;
            for (Filter filter : filters) {
                if (i == (filters.size() - 1)) {
                    queryBody += "\t\t " + ((Term) filter).toSparql() + "\n";
                } else {
                    queryBody += "\t\t " + ((Term) filter).toSparql() + "&&\n";
                }
                i++;
            }
            queryBody += "\t  )\n";
        }
        queryBody += "}";

        logger.info("EDGE QUERY: " + queryBody);
        String sparqlQuery = sparqlPrefixes + queryBody;
        return sparqlQuery;
    }

    @Override
    public String getSparqlQueryBGP(Node gNode, List<Node> from, Exp bgp, Environment environnement) {

        String queryBGP = new String();

        //Handling PREFIX
        String prefixes = getPrefixes(environnement);

        //Handling FROM
        String fromClauses = getFroms(from, gNode);

        //Final query
        queryBGP += prefixes;
        queryBGP += fromClauses;
        String body = bgpBodyQuery(bgp, environnement);
        queryBGP += body;
        logger.info("BGP QUERY: " + body);
        
        return queryBGP;
    }

    @Override
    public List<String> getSparqlQueryBGPQueries(Node gNode, List<Node> from, Exp bgp, Environment environnement) {
                //VALUES clauses
        List<StringBuffer> values = new ArrayList<>();
        if (bgp.getMappings() != null) {
            values = valuesList(bgp.getMappings(), bgp);
        }
        
        List<String> queries  = bgpBodyQueries(gNode, from, bgp, environnement, values);

        return queries;
    }
    
    //generates all pefixes
    private String getPrefixes(Environment env) {
        String prefix = new String();

        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            for (String p : namespaceMgr.getPrefixes()) {
                prefix += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        return prefix;
    }

    //generates all Froms
    private String getFroms(List<Node> from, Node gNode) {
        String fromClauses = new String();
        if ((from != null) && (!from.isEmpty())) {
            for (Node f : from) {
                if (gNode == null) {
                    fromClauses += "FROM ";
                } else {
                    fromClauses += "FROM NAMED ";
                }
                fromClauses += f + " ";
            }
        }
        return fromClauses;
    }

    private Edge getBindings(Environment env, Edge edge) {
        Node subject = env.getNode(edge.getNode(0));
        Node object = env.getNode(edge.getNode(1));
        Node predicate = null;
        // 
        if (edge.getEdgeVariable() != null) {
            predicate = env.getNode(edge.getEdgeVariable());
        }

        //  No bindings found ?
        if (subject == null) {
            subject = edge.getNode(0);
        }
        if (object == null) {
            object = edge.getNode(1);
        }
        if (predicate == null) {
            if (edge.getEdgeVariable() != null) {
                predicate = edge.getEdgeVariable();
            } else {
                predicate = edge.getEdgeNode();
            }
        }

        Edge reqEdge = EdgeImpl.create(null, subject, predicate, object);
        return reqEdge;
    }

//    private String getEdgeBody(Node gNode, Edge edge, String fromClauses ){
//        String queryBody = "";
//          if (gNode == null) {
//            queryBody += "CONSTRUCT  { " + edge.getNode(0) + " " + edge.getEdgeNode() + " " + edge.getNode(1)  + " } " + fromClauses + "\n WHERE { \n";
//            queryBody += "\t " + edge.getNode(0) + " " + edge.getEdgeNode() + " " + edge.getNode(1) + " .\n ";
//        } else {
//            queryBody += "CONSTRUCT  { GRAPH " + gNode.toString() + "{" + edge.getNode(0)+ " " + edge.getEdgeNode() + " " + edge.getNode(1) + " }} " + fromClauses + "\n WHERE { \n";
//            queryBody += "\t GRAPH " + gNode.toString() + "{" + edge.getNode(0)+ " " + edge.getEdgeNode() + " " + edge.getNode(1) + "} .\n ";
//        }
//        return queryBody;
//    }
//    private String bgpBodyQuery(Exp exp, Environment env) {
//
////        String distinct = "";
////        //TO check other keywords to add???
////        if (env.getQuery().isDistinct()) {
////            distinct = " DISTINCT ";
////        }
//
//        //TO DO get the variables instead of select *
//        String sparqlQuery = "\n SELECT * WHERE { \n";
//
//        for (Exp ee : exp.getExpList()) {
//            //edges
//            if (ee.isEdge()) {
//                //TO DO bindings for each exp : if a variable is bound with one value => replace it otherwise  (several values) use filter
////                fr.inria.edelweiss.kgenv.parser.EdgeImpl edge = (fr.inria.edelweiss.kgenv.parser.EdgeImpl) ee.getEdge();
////                Triple t = edge.getTriple();
////                sparqlQuery += "\t " + t.toString() + "\n ";
//                Edge edge = getBindings(env, ee.getEdge());
//                sparqlQuery += "\t " + edge.getNode(0) + " " + edge.getEdgeNode() + " " + edge.getNode(1) + "\n ";
//
//                //filters  To check: if it is necessary and how to add  binding
//                List<Filter> filters = ee.getFilters();
//                for (Filter f : filters) {
//                    if (f != null) {
//                        Expr e = f.getExp();
//                        sparqlQuery += "\t  FILTER (" + e.toString() + ")\n";
//                    }
//                }
//
//            }
//        }
//        sparqlQuery += "} ";
//
//        return sparqlQuery;
//    }
    private String bgpBodyQuery(Exp exp, Environment env) {

        //TO DO get the variables instead of select *
        String sparqlQuery = "\n SELECT * WHERE { \n";

        for (Exp ee : exp.getExpList()) {
            //edges
            if (ee.isEdge()) {
                Edge edge = getBindings(env, ee.getEdge());
                Edge edge1 = ee.getEdge();
                sparqlQuery += "\t " + edge1.getNode(0) + " " + edge.getEdgeNode() + " " + edge1.getNode(1) + ".\n ";

                //filters  To check: if it is necessary and how to add  binding
                List<Filter> filters = ee.getFilters();
                for (Filter f : filters) {
                    if (f != null) {
                        Expr e = f.getExp();
                        sparqlQuery += "\t  FILTER (" + e.toString() + ")\n";
                    }
                }

            }
        }

        //VALUES or FILTERS of variables
//        if (exp.getMappings() != null) {
//            sparqlQuery += strBindings(exp.getMappings(), exp) + "\n";
//        }

        sparqlQuery += "}";
               
        return sparqlQuery;
    }

    private List<String> bgpBodyQueries(Node gNode, List<Node> from, Exp exp, Environment env, List<StringBuffer> values) {

//        //VALUES clauses
//        List<StringBuffer> values = new ArrayList<>();
//        if (exp.getMappings() != null) {
//            values = valuesList(exp.getMappings(), exp);
//        }
        
        List<String> queries = new ArrayList<>();
        Memory memory = (Memory) env;
        for (int i =0; (i<memory.getSubQueries() & i<values.size()); i++) {
            StringBuffer sb = values.get(i);
            String sparqlQuery = getPrefixes(env);
            sparqlQuery += getFroms(from, gNode);
            sparqlQuery += "\n SELECT * WHERE { \n";

            for (Exp ee : exp.getExpList()) {
                //edges
                if (ee.isEdge()) {
                    Edge edge = getBindings(env, ee.getEdge());
                    Edge edge1 = ee.getEdge();
                    sparqlQuery += "\t " + edge1.getNode(0) + " " + edge.getEdgeNode() + " " + edge1.getNode(1) + ".\n ";

                    //filters  To check: if it is necessary and how to add  binding
                    List<Filter> filters = ee.getFilters();
                    for (Filter f : filters) {
                        if (f != null) {
                            Expr e = f.getExp();
                            sparqlQuery += "\t  FILTER (" + e.toString() + ")\n";
                        }
                    }

                }
            }
            sparqlQuery += sb + "\n";
            sparqlQuery += "} ";
            queries.add(sparqlQuery);
        }

        return queries;
    }

    private StringBuffer strBindings(Mappings map, Exp exp) {
        String SPACE = " ";
        StringBuffer sb = new StringBuffer();

        ArrayList<Node> variables = new ArrayList<Node>();

        sb.append("values (");

        if (exp.isEdge()) {
            Edge edge = exp.getEdge();
            if (edge.getNode(0).isVariable() && !variables.contains(edge.getNode(0))) {
                variables.add(edge.getNode(0));
            }

            if (edge.getNode(1).isVariable() && !variables.contains(edge.getNode(1))) {
                variables.add(edge.getNode(1));
            }

            if (edge.getEdgeNode().isVariable() && !variables.contains(edge.getEdgeNode())) {
                variables.add(edge.getEdgeNode());
            }
        } else {
            for (Exp ee : exp.getExpList()) {
                Edge edge = ee.getEdge();
                if (edge.getNode(0).isVariable() && !variables.contains(edge.getNode(0))) {
                    variables.add(edge.getNode(0));
                }

                if (edge.getNode(1).isVariable() && !variables.contains(edge.getNode(1))) {
                    variables.add(edge.getNode(1));
                }

                if (edge.getEdgeNode().isVariable() && !variables.contains(edge.getEdgeNode())) {
                    variables.add(edge.getEdgeNode());
                }
            }
        }
        for (Node n : variables) {
            sb.append(n);
            sb.append(SPACE);
        }
        sb.append(") {\n");

        for (Mapping m : map) {

            sb.append("(");

            for (Node n : variables) {
                Node val = m.getNode(n);
                if (val == null) {
                    sb.append("UNDEF");
                } else {
                    sb.append(val.getValue().toString());
                }
                sb.append(SPACE);
            }
            sb.append(")\n");
        }

        sb.append("}");
        return sb;

    }

    private String getEdgeBody(Node gNode, Environment env, Edge edge, String fromClauses) {
        String queryBody = "";
        Exp exp = env.getExp();
        String values = "";
        if (exp.getMappings() != null) {
            values = strBindings(exp.getMappings(), exp) + "\n";
        }

        if (gNode == null) {
            queryBody += "CONSTRUCT  { " + edge.getNode(0) + " " + edge.getEdgeNode() + " " + edge.getNode(1) + " } " + fromClauses + "\n WHERE { \n";
            queryBody += values;
            queryBody += "\t " + edge.getNode(0) + " " + edge.getEdgeNode() + " " + edge.getNode(1) + " .\n ";
        } else {
            queryBody += "CONSTRUCT  { GRAPH " + gNode.toString() + "{" + edge.getNode(0) + " " + edge.getEdgeNode() + " " + edge.getNode(1) + " }} " + fromClauses + "\n WHERE { \n";
            queryBody += values;
            queryBody += "\t GRAPH " + gNode.toString() + "{" + edge.getNode(0) + " " + edge.getEdgeNode() + " " + edge.getNode(1) + "} .\n ";
        }
        return queryBody;
    }

    private List<StringBuffer> valuesList(Mappings map, Exp exp) {
        String SPACE = " ";
        List<StringBuffer> values = new ArrayList<StringBuffer>();

        ArrayList<Node> variables = new ArrayList<Node>();

        if (exp.isEdge()) {
            Edge edge = exp.getEdge();
            if (edge.getNode(0).isVariable() && !variables.contains(edge.getNode(0))) {
                variables.add(edge.getNode(0));
            }

            if (edge.getNode(1).isVariable() && !variables.contains(edge.getNode(1))) {
                variables.add(edge.getNode(1));
            }

            if (edge.getEdgeNode().isVariable() && !variables.contains(edge.getEdgeNode())) {
                variables.add(edge.getEdgeNode());
            }
        } else {
            for (Exp ee : exp.getExpList()) {
                Edge edge = ee.getEdge();
                if (edge.getNode(0).isVariable() && !variables.contains(edge.getNode(0))) {
                    variables.add(edge.getNode(0));
                }

                if (edge.getNode(1).isVariable() && !variables.contains(edge.getNode(1))) {
                    variables.add(edge.getNode(1));
                }

                if (edge.getEdgeNode().isVariable() && !variables.contains(edge.getEdgeNode())) {
                    variables.add(edge.getEdgeNode());
                }
            }
        }

        for (Mapping m : map) {
            StringBuffer sb = new StringBuffer();
            sb.append("values (");
            for (Node n : variables) {
                sb.append(n);
                sb.append(SPACE);
            }
            sb.append(") {\n");

            sb.append("(");

            for (Node n : variables) {
                Node val = m.getNode(n);
                if (val == null) {
                    sb.append("UNDEF");
                } else {
                    sb.append(val.getValue().toString());
                }
                sb.append(SPACE);
            }
            sb.append(")\n");
            sb.append("}");
            values.add(sb);
        }

        return values;
    }
}
