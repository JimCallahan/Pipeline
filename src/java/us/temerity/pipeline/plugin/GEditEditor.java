// $Id: GEditEditor.java,v 1.2 2004/03/07 02:35:53 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.BaseEditor; 

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
    super("GEdit", 
	  "A lightweight text editor for the GNOME desktop.", 
	  "gedit");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7568592679705964360L;

}


