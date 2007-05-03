// $Id: FoxitEditor.java,v 1.2 2007/05/03 03:26:34 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   F O X I T   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The Foxit Adobe Portable Document Format (PDF) viewer.
 */
public
class FoxitEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  FoxitEditor()
  {
    super("Foxit", new VersionID("2.2.1"), "Temerity", 
	  "The Foxit Portable Document Format (PDF) viewer.",
	  "FoxitReader");

    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3612770324458661723L;

}


