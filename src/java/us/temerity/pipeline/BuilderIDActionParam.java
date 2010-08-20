package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;


/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   A C T I O N   I D   P A R A M                                          */
/*------------------------------------------------------------------------------------------*/

public 
class BuilderIDActionParam
  extends BuilderIDParam
  implements ActionParam
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
  BuilderIDActionParam() 
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
  BuilderIDActionParam
  (
   String name,  
   String desc, 
   BuilderID value
  ) 
  {
    super(name, desc, value);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6076904540439570315L;
}
