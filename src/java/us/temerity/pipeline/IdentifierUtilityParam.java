// $Id: IdentifierUtilityParam.java,v 1.2 2009/05/26 07:09:32 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   I D E N T I F I E R   U T I L I T Y   P A R A M                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An Utility parameter with an identifier String value. <P> 
 */
public 
class IdentifierUtilityParam
  extends StringParam
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
  IdentifierUtilityParam() 
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
  IdentifierUtilityParam
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
    
    if (value != null) {
      char[] cs = ((String) value).toCharArray();
      int wk;
      for(wk=0; wk<cs.length; wk++) {
        if(!(Character.isLetterOrDigit(cs[wk]) || 
          (cs[wk] == '_') ||(cs[wk] == '-') ||(cs[wk] == '.'))) {
          throw ex;
        }
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the value of this parameter from a string.
   */
  public void 
  valueFromString
  (
    String value
  )
  {
    if (value == null)
      return;
    setValue(value);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6562921895708739669L;

}



