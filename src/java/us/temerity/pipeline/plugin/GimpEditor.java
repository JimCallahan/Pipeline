// $Id: GimpEditor.java,v 1.3 2004/03/07 02:35:53 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.BaseEditor; 

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
    super("Gimp", 
	  "The GNU Image Manipulation Program.", 
	  "gimp");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3360850101193207174L;

}


