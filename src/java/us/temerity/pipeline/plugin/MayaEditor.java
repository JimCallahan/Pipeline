// $Id: MayaEditor.java,v 1.1 2004/02/23 23:48:31 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.SingleEditor; 

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
    super("maya", 
	  "3D modeling and animation software from Alias|Wavefront.");
  }

}


