// $Id: KWriteEditor.java,v 1.1 2006/06/21 04:56:35 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   K W R I T E   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The default text editor in KDE. 
 */
public class 
KWriteEditor 
  extends BaseEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  KWriteEditor()
  {
    super("KWrite", new VersionID("2.0.9"), "Temerity", "The KDE default text editor.",
	  "kwrite"); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7047342436681994676L;

}
