// $Id: EmacsClientEditor.java,v 1.3 2004/03/07 02:35:53 jim Exp $

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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3166729535592453265L;

}


