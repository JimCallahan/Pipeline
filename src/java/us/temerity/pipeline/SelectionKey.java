// $Id: SelectionKey.java,v 1.2 2006/02/27 17:54:52 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   K E Y                                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * A symbolic key used to select the best host on which to run a job.
 * 
 * @see JobReqs
 */
public
class SelectionKey
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
  SelectionKey() 
  {
    super();
  }

  /** 
   * Construct a new selection key.
   * 
   * @param name 
   *   The name of the selection key.
   * 
   * @param desc 
   *   A short description of the selection key.
   */ 
  public
  SelectionKey
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

  private static final long serialVersionUID = -8056112617746926162L;

  
}



