// $Id: XDiffComparator.java,v 1.3 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   X D I F F   C O M P A R A T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The graphical text file comparator and merge tool.
 */
public
class XDiffComparator
  extends BaseComparator
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  XDiffComparator()
  {
    super("XDiff", new VersionID("2.0.0"), "Temerity",
	  "The X11 based text file comparator and merge tool.", 
	  "xdiff");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8932693960033574222L;

}


