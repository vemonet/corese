package fr.inria.edelweiss.kgdqp.strategies.parallelism;

import fr.inria.edelweiss.kgdqp.core.RemoteProducerWSImpl;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerWSImpl;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SparqlEndpointInterface;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author tijani on 19/07/16.
 */
public interface Strategy<T> {

    void submit(SparqlEndpointInterface rp, Exp bgp, T data, RemoteProducerWSImpl rpWS, Environment env) throws InterruptedException, ExecutionException;
}
