// $Id: NodeGetWorkingAreasRsp.java,v 1.4 2004/06/08 20:05:11 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   W O R K I N G   A R E A S   R S P                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a <CODE>NodeGetWorkingAreasReq</CODE> request.
 */
public
class NodeGetWorkingAreasRsp
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
   *   The table of the names of the working area views indexed by author user name.
   */ 
  public
  NodeGetWorkingAreasRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeSet<String>> table
  )
  { 
    super(timer);

    if(table == null) 
      throw new IllegalArgumentException("The table cannot be (null)!");
    pTable = table;

    Logs.net.finest("MasterMgr.getWorkingAreas():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the table of working area view names indexed by author user name.
   */
  public TreeMap<String,TreeSet<String>>
  getTable() 
  {
    return pTable;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7065843241551631411L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of working area view names indexed by author user name.
   */ 
  private TreeMap<String,TreeSet<String>>  pTable;

}
  
