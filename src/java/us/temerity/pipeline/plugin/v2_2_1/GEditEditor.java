// $Id: GEditEditor.java,v 1.1 2007/04/30 08:20:58 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   G E D I T   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The lightweight GNOME desktop text editor.                                            
 */
public
class GEditEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GEditEditor()
  {
    super("GEdit", new VersionID("2.2.1"), "Temerity", 
	  "A lightweight text editor for the GNOME desktop.", 
	  "gedit");

    underDevelopment();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5276607364697906004L;

}


