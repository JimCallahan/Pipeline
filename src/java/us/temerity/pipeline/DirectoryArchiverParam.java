// $Id: DirectoryArchiverParam.java,v 1.1 2004/11/11 00:40:09 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D I R E C T O R Y   A R C H I V E R   P A R A M                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An Archiver parameter with a File value which names a directory.
 */
public 
class DirectoryArchiverParam
  extends FileParam
  implements ArchiverParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  public 
  DirectoryArchiverParam() 
  {
    super();
  }

  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the parameter.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  public
  DirectoryArchiverParam
  (
   String name,  
   String desc, 
   File value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7849980565385195672L;

}



