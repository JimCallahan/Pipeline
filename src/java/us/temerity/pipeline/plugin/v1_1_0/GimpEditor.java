// $Id: GimpEditor.java,v 1.1 2004/09/08 19:34:59 jim Exp $

package us.temerity.pipeline.plugin.v1_1_0;

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
    super("Gimp", new VersionID("1.1.0"), 
	  "The GNU Image Manipulation Program.", 
	  "gimp");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3360850101193207175L;

}


