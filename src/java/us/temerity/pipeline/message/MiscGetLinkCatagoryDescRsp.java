// $Id: MiscGetLinkCatagoryDescRsp.java,v 1.1 2004/06/28 23:39:45 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   L I N K   C A T A G O R Y   D E S C   R S P                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a <CODE>MiscGetLinkCatagoryDescRsp</CODE> request.
 */
public
class MiscGetLinkCatagoryDescRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param table
   *   The of link catagory descriptions.
   */ 
  public
  MiscGetLinkCatagoryDescRsp
  (
   TaskTimer timer, 
   TreeMap<String,LinkCatagoryDesc> table
  )
  { 
    super(timer);

    if(table == null) 
      throw new IllegalArgumentException("The link catagory table cannot be (null)!");
    pTable = table;

    Logs.net.finest("MasterMgr.getActiveLinkCatagoryNames():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the table of link catagory descriptions.
   */
  public TreeMap<String,LinkCatagoryDesc>
  getTable() 
  {
    return pTable;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1246474422943899822L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of link catagory descriptions.
   */ 
  private TreeMap<String,LinkCatagoryDesc>  pTable;

}
  
