// $Id: GPlayEditor.java,v 1.1 2007/06/17 15:34:40 jim Exp $

package us.temerity.pipeline.plugin.GPlayEditor.v2_0_0;

import us.temerity.pipeline.*; 


/*------------------------------------------------------------------------------------------*/
/*   G P L A Y   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * The Houdini 3D model viewer.
 */
public
class GPlayEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GPlayEditor()
  {
    super("GPlay", new VersionID("2.0.0"), "Temerity", 
	  "The Houdini 3D model viewer.", 
	  "gplay");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4445596060235641340L;

}


