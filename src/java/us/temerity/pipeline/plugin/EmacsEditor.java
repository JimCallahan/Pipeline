// $Id: EmacsEditor.java,v 1.1 2004/02/23 23:48:31 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.BaseEditor; 

/*------------------------------------------------------------------------------------------*/
/*   E M A C S   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The GNU extensible, customizable, self-documenting text editor.
 */
public
class EmacsEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EmacsEditor()
  {
    super("emacs", 
	  "The GNU extensible, customizable, self-documenting text editor.");
  }

}


