package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgdqp.sparqlendpoint.SparqlEndpointInterface;
import fr.inria.edelweiss.kgdqp.strategies.SourceSelectorWS;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgtool.load.SPARQLResult;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * @author tijani on 12/07/16.
 */
public class CallableParallelGetEdges implements Callable<String> {


    private final static Logger logger = Logger.getLogger(CallableParallelGetEdges.class);
    private String subQuery;
    private SparqlEndpointInterface rp;
    private RemoteProducerWSImpl rpWs;
    private Mappings mappingsAll;
    private Exp bgp;
    private Lock lock;
    private Environment env;



    public CallableParallelGetEdges(Lock lock,SparqlEndpointInterface rp,String subQuery,Exp bgp,Mappings mappingsAll, RemoteProducerWSImpl rpWS, Environment env) {
        this.rp = rp;
        this.subQuery = subQuery;
        this.rpWs = rpWS;
        this.mappingsAll = mappingsAll;
        this.bgp = bgp;
        this.lock = lock;
        this.env = env;
    }

    @Override
    public String call()  {

        Graph g = Graph.create(false);
        g.setTuple(true);
        Mappings mappings = null;
        try {
        if (SourceSelectorWS.ask(bgp, rpWs, env)) {

            String sparqlRes = rp.query(subQuery);
//                logger.info("Result: from "+ rp.getEndpoint() +"\n ---->  "+sparqlRes);
                mappings = SPARQLResult.create(ProducerImpl.create(g)).parseString(sparqlRes);
                SPARQLResult.create(g).parseString(sparqlRes);
                if (mappings.size() != 0) {
                    logger.debug(" results found \n" + subQuery + "\n" + "to " + rp.getEndpoint());
                } else {
                    logger.debug(" no result \n" + subQuery + "\n" + "to " + rp.getEndpoint());
                }
                logger.debug(sparqlRes);
//                logger.info("SPARQL => Mappings result: \n"+mappings.toString());

            boolean isNotEmpty = sparqlRes != null;

            QueryProcessDQP.updateCounters(bgp.toString(), rp.getEndpoint(), isNotEmpty, new Long(mappings.size()));

            if (isNotEmpty) {
                //     logger.debug("Results (cardinality " + mappings.size() + ") merged in  " + sw.getTime() + " ms from " + rp.getEndpoint());
                if (rpWs.isProvEnabled()) {
                    for (int i = 0; i < bgp.getExpList().size(); i++) {
                        rpWs.annotateResultsWithProv(g, bgp.getExpList().get(i).getEdge());
                    }
                }
            }
        } else {
            logger.debug("negative ASK (" + bgp + ") -> pruning data source " + rp.getEndpoint());
        }
        lock.lock();
        mappingsAll.add(mappings);

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }finally{
                lock.unlock();
        }
        return Thread.currentThread().getName();
    }
}
