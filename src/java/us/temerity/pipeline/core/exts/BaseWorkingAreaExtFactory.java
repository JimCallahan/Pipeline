// $Id: BaseWorkingAreaExtFactory.java,v 1.3 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   W O R K I N G   A R E A   E X T   F A C T O R Y                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for working area extension factories.
 */
public 
class BaseWorkingAreaExtFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public 
  BaseWorkingAreaExtFactory
  (
   String author, 
   String view      
  )      
  {
    pAuthor = author; 
    pView   = view; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all nodes associated with the operation.
   */ 
  public LinkedList<String> 
  getNodeNames()
  {
    return new LinkedList<String>();
  }

  /**
   * Get the name of the user performing the operation. 
   */ 
  public String 
  getWorkUser()
  {
    return pAuthor; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user which owns the working area.
   */ 
  protected String  pAuthor; 

  /**
   * The name of the user's working area view. 
   */ 
  protected String  pView; 

}



