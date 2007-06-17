// $Id: AcroreadEditor.java,v 1.1 2007/06/17 15:34:37 jim Exp $

package us.temerity.pipeline.plugin.AcroreadEditor.v2_0_9;

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
    super("Acroread", new VersionID("2.0.9"), "Temerity", 
	  "The Adobe Portable Document Format (PDF) viewer.",
	  "acroread");

    addSupport(OsType.Windows);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the name of the editor executable.
   */
  public String
  getProgram() 
  {
    if(PackageInfo.sOsType == OsType.Windows) 
      return "AcroRd32.exe";
    return super.getProgram();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7300057679884721467L;

}


