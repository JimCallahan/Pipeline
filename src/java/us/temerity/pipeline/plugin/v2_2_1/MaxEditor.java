// $Id: MaxEditor.java,v 1.3 2007/03/29 19:37:36 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A X   E D I T O R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling, animation and rendering application from Autodesk. <P> 
 */
public
class MaxEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MaxEditor()
  {
    super("3dsMax", new VersionID("2.2.1"), "Temerity",
	  "3D modeling and animation software from Autodesk.",
	  "3dsmax");

    removeSupport(OsType.Unix);
    addSupport(OsType.Windows);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4942087187245234995L;

}


