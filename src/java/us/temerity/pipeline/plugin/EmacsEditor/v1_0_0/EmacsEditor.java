// $Id: EmacsEditor.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.EmacsEditor.v1_0_0;

import us.temerity.pipeline.BaseEditor; 
import us.temerity.pipeline.VersionID; 

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
    super("Emacs", new VersionID("1.0.0"), "Temerity", 
	  "The GNU extensible, customizable, self-documenting text editor.", 
	  "emacs");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5044455288055639029L;

}


