package us.temerity.pipeline.plugin.TaskCollection.v2_4_28;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_28.*;
import us.temerity.pipeline.builder.v2_4_28.TaskBuilder;
import us.temerity.pipeline.plugin.TaskRunBuilderAction.v2_4_28.*;

/*------------------------------------------------------------------------------------------*/
/*   P U B L I S H   T A S K   B U I L D E R                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Default publish builder for version 2.4.28 of the task system.  Designed to be run from a 
 * node using the {@link TaskRunBuilderAction}.
 */
public 
class PublishTaskBuilder
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
  PublishTaskBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("PublishTask",
         "A builder which implements a generic task publish operation.", 
         mclient, qclient, builderInfo, EntityType.Ignore);
   
    {
      UtilityParam param = 
        new StringUtilityParam
        (aSourceNode, 
         "The submit node that is being published.", 
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

    addReleaseViewParam();
    
    addSetupPass(new LookupAndValidate());
    
    addConstructPass(new PublishTaskPass());
    
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
            "Lookup the verify and publish nodes and validate their annotations and other " + 
            "builder parameter values."); 
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
      
      /* Deal with the submit node. */
      pVerifyNode = getStringParamValue(new ParamMapping(aSourceNode), false);
      if (!nodeExists(pVerifyNode))
        throw new PipelineException
          ("The node name submitted as the source node (" + pVerifyNode + ") is not a valid " +
           "Pipeline node.");
      
      pCustomWorkingArea = getStringParamValue(new ParamMapping(aCustomWorkingArea), false);
      
      TreeMap<NodePurpose, BaseAnnotation> verifyAnnots = 
        new TreeMap<NodePurpose, BaseAnnotation>();
      String verifyTaskInfo[] = lookupTaskAnnotations(pVerifyNode, verifyAnnots);
      
      if (!verifyAnnots.containsKey(NodePurpose.Verify))
        throw new PipelineException
          ("The node submitted as the source node (" + pVerifyNode + ") is not a " +
           "Submit node.");
      
      setTaskInformation(verifyTaskInfo[0], verifyTaskInfo[1], verifyTaskInfo[2], 
                         verifyTaskInfo[3]);
      
      NodeMod verifyMod = getWorkingVersion(pVerifyNode);
      if (!verifyMod.isLocked())
        throw new PipelineException
          ("The verify node (" + pVerifyNode + ") needs to be locked before running the " +
           "publish builder.  This guarantees that publishing only occurs for checked-in " +
           "versions of the task.");
      
      pVerifyNodeVersion = verifyMod.getWorkingID();
      
      NodeVersion ver = pClient.getCheckedInVersion(pVerifyNode, pVerifyNodeVersion);
      pCheckInMessage = 
        "Published the following verify version:\n" + 
        "VERIFY NODE: " + pVerifyNode + " (v" + ver.getVersionID() + ")\n" +
        "SUBMITTED BY: " + ver.getAuthor() + "\n" +
        "SUBMISSION NOTES: " + ver.getMessage() + "\n";
        
      
      /* Deal with the verify node. */
      pPublishNode = getDefaultPublishNodeName(); 
      if (!nodeExists(pPublishNode))
        throw new PipelineException
          ("The supposed publish node (" + pPublishNode + ") is not a valid Pipeline node.");
      
      TreeMap<NodePurpose, BaseAnnotation> publishAnnots = 
        new TreeMap<NodePurpose, BaseAnnotation>();
      
      String publishTaskInfo[] = lookupTaskAnnotations(pPublishNode, publishAnnots);

      if (!publishAnnots.containsKey(NodePurpose.Publish))
        throw new PipelineException
          ("The supposed publish node (" + pPublishNode + ") is not marked as a " +
           "Publish node.");

      if (!isSameTask(publishTaskInfo))
        throw new PipelineException
          ("The task " + publishTaskInfo  + " assigned to the supposed verify node () is " +
           "not the same as the task " + verifyTaskInfo + " on the submit node.");

      pCheckInLevel = VersionID.Level.Minor;
      if (checkedInVersionExists(pPublishNode)) {
        NodeVersion publishVer = pClient.getCheckedInVersion(pPublishNode, null);
        String message = publishVer.getMessage();
        String buffer[] = message.split("\\(");
        if (buffer.length > 1) {
          buffer = buffer[1].split("\\)");
          String vid = buffer[0].replaceAll("v", "");
          if (VersionID.isValidVersionID(vid)) {
            VersionID id = new VersionID(vid);
            pCheckInLevel = id.compareLevel(pVerifyNodeVersion);
          }
        }
      }
      else
        throw new PipelineException
          ("No checked-in version of the publish node (" + pPublishNode + ") exists.");
    }

    private static final long serialVersionUID = 2489434813228260123L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected
  class PublishTaskPass
    extends ConstructPass
  {
    public 
    PublishTaskPass()
    {
      super("Publish Task", 
            "Check-out the publish and verify node networks, regenerate any stale nodes " + 
            "in the publish network and check-in the changes.");
    }
    
    /**
     * Check-out the latest approve and submit node networks, regenerated anything stale
     * in the approve network and check-in the changes. 
     */ 
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      setContext(new UtilContext(getAuthor(), pCustomWorkingArea, getToolset()));
      
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pPublishNode);
      pClient.checkOut(getAuthor(), getView(), pPublishNode, null, 
                       CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);

      /* 
       * Checkout the verify node second, to guarantee the right versions are used.
       * Otherwise, the checkout of the publish node could bring out newer versions of
       * prepare nodes, if an older version of the verify node is being published.  That 
       * would be less than optimal.
       */
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pVerifyNode);
      pClient.checkOut(getAuthor(), getView(), pVerifyNode, pVerifyNodeVersion, 
                       CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
      
      addToQueueList(pPublishNode);
      addToCheckInList(pPublishNode);
    }

    private static final long serialVersionUID = -4230731723549298240L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7119509967321893837L;
  
  public static final String aSourceNode = "SourceNode";
  public static final String aCustomWorkingArea = "CustomWorkingArea";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private VersionID.Level pCheckInLevel;
  private String pCheckInMessage;
  
  private String pCustomWorkingArea;
  private String pVerifyNode;
  
  private VersionID pVerifyNodeVersion;
  
  private String pPublishNode;
}