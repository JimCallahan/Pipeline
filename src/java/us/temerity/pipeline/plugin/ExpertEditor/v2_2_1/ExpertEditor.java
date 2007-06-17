// $Id: ExpertEditor.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.ExpertEditor.v2_2_1;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   E X P E R T   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The eXPert Portable Document Format (PDF) viewer.
 */
public
class ExpertEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ExpertEditor()
  {
    super("eXPert", new VersionID("2.2.1"), "Temerity", 
	  "The eXPert Portable Document Format (PDF) viewer.",
	  "vspdfreader");

    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3316213120363963144L;

}


