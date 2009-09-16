// $Id: RealFlowEditor.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.RealFlowEditor.v2_0_0;

import us.temerity.pipeline.*;

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

  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5822353500450772800L;

}


