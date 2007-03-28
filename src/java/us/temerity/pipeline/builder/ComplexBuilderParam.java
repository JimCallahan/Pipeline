/**
 * 
 */
package us.temerity.pipeline.builder;

import us.temerity.pipeline.ComplexParam;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   C O M P L E X   B U I L D E R   P A R A M                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Implementation of Complex Parameter for Builders.
 */
public abstract 
class ComplexBuilderParam
  extends ComplexParam<BuilderParam>
  implements BuilderParam
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */   
  public ComplexBuilderParam()
  {
    super();
  }
  
  protected ComplexBuilderParam
  (
    String name,  
    String desc
  )
  {
    super(name, desc);
  }

}
