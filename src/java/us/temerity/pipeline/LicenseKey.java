// $Id: LicenseKey.java,v 1.1 2004/06/19 00:29:07 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L I C E N S E   K E Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * A symbolic key which represents a limited number of floating software licenses.
 * 
 * @see JobReqs
 */
public
class LicenseKey
  extends Described
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public
  LicenseKey() 
  {
    super();
  }

  /** 
   * Construct a new license key.
   * 
   * @param name 
   *   The name of the license key.
   * 
   * @param desc 
   *   A short description of the license key.
   */ 
  public
  LicenseKey
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

  private static final long serialVersionUID = 7616282979518347032L;

}



