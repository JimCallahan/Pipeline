package us.temerity.pipeline.plugin.TaskCollection.v2_4_28;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_28.*;
import us.temerity.pipeline.builder.v2_4_28.TaskBuilder;
import us.temerity.pipeline.plugin.TaskRunBuilderAction.v2_4_28.*;

/*------------------------------------------------------------------------------------------*/
/*   V E R I F Y   T A S K   B U I L D E R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Default verify builder for version 2.4.28 of the task system.  Designed to be run from a 
 * node using the {@link TaskRunBuilderAction}.
 */
public 
class VerifyTaskBuilder
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
  VerifyTaskBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("VerifyTask",
         "A builder which implements a generic task verify operation.", 
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

    addReleaseViewParam();
    
    addSetupPass(new LookupAndValidate());
    
    addConstructPass(new VerifyTaskPass());
    
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
            "Lookup the submit and verify nodes and validate their annotations and other " + 
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
      
      NodeVersion ver = pClient.getCheckedInVersion(pSubmitNode, pSubmitNodeVersion);
      pCheckInMessage = 
        "Verified the following submit version:\n" + 
        "SUBMIT NODE: " + pSubmitNode + " (v" + ver.getVersionID() + ")\n" +
        "SUBMITTED BY: " + ver.getAuthor() + "\n" +
        "SUBMISSION NOTES: " + ver.getMessage() + "\n";
        
      
      /* Deal with the verify node. */
      pVerifyNode = getDefaultVerifyNodeName(); 
      if (!nodeExists(pVerifyNode))
        throw new PipelineException
          ("The supposed verify node (" + pVerifyNode + ") is not a valid Pipeline node.");
      
      TreeMap<NodePurpose, BaseAnnotation> verifyAnnots = 
        new TreeMap<NodePurpose, BaseAnnotation>();
      
      String verifyTaskInfo[] = lookupTaskAnnotations(pVerifyNode, verifyAnnots);

      if (!verifyAnnots.containsKey(NodePurpose.Verify))
        throw new PipelineException
          ("The supposed verify node (" + pVerifyNode + ") is not marked as a " +
           "Verify node.");

      if (!isSameTask(verifyTaskInfo))
        throw new PipelineException
          ("The task " + verifyTaskInfo  + " assigned to the supposed verify node () is " +
           "not the same as the task " + submitTaskInfo + " on the submit node.");

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
      else
        throw new PipelineException
          ("No checked-in version of the verify node (" + pVerifyNode + ") exists.");
    }
    
    private static final long serialVersionUID = 5741725732094455952L;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected
  class VerifyTaskPass
    extends ConstructPass
  {
    public 
    VerifyTaskPass()
    {
      super("Verify Task", 
            "Check-out the verify and submit node networks, regenerate any stale nodes " + 
            "in the verify network and check-in the changes.");
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
      
      
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pVerifyNode);
      pClient.checkOut(getAuthor(), getView(), pVerifyNode, null, 
                       CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      
      /* 
       * Checkout the submit node second, to guarantee the right versions are used.
       * Otherwise, the checkout of the verify node could bring out newer versions of
       * prepare nodes, if an older version of the submit node is being verified.  That 
       * would be less than optimal.
       */
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pSubmitNode);
      pClient.checkOut(getAuthor(), getView(), pSubmitNode, pSubmitNodeVersion, 
                       CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);

      addToQueueList(pVerifyNode);
      addToCheckInList(pVerifyNode);
    }

    private static final long serialVersionUID = -8940672311813235058L;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1615727654655141873L;
  
  public static final String aSourceNode = "SourceNode";
  public static final String aCustomWorkingArea = "CustomWorkingArea";
  
  
  
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
