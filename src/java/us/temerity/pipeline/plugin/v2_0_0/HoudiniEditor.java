// $Id: HoudiniEditor.java,v 1.2 2005/07/26 04:58:30 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

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
    super("Houdini", new VersionID("2.0.0"), 
	  "3D modeling and animation software from Side Effects Software.", 
	  "houdini");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2538924938123443238L;

}


