// $Id: TemplateBuilder.java,v 1.2 2008/09/29 19:02:17 jim Exp $

package us.temerity.pipeline.builder.v2_4_1;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.execution.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   B U I L D E R                                                        */
/*------------------------------------------------------------------------------------------*/


/**
 * Builder to read a template node network, built using the v2.4.1 Task system, and 
 * create an instance of it for particular project. 
 * <p>
 * 
 */
public 
class TemplateBuilder
  extends TaskBuilder
{
  public
  TemplateBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    String startNode,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> maps
  ) 
    throws PipelineException
  {
    super("TemplateBuilder",
          "Builder to read a template node network, built using the v2.4.1 Task system, " +
          "and create an instance of it for particular project.",
          mclient, qclient, builderInformation, null);
    
    pStartNode = startNode;
    pReplacements = new TreeMap<String, String>();
    if (stringReplacements != null)
      pReplacements.putAll(stringReplacements);
    
    pMaps = new TreeMap<String, ArrayList<TreeMap<String,String>>>();
    if (maps != null)
      pMaps.putAll(maps);
    
    pAnnotCache = new TreeMap<String, TreeMap<String,BaseAnnotation>>();
    
    addCheckinWhenDoneParam();
    
    addSetupPass(new InformationPass());
    addConstructPass(new BuildPass());
    addConstructPass(new FinalizePass());
    
    PassLayoutGroup rootLayout = new PassLayoutGroup("Root", "Root Layout");
    
    AdvancedLayoutGroup layout = 
      new AdvancedLayoutGroup
      ("Builder Information", 
       "The pass where all the basic information about the asset is collected " +
       "from the user.", 
       "BuilderSettings", 
       true);
    layout.addEntry(1, aUtilContext);
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckinWhenDone);
    layout.addEntry(1, aActionOnExistence);
    layout.addEntry(1, aReleaseOnError);
    
    rootLayout.addPass(layout.getName(), layout);
    setLayout(rootLayout);
  }
  
  @Override
  public String
  getCheckInMessage()
  {
    String message = "Node tree created using the (" + getName() + ") Builder.\n";
    message += "It was based off the template anchored by ()" + pSubmitNode;
    return message;
  }
  
  /**
   * Search upstream from the specified node until all leaf Task nodes are found.
   */
  private void
  getLeafNodes
  (
    String startNode
  )
    throws PipelineException
  {
    NodeStatus status = pClient.status(new NodeID(getAuthor(), getView(), startNode), 
                                       true, DownstreamMode.None);
    
    findUpstreamNodes(null, status);
  }
  
  /**
   * Search downstream from an edit node until the Submit and Approve nodes are found.
   *
   * @param editNode
   *   The edit node of the Task.  This can be found with the 
   *   {@link #getEditNode(String, MasterMgrClient)} method.
   * @return A String Array containing two values. The first entry is the Submit node and the
   *   second is the Approve Node.
   * @throws PipelineException If either the Submit or the Approve node associated with 
   *    the task cannot be found.
   */
  private String[]
  getSubmitAndApproveNodes
  (
    String editNode
  )
    throws PipelineException
  {
    pSubmitNode = null;
    pApprovalNode = null;
    
    NodeStatus status = pClient.status(new NodeID(getAuthor(), getView(), editNode), 
                                       true, DownstreamMode.All);  // Correct DownstreamMode?

    findDownstreamNodes(status);
    
    if (pSubmitNode == null)
      throw new PipelineException
        ("Unable to find the submit node for the task annotation on the node " +
        "(" + editNode + ")!"); 
    
    if (pApprovalNode == null)
      throw new PipelineException
        ("Unable to find the approve node for the task annotation on the node " +
        "(" + editNode + ")!"); 
    
    String toReturn[] = new String[2];
    toReturn[0] = pSubmitNode;
    toReturn[1] = pApprovalNode;
    return toReturn;
  }
  
  /**
   * Recursively finds all the nodes upstream of the current one which are a member
   * of the same task as the original node.
   * 
   * @param status
   *   The status of the current node.
   */
  private void
  findDownstreamNodes
  (
    NodeStatus status
  )
    throws PipelineException
  {
    if (pSubmitNode != null && pApprovalNode != null)
      return;
    String nodeName = status.getName();
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(nodeName);
    if (annots != null && !annots.isEmpty()) {
      boolean taskMatch = false;
      for ( String aName : annots.keySet()) {
        BaseAnnotation annot = annots.get(aName);
        String purpose = lookupPurpose(nodeName, aName, annot);
        taskMatch = doesTaskMatch(nodeName, aName, annot);
        if (taskMatch) {
          if (purpose.equals(NodePurpose.Submit.toString())) {
            pSubmitNode = nodeName;
            return;
          }
          if (purpose.equals(NodePurpose.Approve.toString())) {
            pApprovalNode = nodeName;
            return;
          }
        }
      }
      if (taskMatch) {
        Collection<NodeStatus> stati = status.getTargets();
        for (NodeStatus parentStatus : stati) {
          findDownstreamNodes(parentStatus);
          if (pSubmitNode != null && pApprovalNode != null)
            return;
        }
      }
    }
  }
  
  /**
   * Recursively finds all the nodes underneath the current one which are a member
   * of the same task as the original node.
   * 
   * @param status
   *   The status of the current node.
   */
  private void
  findUpstreamNodes
  (
    String parent,
    NodeStatus status
  )
    throws PipelineException
  {
    String nodeName = status.getName();
    // Easy out if we have already found this node and established that it belongs.
    if (pNodesDependingOnMe.containsKey(nodeName)) {
      pNodesIDependedOn.put(parent, nodeName);
      pNodesDependingOnMe.put(nodeName, parent);
      return;
    }
    TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(nodeName);
    TreeSet<String> purposes = new TreeSet<String>();
    if (annots != null && !annots.isEmpty()) {
      boolean taskMatch = false;
      for ( String aName : annots.keySet()) {
        BaseAnnotation annot = annots.get(aName);
        taskMatch = doesTaskMatch(nodeName, aName, annot);
        purposes.add(lookupPurpose(nodeName, aName, annot));
      }
      if (taskMatch) {
        if (parent != null) {
          pNodesIDependedOn.put(parent, nodeName);
          pNodesDependingOnMe.put(nodeName, parent);
        }
        if (purposes.contains(NodePurpose.Edit.toString()))
          pEditNodes.add(nodeName);
        Collection<NodeStatus> stati = status.getSources();
        for (NodeStatus childStatus : stati) {
          findUpstreamNodes(nodeName, childStatus);
        }
      }
      else { //if the task doesn't match, it better be a Product node.
        if (!purposes.contains(NodePurpose.Product.toString()))
          throw new PipelineException
            ("The node ("+ nodeName + ") connected to this network belongs to a different " +
             "task, but is not a Product node.");
        pProductNodes.add(nodeName);
      }
    }//if (annots != null && !annots.isEmpty()) {
    else { //If there are no task annotations/ {
      pProductNodes.add(nodeName);
    }
  }
  
  /**
   * Get the Task Annotations on the given node.  
   *
   * @param name
   *   The name of the node.
   * @return
   *   A TreeMap of Task Annotations indexed by annotation name or 
   *   <code>null</code> if none exists.
   */
  @Override
  protected TreeMap<String, BaseAnnotation>
  getTaskAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = pAnnotCache.get(name);
    if (annots == null) {
      annots = getMasterMgrClient().getAnnotations(name);
      pAnnotCache.put(name, annots);
    }
    TreeMap<String, BaseAnnotation> toReturn = null;
      for(String aname : annots.keySet()) {
        if(aname.equals("Task") || aname.startsWith("AltTask")) {
          if (toReturn == null)
            toReturn = new TreeMap<String, BaseAnnotation>();
          BaseAnnotation tannot = annots.get(aname);
          toReturn.put(aname, tannot);
        }
      }
   return toReturn;
  }
  
  /**
   * Get the Annotations on the given node.  
   *
   * @param name
   *   The name of the node.
   * @return
   *   A TreeMap of Annotations indexed by annotation name or 
   *   <code>null</code> if none exists.
   */
  protected TreeMap<String, BaseAnnotation>
  getAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = pAnnotCache.get(name);
    if (annots == null) {
      annots = getMasterMgrClient().getAnnotations(name);
      pAnnotCache.put(name, annots);
    }
   return annots;
  }
  
  private boolean
  doesTaskMatch
  (
    String nodeName,
    String aName,
    BaseAnnotation annot
  )
    throws PipelineException
  {
    String projectName = lookupProjectName(nodeName, aName, annot);
    String taskName = lookupTaskName(nodeName, aName, annot);
    String taskType = lookupTaskType(nodeName, aName, annot);
    if (pTaskName.equals(taskName) && 
        pProjectName.equals(projectName) &&
        pTaskType.equals(taskType)) 
      return true;
    return false;
  }
  
  
  protected String
  stringReplace
  (
    String source,
    TreeMap<String, String> stringReplacements
  )
  {
    String toReturn = source;
    for (String pattern : stringReplacements.keySet())
      toReturn = toReturn.replaceAll(pattern, stringReplacements.get(pattern));
    return toReturn;
  }
  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class InformationPass
    extends SetupPass
  {
    public 
    InformationPass()
    {
      super("Information Pass", 
            "Information pass for the TemplateBuilder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      //grr, can't forget this call or stuff don't work.
      getStageInformation().setDoAnnotations(true);
      
      TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(pStartNode);
      if (annots == null || annots.isEmpty())
        throw new PipelineException
          ("There were no Task Annotations on the node (" + pStartNode + ")");
      String aName = annots.firstKey();
      BaseAnnotation annot = annots.get(aName);
      pProjectName = lookupProjectName(pStartNode, aName, annot);
      pTaskName = lookupTaskName(pStartNode, aName, annot);
      pTaskType = lookupTaskType(pStartNode, aName, annot);
      
      pEditNodes = new TreeSet<String>();
      pProductNodes = new TreeSet<String>();
      
      pNodesIDependedOn = new MappedSet<String, String>();
      pNodesDependingOnMe = new MappedSet<String, String>();
      
      getLeafNodes(pStartNode);
      getSubmitAndApproveNodes(pEditNodes.first());
      getLeafNodes(pSubmitNode);
      getLeafNodes(pApprovalNode);
      
      pNodesToBuild = new TreeSet<String>();
      pNodesToBuild.addAll(pNodesDependingOnMe.keySet());
      pNodesToBuild.addAll(pNodesIDependedOn.keySet());
      
      pFinalizableStages = new ArrayList<FinalizableStage>();
    }
    
    private static final long serialVersionUID = -9082066996485427448L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected
  class BuildPass
    extends ConstructPass
  {
    public
    BuildPass()
    {
      super("BuildPass", "Pass with makes all the nodes.");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      acquireProducts();
      
      while (!pNodesToBuild.isEmpty() ) {
        String toBuild = findNodeToBuild();
        System.out.println("template: " + toBuild);
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), toBuild);
        TreeMap<String, BaseAnnotation> annots = getAnnotations(toBuild);
        TreeSet<String> maps = new TreeSet<String>();
        for (String aName : annots.keySet()) {
          BaseAnnotation annot = annots.get(aName);
          if (aName.startsWith("TemplateMap")) {
            String mapName = (String) annot.getParamValue(aMapName);
            maps.add(mapName);
          }
        }
        if (maps.size() == 0) { //no maps, just do a straight build
          makeNode(toBuild, mod, pReplacements, pMaps);
        }
        else { //uh-oh, there are maps!
          mapLoop(toBuild, mod, maps, pReplacements, pMaps);
        }

        pNodesToBuild.remove(toBuild);
        TreeSet<String> targets = pNodesDependingOnMe.get(toBuild);
        if (targets != null) {
          for (String target : targets ) {
            TreeSet<String> temp = pNodesIDependedOn.get(target);
            temp.remove(toBuild);
            if (temp.isEmpty())
              pNodesIDependedOn.remove(target);
            else
              pNodesIDependedOn.put(target, temp);
          }
        }
      } // while (!pNodesToBuild.isEmpty() )
      {
        String submit = stringReplace(pSubmitNode, pReplacements);
        String approve = stringReplace(pApprovalNode, pReplacements);
        addToQueueList(submit);
        addToCheckInList(submit);
        addToQueueList(approve);
        addToCheckInList(approve);
      }
    }

    private void 
    mapLoop
    (
      String toBuild,
      NodeMod mod,
      TreeSet<String> mapList,
      TreeMap<String, String> replace,
      TreeMap<String, ArrayList<TreeMap<String, String>>> maps
    )
      throws PipelineException
    {
      String currentMap = mapList.pollFirst();
      ArrayList<TreeMap<String, String>> values = maps.get(currentMap);
      
      if (values == null || values.isEmpty()) 
        throw new PipelineException
          ("The map (" + currentMap + ") specified on (" + toBuild + ") has no values " +
           "defined for it.");
      
      for (TreeMap<String, String> mapEntry : values) {
        TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
        newReplace.putAll(mapEntry);
        TreeMap<String, ArrayList<TreeMap<String, String>>> newMaps = 
          new TreeMap<String, ArrayList<TreeMap<String,String>>>(maps);
        ArrayList<TreeMap<String, String>> newStuff = new ArrayList<TreeMap<String,String>>();
        newStuff.add(new TreeMap<String, String>(mapEntry));
        newMaps.put(currentMap, newStuff);
        
        if (mapList.isEmpty()) {  //bottom of the recursion
          makeNode(toBuild, mod, newReplace, newMaps);
        }
        else {
          mapLoop(toBuild, mod, mapList, newReplace, newMaps);
        }
      }
    }

    private void 
    makeNode
    (
      String toBuild,
      NodeMod mod,
      TreeMap<String, String> replace,
      TreeMap<String, ArrayList<TreeMap<String, String>>> maps
    )
      throws PipelineException
    {
      TemplateStage stage = 
        TemplateStage.getTemplateStage
        (mod, getAnnotations(toBuild), getStageInformation(), pContext, pClient, 
         replace, maps, pAnnotCache);
      System.out.println("building: " + stage.getNodeName());

      if (stage.build())
        if (stage.needsFinalization())
          pFinalizableStages.add(stage);
    }
    
    private String
    findNodeToBuild()
      throws PipelineException
    {
      for (String node : pNodesToBuild) {
        if (pNodesIDependedOn.get(node) == null)
          return node;
      }
      throw new PipelineException
        ("There are no nodes which is is possible to construct.  " +
         "This state should be imposible with any node network I can think of.");
    }
    
    private void
    acquireProducts()
     throws PipelineException
    {
      for (String product : pProductNodes) {
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), product);
        String realProduct = stringReplace(product, pReplacements);
        if (!nodeExists(realProduct))
          throw new PipelineException("Needed source node (" + realProduct + ") does not exist.");
        if (mod.isLocked())
          lockLatest(realProduct);
        else
          frozenStomp(realProduct);
      }
    }

    private static final long serialVersionUID = -8757997441040344237L;
  }
  
  protected
  class FinalizePass
    extends ConstructPass
  {
    public
    FinalizePass()
    {
      super("FinalizePass", "Pass with finalizes all the nodes.");
    }
    
    @Override
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> regenerate = new LinkedList<String>();

      for(FinalizableStage stage : pFinalizableStages) 
        regenerate.add(stage.getNodeName());

      return regenerate;
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      for(FinalizableStage stage : pFinalizableStages) 
        stage.finalizeStage();
    }    
    private static final long serialVersionUID = -2948200152727649296L;
  }
  
  public static void 
  main(
    String[] args
  ) 
    throws PipelineException
  {
    PluginMgrClient.init();
    
    TreeMap<String, String> subs = new TreeMap<String, String>();
    subs.put("TEMPLATE", "templ");
    subs.put("ASSET", "horse");
    subs.put("TYPE", "prop");
    
    
    TemplateBuilder builder = new TemplateBuilder
      (new MasterMgrClient(), new QueueMgrClient(), 
       new BuilderInformation(false, true, true, new MultiMap<String, String>()), 
       "/projects/TEMPLATE/assets/TYPE/ASSET/model/submit/ASSET_model_submit", subs, null);
    
    GUIExecution exec = new GUIExecution(builder);
    exec.run();
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3515581617122035575L;

  public static final String aMapName = "MapName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  private String pStartNode;
  
  private TreeMap<String, String> pReplacements;
  
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pMaps;
  
  private TreeMap<String, TreeMap<String, BaseAnnotation>> pAnnotCache;
  
  /** 
   * The task identifiers.
   */ 
  private String pProjectName;
  private String pTaskName;
  private String pTaskType;
  
  private TreeSet<String> pEditNodes;
  private String pSubmitNode;
  private String pApprovalNode;
  private TreeSet<String> pProductNodes;
  
  private ArrayList<FinalizableStage> pFinalizableStages;
  
  private TreeSet<String> pNodesToBuild;
  
  private MappedSet<String, String> pNodesIDependedOn;
  private MappedSet<String, String> pNodesDependingOnMe;
}
