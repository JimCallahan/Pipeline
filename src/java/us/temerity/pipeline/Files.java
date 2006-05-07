// $Id: Files.java,v 1.1 2006/05/07 21:24:04 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E S                                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Additional useful static methods for manipulating File objects.
 */
public
class Files
{
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Recursively delete all files/directories under the given root.
   */ 
  public static void 
  deleteAll
  (
   File root
  ) 
  {
    if((root == null) || !root.exists()) 
      return;

    if(root.isDirectory()) {
      File files[] = root.listFiles();
      int wk;
      for(wk=0; wk<files.length; wk++)
	deleteAll(files[wk]);
    }

    root.delete();
  }
}
