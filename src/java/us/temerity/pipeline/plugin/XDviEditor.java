// $Id: XDviEditor.java,v 1.2 2004/03/07 02:35:53 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.SingleEditor; 

/*------------------------------------------------------------------------------------------*/
/*   X D V I   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The DVI previewer for the X Window System.             
 */
public
class XDviEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  XDviEditor()
  {
    super("XDvi",
	  "The DVI previewer for the X Window System.", 
	  "xdvi");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6803361743847912904L;

}


