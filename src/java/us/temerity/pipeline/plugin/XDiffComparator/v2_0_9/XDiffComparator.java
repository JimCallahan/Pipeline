// $Id: XDiffComparator.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.XDiffComparator.v2_0_9;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   X D I F F   C O M P A R A T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A graphical text file comparator and merge tool. <P> 
 * 
 * See <A href="http://xxdiff.sourceforge.net/local/index.html">xxdiff</A> on Unix systems 
 * and <A href="http://en.wikipedia.org/wiki/WinDiff">WinDiff</A> on Windows systems for 
 * details.
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
    super("XDiff", new VersionID("2.0.9"), "Temerity",
	  "A graphical text file comparator and merge tool.",
	  "xxdiff");

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
      return "Windiff.exe";
    return super.getProgram();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -429701799824287850L;

}


