// $Id: AcroreadEditor.java,v 1.3 2004/03/07 02:35:53 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.SingleEditor; 

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
    super("Acroread", 
	  "The Adobe Portable Document Format (PDF) viewer.",
	  "acroread");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2396414608583621951L;

}


