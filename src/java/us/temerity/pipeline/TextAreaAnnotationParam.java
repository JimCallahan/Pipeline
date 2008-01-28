// $Id: TextAreaAnnotationParam.java,v 1.2 2008/01/28 12:08:26 jesse Exp $

package us.temerity.pipeline;

import java.awt.TextArea;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   A R E A   A N N O T A T I O N   P A R A M                                    */
/*------------------------------------------------------------------------------------------*/
/**
 * An annotation parameter with a String value that is represented with a {@link TextArea} in
 * the ui.<P>
 */
public 
class TextAreaAnnotationParam
  extends StringAnnotationParam
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
   *   
   * @param rows
   *   The number of rows that should be displayed in the user interface
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
    super(name, desc, value);
    if (rows < 1)
      rows = 1;
    pRows = rows;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the number of rows that this param should have.
   */
  public int
  getRows()
  {
    return pRows;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("Rows", pRows);
  }
  
  @SuppressWarnings("unchecked")
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pRows = (Integer) decoder.decode("Rows"); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8664266546304408658L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The number of rows that this param should have.
   */
  private int pRows;
  

}
