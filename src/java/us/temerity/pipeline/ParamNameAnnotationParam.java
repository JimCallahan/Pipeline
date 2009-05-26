// $Id: ParamNameAnnotationParam.java,v 1.1 2009/05/26 07:09:32 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   P A R A M   N A M E   A N N O T A T I O N   P A R A M                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An annotation param with a value that is a valid param name.
 */
public 
class ParamNameAnnotationParam
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
  ParamNameAnnotationParam() 
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
  ParamNameAnnotationParam
  (
   String name,  
   String desc, 
   String value
  ) 
  {
    super(name, desc, value);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @Override
  @SuppressWarnings({ "unchecked"})
  protected void 
  validate
  (
    Comparable value      
  )
    throws IllegalArgumentException 
  {
    IllegalArgumentException ex = 
      new IllegalArgumentException("String (" + value + ") is not a valid parameter name.");
    
    if((value != null) && !(value instanceof String))
      throw ex;
    
    if (value != null) {
      char[] cs = ((String) value).toCharArray();
      int wk;
      for(wk=0; wk<cs.length; wk++) {
        if(!(Character.isLetterOrDigit(cs[wk]))) {
          throw ex;
        }
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -953093013890655529L;
}
