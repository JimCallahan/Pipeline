// $Id: KPdfEditor.java,v 1.1 2007/04/30 08:20:58 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   K P D F   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The default PDF viewer for KDE. 
 */
public 
class KPdfEditor 
  extends SimpleSingleEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  KPdfEditor()
  {
    super("KPdf", new VersionID("2.2.1"), "Temerity", 
          "The KDE default Portable Document Format (PDF) viwer.",
	  "kpdf"); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5676988982687232990L; 

}
