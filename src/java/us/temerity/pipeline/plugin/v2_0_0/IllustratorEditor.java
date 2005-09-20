// $Id: IllustratorEditor.java,v 1.1 2005/09/20 04:18:28 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   I L L U S T R A T O R   E D I T O R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The Adobe Illustrator vector graphics editor.
 */
public
class IllustratorEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  IllustratorEditor()
  {
    super("Illustrator", new VersionID("2.0.0"), "Temerity",
	  "Adobe Illustrator vector graphics editor.", 
	  "Adobe Illustrator");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1719802467906767180L;

}


