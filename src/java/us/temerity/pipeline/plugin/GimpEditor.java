// $Id: GimpEditor.java,v 1.1 2004/02/23 23:48:31 jim Exp $

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
    super("gimp", 
	  "The GNU Image Manipulation Program.");
  }

}


