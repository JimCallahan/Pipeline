// $Id: AirShowEditor.java,v 1.2 2005/07/26 04:58:30 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   S H O W   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The AIR image viewer. <P> 
 * 
 * See the <A href="http://www.sitexgraphics.com/html/air.html">AIR</A> documentation for 
 * <A href="http://www.sitexgraphics.com/air.pdf"><B>airshow</B></A>(1) for details. <P> 
 */
public
class AirShowEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirShowEditor()
  {
    super("AirShow", new VersionID("2.0.0"), 
	  "The AIR image viewer.", 
	  "airshow"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1495782042315500934L;

}


