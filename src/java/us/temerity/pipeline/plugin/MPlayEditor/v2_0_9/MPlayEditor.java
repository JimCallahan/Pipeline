// $Id: MPlayEditor.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.MPlayEditor.v2_0_9;

import us.temerity.pipeline.*;

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
    super("MPlay", new VersionID("2.0.9"), "Temerity", 
	  "The Houdini image viewer.", 
	  "mplay"); 

    addSupport(OsType.Windows); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 384490476774363786L;

}


