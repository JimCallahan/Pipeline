// $Id: TemplateNetworkScan.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_12;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   N E T W O R K   S T A G E                                            */
/*------------------------------------------------------------------------------------------*/

public 
class TemplateNetworkScan
{
  public
  TemplateNetworkScan
  (
    MasterMgrClient mclient,
    String author,
    String view,
    String startNode,
    TreeSet<String> nodesInTemplate
  )
  {
    pClient = mclient;
    pAuthor = author;
    pView = view;
    pStartNode = startNode;
    pInitialNodesInTemplate = nodesInTemplate;
    
    pTaskNetwork = (nodesInTemplate == null || nodesInTemplate.isEmpty());
    
    pAllAnnotCache = 
      new TripleMap<String, String, String, TreeMap<String,BaseAnnotation>>();
  }
  
  public void
  scan() 
    throws PipelineException
  {
    pNodesInTemplate      = new TreeSet<String>();
    pProductNodes         = new DoubleMap<String, String, Boolean>();
    pProductContexts      = new MappedSet<String, String>();
    pProductNodeContexts  = new DoubleMap<String, String, TreeSet<String>>();
    pOptionalBranchValues = new MappedSet<String, String>();
    pContexts             = new MappedSet<String, String>();
    pAoEModes             = new DoubleMappedSet<String, ActionOnExistence, String>();
    pFrameRanges          = new MappedSet<String, String>();
    pExternals            = new MappedSet<String, String>();
    pConditionalBuilds    = new TreeMap<String, String>();
    pCheckpoints          = new TreeSet<String>();
    pVouchable            = new TreeSet<String>();
    pOrder                = new MappedSet<Integer, String>();
    
    NodeStatus status = 
      pClient.status(pAuthor, pView, pStartNode, true, DownstreamMode.None);
    
    boolean first = true;
    
    for (NodeStatus child : status.getSources()) {
      if (first) {
        first = false;
        if (pTaskNetwork) {
          String nodeName = child.getName();
          TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(nodeName);
          if (annots == null || annots.isEmpty())
            throw new PipelineException
              ("No task annotation on (" + nodeName + ") which is a direct source " +
               "of the template glue node");
          String aName = annots.firstKey();
          BaseAnnotation annot = annots.get(aName);
          pProjectName = lookupProjectName(nodeName, aName, annot);
          pTaskName = lookupTaskName(nodeName, aName, annot);
          pTaskType = lookupTaskType(nodeName, aName, annot);
        }
      } // if (first)
      mineNode(child, null);
    }
  }

  
  /**
   * @return the nodesInTemplate
   */
  public final TreeSet<String> 
  getNodesInTemplate()
  {
    return pNodesInTemplate;
  }
  
  /**
   * @return the productNodes
   */
  public final DoubleMap<String, String, Boolean> 
  getProductNodes()
  {
    return pProductNodes;
  }
  
  /**
   * @return the productNodeContexts
   */
  public final DoubleMap<String, String, TreeSet<String>> 
  getProductNodeContexts()
  {
    return pProductNodeContexts;
  }
  
  /**
   * @return the optionalBranchValues
   */
  public final MappedSet<String, String> 
  getOptionalBranchValues()
  {
    return pOptionalBranchValues;
  }

  /**
   * @return the contexts
   */
  public final MappedSet<String, String> 
  getContexts()
  {
    return pContexts;
  }

  /**
   * @return the productContexts
   */
  public final MappedSet<String, String> 
  getProductContexts()
  {
    return pProductContexts;
  }
  
  /**
   * @return the aoEModes
   */
  public final DoubleMappedSet<String, ActionOnExistence, String> 
  getAoEModes()
  {
    return pAoEModes;
  }

  /**
   * @return the frameRanges
   */
  public final MappedSet<String, String> 
  getFrameRanges()
  {
    return pFrameRanges;
  }
  
  /**
   * @return the externals
   */
  public final MappedSet<String, String> 
  getExternals()
  {
    return pExternals;
  }
  
  /**
   * @return the optionalProducts
   */
  public final MappedSet<String, String> 
  getOptionalProducts()
  {
    return pOptionalProducts;
  }

