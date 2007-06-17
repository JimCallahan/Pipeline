// $Id: GimpEditor.java,v 1.1 2007/06/17 15:34:40 jim Exp $

package us.temerity.pipeline.plugin.GimpEditor.v1_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   G I M P   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The GNU Image Manipuation Program. 
 */
public
class GimpEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GimpEditor()
  {
    super("Gimp", new VersionID("1.0.0"), "Temerity", 
	  "The GNU Image Manipulation Program.", 
	  "gimp");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3360850101193207174L;

}


