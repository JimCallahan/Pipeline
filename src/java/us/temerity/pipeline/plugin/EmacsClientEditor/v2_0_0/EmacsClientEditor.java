// $Id: EmacsClientEditor.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.EmacsClientEditor.v2_0_0;

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
    super("EmacsClient", new VersionID("2.0.0"), "Temerity", 
	  "The GNU extensible, customizable, self-documenting text editor.", 
	  "emacsclient");
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2236633074925181765L;

}


