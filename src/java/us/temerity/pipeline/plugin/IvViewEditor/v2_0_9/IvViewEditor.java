// $Id: IvViewEditor.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.IvViewEditor.v2_0_9;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   I V V I E W   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The scene viewer distributed with OpenInventor.
 */
public
class IvViewEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  IvViewEditor()
  {
    super("IvView", new VersionID("2.0.9"), "Temerity", 
	  "The scene viewer distributed with OpenInventor.", 
	  "ivview");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2091936457088365576L;

}


