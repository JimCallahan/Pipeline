// $Id: HoudiniEditor.java,v 1.2 2004/02/25 01:24:44 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.SingleEditor; 

/*------------------------------------------------------------------------------------------*/
/*   H O U D I N I   E D I O T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling and animation software from Side Effects Software.
 */
public
class HoudiniEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HoudiniEditor()
  {
    super("Houdini", 
	  "3D modeling and animation software from Side Effects Software.", 
	  "houdini");
  }

}


