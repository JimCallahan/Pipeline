// $Id: XdviEditor.java,v 1.1 2004/02/23 23:48:31 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.SingleEditor; 

/*------------------------------------------------------------------------------------------*/
/*   X D V I   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The DVI previewer for the X Window System.             
 */
public
class XdviEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  XdviEditor()
  {
    super("xdvi",
	  "The DVI previewer for the X Window System.");
  }

}


