// $Id: GPlayEditor.java,v 1.2 2008/09/14 22:14:42 jim Exp $

package us.temerity.pipeline.plugin.GPlayEditor.v2_4_3;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   G P L A Y   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * The Houdini 3D model viewer.
 */
public
class GPlayEditor
  extends SimpleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GPlayEditor()
  {
    super("GPlay", new VersionID("2.4.3"), "Temerity", 
	  "The Houdini 3D model viewer.", 
	  "gplay");

    addSupport(OsType.Windows); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5851994521361489265L;

}