  /**
   * @return the conditionalBuilds
   */
  public final TreeMap<String, String> 
  getConditionalBranches()
  {
    return pConditionalBuilds;
  }
  
  /**
   * @return the order
   */
  public final MappedSet<Integer, String> 
  getOrder()
  {
    return pOrder;
  }
  
  /**
   * @return the checkpoints
   */
  public final TreeSet<String> 
  getCheckpoints()
  {
    return pCheckpoints;
  }
  
  /**
   * @return the vouchable
   */
  public final TreeSet<String> 
  getVouchable()
  {
    return pVouchable;
  }

  private void
  mineNode
  (
    NodeStatus status,
    String parent
  )
    throws PipelineException
  {
    String nodeName = status.getName();
    if (status.getLightDetails() == null)
      throw new PipelineException
        ("No lightweight status information for node (" + nodeName +")");
    NodeMod mod = status.getLightDetails().getWorkingVersion();
    if (mod == null)
      throw new PipelineException
        ("No working version for node (" + nodeName +")");
    
    TreeMap<String, BaseAnnotation> annots = mod.getAnnotations();
    
    if (inTemplate(nodeName)) {
      // Check if we did this node already
      if (pNodesInTemplate.contains(nodeName))
        return;
      pNodesInTemplate.add(nodeName);
      
      MappedSet<String, String> productContext = new MappedSet<String, String>();
      
      for (Entry<String, BaseAnnotation> entry : annots.entrySet()) {
        String aname = entry.getKey();
        BaseAnnotation annot = entry.getValue();
        if (aname.startsWith("TemplateContextLink")) {
          String src = (String) annot.getParamValue(aLinkName);
          String context = (String) annot.getParamValue(aContextName);
          productContext.put(src, context);
          pProductContexts.put(context, src);
        }
        else if (aname.startsWith("TemplateContextLink")) {
          String context = (String) annot.getParamValue(aContextName);
          String link = (String) annot.getParamValue(aLinkName);
          pContexts.put(context, link);
        }
        else if (aname.startsWith("TemplateContext")) {
          String context = (String) annot.getParamValue(aContextName);
          pContexts.put(context, nodeName);
        }
        else if (aname.startsWith("TemplateSecContext")) {
          String context = (String) annot.getParamValue(aContextName);
          pContexts.put(context, nodeName);
        }
        else if (aname.startsWith("TemplateRange")) {
          String range = (String) annot.getParamValue(aRangeName);
          pFrameRanges.put(range, nodeName);
        }
        else if (aname.startsWith("TemplateIgnoreProduct")) {
          String src = (String) annot.getParamValue(aLinkName);
          pProductNodes.put(src, nodeName, true);
        }
        else if (aname.startsWith("TemplateAOE")) {
          String mode = (String) annot.getParamValue(aModeName);
          ActionOnExistence aoe = 
            ActionOnExistence.valueFromString(
              (String) annot.getParamValue(aActionOnExistence));
          pAoEModes.put(mode, aoe, nodeName);
        }
        else if (aname.equals("TemplateCheckpoint")) {
          pCheckpoints.add(nodeName);
        }
        else if (aname.equals("TemplateVouchable")) {
          pVouchable.add(nodeName);
        }
        else if (aname.equals("TemplateExternal")) {
          String eName = (String) annot.getParamValue(aExternalName);
          pExternals.put(eName, nodeName);
        }
        else if (aname.equals("TemplateOptionalBranch")) {
          String oname = (String) annot.getParamValue(aOptionName);
          pOptionalBranchValues.put(oname, nodeName);
        }
        else if (aname.equals("TemplateOrder")) {
          Integer order = (Integer) annot.getParamValue(aOrder);
          pOrder.put(order, nodeName);
        }
        else if (aname.equals("TemplateConditionalBuild")) {
          String cname = (String) annot.getParamValue(aConditionName);
          pConditionalBuilds.put(nodeName, cname);
        }
      } //  for (Entry<String, BaseAnnotation> entry : annots.entrySet())
      for (Entry<String, TreeSet<String>> entry : productContext.entrySet()) {
       pProductNodeContexts.put(nodeName, entry.getKey(), entry.getValue()); 
      }
       
      for (NodeStatus child : status.getSources()) 
        mineNode(child, nodeName);
      
    } // if (inTemplate(nodeName)) 
    else { //product node
      if (parent == null)
        throw new PipelineException
          ("The non-template node (" + nodeName + ") appears to be a direct source " +
           "of the template glue node");
      if (!pProductNodes.containsKey(nodeName, parent))
        pProductNodes.put(nodeName, parent, false);
      for (Entry<String, BaseAnnotation> entry : annots.entrySet()) {
        String aname = entry.getKey();
        BaseAnnotation annot = entry.getValue();
        if (aname.startsWith("TemplateSecContext")) {
          String context = (String) annot.getParamValue(aContextName);
          pContexts.put(context, nodeName);
        }
      }
    }
  }
  
