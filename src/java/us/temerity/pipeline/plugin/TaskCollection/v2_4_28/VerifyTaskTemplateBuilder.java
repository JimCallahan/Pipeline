package us.temerity.pipeline.plugin.TaskCollection.v2_4_28;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.builder.v2_4_28.*;
import us.temerity.pipeline.builder.v2_4_28.TaskBuilder;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.plugin.TemplateCollection.v2_4_12.*;

/*------------------------------------------------------------------------------------------*/
/*   R U N   V E R I F Y   T E M P L A T E   B U I L D E R                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Verify builder for version 2.4.28 of the task system that runs a template to perform the 
 * verification.  Designed to be run from a node using the 
 * {@link TaskRunTemplateBuilderAction}.
 */
public 
class VerifyTaskTemplateBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Required constructor to launch the builder.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param builderInfo
   *   Information that is shared among all builders in a given invocation.
   */ 
  public
  VerifyTaskTemplateBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("VerifyTaskTemplate",
          "Verify builder for version 2.4.28 of the task system that runs a template to " +
          "perform the verification", 
          mclient, qclient, builderInfo, EntityType.Ignore);
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (aSourceNode, 
         "The submit node that is being verified.", 
         null);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (aCustomWorkingArea,
         "The name of the custom working area to run this builder in.",
         null);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new PathUtilityParam
        (aParamManifest,
         "The node name of the manifest that contains all the replacement values.",
         null);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new PathUtilityParam
        (aDescManifest,
         "The node name of the manifest that contains the description of where the " +
         "template is stored.",
          null);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aAllowZeroContexts,
         "Allow contexts to have no replacements.",
         false);
      addParam(param);
    }
    
    addReleaseViewParam();
    
    /* not really applicable to this builder, so hide it from the users */ 
    disableParam(new ParamMapping(aActionOnExistence));

    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aUtilContext);
    layout.addSeparator();
    layout.addEntry(aActionOnExistence);  
    layout.addEntry(aReleaseOnError);     
    layout.addEntry(aReleaseView);
    layout.addSeparator();
    layout.addEntry(aSourceNode);
    layout.addEntry(aCustomWorkingArea);
    layout.addEntry(aParamManifest);
    layout.addEntry(aDescManifest);
    layout.addEntry(null);
    layout.addEntry(aAllowZeroContexts);
    
    setParamValue(aReleaseView, ReleaseView.OnSuccess.toString());
    disableParam(aReleaseViewParam);
    
    PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
    setLayout(finalLayout);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public boolean 
  performCheckIn()
  {
    return true;
  }
  
  /**
   * Level of check-in that the builder should perform.
   */ 
  @Override
  public VersionID.Level
  getCheckinLevel()
  {
    return pCheckInLevel;
  }
  
  @Override
  public String 
  getCheckInMessage()
  {
    return pCheckInMessage;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class LookupAndValidate
    extends SetupPass
  {
    public 
    LookupAndValidate()
    {
      super("Lookup and Validate", 
            "Lookup the submit node and validate its annotations and other " + 
            "builder parameter values, including the template settings."); 
    }
    
    /**
     * Phase in which parameter values should be extracted from parameters and checked
     * for consistency and applicability.
     */
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      /* sets up the built-in parameters common to all builders */    
      validateBuiltInParams();
      
      /* kinda important. */
      getStageInformation().setDoAnnotations(true);
      
      /* Deal with the submit node. */
      pSubmitNode = getStringParamValue(new ParamMapping(aSourceNode), false);
      if (!nodeExists(pSubmitNode))
        throw new PipelineException
          ("The node name submitted as the source node (" + pSubmitNode + ") is not a valid " +
           "Pipeline node.");
      
      pCustomWorkingArea = getStringParamValue(new ParamMapping(aCustomWorkingArea), false);
      
      TreeMap<NodePurpose, BaseAnnotation> submitAnnots = 
        new TreeMap<NodePurpose, BaseAnnotation>();
      String submitTaskInfo[] = lookupTaskAnnotations(pSubmitNode, submitAnnots);
      
      if (!submitAnnots.containsKey(NodePurpose.Submit))
        throw new PipelineException
          ("The node submitted as the source node (" + pSubmitNode + ") is not a " +
           "Submit node.");
      
      setTaskInformation(submitTaskInfo[0], submitTaskInfo[1], submitTaskInfo[2], submitTaskInfo[3]);
      
      NodeMod submitMod = getWorkingVersion(pSubmitNode);
      if (!submitMod.isLocked())
        throw new PipelineException
          ("The submit node (" + pSubmitNode + ") needs to be locked before running the " +
           "verify builder.  This guarantees that verification only occurs for checked-in " +
           "versions of the task.");
      
      pSubmitNodeVersion = submitMod.getWorkingID();
      
      pLog.log
        (Kind.Ops, Level.Fine, 
         "The submit node and version are: " + pSubmitNode + "(v" + pSubmitNodeVersion + ")");
      
      NodeVersion ver = pClient.getCheckedInVersion(pSubmitNode, pSubmitNodeVersion);
      pCheckInMessage = 
        "Verified the following submit version:\n" + 
        "SUBMIT NODE: " + pSubmitNode + " (v" + ver.getVersionID() + ")\n" +
        "SUBMITTED BY: " + ver.getAuthor() + "\n" +
        "SUBMISSION NOTES: " + ver.getMessage() + "\n";
      
      /* Deal with all the template related stuff.*/
      Path manifestPath = (Path) getParamValue(aParamManifest);
      Path descPath = (Path) getParamValue(aDescManifest);
      
      if (manifestPath == null)
        throw new PipelineException("The ParamManifest must have a non-null value.");

      if (descPath == null)
        throw new PipelineException("The DescManifest must have a non-null value.");
      
      {
        NodeID nodeID = new NodeID(getAuthor(), getView(), manifestPath.toString());
        NodeMod mod = getWorkingVersion(manifestPath.toString());
        Path p = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());
        p = new Path(p, mod.getPrimarySequence().getPath(0));
        try {
          pParamManifest = (TemplateParamManifest) 
            GlueDecoderImpl.decodeFile("ParamManifest", p.toFile());
        }
        catch (GlueException ex) {
          throw new PipelineException("Unable to read the Param Manifest Glue File", ex);
        }
      }
      
      {
        NodeID nodeID = new NodeID(getAuthor(), getView(), descPath.toString());
        NodeMod mod = getWorkingVersion(descPath.toString());
        Path p = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());
        p = new Path(p, mod.getPrimarySequence().getPath(0));
        try {
          pDescManifest = (TemplateDescManifest) 
            GlueDecoderImpl.decodeFile("DescManifest", p.toFile());
        }
        catch (GlueException ex) {
          throw new PipelineException("Unable to read the Desc Manifest Glue File", ex);
        }
      }
      
      pAOEMode = "Verify";
      if (!pParamManifest.getAOEModes().containsKey(pAOEMode))
        throw new PipelineException
          ("The template specified by the Param Manifest (" + manifestPath.toString() + ") " +
           "does not contain an AOE Mode named Verify.");
      
      if (!pParamManifest.getOptionalBranches().containsKey("Verify"))
        throw new PipelineException
          ("The template specified by the Param Manifest (" + manifestPath.toString() + ") " +
           "does not contain an Optional Branch named Verify.");
        
      /* Deal with the verify node. */
      pVerifyNode = getDefaultVerifyNodeName(); 

      pCheckInLevel = VersionID.Level.Minor;
      if (checkedInVersionExists(pVerifyNode)) {
        NodeVersion verifyVer = pClient.getCheckedInVersion(pVerifyNode, null);
        String message = verifyVer.getMessage();
        String buffer[] = message.split("\\(");
        if (buffer.length > 1) {
          buffer = buffer[1].split("\\)");
          String vid = buffer[0].replaceAll("v", "");
          if (VersionID.isValidVersionID(vid)) {
            VersionID id = new VersionID(vid);
            pCheckInLevel = id.compareLevel(pSubmitNodeVersion);
          }
        }
      }
    }
    
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      TreeMap<String, String> replacements = pParamManifest.getReplacements();
      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts = 
        pParamManifest.getContexts();
      TreeMap<String, FrameRange> frameRanges = pParamManifest.getFrameRanges();
      TreeMap<String, TemplateExternalData> externals = pParamManifest.getExternals();
      TreeMap<String, Boolean> obranches = pParamManifest.getOptionalBranches();
      TreeMap<String, Integer> offsets = pParamManifest.getOffsets();
      TreeMap<String, ActionOnExistence> aoeModes = pParamManifest.getAOEModes();
      
      for (String branch : new TreeSet<String>(obranches.keySet()))
        obranches.put(branch, false);
      obranches.put("Verify", true);
      
      pClient.createWorkingArea(getAuthor(), pCustomWorkingArea);
      UtilContext context = new UtilContext(getAuthor(), pCustomWorkingArea, getToolset());
      setContext(context);

      TemplateType ttype = pDescManifest.getTemplateType();
      BaseBuilder templateBuilder;
      switch (ttype) {
      case TaskSingle:
        {
          String rootNode = pDescManifest.getStartNode();
          VersionID verID = pDescManifest.getStartNodeVersion();
          checkOutVersionIfDifferent
            (rootNode, verID, CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
          templateBuilder = new TemplateBuilder
            (pClient, pQueue, getBuilderInformation(), rootNode, replacements, contexts, 
             frameRanges, aoeModes, externals, obranches, offsets);
        }
        break;
      
      case TaskList:
        {
          SortedMap<String, VersionID> rootNodes = pDescManifest.getRootNodes();
          for (Entry<String, VersionID> entry : rootNodes.entrySet())
            checkOutVersionIfDifferent
             (entry.getKey(), entry.getValue(), 
              CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
          templateBuilder = new TemplateBuilder
            (pClient, pQueue, getBuilderInformation(), rootNodes.keySet(), replacements, 
             contexts, frameRanges, aoeModes, externals, obranches, offsets);
        }
        break;

      case NonTask:
        {
          SortedMap<String, VersionID> rootNodes = pDescManifest.getRootNodes();
          Set<String> allNodes = pDescManifest.getAllNodes();
          for (Entry<String, VersionID> entry : rootNodes.entrySet())
            checkOutVersionIfDifferent
             (entry.getKey(), entry.getValue(), 
              CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
          templateBuilder = new TemplateBuilder
            (pClient, pQueue, getBuilderInformation(), rootNodes.keySet(), allNodes, 
             replacements, contexts, frameRanges, aoeModes, externals, obranches, offsets);          
        }
        break;
        
        default:
          throw new IllegalStateException("This should never happen");
      }
 
      templateBuilder.setParamValue(aActionOnExistence, pAOEMode);
      templateBuilder.setParamValue(aCheckInLevel, pCheckInLevel.toString());
      templateBuilder.setParamValue(aCheckInMessage, pCheckInMessage);
      
      addSubBuilder(templateBuilder, false, 100);
      addMappedParam(templateBuilder.getName(), aCheckinWhenDone, aCheckinWhenDone);
      addMappedParam(templateBuilder.getName(), aAllowZeroContexts, aAllowZeroContexts);
      addMappedParam(templateBuilder.getName(), aReleaseOnError, aReleaseOnError);
      addMappedParam(templateBuilder.getName(), aUtilContext, aUtilContext);
    }


    private static final long serialVersionUID = -4095514516416101012L;
    
    /*-- INTERNALS -------------------------------------------------------------------------*/
    private TemplateParamManifest pParamManifest;
    private TemplateDescManifest pDescManifest;
    private String pAOEMode;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7543958241558006103L;

  private static final String aSourceNode        = "SourceNode";
  private static final String aCustomWorkingArea = "CustomWorkingArea";
 
  private static final String aParamManifest     = "ParamManifest";
  private static final String aDescManifest      = "DescManifest";
  private static final String aAllowZeroContexts = "AllowZeroContexts";
  private static final String aCheckInLevel      = "CheckInLevel";
  private static final String aCheckInMessage    = "CheckInMessage";

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private VersionID.Level pCheckInLevel;
  private String pCheckInMessage;
  
  private String pCustomWorkingArea;
  private String pSubmitNode;
  
  private VersionID pSubmitNodeVersion;
  
  private String pVerifyNode;
}
