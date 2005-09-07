// $Id: XDviEditor.java,v 1.2 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

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
    super("XDvi", new VersionID("1.0.0"), "Temerity", 
	  "The DVI previewer for the X Window System.", 
	  "xdvi");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6803361743847912904L;

}


