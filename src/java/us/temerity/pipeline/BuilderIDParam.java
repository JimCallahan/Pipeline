// $Id: BuilderIDParam.java,v 1.1 2008/02/11 03:16:25 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   I D   P A R A M                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with a BuilderID value. <P> 
 */
public 
class BuilderIDParam
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
  BuilderIDParam() 
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
   * @param value 
   *   The default value for this parameter.
   */ 
  public
  BuilderIDParam
  (
   String name,  
   String desc, 
   BuilderID value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public BuilderID
  getBuilderIDValue() 
  {
    return ((BuilderID) getValue());
  }
    
  /* 
   * Sets the value of the parameter from a String.<P>
   * 
   * This method is not supported for BuilderIDs.
   */
  public void
  fromString
  (
    String value
  )
  {
    throw new IllegalArgumentException("This method is not supported!"); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    Comparable value	  
  )
    throws IllegalArgumentException 
  {
    if((value != null) && !(value instanceof BuilderID))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (BuilderID) values!");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4181906660974003289L;

}



