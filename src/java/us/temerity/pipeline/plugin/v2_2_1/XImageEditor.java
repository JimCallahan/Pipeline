// $Id: XImageEditor.java,v 1.1 2007/04/12 12:28:46 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

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
 * this editor plugin in order to view the HDR image. <P> 
 *
 * On Windows, the "winimage.exe" program is used to display images.  This Windows equivalent
 * to ximage is part of the <A href="http://floyd.lbl.gov/deskrad/dradHOME.html">Desktop 
 * Radiance</A> package.
 */
public
class XImageEditor
  extends SimpleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  XImageEditor() 
  {
    super("XImage", new VersionID("2.2.1"), "Temerity",
	  "The Radiance HDR image viewer.", 
	  "ximage"); 

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the name of the editor executable. 
   */
  public String
  getProgram() 
  {
    if(PackageInfo.sOsType == OsType.Windows) 
      return ("winimage.exe");
    return super.getProgram();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7140834515401234828L;

}


