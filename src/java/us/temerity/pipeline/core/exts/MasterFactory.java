// $Id: MasterFactory.java,v 1.1 2007/06/19 22:05:03 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   F A C T O R Y                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class for master manager extension plugin factories.
 */
public 
interface MasterFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all nodes associated with the operation.
   */ 
  public LinkedList<String> 
  getNodeNames(); 

}



