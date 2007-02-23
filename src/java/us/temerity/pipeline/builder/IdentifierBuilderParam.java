// $Id: IdentifierBuilderParam.java,v 1.3 2007/02/23 21:08:39 jesse Exp $

package us.temerity.pipeline.builder;

import us.temerity.pipeline.StringParam;

/*------------------------------------------------------------------------------------------*/
/*   I D E N T I F I E R   B U I D E R   P A R A M                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An Builder parameter with an identifier String value. <P> 
 */
public 
class IdentifierBuilderParam
  extends StringParam
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
  IdentifierBuilderParam() 
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
  IdentifierBuilderParam
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
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    Comparable value	  
  )
    throws IllegalArgumentException 
  {
    IllegalArgumentException ex = 
      new IllegalArgumentException("String (" + value + ") is not a valid identifier value");
    
    if((value != null) && !(value instanceof String))
      throw ex;
    
    char[] cs = ((String) value).toCharArray();
    int wk;
    for(wk=0; wk<cs.length; wk++) {
      if(!(Character.isLetterOrDigit(cs[wk]) || 
	(cs[wk] == '_') ||(cs[wk] == '-') ||(cs[wk] == '.'))) {
	throw ex;
      }
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6562921895708739669L;

}



