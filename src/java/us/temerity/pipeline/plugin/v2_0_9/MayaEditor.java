// $Id: MayaEditor.java,v 1.1 2006/05/07 21:34:00 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * The 3D modeling, animation and rendering program from Alias|Wavefront.             
 */
public
class MayaEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaEditor()
  {
    super("Maya", new VersionID("2.0.9"), "Temerity", 
	  "3D modeling and animation software from Alias|Wavefront.", 
	  "maya");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8248071879927363867L;

}


