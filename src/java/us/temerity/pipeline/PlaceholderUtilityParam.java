// $Id: PlaceholderUtilityParam.java,v 1.4 2008/02/11 19:22:10 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   P L A C E H O L D E R   U T I L I T Y   P A R A M                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A plugin parameter that is meant to be replaced.
 * <p>
 * Trying to set a value for this parameter or trying to display it in the GUI will result in
 * an exception being thrown. It exists to allow layouts to be constructed with all the
 * correct parameters in them when all parameter values may not be known at the beginning
 * of the builder run.
 * <P>
 */
public 
class PlaceholderUtilityParam
  extends SimpleParam
  implements UtilityParam
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
  PlaceholderUtilityParam() 
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
   */ 
  public
  PlaceholderUtilityParam
  (
   String name,  
   String desc
  ) 
  {
    super(name, desc, null);
    pBuilt = true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Throws an {@link IllegalStateException} since this parameter type cannot be set from
   * the command line.
   */
  public void
  fromString
  (
    String value
  )
  {
    throw new IllegalStateException("Cannot set the value of a Placeholder Parameter.");
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    Comparable value	  
  )
    throws IllegalArgumentException 
  {
    if (pBuilt)
      throw new IllegalArgumentException
	("The parameter (" + pName + ") is a placeholder parameter and does not " +
	 "accept a value!");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private boolean pBuilt = false;;
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4873922978984322955L;
}



