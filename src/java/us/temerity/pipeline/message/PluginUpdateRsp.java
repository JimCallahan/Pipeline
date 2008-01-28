// $Id: PluginUpdateRsp.java,v 1.9 2008/01/28 11:58:52 jesse Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

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
   *   The new or updated Editor plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   * 
   * @param actions
   *   The new or updated Action plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   * 
   * @param comparators
   *   The new or updated Comparator plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   * 
   * @param tools
   *   The new or updated Tool plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   * 
   * @param annotations
   *   The new or updated Annotation plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   * 
   * @param archivers
   *   The new or updated Archiver plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   * 
   * @param masterExts
   *   The new or updated Master Extension plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   * 
   * @param queueExts
   *   The new or updated Queue Extension plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   *   
   * @param keyChoosers
   *   The new or updated Key Chooser plugin class [name, bytes, supports] 
   *   indexed by class name and revision number.
   */
  public
  PluginUpdateRsp
  (
   TaskTimer timer, 
   Long cycleID, 
   TripleMap<String,String,VersionID,Object[]> editors,
   TripleMap<String,String,VersionID,Object[]> actions,
   TripleMap<String,String,VersionID,Object[]> comparators,
   TripleMap<String,String,VersionID,Object[]> tools,
   TripleMap<String,String,VersionID,Object[]> annotations,
   TripleMap<String,String,VersionID,Object[]> archivers,
   TripleMap<String,String,VersionID,Object[]> masterExts, 
   TripleMap<String,String,VersionID,Object[]> queueExts,
   TripleMap<String,String,VersionID,Object[]> keyChoosers,
   TripleMap<String,String,VersionID,Object[]> builderCollections
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

    if(annotations == null) 
      throw new IllegalArgumentException("The Annotation plugins cannot be (null)!");
    pAnnotations = annotations;

    if(archivers == null) 
      throw new IllegalArgumentException("The Archiver plugins cannot be (null)!");
    pArchivers = archivers;

    if(masterExts == null) 
      throw new IllegalArgumentException("The Master Extension plugins cannot be (null)!");
    pMasterExts = masterExts;

    if(queueExts == null) 
      throw new IllegalArgumentException("The Queue Extension plugins cannot be (null)!");
    pQueueExts = queueExts;
    
    if(keyChoosers == null) 
      throw new IllegalArgumentException("The Key Chooser plugins cannot be (null)!");
    pKeyChoosers = keyChoosers;
    
    if(builderCollections == null) 
      throw new IllegalArgumentException("The Builder Collections plugins cannot be (null)!");
    pBuilderCollections = builderCollections;

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest)) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Finest,
	 "PluginMgr.update(): cycle " + cycleID + ":\n  " + getTimer());
      LogMgr.getInstance().flush();
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
   * Gets the new or updated Editor plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getEditors() 
  {
    return pEditors; 
  }

  /**
   * Gets the new or updated Action plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getActions() 
  {
    return pActions; 
  }

  /**
   * Gets the new or updated Comparator plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getComparators() 
  {
    return pComparators; 
  }

  /**
   * Gets the new or updated Tool plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getTools() 
  {
    return pTools; 
  }

  /**
   * Gets the new or updated Annotation plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getAnnotations() 
  {
    return pAnnotations; 
  }

  /**
   * Gets the new or updated Archiver plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getArchivers() 
  {
    return pArchivers; 
  }

  /**
   * Gets the new or updated Master Extension plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getMasterExts() 
  {
    return pMasterExts; 
  }

  /**
   * Gets the new or updated Queue Extension plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getQueueExts() 
  {
    return pQueueExts; 
  }

  /**
   * Gets the new or updated Key Chooser plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getKeyChoosers() 
  {
    return pKeyChoosers; 
  }
  
  /**
   * Gets the new or updated Builder Collections plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */
  public TripleMap<String,String,VersionID,Object[]>
  getBuilderCollections() 
  {
    return pBuilderCollections; 
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
   * The new or updated Editor plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pEditors; 

  /**
   * The new or updated Action plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pActions; 

  /**
   * The new or updated Comparator plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pComparators; 

  /**
   * The new or updated Tool plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pTools; 

  /**
   * The new or updated Annotation plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pAnnotations; 

  /**
   * The new or updated Archiver plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pArchivers; 

  /**
   * The new or updated Master Extension plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pMasterExts; 

  /**
   * The new or updated Queue Extension plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pQueueExts; 
  
  /**
   * The new or updated Key Choosers plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pKeyChoosers; 
  
  /**
   * The new or updated Builder Collection plugin class [name, bytes, supports] 
   * indexed by class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,Object[]>  pBuilderCollections; 

}
  
