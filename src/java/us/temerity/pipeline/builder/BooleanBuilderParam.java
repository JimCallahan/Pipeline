// $Id: BooleanBuilderParam.java,v 1.2 2006/12/10 23:02:25 jesse Exp $

package us.temerity.pipeline.builder;

import us.temerity.pipeline.BooleanParam;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   B U I L D E R   P A R A M                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A Builder parameter with an Boolean value. <P> 
 */
public 
class BooleanBuilderParam
  extends BooleanParam
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
  public 
  BooleanBuilderParam() 
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
  BooleanBuilderParam
  (
   String name,  
   String desc, 
   Boolean value
  ) 
  {
    super(name, desc, value);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1946048474834302993L;

}



