// $Id: XImageEditor.java,v 1.1 2006/05/07 21:34:00 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

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
 * 
 * On Mac OS X, the X11 server must already be running before launching ximage(1) with 
 * this editor plugin in order to view the HDR image.
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
    super("XImage", new VersionID("2.0.9"), "Temerity",
	  "The Radiance HDR image viewer.", 
	  "ximage"); 

    addSupport(OsType.MacOS);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6657128382880282010L;

}


