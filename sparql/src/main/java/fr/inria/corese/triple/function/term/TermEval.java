/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.triple.function.term;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Function;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TermEval extends Term {
    public static Logger logger = LogManager.getLogger(TermEval.class);
    public static final IDatatype TRUE = DatatypeMap.TRUE;
    public static final IDatatype FALSE = DatatypeMap.FALSE;
    
    
    public TermEval(String name, Expression e1, Expression e2, Expression e3) {
        super(name, e1, e2);
        add(e3);
    }

    public TermEval(String name, Expression e1, Expression e2) {
        super(name, e1, e2);
    }
    
    public TermEval(String name, Expression e){
        super(name, e);
    }
    
    public TermEval(String name){
        super(name);
    }
    
    public TermEval(){
    } 
    
    public boolean isTrue(IDatatype dt) {
        try {
            return dt.isTrue();
        } catch (CoreseDatatypeException e) {
            return false;
        }
    }
    
    public IDatatype value(boolean value){
        return (value) ? DatatypeMap.TRUE : DatatypeMap.FALSE;
    }
    
    public IDatatype value(int value){
        return DatatypeMap.newInstance(value);
    }
       
    /**
     * Compatibility for strbefore (no lang or same lang)
     */
    public boolean compatible(IDatatype dt1, IDatatype dt2) {
        if (!dt1.hasLang()) {
            return !dt2.hasLang();
        } else if (!dt2.hasLang()) {
            return true;
        } else {
            return dt1.getLang().equals(dt2.getLang());
        }
    }
    
    public boolean isStringLiteral(IDatatype dt){
        return DatatypeMap.isStringLiteral(dt);
    }
    
    public boolean isSimpleLiteral(IDatatype dt){
        return DatatypeMap.isSimpleLiteral(dt);
    }
    
    public IDatatype result(String str, IDatatype dt1, IDatatype dt2) {
        if (dt1.hasLang() && str != "") {
            return DatatypeMap.createLiteral(str, null, dt1.getLang());
        } else if (DatatypeMap.isString(dt1)) {
            return DatatypeMap.newInstance(str);
        }
        return DatatypeMap.newLiteral(str);
    }
    
    public IDatatype result(IDatatype dt, String value) {
        if (dt.hasLang()) {
            return DatatypeMap.createLiteral(value, null, dt.getLang());
        } else if (dt.isLiteral() && dt.getDatatype() == null) {
            return DatatypeMap.newLiteral(value);
        }
        return DatatypeMap.newInstance(value);
    }
    

    public IDatatype[] evalArguments(Computer eval, Binding b, Environment env, Producer p, int start) {
        IDatatype[] args = new IDatatype[arity() - start];
        int i = 0;
        for (int j = start; j < arity(); j++) {
            args[i] = getArg(j).eval(eval, b, env, p);
            if (args[i] == null) {                
                return null;
            }
            i++;
        }
        return args;
    }
    
    /**
     * This is an Extension function call    
     */
    public boolean evalPushArguments(Function function, Computer eval, Binding b, Environment env, Producer p, int start) {
        int i = 0;
        for (int j = start; j < arity(); j++) {
            IDatatype dt = getArg(j).eval(eval, b, env, p);
            if (dt == null) {   
                return false;
            }
            else {
                b.set(function.getSignature().getArg(i++), dt);
            }           
        }
        return true;
    }
    
     public IDatatype[] evalArguments(Computer eval, Environment env, Producer p, IDatatype[] param, int size, int start) {
        IDatatype[] args = new IDatatype[size];
        int i = 0;
        for (int j = start; j < arity(); j++) {
            args[i] = getArg(j).eval(eval, env, p, param);
            if (args[i] == null) {                
                return null;
            }
            i++;
        }
        return args;
    }
    
     public boolean isReturn(IDatatype dt){
        return dt == null || DatatypeMap.isResult(dt);
    }
    
}
