// $Id: MayaEditor.java,v 1.3 2004/03/07 02:35:53 jim Exp $

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
    super("Maya", 
	  "3D modeling and animation software from Alias|Wavefront.", 
	  "maya");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 480056724240714359L;

}


