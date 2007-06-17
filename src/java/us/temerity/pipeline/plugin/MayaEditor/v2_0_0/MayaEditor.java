// $Id: MayaEditor.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MayaEditor.v2_0_0;

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
    super("Maya", new VersionID("2.0.0"), "Temerity", 
	  "3D modeling and animation software from Alias|Wavefront.", 
	  "maya");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4921105597647706054L;

}


