// $Id: AcroreadEditor.java,v 1.1 2004/02/23 23:48:31 jim Exp $

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
    super("acroread", 
	  "The Adobe Portable Document Format (PDF) viewer." );
  }

}


