// $Id: HoudiniEditor.java,v 1.3 2004/03/07 02:35:53 jim Exp $

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


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1104835857294060158L;

}


