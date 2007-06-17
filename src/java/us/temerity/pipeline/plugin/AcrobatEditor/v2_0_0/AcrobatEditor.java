// $Id: AcrobatEditor.java,v 1.1 2007/06/17 15:34:37 jim Exp $

package us.temerity.pipeline.plugin.AcrobatEditor.v2_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   A C R O B A T   E D I T O R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Adobe Acrobat PDF document editor.
 */
public
class AcrobatEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AcrobatEditor()
  {
    super("Acrobat", new VersionID("2.0.0"), "Temerity",
	  "Adobe Acrobat PDF document editor.",           
	  "Adobe Acrobat 7.0 Professional");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1687959472551762511L;

}


