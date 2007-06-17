// $Id: UltraEditEditor.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.UltraEditEditor.v2_2_1;

import us.temerity.pipeline.*;

/**
 * The UltraEdit text editor for Windows.
 */
public 
class UltraEditEditor 
  extends SimpleSingleEditor
{
  /*----------------------------------------------------------------------------------------*/
  /* C O N S T R U C T O R */
  /*----------------------------------------------------------------------------------------*/
  
  public UltraEditEditor()
  {
    super("UltraEdit", new VersionID("2.2.1"), "Temerity",
	  "The Ultra Edit text editor.", 
          "uedit32");
    
    removeSupport(OsType.Unix);
    addSupport(OsType.Windows);
  }

	

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 387102995316563827L;

}
