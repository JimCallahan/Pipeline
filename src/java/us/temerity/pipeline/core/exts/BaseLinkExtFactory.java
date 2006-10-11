// $Id: BaseLinkExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   L I N K   E X T   F A C T O R Y                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for extension factories related to node links.
 */
public 
class BaseLinkExtFactory
  extends BaseWorkingAreaExtFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param target 
   *   The fully resolved name of the downstream node.
   * 
   * @param source 
   *   The fully resolved name of the upstream node.
   */ 
  public 
  BaseLinkExtFactory
  (
   String author, 
   String view, 
   String target, 
   String source
  )      
  {
    super(author, view);

    pTarget = target; 
    pSource = source; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the downstream node.
   */ 
  protected String  pTarget; 

  /**
   * The fully resolved name of the upstream node.
   */ 
  protected String  pSource; 

}



