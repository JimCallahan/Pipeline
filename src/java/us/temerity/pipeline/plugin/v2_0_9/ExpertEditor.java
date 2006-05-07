// $Id: ExpertEditor.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   E X P E R T   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The eXPert Portable Document Format (PDF) viewer.
 */
public
class ExpertEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ExpertEditor()
  {
    super("eXPert", new VersionID("2.0.9"), "Temerity", 
	  "The eXPert Portable Document Format (PDF) viewer.",
	  "vspdfreader");

    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8945144125139170445L;

}


