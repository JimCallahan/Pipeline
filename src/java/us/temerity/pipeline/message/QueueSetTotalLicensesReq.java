// $Id: QueueSetTotalLicensesReq.java,v 1.1 2004/07/24 18:28:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S E T   T O T A L   L I C E N S E S   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request set the total number of licenses associated with the named license key. <P> 
 * 
 * @see MasterMgr
 * @see QueueMgr
 */
public
class QueueSetTotalLicensesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param kname
   *   The name of the license key.
   * 
   * @param total 
   *   The total number of licenses.
   */
  public
  QueueSetTotalLicensesReq
  (
   String kname, 
   int total   
  )
  { 
    if(kname == null) 
      throw new IllegalArgumentException
	("The license key name cannot be (null)!");
    pKeyName = kname; 

    if(total < 0) 
      throw new IllegalArgumentException
	("The total number of licenses (" + total + ") must be positive!");      
    pTotal = total;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the license key to remove. 
   */
  public String
  getKeyName() 
  {
    return pKeyName;
  }
  
  /**
   * Gets the total number of licenses.
   */
  public int
  getTotal()
  {
    return pTotal;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6536924424277683993L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the license key to remove.
   */ 
  private String  pKeyName;

  /**
   * The total number of licenses.
   */ 
  private int  pTotal;

}
  
