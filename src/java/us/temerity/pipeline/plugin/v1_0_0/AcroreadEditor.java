// $Id: AcroreadEditor.java,v 1.2 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   A C R O R E A D   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The Adobe Portable Document Format (PDF) viewer.
 */
public
class AcroreadEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AcroreadEditor()
  {
    super("Acroread", new VersionID("1.0.0"), "Temerity", 
	  "The Adobe Portable Document Format (PDF) viewer.",
	  "acroread");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2396414608583621951L;

}


