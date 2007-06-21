// $Id: WorkGroupParam.java,v 1.1 2007/06/21 20:18:31 jim Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   W O R K   G R O U P   P A R A M                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter which contains the name of a Pipeline user or WorkGroup.<P> 
 * 
 * This parameter can be configured to only allow user names, only allow WorkGroup names
 * or allow both.
 */
public 
class WorkGroupParam
  extends SimpleParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */    
  public
  WorkGroupParam() 
  {
    super();
  }

  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the parameter.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param allowsUsers
   *   Whether the value can be a user name.
   * 
   * @param allowsGroups
   *   Whether the value can be a WorkGroup name.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  public
  WorkGroupParam
  (
   String name,  
   String desc, 
   boolean allowsUsers, 
   boolean allowsGroups, 
   String value
  ) 
  {
    super(name, desc, value);
   
    if(!allowsUsers && !allowsGroups) 
      throw new IllegalArgumentException
	("The parameter must allow at least either users or work groups (or both)!"); 
    
    pAllowsUsers  = allowsUsers;    
    pAllowsGroups = allowsGroups; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the value can be a user name.
   */
  public boolean
  allowsUsers() 
  {
    return pAllowsUsers;
  }

  /**
   * Whether the value can be a WorkGroup name.
   */
  public boolean
  allowsGroups() 
  {
    return pAllowsGroups;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public String
  getStringValue() 
  {
    return ((String) getValue());
  }
    
  /**
   * Sets the value of the parameter from a String.
   * <p>
   * This method is used for setting parameter values from command line arguments.
   */
  public void
  fromString
  (
   String value
  )
  {
    setValue(value); 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3412336796354414623L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the value can be a user name.
   */
  private boolean  pAllowsUsers; 

  /**
   * Whether the value can be a WorkGroup name.
   */
  private boolean  pAllowsGroups; 

}



