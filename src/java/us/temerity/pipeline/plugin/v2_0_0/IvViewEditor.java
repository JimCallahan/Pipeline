// $Id: IvViewEditor.java,v 1.3 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

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
    super("IvView", new VersionID("2.0.0"), "Temerity", 
	  "The scene viewer distributed with OpenInventor.", 
	  "ivview");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3067701731504663918L;

}


