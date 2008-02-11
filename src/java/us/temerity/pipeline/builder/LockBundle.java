package us.temerity.pipeline.builder;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L O C K   B U N D L E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Data structure for use with Builders that provides a list of nodes to lock and a list of
 * nodes that have to be checked-in after the first group of nodes are locked.
 * <p>
 * Lock Bundles are executed to correctly setup networks that need to have portions of then
 * locked. Lock Bundles contain two lists of nodes, a list of nodes to lock and a list of
 * nodes to check-in. First, all the nodes in the lock list are locked to the latest version.
 * Then, all the nodes that need to be checked-in are queued. If any jobs are generated, the
 * Builder waits for them to finish and then checks-in all of the nodes.
 * 
 */
public 
class LockBundle
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Creates a new Lock Bundle.
   */
  public
  LockBundle()
  {
    pNodesToLock = new LinkedList<String>();
    pNodesToCheckin = new LinkedList<String>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds a node which will need to be locked to the bundle.
   * 
   * @param node
   *   The name of the node.
   */
  public void
  addNodeToLock
  (
    String node  
  )
  {
    if (!pNodesToLock.contains(node))
      pNodesToLock.add(node);
  }
  
  /**
   * Gets all the nodes in the bundle with need to be locked.
   * 
   * @return
   *   The list of nodes to lock.
   */
  public List<String>
  getNodesToLock()
  {
    return Collections.unmodifiableList(pNodesToLock);
  }
  
  /**
   * Adds a node which will need to be checked-in to the bundle.
   * 
   * @param node
   *   The name of the node.
   */
  public void
  addNodeToCheckin
  (
    String node  
  )
  {
    if (!pNodesToCheckin.contains(node))
      pNodesToCheckin.add(node);
  }
  
  /**
   * Gets all the nodes in the bundle with need to be checked-in.
   * 
   * @return
   *   The list of nodes to check-in.
   */
  public List<String>
  getNodesToCheckin()
  {
    return Collections.unmodifiableList(pNodesToCheckin);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private LinkedList<String> pNodesToLock;
  private LinkedList<String> pNodesToCheckin;
}
