// $Id: NodeGetWorkingAreasRsp.java,v 1.8 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   W O R K I N G   A R E A S   R S P                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the table of current working area authors and views.
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

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getWorkingAreas():\n  " + getTimer());
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
  
