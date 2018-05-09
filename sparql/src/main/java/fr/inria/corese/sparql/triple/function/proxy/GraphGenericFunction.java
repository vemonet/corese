package fr.inria.corese.sparql.triple.function.proxy;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class GraphGenericFunction extends TermEval {  
    
    public GraphGenericFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }  
        
        switch (param.length){
            case 0:  return eval.getComputerPlugin().function(this, env, p); 
            case 1:  return eval.getComputerPlugin().function(this, env, p, param[0]); 
            case 2:  return eval.getComputerPlugin().function(this, env, p, param[0], param[1]); 
            default: return eval.getComputerPlugin().eval(this, env, p, param); 
        }
    }
    
    
  
         
}
