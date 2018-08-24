package fr.inria.corese.sparql.api;

import fr.inria.corese.sparql.datatype.ICoresePolymorphDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.core.Loopable;
import java.util.List;

/**
 * This is an interface for all Corese datatypes.<br />
 *
 * This is an interface for all xsd:datatypes: each has a normalized label and a
 * lower case label, that are comparable with an other datatype(instance). Each
 * can also have a value space (which is a string or not and so allow regular
 * expression matching) that have an order relation.
 *
 * @author Olivier Corby & Olivier Savoie & Virginie Bottollier
 */
public interface IDatatype
        extends Iterable<IDatatype>, ICoresePolymorphDatatype, Node, Loopable, DatatypeValue, Comparable {
    static final int VALUE  = -1;
    static final int RESULT = -2;
    // use case: cast
    static final int UNDEFINED = -1;
    static final int LITERAL = 0;
    static final int STRING = 1;
    static final int XMLLITERAL = 2;
    static final int NUMBER = 3;
    static final int DATE = 4;
    static final int BOOLEAN = 5;
    static final int STRINGABLE = 6;
    static final int URI = 7;
    static final int UNDEF = 8;
    static final int BLANK = 9;
    static final int DOUBLE = 11;
    static final int FLOAT = 12;
    static final int DECIMAL = 13;
    static final int LONG = 14;
    static final int INTEGER = 15;
    // Pseudo codes (target is Integer or String ...)
    static final int DAY = 21;
    static final int MONTH = 22;
    static final int YEAR = 23;
    static final int DURATION = 24;
    static final int DATETIME = 25;
    static final int GENERIC_INTEGER = 26;
    
    static final String KGRAM           = ExpType.KGRAM;
    public static final String RULE     = KGRAM + "Rule";
    public static final String QUERY    = KGRAM + "Query";
    public static final String GRAPH    = KGRAM + "Graph";
    public static final String MAPPINGS = KGRAM + "Mappings";
    
    public static final String ENTITY_DATATYPE   = ExpType.DT + "entity";
    public static final String RESOURCE_DATATYPE = ExpType.DT + "resource";
    public static final String URI_DATATYPE      = ExpType.DT + "uri";
    public static final String BNODE_DATATYPE    = ExpType.DT + "bnode";
    public static final String LITERAL_DATATYPE  = ExpType.DT + "literal";
    public static final String STANDARD_DATATYPE = ExpType.DT + "standard";
    public static final String EXTENDED_DATATYPE = ExpType.DT + "extended";
    
    public static final String ITERATE_DATATYPE  = ExpType.DT + "iterate";   
    public static final String MAP_DATATYPE      = ExpType.DT + "map";   
    public static final String LIST_DATATYPE     = ExpType.DT + "list";   
    public static final String SYSTEM            = ExpType.DT + "system";
    public static final String POINTER           = ExpType.DT + "pointer";
    public static final String GRAPH_DATATYPE    = ExpType.DT + "graph";
    public static final String TRIPLE_DATATYPE   = ExpType.DT + "triple";
    public static final String QUERY_DATATYPE    = ExpType.DT + "query";
    public static final String MAPPINGS_DATATYPE = ExpType.DT + "mappings";
    public static final String MAPPING_DATATYPE  = ExpType.DT + "mapping";
    public static final String CONTEXT_DATATYPE  = ExpType.DT + "context";
    public static final String NSM_DATATYPE      = ExpType.DT + "nsmanager";
    public static final String METADATA_DATATYPE = ExpType.DT + "annotation";
    public static final String EXPRESSION_DATATYPE = ExpType.DT + "expression";
    public static final String DATAPRODUCER_DATATYPE = ExpType.DT + "producer";
    public static final String PATH_DATATYPE       = ExpType.DT + "path";
    public static final String VISITOR_DATATYPE    = ExpType.DT + "visitor";
      
    boolean isSkolem();

    boolean isXMLLiteral();
    
    boolean isUndefined();
    
    boolean isArray();
    
    boolean isList();
    boolean isMap();
    
    boolean isLoop();

    List<IDatatype> getValues();
    List<IDatatype> getValueList();
    IDatatype toList();
    IDatatypeList getList();    
    
    Iterable getLoop();

    IDatatype get(int n);
    IDatatype get(IDatatype name);
    IDatatype set(IDatatype name, IDatatype value);
   

    int size();
         
    @Override boolean isBlank();
    @Override boolean isLiteral();
    @Override boolean isURI();
    IDatatype isWellFormed();
    
    IDatatype isBlankNode();
    IDatatype isLiteralNode();
    IDatatype isURINode();
    
    boolean isFuture();
    
    boolean isPointer();
    
    int pointerType();
    
    Pointerable getPointerObject();

    /**
     * Compare 2 datatypes
     *
     * @param dt2 another datatype
     * @return 0 if they are equals, an int > 0 if the datatype is greater than
     * dt2, an int < 0 if the datatype is lesser
     */
    int compareTo(IDatatype dt2);
    
    int compare(IDatatype dt) throws CoreseDatatypeException;

    /**
     * Cast a value
     *
     * @param datatype ex: xsd:integer
     * @return the datatype casted
     */
    //IDatatype cast(IDatatype target, IDatatype javaType);
    IDatatype cast(IDatatype datatype);
    IDatatype cast(String datatype);

    /**
     * @return the lang as a datatype
     */
    IDatatype getDataLang();

    /**
     * @return the Sparql form of the datatype
     */
    String toSparql();

    String toSparql(boolean prefix);

    String toSparql(boolean prefix, boolean xsd);

    // Used by XMLLiteral to store a XML DOM 
    void setObject(Object obj);

    Object getObject();
    
    String getContent();
    
    IDatatype display();
    
    void setVariable(boolean b);

    /**
     * ************************************************************************
     */
    /**
     * test if this.getLowerCaseLabel() contains iod.getLowerCaseLabel()
     *
     * @param iod the instance to be tested with
     * @return this.getLowerCaseLabel() contains iod.getLowerCaseLabel()
     */
    boolean contains(IDatatype iod);

    /**
     * test if this.getLowerCaseLabel() starts with iod.getLowerCaseLabel()
     *
     * @param iod the instance to be tested with
     * @return this.getLowerCaseLabel() starts with iod.getLowerCaseLabel()
     */
    boolean startsWith(IDatatype iod);

    /**
     * test the equality (by value) between two instances of datatype class
     *
     * @param iod the instance to be tested with this
     * @return true if the param has the same runtime class and if values are
     * equals, else false note: equals correponds to the SPARQL equals, with
     * type checking
     */
    boolean equalsWE(IDatatype iod) throws CoreseDatatypeException;
    
    /**
     * test the equality (by value) between two instances of datatype class
     *
     * @param iod the instance to be tested with this
     * @return true if the param has the same runtime class and if values are
     * equals, else false
     */
    boolean sameTerm(IDatatype iod);

    /**
     *
     * @param iod
     * @return iod.getValue() < this.getValue() @throws Core
     * seDatatypeException
     */
    boolean less(IDatatype iod) throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() <= to this.getValue() @throws CoreseDa
     * tatypeException
     */
    boolean lessOrEqual(IDatatype iod)
            throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() > this.getValue()
     * @throws CoreseDatatypeException
     */
    boolean greater(IDatatype iod) throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() >= to this.getValue()
     * @throws CoreseDatatypeException
     */
    boolean greaterOrEqual(IDatatype iod)
            throws CoreseDatatypeException;
    
    IDatatype eq(IDatatype dt);
    IDatatype neq(IDatatype dt);
    IDatatype ge(IDatatype dt);
    IDatatype gt(IDatatype dt);
    IDatatype lt(IDatatype dt);
    IDatatype le(IDatatype dt);
    
    /**
     *
     * @param iod
     * @return iod.getValue() + this.getValue()
     */
    IDatatype plus(IDatatype iod);

    /**
     *
     * @param iod
     * @return iod.getValue() - this.getValue()
     */
    IDatatype minus(IDatatype iod);
    IDatatype minus(long val);

    /**
     *
     * @param iod
     * @return iod.getValue() * this.getValue()
     */
    IDatatype mult(IDatatype iod);

    /**
     *
     * @param iod
     * @return iod.getValue() / this.getValue()
     */
    IDatatype div(IDatatype iod);

    /**
     * ************************************************************************
     */
    /**
     * @return the datatype of this
     */
    IDatatype datatype();
    IDatatype getDatatype();

    // same as getDatatype but URI return rdfs:Resource
    IDatatype getIDatatype();

    /**
     * @return the lang of this ('fr', 'en',...)
     */
    String getLang();

    /**
     * @return the datatype of this as a URI
     */
    String getDatatypeURI();

    /**
     * @return the string depending on the datatype
     * <br>representing the value of this
     */
    String getLabel();

    String getID();

    StringBuilder getStringBuilder();
    void setStringBuilder(StringBuilder s);

    /**
     * @return true if this instance class is a number
     */
    boolean isNumber();
    
    boolean isDecimalInteger();
    
    boolean isDate();
    
    boolean isBoolean();

    Class getJavaClass();

    /**
     * ************************************************
     */
    @Deprecated
    double getDoubleValue();

    @Deprecated
    int getIntegerValue();

    @Deprecated
    int getiValue();

    @Deprecated
    double getdValue();

    @Deprecated
    String getNormalizedLabel();

    @Deprecated
    IDatatype getExtDatatype();

    @Deprecated
    String getLowerCaseLabel();
}
