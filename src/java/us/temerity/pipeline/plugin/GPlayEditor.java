// $Id: GPlayEditor.java,v 1.2 2004/03/07 02:35:53 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.SingleEditor; 


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
    super("GPlay", 
	  "The Houdini 3D model viewer.", 
	  "gplay");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3692817573568021007L;

}


