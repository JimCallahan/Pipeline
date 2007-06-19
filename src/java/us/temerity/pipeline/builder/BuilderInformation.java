package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.ListMap;
import us.temerity.pipeline.MultiMap;
import us.temerity.pipeline.builder.BaseBuilder.ConstructPass;
import us.temerity.pipeline.stages.StageInformation;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   I N F O R M A T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

public 
class BuilderInformation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  BuilderInformation
  (
    boolean usingGui,
    boolean abortOnBadParam,
    MultiMap<String, String> commandLineParams
  )
  {
    pUsingGUI = usingGui;
    pAbortOnBadParam = abortOnBadParam;
    pCommandLineParams = commandLineParams;
    pAllConstructPasses = new LinkedList<ConstructPass>();
    pPassToBuilderMap = new ListMap<ConstructPass, BaseBuilder>();
    pNodesToQueue = new TreeSet<String>();
    pCheckInOrder = new LinkedList<BaseBuilder>();
    pCommandLineParams = new MultiMap<String, String>();
    pCallHierarchy = new LinkedList<BaseBuilder>();
    pStageInformation = new StageInformation();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   Q U E U E   L I S T                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds a node to the list of things to be queued at the end of the builder run.
   */
  public final void 
  addToQueueList
  (
    String nodeName
  )
  {
    pNodesToQueue.add(nodeName);
  }

  /**
   * Removes a node from the list of things to be queued at the end of the builder run.
   */
  public final void 
  removeFromQueueList
  (
    String nodeName
  )
  {
    pNodesToQueue.remove(nodeName);
  }
  
  /**
   * Clears the list of things to be queued at the end of the builder run.
   */
  public final void 
  clearQueueList()
  {
    pNodesToQueue.clear();
  }

  /**
   * Returns the list of things to be queued at the end of the builder run.
   */
  public final TreeSet<String> 
  getQueueList()
  {
    return new TreeSet<String>(pNodesToQueue);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final boolean
  addConstuctPass
  (
    ConstructPass pass,
    BaseBuilder builder
  )
  {
    if (pPassToBuilderMap.containsKey(pass))
      return false;
    pPassToBuilderMap.put(pass, builder);
    pAllConstructPasses.add(pass);
    return true;
  }
  
  /**
   * Gets the Builder that is associated with a given ConstructPass.
   */
  public final BaseBuilder
  getBuilderFromPass
  (
    ConstructPass pass
  ) 
  {
    return pPassToBuilderMap.get(pass);
  }
  
  public final LinkedList<ConstructPass>
  getAllConstructPasses()
  {
    return new LinkedList<ConstructPass>(pAllConstructPasses);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N   L I S T                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  public void
  addToCheckinList
  (
    BaseBuilder builder
  )
  {
    pCheckInOrder.add(builder);
  }
  
  public List<BaseBuilder>
  getCheckinList()
  {
    return Collections.unmodifiableList(pCheckInOrder);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M M A N D   L I N E   P A R A M S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns a {@link MultiMap} of all the command line parameters.
   * <p>
   * The first level in the MultiMap is made up of the names of all the builders, the second
   * level is all the parameter names, and every level after that (if they exist) are keys
   * into Complex Parameters.  Values are stored in the leaf nodes.
   */
  public final MultiMap<String, String>
  getCommandLineParams()
  {
    return pCommandLineParams;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  C A L L   H I E R A R C H Y                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  public int
  getCallHierarchySize()
  {
    return pCallHierarchy.size();
  }
  
  public BaseBuilder
  pollCallHierarchy()
  {
    return pCallHierarchy.poll();
  }
  
  public void
  addToCallHierarchy
  (
    BaseBuilder builder
  )
  {
    pCallHierarchy.addFirst(builder);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  public final boolean
  usingGui()
  {
    return pUsingGUI;
  }
  
  public final boolean
  abortOnBadParam()
  {
    return pAbortOnBadParam;
  }
  
  public final StageInformation
  getStageInformation()
  {
    return pStageInformation;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A list of all the ConstructPasses 
   */
  private LinkedList<ConstructPass> pAllConstructPasses; 
  
  /**
   * A mapping of each ConstructPass to the BaseBuilder that created it.
   */
  private ListMap<ConstructPass, BaseBuilder> pPassToBuilderMap; 

  /**
   * A list of nodes names that need to be queued.
   */
  private TreeSet<String> pNodesToQueue;
  
  private MultiMap<String, String> pCommandLineParams; 
  
  /**
   * Is this Builder in GUI mode.
   */
  private boolean pUsingGUI = false;
  
  private boolean pAbortOnBadParam = false;
  
  private LinkedList<BaseBuilder> pCheckInOrder;
  
  private LinkedList<BaseBuilder> pCallHierarchy;
  
  private StageInformation pStageInformation;
  
}
