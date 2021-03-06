// $Id: KWriteEditor.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.KWriteEditor.v2_2_1;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   K W R I T E   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The default text editor in KDE. 
 */
public 
class KWriteEditor 
  extends SimpleSingleEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  KWriteEditor()
  {
    super("KWrite", new VersionID("2.2.1"), "Temerity", 
          "The KDE default text editor.",
	  "kwrite"); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 251857594035376091L;

}
