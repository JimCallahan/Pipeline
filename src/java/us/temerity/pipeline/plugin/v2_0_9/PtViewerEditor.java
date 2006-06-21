// $Id: PtViewerEditor.java,v 1.2 2006/06/21 04:03:23 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   P T   V I E W E R   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Interactive viewer for examining Pixar's 3D point cloud file format. <P> 
 * 
 * See the <A href="https://renderman.pixar.com/products/tools/rps.html">RenderMan 
 * ProServer</A> documentation for details about <B>ptviewer</B>(1). <P> 
 */
public
class PtViewerEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PtViewerEditor()
  {
    super("PtViewer", new VersionID("2.0.9"), "Temerity",
	  "Interactive viewer for examining Pixar's 3D point cloud file format.", 
	  "ptviewer"); 
    
    addSupport(OsType.Windows); 
    addSupport(OsType.MacOS); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 876798808731523453L;

}

