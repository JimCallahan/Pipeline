// $Id: PluginUpdateRsp.java,v 1.1 2005/01/15 02:56:32 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   U P D A T E   R S P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link PluginUpdateReq PluginUpdateReq} request.
 */
public
class PluginUpdateRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response. <P> 
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param cycleID
   *   The plugin load cycle sequence identifier of this update.
   * 
   * @param editors
   *   The new or updated Editor plugin class [name, bytes] indexed by class name 
   *   and revision number.
   * 
   * @param actions
   *   The new or updated Action plugin class [name, bytes] indexed by class name 
   *   and revision number.
   * 
   * @param comparators
   *   The new or updated Comparator plugin class [name, bytes] indexed by class name 
   *   and revision number.
   * 
   * @param tools
   *   The new or updated Tool plugin class [name, bytes] indexed by class name 
   *   and revision number.
   * 
   * @param archivers
   *   The new or updated Archiver plugin class [name, bytes] indexed by class name 
   *   and revision number.
   */
  public
  PluginUpdateRsp
  (
   TaskTimer timer, 
   Long cycleID, 
   TreeMap<String,TreeMap<VersionID,Object[]>> editors,
   TreeMap<String,TreeMap<VersionID,Object[]>> actions,
   TreeMap<String,TreeMap<VersionID,Object[]>> comparators,
   TreeMap<String,TreeMap<VersionID,Object[]>> tools,
   TreeMap<String,TreeMap<VersionID,Object[]>> archivers
  )
  { 
    super(timer);

    if(cycleID == null)
      throw new IllegalArgumentException("The plugin load cycle ID cannot (null)!");
    pCycleID = cycleID;

    if(editors == null) 
      throw new IllegalArgumentException("The Editor plugins cannot be (null)!");
    pEditors = editors;

    if(actions == null) 
      throw new IllegalArgumentException("The Action plugins cannot be (null)!");
    pActions = actions;

    if(comparators == null) 
      throw new IllegalArgumentException("The Comparator plugins cannot be (null)!");
    pComparators = comparators;

    if(tools == null) 
      throw new IllegalArgumentException("The Tool plugins cannot be (null)!");
    pTools = tools;

    if(archivers == null) 
      throw new IllegalArgumentException("The Archiver plugins cannot be (null)!");
    pArchivers = archivers;

    if(Logs.net.isLoggable(Level.FINEST)) {
      Logs.net.finest("PluginMgr.update(): cycle " + cycleID + ":\n  " + getTimer());
      Logs.flush();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the plugin load cycle sequence identifier of this update. <P> 
   */
  public Long
  getCycleID() 
  {
    return pCycleID; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the new or updated Editor plugin class [name, bytes] indexed by class name 
   * and revision number.
   */
  public TreeMap<String,TreeMap<VersionID,Object[]>>
  getEditors() 
  {
    return pEditors; 
  }

  /**
   * Gets the new or updated Action plugin class [name, bytes] indexed by class name 
   * and revision number.
   */
  public TreeMap<String,TreeMap<VersionID,Object[]>>
  getActions() 
  {
    return pActions; 
  }

  /**
   * Gets the new or updated Comparator plugin class [name, bytes] indexed by class name  
   * and revision number.
   */
  public TreeMap<String,TreeMap<VersionID,Object[]>>
  getComparators() 
  {
    return pComparators; 
  }

  /**
   * Gets the new or updated Tool plugin class [name, bytes] indexed by class name 
   * and revision number.
   */
  public TreeMap<String,TreeMap<VersionID,Object[]>>
  getTools() 
  {
    return pTools; 
  }

  /**
   * Gets the new or updated Archiver plugin class [name, bytes] indexed by class name 
   * and revision number.
   */
  public TreeMap<String,TreeMap<VersionID,Object[]>>
  getArchivers() 
  {
    return pArchivers; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2558932107540179286L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The plugin load cycle sequence identifier of this update.
   */ 
  private Long  pCycleID; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The new or updated Editor plugin class [name, bytes] indexed by class name 
   * and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Object[]>>  pEditors; 

  /**
   * The new or updated Action plugin class [name, bytes] indexed by class name 
   * and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Object[]>>  pActions; 

  /**
   * The new or updated Comparator plugin class [name, bytes] indexed by class name 
   * and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Object[]>>  pComparators; 

  /**
   * The new or updated Tool plugin class [name, bytes] indexed by class name 
   * and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Object[]>>  pTools; 

  /**
   * The new or updated Archiver plugin class [name, bytes] indexed by class name 
   * and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Object[]>>  pArchivers; 

}
  
