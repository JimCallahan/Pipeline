// $Id: ShoEditor.java,v 1.1 2005/12/09 09:25:12 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S H O   E D I T O R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays images using the PhotoRealistic RenderMan display server. 
 * 
 * See the <A href="https://renderman.pixar.com/products/tools/rps.html">RenderMan ProServer</A>
 * documentation for details about <B>sho</B>(1). <P> 
 */
public
class ShoEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShoEditor()
  {
    super("Sho", new VersionID("2.0.0"), "Temerity",
	  "The Pixar image viewer.", 
	  "sho"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -358704401460181049L;

}

