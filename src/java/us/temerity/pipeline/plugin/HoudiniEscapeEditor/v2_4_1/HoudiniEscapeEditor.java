// $Id: HoudiniEscapeEditor.java,v 1.1 2008/03/19 19:49:27 jim Exp $

package us.temerity.pipeline.plugin.HoudiniEscapeEditor.v2_4_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H O U D I N I   E S C A P E   E D I O T O R                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling and animation software from Side Effects Software.
 */
public
class HoudiniEscapeEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HoudiniEscapeEditor()
  {
    super("HoudiniEscape", new VersionID("2.4.1"), "Temerity",
	  "3D modeling and animation software from Side Effects Software.", 
	  "hescape");

    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5138499139690064244L;

}


