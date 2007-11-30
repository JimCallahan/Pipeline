// $Id: HardwareKey.java,v 1.1 2007/11/30 20:06:24 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   H A R D W A R E   K E Y                                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * A symbolic key used to select the best host on which to run a job.
 * 
 * @see JobReqs
 */
public
class HardwareKey
  extends Described
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
  HardwareKey() 
  {
    super();
  }

  /** 
   * Construct a new hardware key.
   * 
   * @param name 
   *   The name of the hardware key.
   * 
   * @param desc 
   *   A short description of the hardware key.
   */ 
  public
  HardwareKey
  (
   String name,  
   String desc
  ) 
  {
    super(name, desc);
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
    
    encoder.encode("Description", pDescription);  
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    String desc = (String) decoder.decode("Description"); 
    if(desc == null) 
      throw new GlueException("The \"Description\" was missing!");
    pDescription = desc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8708059351617513560L;
}



