// $Id: TemplateBuilder.java,v 1.2 2008/10/17 03:36:46 jesse Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   B U I L D E R                                                        */
/*------------------------------------------------------------------------------------------*/


/**
 * Builder to read a template node network, and create an instance of it. 
 * 
 */
public 
class TemplateBuilder
  extends TaskBuilder
{
  /**
   * Constructor for the basic template builder.
   * <p>
   * This is constructed to be as basic as possible.  All you have to pass it is a list of
   * nodes you want to build (as part of the Template Info) and any string replacements you 
   * want and it will do the rest.
   * <p>  
   * There are additional parameters that you can pass in if they are things that you've 
   * already generated as part of constructing the list of nodes to builder.  For example, any
   * sort of node traversal should generate a list of upstream and downstream connections that
   * can be passed into this builder.  This will save time.  If that data is not available,
   * then the template builder will perform a traversal itself and figure that information out. 
   *
   * @param mclient
   *   The instance of the Master Manager that the Builder is going to use.
   * 
   * @param qclient
   *   The instance of the Queue Manager that the Builder is going to use
   * 
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   * 
   * @param templateInfo
   *   Information about what exactly this builder should generated.  If any of the information
   *   is missing, the builder will generate it itself.
   * 
   * @param stringReplacements
   *   A list of all the string replacements to be made for all nodes in the template and on
   *   all product nodes.
   * 
   * @param contexts
   *   A set of the replacements to be made, each indexed by the context name that will trigger 
   *   those replacements to be used.
   * 
   * @throws PipelineException
   */
  public
  TemplateBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    TemplateBuildInfo templateInfo,
    TreeMap<String, String> stringReplacements,
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts
  ) 
    throws PipelineException
  {
    super("TemplateBuilder",
          "Builder to read a template node network, built using the v2.4.1 Task system, " +
          "and create an instance of it for particular project.",
          mclient, qclient, builderInformation, null);
    
    TreeSet<String> nodesToBuild = templateInfo.getNodesToBuild();
    if (nodesToBuild == null || nodesToBuild.isEmpty())
      throw new PipelineException("Empty list of nodes to build passed to Template Builder.");
    pNodesToBuild = new TreeSet<String>(nodesToBuild);
    
    LogMgr.getInstance().log(Kind.Ops, Level.Finer, 
      "The list of nodes being built is " + pNodesToBuild);
    pGenerateDependSets = false;
    
    MappedSet<String, String> nodesDependingOnMe = templateInfo.getNodesDependingOnMe();
    pNodesDependingOnMe = new MappedSet<String, String>();
    if (nodesDependingOnMe == null || nodesDependingOnMe.isEmpty())
      pGenerateDependSets = true;
    else
      pNodesDependingOnMe.putAll(nodesDependingOnMe);
    
    MappedSet<String, String> nodesIDependedOn = templateInfo.getNodesIDependedOn();
    pNodesIDependedOn = new MappedSet<String, String>();
    if (nodesIDependedOn == null || nodesIDependedOn.isEmpty())
      pGenerateDependSets = true;
    else
      pNodesIDependedOn.putAll(nodesIDependedOn);
    
    // Is this stringent a condition needed for product nodes?  not sure.
    TreeSet<String> products = templateInfo.getProductNodes();
    pProductNodes = new TreeSet<String>();
    if (products == null || products.isEmpty())
      pGenerateDependSets = true;
    else
      pProductNodes.addAll(products);
    
    DoubleMap<String, String, TreeSet<String>> productContexts = templateInfo.getProductContexts();
    pProductContexts = new DoubleMap<String, String, TreeSet<String>>();
    if (productContexts == null) 
      pGenerateDependSets = true;
    else
      pProductContexts.putAll(productContexts);
    
    pTemplateInfo = templateInfo;
    
    pReplacements = new TreeMap<String, String>();
    if (stringReplacements != null)
      pReplacements.putAll(stringReplacements);
    LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
      "The list of top-level string replacements: " + pReplacements);

    pContexts = new TreeMap<String, ArrayList<TreeMap<String,String>>>();
    if (contexts != null)
      pContexts.putAll(contexts);
    
    LogMgr.getInstance().log(Kind.Ops, Level.Finest, 
      "The list of contexts to apply to this template: " + pContexts);
    
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
//    message += "It was based off the template anchored by ()" + pSubmitNode;
    return message;
  }
  
  
  /**
   * Return a list of all the contexts that the source node is identified as belonging
   * to on the target node.
   * @param target
   *   The node that is part of the template that contains the TemplateContextLink
   *   Annotations.
   * @param src
   *   The node outside the template that may be identified in the TemplateContextLink
   *   Annotations.
   * @return
   *   The set of contexts the src node is mentioned as belonging to. 
   */
  protected TreeSet<String>
  getContextLinks
  (
    String target,
    String src
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = getAnnotations(target);
    TreeSet<String> toReturn = new TreeSet<String>();
    for (String aName : annots.keySet()) {
      if (aName.startsWith("TemplateContextLink")) {
        BaseAnnotation annot = annots.get(aName);
        String aSrc = (String) annot.getParamValue(aLinkName);
        if (aSrc.equals(src))
          toReturn.add((String) annot.getParamValue(aContextName));
      }
    }
    return toReturn;
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
    TreeMap<String, BaseAnnotation> annots = getAnnotations(name);
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
      
      if (pGenerateDependSets) {
        LogMgr.getInstance().log(Kind.Ops, Level.Finer, 
          "Generating the product and dependency nodes.");

        pProductNodes = new TreeSet<String>();
        pProductContexts = new DoubleMap<String, String, TreeSet<String>>();
        pNodesDependingOnMe = new MappedSet<String, String>();
        pNodesIDependedOn = new MappedSet<String, String>();
        
        TreeMap<String, NodeStatus> statusCache = new TreeMap<String, NodeStatus>();
        for (String node : pNodesToBuild) {
          NodeStatus stat = statusCache.get(node);
          if (stat == null) {
            stat = pClient.status(new NodeID(getAuthor(), getView(), node), true, 
              DownstreamMode.WorkingOnly);
            mineStatus(stat, statusCache);
          }
          for (String src : stat.getSourceNames()) {
            if (pNodesToBuild.contains(src))
              pNodesIDependedOn.put(node, src);
            else {
              TreeSet<String> contexts = getContextLinks(node, src);
              if (!contexts.isEmpty())
                pProductContexts.put(src, node, contexts);
              pProductNodes.add(src);
            }
          }
          for (String trgt : stat.getTargetNames()) {
            if (pNodesToBuild.contains(trgt))
              pNodesDependingOnMe.put(node, trgt);
          }
        }
      }
      pFinalizableStages = new ArrayList<FinalizableStage>();
    }
    
    private void 
    mineStatus
    (
      NodeStatus stat,
      TreeMap<String, NodeStatus> statusCache
    )
    {
      if (statusCache.containsKey(stat.getName()))
        return;
      statusCache.put(stat.getName(), stat);
      for (NodeStatus src: stat.getSources())
        if (!statusCache.containsKey(src.getName()))
          mineStatus(src, statusCache);
      
      for (NodeStatus trg : stat.getTargets())
        if (!statusCache.containsKey(trg.getName()))
          mineStatus(trg, statusCache);
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
      
      ArrayList<String> roots = new ArrayList<String>(); 
      MappedSet<Integer, String> orderedRoots = new MappedSet<Integer, String>();
      
      while (!pNodesToBuild.isEmpty() ) {
        String toBuild = findNodeToBuild();
        LogMgr.getInstance().log(Kind.Ops, Level.Fine, 
          "template: " + toBuild);
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), toBuild);
        TreeMap<String, BaseAnnotation> annots = getAnnotations(toBuild);
        TreeSet<String> contexts = new TreeSet<String>();
        for (String aName : annots.keySet()) {
          BaseAnnotation annot = annots.get(aName);
          if (aName.startsWith("TemplateContext") && !aName.startsWith("TemplateContextLink")) {
            String contextName = (String) annot.getParamValue(aContextName);
            contexts.add(contextName);
          }
        }
        
        Integer order = null;
        {
          BaseAnnotation annot = annots.get("TemplateOrder");
          if (annot != null) {
            order = (Integer) annot.getParamValue("Order");
          }
        }
        
        TreeSet<String> nodesMade = new TreeSet<String>();
        if (contexts.size() == 0) { //no contexts, just do a straight build
          makeNode(mod, pReplacements, pContexts, nodesMade);
        }
        else { //uh-oh, there are contexts!
          contextLoop(toBuild, mod, contexts, pReplacements, pContexts, nodesMade);
        }

        pNodesToBuild.remove(toBuild);
        TreeSet<String> targets = pNodesDependingOnMe.get(toBuild);
        if (targets != null && !targets.isEmpty()) {
          for (String target : targets ) {
            TreeSet<String> temp = pNodesIDependedOn.get(target);
            temp.remove(toBuild);
            if (temp.isEmpty())
              pNodesIDependedOn.remove(target);
            else
              pNodesIDependedOn.put(target, temp);
          }
        } 
        else { // we've got a node that nothing depends on, must be a root.
          if (order == null)
            roots.addAll(nodesMade);
          else {
            for (String node : nodesMade)
              orderedRoots.put(order, node);
          }
        }
      } // while (!pNodesToBuild.isEmpty() )
      for (Integer order : orderedRoots.keySet()) {
        for (String root : orderedRoots.get(order)) {
          addToQueueList(root);
          addToCheckInList(root);  
        }
      }
      for (String root : roots) {
        addToQueueList(root);
        addToCheckInList(root);  
      }
    }

    private void 
    contextLoop
    (
      String toBuild,
      NodeMod mod,
      TreeSet<String> contextList,
      TreeMap<String, String> replace,
      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts, 
      TreeSet<String> nodesMade
    )
      throws PipelineException
    {
      String currentContext = contextList.pollFirst();
      ArrayList<TreeMap<String, String>> values = contexts.get(currentContext);
      
      if (values == null || values.isEmpty()) 
        throw new PipelineException
          ("The context (" + currentContext + ") specified on (" + toBuild + ") has no values " +
           "defined for it.");
      
      for (TreeMap<String, String> contextEntry : values) {
        TreeMap<String, String> newReplace = new TreeMap<String, String>(replace);
        newReplace.putAll(contextEntry);
        TreeMap<String, ArrayList<TreeMap<String, String>>> newMaps = 
          new TreeMap<String, ArrayList<TreeMap<String,String>>>(contexts);
        ArrayList<TreeMap<String, String>> newStuff = new ArrayList<TreeMap<String,String>>();
        newStuff.add(new TreeMap<String, String>(contextEntry));
        newMaps.put(currentContext, newStuff);
        
        if (contextList.isEmpty()) {  //bottom of the recursion
          makeNode(mod, newReplace, newMaps, nodesMade);
        }
        else {
          contextLoop
            (toBuild, mod, new TreeSet<String>(contextList), newReplace, newMaps, nodesMade);
        }
      }
    }

    private void 
    makeNode
    (
      NodeMod mod,
      TreeMap<String, String> replace,
      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts, 
      TreeSet<String> nodesMade
    )
      throws PipelineException
    {
      TemplateStage stage = 
        TemplateStage.getTemplateStage
        (mod, getStageInformation(), pContext, pClient, 
         pTemplateInfo, replace, contexts, pAnnotCache);

      if (stage.build()) {
        if (stage.needsFinalization())
          pFinalizableStages.add(stage);
        nodesMade.add(stage.getNodeName());
      }
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
        LogMgr.getInstance().log(Kind.Ops, Level.Fine, 
          "Searching for product nodes based on the template node (" + product + ")");
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), product);
        TreeSet<String> allProducts = new TreeSet<String>();
        if (pProductContexts.containsKey(product)) {
          TreeMap<String,TreeSet<String>> allContexts = pProductContexts.get(product);
          for (String target : allContexts.keySet()) {
            TreeSet<String> contexts = new TreeSet<String>(allContexts.get(target));
            expandContexts(product, contexts, pReplacements, allProducts);
          }
        }
        else {
          String realProduct = stringReplace(product, pReplacements);
          allProducts.add(realProduct);
        }
        
        for (String realProduct : allProducts) {
        
          if (!nodeExists(realProduct))
            throw new PipelineException("Needed source node (" + realProduct + ") does not exist.");
          if (mod.isLocked())
            lockLatest(realProduct);
          else
            frozenStomp(realProduct);
        }
      }
    }

    private void 
    expandContexts
    (
      String product,
      TreeSet<String> contexts,
      TreeMap<String,String> replacements, 
      TreeSet<String> allProducts
    )
      throws PipelineException
    {
      String currentContext = contexts.pollFirst();
      ArrayList<TreeMap<String, String>> values = pContexts.get(currentContext);
      
      if (values == null || values.isEmpty()) 
        throw new PipelineException
          ("The context (" + currentContext + ") specified for the product (" + product + ") has no " +
           "values defined for it.");
      
      for (TreeMap<String, String> contextEntry : values) {
        TreeMap<String, String> newReplace = new TreeMap<String, String>(replacements);
        newReplace.putAll(contextEntry);
       
        if (contexts.isEmpty()) {  //bottom of the recursion
          allProducts.add(stringReplace(product, newReplace));
        }
        else {
          expandContexts(product, new TreeSet<String>(contexts), newReplace, allProducts);
        }
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
  
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3515581617122035575L;

  public static final String aContextName = "ContextName";
  public static final String aLinkName = "LinkName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  private boolean pGenerateDependSets;
  
  private TreeMap<String, String> pReplacements;
  
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  
  private TreeMap<String, TreeMap<String, BaseAnnotation>> pAnnotCache;
  
  private TemplateBuildInfo pTemplateInfo;
  
  private ArrayList<FinalizableStage> pFinalizableStages;
  
  private TreeSet<String> pNodesToBuild;
  
  private MappedSet<String, String> pNodesIDependedOn;
  private MappedSet<String, String> pNodesDependingOnMe;
  private TreeSet<String> pProductNodes;
  private DoubleMap<String, String, TreeSet<String>> pProductContexts;
}