// $Id: GEditEditor.java,v 1.1 2007/06/17 15:34:40 jim Exp $

package us.temerity.pipeline.plugin.GEditEditor.v1_0_0;

import us.temerity.pipeline.BaseEditor; 
import us.temerity.pipeline.VersionID; 

/*------------------------------------------------------------------------------------------*/
/*   G E D I T   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The lightweight GNOME desktop text editor.                                            
 */
public
class GEditEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GEditEditor()
  {
    super("GEdit", new VersionID("1.0.0"), "Temerity",
	  "A lightweight text editor for the GNOME desktop.", 
	  "gedit");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7568592679705964360L;

}


