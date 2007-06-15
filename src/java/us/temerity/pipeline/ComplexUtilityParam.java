/**
 * 
 */
package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   C O M P L E X   U T I L I T Y   P A R A M                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Implementation of Complex Parameter for Utilities.
 */
public abstract 
class ComplexUtilityParam
  extends ComplexParam<UtilityParam>
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
  public ComplexUtilityParam()
  {
    super();
  }
  
  protected ComplexUtilityParam
  (
    String name,  
    String desc
  )
  {
    super(name, desc);
  }

}
