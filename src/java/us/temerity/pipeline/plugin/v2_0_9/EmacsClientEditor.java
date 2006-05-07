// $Id: EmacsClientEditor.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*; 

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
    super("EmacsClient", new VersionID("2.0.9"), "Temerity", 
	  "The GNU extensible, customizable, self-documenting text editor.", 
	  "emacsclient");
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2236633074925181765L;

}


