// $Id: HoudiniEditor.java,v 1.1 2007/06/17 15:34:41 jim Exp $

package us.temerity.pipeline.plugin.HoudiniEditor.v2_0_9;

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
    super("Houdini", new VersionID("2.0.9"), "Temerity",
	  "3D modeling and animation software from Side Effects Software.", 
	  "houdini");

    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6173533378452235133L;

}


