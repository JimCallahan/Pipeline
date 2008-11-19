// $Id: FrameRangeUtilityParam.java,v 1.1 2008/11/19 04:34:47 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   F R A M E   R A N G E   U T I L I T Y   P A R A M                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A Complex Utility parameter that represents a frame range. <P> 
 */
public 
class FrameRangeUtilityParam
  extends FrameRangeParam<UtilityParam>
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
  FrameRangeUtilityParam() 
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
  FrameRangeUtilityParam
  (
   String name,  
   String desc
  ) 
  {
    super(name, desc);
  }
  
  /**
   * Construct a parameter with the given name, description, and default value
   *
   * @param name
   *   The short name of the parameter.
   * 
   * @param desc
   *   A short description used in tooltips.
   *   
   * @param value
   *   The default value
   */
  public
  FrameRangeUtilityParam
  (
   String name, 
   String desc,
   FrameRange value
  ) 
  {
    super(name, desc, value);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R I C S   S U P P O R T                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  protected UtilityParam 
  createIntegerParam
  (
    String name,
    String desc,
    Integer value
  )
  {
    return new IntegerUtilityParam(name, desc, value);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2676200105103001379L;
}
