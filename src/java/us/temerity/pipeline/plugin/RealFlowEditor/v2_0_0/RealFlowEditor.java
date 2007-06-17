// $Id: RealFlowEditor.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.RealFlowEditor.v2_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E A L   F L O W   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * The RealFlow fluid dynamics simulator from NextLimit.      
 */
public
class RealFlowEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  RealFlowEditor()
  {
    super("RealFlow", new VersionID("2.0.0"), "Temerity", 
	  "The RealFlow fluid dynamics simulator from NextLimit.", 
	  "realflow");

    underDevelopment();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5822353500450772800L;

}


