// $Id: FoxitEditor.java,v 1.1 2007/06/17 15:34:40 jim Exp $

package us.temerity.pipeline.plugin.FoxitEditor.v2_0_9;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   F O X I T   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The Foxit Adobe Portable Document Format (PDF) viewer.
 */
public
class FoxitEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  FoxitEditor()
  {
    super("Foxit", new VersionID("2.0.9"), "Temerity", 
	  "The Foxit Portable Document Format (PDF) viewer.",
	  "FoxitReader");

    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6841045046556564077L;

}


