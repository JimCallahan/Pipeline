// $Id: PreviewEditor.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.PreviewEditor.v1_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   P R E V I E W   E D I T O R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The default Mac OS X image viewer.
 */
public
class PreviewEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PreviewEditor()
  {
    super("Preview", new VersionID("1.0.0"), "Temerity", 
	  "The Mac OS X Image Viewer.", 
	  "Preview");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8082491926849396497L;

}


