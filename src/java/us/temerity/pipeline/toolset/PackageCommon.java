// $Id: PackageCommon.java,v 1.2 2004/05/23 19:56:48 jim Exp $

package us.temerity.pipeline.toolset;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   C O M M O N                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of <CODE>PackageVersion</CODE> and <CODE>PackageMode</CODE> which provides 
 * the common fields and methods needed by both classes. <P>
 */
public
class PackageCommon
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  protected 
  PackageCommon() 
  {
    pEntries = new TreeMap<String,Entry>();
  }

  /**
   * Internal constructor used by subclasses to create an empty Package.
   * 
   * @param name 
   *   The name of the Package.
   */ 
  protected
  PackageCommon
  (
   String name 
  ) 
  {
    super(name);
    pEntries = new TreeMap<String,Entry>();
  }
  
  /** 
   * Internal copy constructor used by both <CODE>PackageMod</CODE> and 
   * <CODE>PackageVersion</CODE> when constructing instances based off an instance of 
   * the other subclass.
   */
  protected 
  PackageCommon
  (
   PackageCommon com
  ) 
  {
    super(com.getName());
    pEntries = (TreeMap<String,Entry>) com.pEntries.clone();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the environmental variables defined for this Package.
   */ 
  public Set<String>
  getNames() 
  {
    return Collections.unmodifiableSet(pEntries.keySet());
  }

  /**
   * Get the value of the environmental variable with the given name.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if the variable is undefined or has no value.
   */ 
  public String
  getValue
  (
   String name
  ) 
  {
    Entry e = pEntries.get(name);
    if(e != null)
      return e.getValue();
    return null;
  }
  
  /**
   * Get the Package combine policy for the environmental variable with the given name.
   * 
   * @return 
   *   The policy or <CODE>null</CODE> if the variable is undefined.
   */ 
  public Policy
  getPolicy
  (
   String name
  ) 
  {
    Entry e = pEntries.get(name);
    if(e != null)
      return e.getPolicy();
    return null;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof PackageCommon)) {
      PackageCommon com = (PackageCommon) obj;
      return pEntries.equals(com.pEntries);
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    return super.clone();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("Entries", pEntries);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,Entry> entries = (TreeMap<String,Entry>) decoder.decode("Entries"); 
    if(entries == null) 
      throw new GlueException("The \"Entries\" was missing!");
    pEntries = entries;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The policy on how entries with the same name are resolved when Packages are combined.
   */ 
  public 
  enum Policy
  {
    /**
     * The entry must not be defined by any previous Package.
     */ 
    Exclusive, 

    /**
     * The entry will replace any existing entry with the same name defined by any 
     * previous Package.
     */ 
    Override, 

    /**
     * The entry is a colon seperated path who's value is appended to the value of any 
     * previously defined entry with the same name.
     */ 
    AppendPath,
    
    /**
     * The entry is a colon seperated path who's value is prepended to the value of any 
     * previously defined entry with the same name.
     */ 
    PrependPath;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * An environmental variable Package entry.
   */ 
  public 
  class Entry
    implements Glueable
  {
    /**
     * Construct an entry.
     * 
     * @param name
     *   The name of the environmental variable.
     * 
     * @param value
     *   The value of the environmental variable or <CODE>null</CODE>.
     * 
     * @param policy
     *   The Package combine policy for this entry.
     */ 
    public 
    Entry
    (
     String name, 
     String value, 
     Policy policy
    ) 
    {
      pName   = name;
      pValue  = value; 
      pPolicy = policy;
    }


    /*-- ACCESS ----------------------------------------------------------------------------*/

    /**
     * Get the name of the environmental variable.
     */ 
    public String 
    getName()
    {
      return pName;
    }

    /**
     * Get the value of the environmental variable.
     */ 
    public String 
    getValue()
    {
      return pValue;
    }

    /**
     * Get the Package combine policy for this entry.
     */ 
    public Policy
    getPolicy() 
    {
      return pPolicy;
    }


    /*-- OBJECT OVERRIDES ------------------------------------------------------------------*/

    /** 
     * Indicates whether some other object is "equal to" this one.
     * 
     * @param obj 
     *   The reference object with which to compare.
     */
    public boolean
    equals
    (
     Object obj   
     )
    {
      if((obj != null) && (obj instanceof Entry)) {
	Entry e = (Entry) obj;
	return (pName.equals(e.pName) && pValue.equals(e.pValue) && (pPolicy == e.pPolicy));
      }
      return false;
    }


    /*-- GLUEBALE --------------------------------------------------------------------------*/

    public void 
    toGlue
    ( 
     GlueEncoder encoder   
    ) 
      throws GlueException
    {
      encoder.encode("Name", pName);
      encoder.encode("Value", pValue);
      encoder.encode("Policy", pPolicy);
    }

    public void 
    fromGlue
    (
     GlueDecoder decoder 
    ) 
      throws GlueException
    {
      String name = (String) decoder.decode("Name");
      if(name == null) 
	throw new GlueException("The \"Name\" was missing or (null)!");
      pName = name;

      String value = (String) decoder.decode("Value");
      pValue = value;

      Policy policy = (Policy) decoder.decode("Policy");
      if(policy == null) 
	throw new GlueException("The \"Policy\" was missing or (null)!");
      pPolicy = policy;
    }


    /*-- INTERNALS -------------------------------------------------------------------------*/

    private String  pName;
    private String  pValue;
    private Policy  pPolicy;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6890472432252789088L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of environmental variable entries.
   */
  protected TreeMap<String,Entry>  pEntries; 

}
