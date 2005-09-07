// $Id: MayaEditor.java,v 1.2 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

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
    super("Maya", new VersionID("1.0.0"), "Temerity", 
	  "3D modeling and animation software from Alias|Wavefront.", 
	  "maya");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 480056724240714359L;

}


