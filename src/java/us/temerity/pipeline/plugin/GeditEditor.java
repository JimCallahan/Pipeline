// $Id: GeditEditor.java,v 1.1 2004/02/23 23:48:31 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.BaseEditor; 

/*------------------------------------------------------------------------------------------*/
/*   G E D I T   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The lightweight GNOME desktop text editor.                                            
 */
public
class GeditEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GeditEditor()
  {
    super("gedit", 
	  "A lightweight text editor for the GNOME desktop.");
  }

}


