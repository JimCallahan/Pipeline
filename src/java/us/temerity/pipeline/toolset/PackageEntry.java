// $Id: PackageEntry.java,v 1.1 2004/05/29 06:37:41 jim Exp $

package us.temerity.pipeline.toolset;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   E N T R Y                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * An entry in a toolset package which represents an environmental variable name/value pair
 * along with the package merge policy for the variable.
 */ 
public 
class PackageEntry
  implements Glueable, Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  PackageEntry() 
  {}

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
   *   The package merge policy for this entry.
   */ 
  public 
  PackageEntry
  (
   String name, 
   String value, 
   MergePolicy policy
  ) 
  {
    pName  = name;
    pValue = value; 

    pMergePolicy = policy;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

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
   * Get the package merge policy for this entry.
   */ 
  public MergePolicy
  getMergePolicy() 
  {
    return pMergePolicy;
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
    if((obj != null) && (obj instanceof PackageEntry)) {
      PackageEntry e = (PackageEntry) obj;
      return (pName.equals(e.pName) && 
	      pValue.equals(e.pValue) && 
	      (pMergePolicy == e.pMergePolicy));
    }

    return false;
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
    encoder.encode("Name", pName);
    encoder.encode("Value", pValue);
    encoder.encode("MergePolicy", pMergePolicy);
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
    
    MergePolicy policy = (MergePolicy) decoder.decode("MergePolicy");
    if(policy == null) 
      throw new GlueException("The \"MergePolicy\" was missing or (null)!");
    pMergePolicy = policy;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6890472432252789088L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The environmental variable name.
   */ 
  private String  pName;

  /**
   * The environmental variable value.
   */ 
  private String  pValue;


  /**
   * The package merge policy for this entry.
   */ 
  private MergePolicy  pMergePolicy;

}

  
