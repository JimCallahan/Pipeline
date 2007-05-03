// $Id: AcroreadEditor.java,v 1.2 2007/05/03 03:26:34 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   A C R O R E A D   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The Adobe Portable Document Format (PDF) viewer.
 */
public
class AcroreadEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AcroreadEditor()
  {
    super("Acroread", new VersionID("2.2.1"), "Temerity", 
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

  private static final long serialVersionUID = -3708653365446832856L;

}


