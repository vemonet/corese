package fr.inria.edelweiss.kgdqp.strategies.parallelism;

import fr.inria.edelweiss.kgdqp.core.CallableParallelGetEdges;
import fr.inria.edelweiss.kgdqp.core.CallableParallelGetEdges;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerWSImpl;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerWSImpl;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SparqlEndpointInterface;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author tijani on 19/07/16.
 */
public class ParallelismLevelStrategy implements Strategy<Mappings> {

    private int parallelismLevel;
    private CompletionService<String> completions;
    private List<String> subQueries;
    private Lock lock;

        public ParallelismLevelStrategy(List<String> subQueries,int parallelismLevel, CompletionService<String> completions) {
            this.parallelismLevel = parallelismLevel;
            this.completions = completions;
            this.lock = new ReentrantLock();
            this.subQueries = subQueries;
        }


        @Override
    public void submit(SparqlEndpointInterface rp, Exp bgp, Mappings mappingsAll, RemoteProducerWSImpl rpWS, Environment env) throws InterruptedException, ExecutionException {

        int nbSubQueries = subQueries.size();
        int nbIteration = nbSubQueries / parallelismLevel;
        int remainingSubQueries = nbSubQueries % parallelismLevel;
        int startSubQueries = 0;
        int endSubQueries = parallelismLevel;
            boolean hasNextIt = remainingSubQueries != 0;

        if(hasNextIt) {
            nbIteration += 1;
        }

        if(nbIteration == 0) {
            nbIteration = 1;
            endSubQueries = nbSubQueries;
            remainingSubQueries = 0;
        }

        for(int i=0;i<nbIteration;i++) {
            List<Future<String>> futures = new ArrayList<>();
            if (i == nbIteration - 1 && hasNextIt) {
                endSubQueries = startSubQueries + remainingSubQueries;
            }

            for (int j = startSubQueries; j < endSubQueries; j++) {
                CallableParallelGetEdges parallelEdges = new CallableParallelGetEdges(lock, rp, subQueries.get(j), bgp, mappingsAll, rpWS, env);
                futures.add(completions.submit(parallelEdges));
            }

            for (Future<String> future : futures) {
                completions.take().get();
            }
            startSubQueries = endSubQueries;
            endSubQueries += parallelismLevel;
        }

        }
}
