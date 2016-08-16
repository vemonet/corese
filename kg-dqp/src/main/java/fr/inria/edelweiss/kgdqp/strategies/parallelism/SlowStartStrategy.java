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
public class SlowStartStrategy implements Strategy<Mappings> {

    private CompletionService<String> completions;
    private Lock lock;
    private List<String> subQueries;

    public SlowStartStrategy(List<String> subQueries, CompletionService<String> completions) {
        this.completions = completions;
        this.lock = new ReentrantLock();
        this.subQueries = subQueries;
    }

    @Override
    public void submit(SparqlEndpointInterface rp,Exp bgp,Mappings mappingsAll, RemoteProducerWSImpl rpWS, Environment env) throws InterruptedException, ExecutionException {
        double time = 0;
        int i = 1;
        int k = 1;
        int lastIndex = 0;
        double start;
        double end;
        while (i < subQueries.size() - 1) {
            List<Future<String>> futures = new ArrayList<>();
            start = System.nanoTime();
            for(int j=lastIndex;j<i;j++){
                CallableParallelGetEdges parallelEdges = new CallableParallelGetEdges(lock, rp, subQueries.get(j), bgp, mappingsAll, rpWS, env);
                futures.add(completions.submit(parallelEdges));
            }

            for(Future<String> future : futures) {
                completions.take().get();
            }
            end = System.nanoTime();

            lastIndex = i;

               if(end - start <= time || lastIndex == 1) {
                k++;
                time = end - start;
            } else {
                k--;
            }
            i +=  k;

        }
    }
}
