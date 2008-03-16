// $Id: TextAreaParam.java,v 1.1 2008/03/16 13:02:34 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   A R E A   P A R A M                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with a long multi-line String value. <P> 
 */
public 
class TextAreaParam
  extends StringParam
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
  TextAreaParam() 
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
  TextAreaParam
  (
   String name,  
   String desc, 
   String value
  ) 
  {
    this(name, desc, value, 5);
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
  TextAreaParam
  (
   String name,  
   String desc, 
   String value,
   int rows
  ) 
  {
    super(name, desc, value);
    
    if(rows < 1) 
      throw new IllegalArgumentException
        ("The number of displayed rows (" + rows + ") must be at least (1)!"); 
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

  private static final long serialVersionUID = 166377310178312810L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The number of rows that this param should have.
   */
  private int pRows;
  
}


