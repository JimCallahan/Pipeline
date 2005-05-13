// $Id: XImageEditor.java,v 1.1 2005/05/13 10:56:22 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   X I M A G E    E D I T O R                                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * The Radiance HDR image viewer. <P> 
 * 
 * See the <A href="http://radsite.lbl.gov/radiance">Radiance</A> documentation for 
 * <A href="http://radsite.lbl.gov/radiance/man_html/ximage.1.html"><B>ximage</B></A>(1) for 
 * details. <P> 
 */
public
class XImageEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  XImageEditor() 
  {
    super("XImage", new VersionID("1.0.0"), 
	  "The Radiance HDR image viewer.", 
	  "ximage"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1655378268712649942L;

}


