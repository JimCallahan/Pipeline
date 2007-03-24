// $Id: TextDiffComparator.java,v 1.1 2007/03/24 03:45:34 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E X T   D I F F   C O M P A R A T O R                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A graphical text file comparator and merge tool. <P> 
 * 
 * The actual application used depends on the operating system the Comparator is run unde.
 * On Unix and MacOS systems, the <A href="http://xxdiff.sourceforge.net/local/index.html">
 * xxdiff</A> application is used.  While on Windows systems, where xxdiff(1) is unavailable
 * the similar <A href="http://winmerge.org">WinMerge</A> application is used.  Both of these
 * applications are not distributed by default with the OS but are freely available.
 */
public
class TextDiffComparator
  extends BaseComparator
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TextDiffComparator()
  {
    super("TextDiff", new VersionID("2.2.1"), "Temerity",
	  "A graphical text file comparator and merge tool.",
	  "xxdiff");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether Pipeline programs which launch Comparator plugins should ignore the exit code 
   * returned by the Subprocess created in the {@link #prep prep} method. <P> 
   */ 
  public boolean
  ignoreExitCode() 
  {
    return true;
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
      return "WinMerge.exe";
    return super.getProgram();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1361002776907493804L;

}


