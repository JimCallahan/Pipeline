package us.temerity.pipeline.plugin.TemplateCollection.v2_4_12;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   M A N I F E S T   B U I L D E R                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Builder to invoke a template from manifest files.
 */
public 
class TemplateManifestBuilder
  extends TaskBuilder
{
  /**
   * Constructor.
   * 
   * @param mclient
   *   The instance of MasterMgrClient to use in the builder.
   * 
   * @param qclient
   *   The instance of QueueMgrClient to use in the builder.
   * 
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.
   */
  public
  TemplateManifestBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super("TemplateManifestBuilder",
          "Builder to invoke a template from manifest files.",
          mclient, qclient, builderInformation, EntityType.Ignore);
    
    noDefaultConstructPasses();
    
    addSetupPass(new InformationPass());
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
          (aAllowZeroContexts,
           "Allow contexts to have no replacements.",
           false);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
          (aInhibitFileCopy,
           "Inhibit the CopyFile flag on all nodes in the template.",
           false);
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
        new StringUtilityParam
          (aAOEMode,
           "The Action on Existence mode to use in the template.",
           null);
      addParam(param);
    }
    {
      UtilityParam param = 
        new StringUtilityParam
          (aCheckInMessage,
           "The check-in message to use.",
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
      ArrayList<String> values = new ArrayList<String>();
      Collections.addAll(values, "Major", "Minor", "Micro");
      UtilityParam param = 
        new EnumUtilityParam
          (aCheckInLevel,
           "The check-in level to use.",
           "Minor",
           values);
      addParam(param);
    }
    
    addCheckinWhenDoneParam();
    addReleaseViewParam();
    
    setParamValue(aReleaseView, ReleaseView.Always.toString());
    disableParam(new ParamMapping(aReleaseView));
    
    AdvancedLayoutGroup layout = new AdvancedLayoutGroup
    ("Information Pass", 
      "The First pass of the Template Info Builder",
      "ManifestInfo", true);
    
    layout.addEntry(1, aUtilContext);
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckinWhenDone);
    layout.addEntry(1, aActionOnExistence);
    layout.addEntry(1, aReleaseOnError);
    layout.addEntry(1, aReleaseView);
    layout.addEntry(1, null);
    layout.addEntry(1, aParamManifest);
    layout.addEntry(1, aDescManifest);
    layout.addEntry(1, aCustomWorkingArea);
    layout.addEntry(1, null);
    layout.addEntry(1, aAOEMode);
    layout.addEntry(1, aAllowZeroContexts);
    layout.addEntry(1, aInhibitFileCopy);
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckInLevel);
    layout.addEntry(1, aCheckInMessage);
    
    disableParam(new ParamMapping(aActionOnExistence));
    
    PassLayoutGroup passLayout = new PassLayoutGroup(layout.getName(), layout);
    setLayout(passLayout);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  private 
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
      getStageInformation().setDoAnnotations(true);
      
      pAOEMode = getStringParamValue(new ParamMapping(aAOEMode), false);
      pCustomWorkingArea = getStringParamValue(new ParamMapping(aCustomWorkingArea));
      
      Path manifestPath = (Path) getParamValue(aParamManifest);
      Path descPath = (Path) getParamValue(aDescManifest);
      
      if (!workingVersionExists(manifestPath.toString()))
        checkOutLatest(manifestPath.toString(), 
                       CheckOutMode.KeepModified, CheckOutMethod.AllFrozen);
      if (!workingVersionExists(descPath.toString()))
        checkOutLatest(descPath.toString(), 
                       CheckOutMode.KeepModified, CheckOutMethod.AllFrozen);
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
      
      if (pCustomWorkingArea != null) {
        pClient.createWorkingArea(getAuthor(), pCustomWorkingArea);
        UtilContext context = new UtilContext(getAuthor(), pCustomWorkingArea, getToolset());
        setContext(context);
      }

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
 
      pTemplateBuilder = templateBuilder;
      
      templateBuilder.setParamValue(aActionOnExistence, pAOEMode);
      
      addSubBuilder(templateBuilder, false, 100);
      addMappedParam(templateBuilder.getName(), aCheckinWhenDone, aCheckinWhenDone);
      addMappedParam(templateBuilder.getName(), aAllowZeroContexts, aAllowZeroContexts);
      addMappedParam(templateBuilder.getName(), aInhibitFileCopy, aInhibitFileCopy);
      addMappedParam(templateBuilder.getName(), aReleaseOnError, aReleaseOnError);
      addMappedParam(templateBuilder.getName(), aCheckInLevel, aCheckInLevel);
      addMappedParam(templateBuilder.getName(), aCheckInMessage, aCheckInMessage);
      
      if (pCustomWorkingArea == null)
        addMappedParam(templateBuilder.getName(), aUtilContext, aUtilContext);
      else {
        templateBuilder.setParamValue(aUtilContextAuthorParam, getAuthor());
        templateBuilder.setParamValue(aUtilContextViewParam, getView());
        templateBuilder.setParamValue(aUtilContextToolsetParam, getToolset());
        templateBuilder.disableParam(new ParamMapping(aUtilContext));
      }
      
      
    }
    

    
    /*--------------------------------------------------------------------------------------*/
    /*   S T A T I C   I N T E R N A L S                                                    */
    /*--------------------------------------------------------------------------------------*/
    
    private static final long serialVersionUID = 15485784468334415L;

    
    
    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    private TemplateParamManifest pParamManifest;
    private TemplateDescManifest pDescManifest;
    private String pAOEMode;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8364228727936427730L;

  private static final String aParamManifest     = "ParamManifest";
  private static final String aDescManifest      = "DescManifest";
  private static final String aAOEMode           = "AOEMode";
  private static final String aCheckInLevel      = "CheckInLevel";
  private static final String aCheckInMessage    = "CheckInMessage";
  private static final String aAllowZeroContexts = "AllowZeroContexts";
  private static final String aInhibitFileCopy   = "InhibitFileCopy";
  private static final String aCustomWorkingArea = "CustomWorkingArea";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private String pCustomWorkingArea;
  
  private BaseBuilder pTemplateBuilder;
  
}
