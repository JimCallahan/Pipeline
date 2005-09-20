// $Id: InDesignEditor.java,v 1.1 2005/09/20 04:18:28 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   I N   D E S I G N   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The Adobe InDesign document editor.
 */
public
class InDesignEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  InDesignEditor()
  {
    super("InDesign", new VersionID("2.0.0"), "Temerity",
	  "Adobe InDesign document editor.", 
	  "Adobe InDesign CS2");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 37274646604704204L;

}


