// $Id: HoudiniEditor.java,v 1.2 2007/04/05 10:14:49 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H O U D I N I   E D I O T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling and animation software from Side Effects Software.
 */
public
class HoudiniEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HoudiniEditor()
  {
    super("Houdini", new VersionID("2.2.1"), "Temerity",
	  "3D modeling and animation software from Side Effects Software.", 
	  "houdini");

    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6926741198101937041L;

}


