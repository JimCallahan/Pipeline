// $Id: TextAreaAnnotationParam.java,v 1.3 2008/03/16 13:02:34 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   A R E A   A N N O T A T I O N   P A R A M                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An annotation parameter with a long multi-line String value. <P> 
 */
public 
class TextAreaAnnotationParam
  extends TextAreaParam
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
  TextAreaAnnotationParam() 
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
  TextAreaAnnotationParam
  (
   String name,  
   String desc, 
   String value
  ) 
  {
    super(name, desc, value);
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
   *   
   * @param rows
   *   The number of rows that should be displayed at one time in the user interface.
   */ 
  public
  TextAreaAnnotationParam
  (
   String name,  
   String desc, 
   String value,
   int rows
  ) 
  {
    super(name, desc, value, rows);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8664266546304408658L;
  
}
