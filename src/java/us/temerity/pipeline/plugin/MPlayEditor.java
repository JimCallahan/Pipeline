// $Id: MPlayEditor.java,v 1.1 2004/02/25 01:24:44 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.BaseEditor;

/*------------------------------------------------------------------------------------------*/
/*   M P L A Y   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The Houdini image viewer.                                    
 */

public
class MPlayEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MPlayEditor()
  {
    super("MPlay", 
	  "The Houdini image viewer.", 
	  "mplay");              
  }
}


