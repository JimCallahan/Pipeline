// $Id: ExecPath.java,v 1.3 2004/02/25 02:59:28 jim Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E X E C   P A T H                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A list of directories used to resolve the absolute file system path to an executable. <P>
 * 
 * @see SubProcess
 */
public
class ExecPath
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct from a <CODE>String</CODE> containing a colon seperated list of absolute 
   * paths to file system directories. Typically, the <CODE>path</CODE> originates from
   * the environmental variable PATH in a Toolset.
   * 
   * @param path [<B>in</B>] 
   *   The execution search path.  
   */ 
  public
  ExecPath
  (
   String path   
  ) 
  {
    if(path == null) 
      throw new IllegalArgumentException("The path cannot be (null)!");

    if(path.length() == 0) 
      throw new IllegalArgumentException("The path cannot be empty!");

    String dirs[] = path.split(":");
    pDirs = new ArrayList<File>(dirs.length);
    
    int wk;
    for(wk=0; wk<dirs.length; wk++) {
      File dir = new File(dirs[wk]);

      if(!dir.isDirectory()) 
	throw new IllegalArgumentException
	  ("A component of path (" + dir + ") was NOT a directory!");

      if(!dir.isAbsolute()) 
	throw new IllegalArgumentException
	  ("A component of path (" + dir + ") was NOT absolute!");
	
      pDirs.add(dir);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the list of directories which make up the execution path. 
   */ 
  public ArrayList<File>
  getDirectories() 
  {
    return new ArrayList<File>(pDirs);
  }
  

  /**
   * Resolve the absolute filesystem path to an executable using the list of directories 
   * which make up this instance and a simple program name.  Behaves in a similar manner
   * as the UNIX shell command <I>which(1)</I>.
   * 
   * @param program [<B>in</B>] 
   *   The name of the program to resolve.
   * 
   * @return
   *   The absolute filename of the program executable or <CODE>null</CODE> if the given 
   *   program cannot be resolved.
   */
  public File
  which
  (
   String program  
  ) 
  {
    if(program == null) 
      throw new IllegalArgumentException("The program name cannot be (null)!");

    for(File dir : pDirs) {
      File file = new File(dir, program);
      if(file.exists()) 
	return file;
    }
    
    return null;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The list of directories which make up the path. 
   */ 
  private ArrayList<File> pDirs;  
  
}