  private boolean
  inTemplate
  (
    String nodeName  
  ) 
    throws PipelineException
  {
    if (pTaskNetwork) {
      TreeMap<String, BaseAnnotation> taskAnnots = getTaskAnnotations(nodeName);
      TreeSet<String> purposes = new TreeSet<String>();
      boolean taskMatch = false;
      if (taskAnnots != null && !taskAnnots.isEmpty()) {
        for ( String aName : taskAnnots.keySet()) {
          BaseAnnotation annot = taskAnnots.get(aName);
          taskMatch = doesTaskMatch(nodeName, aName, annot);
          String purpose = lookupPurpose(nodeName, aName, annot); 
          if (taskMatch)
            purposes.add(purpose);
          else {
            if (!purpose.equals("Product"))
              throw new PipelineException
              ("The node ("+ nodeName + ") connected to this network belongs to a different " +
               "task, but is not a Product node.");
          }
        }
      }
      if (purposes.contains(NodePurpose.Approve.toString())) 
        pApprovalNode = nodeName;
      if (purposes.contains(NodePurpose.Submit.toString()))
        pSubmitNode = nodeName;
      return taskMatch;
    } // if (pTaskNetwork)
    else {
      return pInitialNodesInTemplate.contains(nodeName);
    }
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
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N   L O O K U P                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the per-node and per-version Annotations on the given node.  
   * <p>
   * If there is no working version of the node, this method will fail.  If per-node 
   * annotations need to be retrieved then the method should be used. 
   * <p>
   * This method uses a cache to accelerate access to the annotations.  Annotations will only
   * be looked up once for each node.  This is not a cross-builder cache, so multiple
   * builders based on the TaskBuilder (perhaps being used a sub-builders) may lookup the
   * same information.
   * 
   * @param name
   *   The name of the node.
   * 
   * @return
   *   A TreeMap of Annotations indexed by annotation name, which may be empty if no 
   *   annotations exist on the node.
   */
  private TreeMap<String, BaseAnnotation>
  getAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = 
      pAllAnnotCache.get(pAuthor, pView, name);
    if (annots == null) {
      annots = pClient.getAnnotations(pAuthor, pView, name);
      pAllAnnotCache.put(pAuthor, pView, name, annots);
    }
   return annots;
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
  protected TreeMap<String, BaseAnnotation>
  getTaskAnnotations
  (
    String name
  )
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annotations = getAnnotations(name);
    
    TreeMap<String, BaseAnnotation> toReturn = null;
    for(String aname : annotations.keySet()) {
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        if (toReturn == null)
          toReturn = new TreeMap<String, BaseAnnotation>();
        BaseAnnotation tannot = annotations.get(aname);
        toReturn.put(aname, tannot);
      }
    }
   return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A N N O T A T I O N   P A R A M E T E R   L O O K U P                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Lookup the value of the ProjectName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupProjectName
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String projectName = (String) annot.getParamValue(aAnnotProjectName);
    if(projectName == null) 
      throw new PipelineException
        ("No " + aAnnotProjectName + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 
    
    return projectName;
  }

