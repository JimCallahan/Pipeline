// $Id: BrickViewerEditor.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   B R I C K   V I E W E R   E D I T O R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Interactive viewer for examining Pixar's brick map format. <P> 
 * 
 * See the <A href="https://renderman.pixar.com/products/tools/rps.html">RenderMan 
 * ProServer</A> documentation for details about <B>brickviewer</B>(1). <P> 
 */
public
class BrickViewerEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  BrickViewerEditor()
  {
    super("BrickViewer", new VersionID("2.0.9"), "Temerity",
	  "Interactive viewer for examining Pixar's brick map file format.", 
	  "brickviewer"); 
    
    addSupport(OsType.Windows); 
    addSupport(OsType.MacOS); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1795580258226892536L;

}

