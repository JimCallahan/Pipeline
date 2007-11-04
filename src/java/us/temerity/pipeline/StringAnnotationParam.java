// $Id: StringAnnotationParam.java,v 1.2 2007/11/04 20:44:35 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   S T R I N G   A N N O T A T I O N   P A R A M                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An Annotation parameter with a short String value. <P> 
 */
public 
class StringAnnotationParam
  extends StringParam
  implements AnnotationParam
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
  StringAnnotationParam() 
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
  StringAnnotationParam
  (
   String name,  
   String desc, 
   String value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8664266546304408658L;

}