  /**
   * Lookup the value of the TaskName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskName
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskName = (String) annot.getParamValue(aAnnotTaskName);
    if(taskName == null) 
      throw new PipelineException
        ("No " + aAnnotTaskName + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 

    return taskName;
  }

  /**
   * Lookup the value of the (Custom)TaskType annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupTaskType
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskType = (String) annot.getParamValue(aAnnotTaskType);
    if(taskType == null) 
      throw new PipelineException
        ("No " + aAnnotTaskType + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 

    if(taskType.equals(TaskType.CUSTOM.toTitle())) {
      taskType = (String) annot.getParamValue(aAnnotCustomTaskType);
      if(taskType == null) 
        throw new PipelineException
          ("No " + aAnnotCustomTaskType + " parameter was specified for the " + 
           "(" + aname + ") annotation on the node (" + name + ") even though the " + 
           aAnnotTaskType + " " + "parameter was set to (" + TaskType.CUSTOM.toTitle() + ")!"); 
    }

    return taskType;
  }

  /**
   * Lookup the value of the TaskName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  protected String
  lookupPurpose
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String purpose = (String) annot.getParamValue(aAnnotPurpose);
    if(purpose == null) 
      throw new PipelineException
        ("No " + aAnnotPurpose + " parameter was specified for the (" + aname + ") " + 
         "annotation on the node (" + name + ")!"); 

    return purpose;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*  S T A T I C   I N T E R N A L S                                                       */
  /*----------------------------------------------------------------------------------------*/
 
  private static final String aAnnotProjectName    = "ProjectName";
  private static final String aAnnotTaskName       = "TaskName";
  private static final String aAnnotTaskType       = "TaskType";
  private static final String aAnnotCustomTaskType = "CustomTaskType";
  private static final String aAnnotPurpose        = "Purpose"; 

  private static final String aContextName       = "ContextName";
  private static final String aLinkName          = "LinkName";
  private static final String aExternalName      = "ExternalName";
  private static final String aConditionName     = "ConditionName";
  private static final String aModeName          = "ModeName";
  private static final String aActionOnExistence = "ActionOnExistence";
  private static final String aOptionName        = "OptionName";
  private static final String aOrder             = "Order";
  private static final String aRangeName         = "RangeName";

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  private TripleMap<String, String, String, TreeMap<String, BaseAnnotation>> pAllAnnotCache;
  
  private MasterMgrClient pClient;
  private String pStartNode;
  private TreeSet<String> pInitialNodesInTemplate;
  
  private String pAuthor;
  private String pView;
  
  boolean pTaskNetwork;
  
  private String pProjectName;
  private String pTaskName;
  private String pTaskType;

  
  
  /* info that we are gathering */

  private TreeSet<String> pNodesInTemplate;
  private String pApprovalNode;
  private String pSubmitNode;
  
  
  /**
   * <ProductNode,TemplateNode,Ignorable>
   */
  private DoubleMap<String, String, Boolean> pProductNodes;
  
  /**
   * <ProductNode,TargetNode,ListOfContexts>
   */
  private DoubleMap<String, String, TreeSet<String>> pProductNodeContexts;
  
  /**
   * <OptionBranchName,ListOfTemplateNodes>
   */
  private MappedSet<String, String> pOptionalBranchValues;
  
  /**
   * <ContextName,ListOfTemplateNodes>
   */
  private MappedSet<String, String> pContexts;
  
  /**
   * <ContextName,ListOfProductNodes>
   */
  private MappedSet<String, String> pProductContexts;
  
  /**
   * <AoEName,<ActionOnExistence, ListOfTemplateNodes>>
   */
  private DoubleMappedSet<String, ActionOnExistence, String> pAoEModes;
  
  /**
   * <FrameRangeName,ListOfTemplateNodes>
   */
  private MappedSet<String, String> pFrameRanges;
  
  /**
   * <ExternalName,ListOfTemplateNodes>
   */
  private MappedSet<String, String> pExternals;
  
  /**
   * <ProductName,ListOfTemplateNodes>
   */
  private MappedSet<String, String> pOptionalProducts;
  
  /**
   * <TemplateNode, Condition>
   */
  private TreeMap<String, String> pConditionalBuilds;
  
  /**
   * <Order, ListOfTemplateNodes>
   */
  private MappedSet<Integer, String> pOrder;
  
  private TreeSet<String> pCheckpoints;
  
  private TreeSet<String> pVouchable;
  
}
