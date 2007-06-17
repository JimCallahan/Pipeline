// $Id: DviViewerEditor.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.DviViewerEditor.v2_0_9;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   D V I   V I E W E R   E D I T O R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The DVI Previewer. <P> 
 * 
 * See <A href="http://sourceforge.net/projects/xdvi">xdvi(1)</A> on Unix systems, 
 * <A href="http://www.kiffe.com/textools.html">MacDviX</A> on Mac OS X and 
 * <A href="http://www.miktex.org">Yap</A> on Windows systems.
 */
public
class DviViewerEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DviViewerEditor() 
  {
    super("DviViewer", new VersionID("2.0.9"), "Temerity", 
	  "The DVI Previewer.", 
	  "xdvi");

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
    if(PackageInfo.sOsType == OsType.MacOS) 
      return "macdvix"; 
    else if(PackageInfo.sOsType == OsType.Windows) 
      return "yap.exe"; 
    return super.getProgram();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4550803183929769260L;

}


