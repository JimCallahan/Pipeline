// $Id: EmacsClientEditor.java,v 1.2 2004/02/25 01:24:44 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.BaseEditor; 

/*------------------------------------------------------------------------------------------*/
/*   E M A C S   C L I E N T   E D I T O R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The GNU extensible, customizable, self-documenting text editor run as a client.
 */
public
class EmacsClientEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EmacsClientEditor()
  {
    super("EmacsClient", 
	  "The GNU extensible, customizable, self-documenting text editor.", 
	  "emacsclient");
  }

}


