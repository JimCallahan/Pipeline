package us.temerity.pipeline.plugin.TaskCollection.v2_4_28;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_28.*;
import us.temerity.pipeline.builder.v2_4_28.TaskBuilder;

/*------------------------------------------------------------------------------------------*/
/*   R U N   V E R I F Y   B U I L D E R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Builder which is meant to be run by a server-side extension (or other automated process), 
 * which will find and checkout the run verify node for a task, lock the right version of the 
 * submit node (and connect it), and then queue the node.
 */
public 
class RunVerifyBuilder
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
  RunVerifyBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("RunVerify",
         "A builder which finds and runs the node that runs the verify builder.", 
         mclient, qclient, builderInfo, EntityType.Ignore);
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (aSubmitNode, 
         "The submit node that is being verified.", 
         null);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (aSubmitVersion, 
         "The version of the submit node that is being verified.", 
         null);
      addParam(param);
    }
    
    addReleaseViewParam();
    
    /* not really applicable to this builder, so hide it from the users */ 
    disableParam(new ParamMapping(aActionOnExistence));
    
    addSetupPass(new ValidateParamsPass());
    addConstructPass(new RunVerifyPass());
    
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aUtilContext);
    layout.addSeparator();
    layout.addEntry(aActionOnExistence);  
    layout.addEntry(aReleaseOnError);     
    layout.addEntry(aReleaseView);
    layout.addSeparator();
    layout.addEntry(aSubmitNode);
    layout.addEntry(aSubmitVersion);
    
    setParamValue(aReleaseView, ReleaseView.Always.toString());
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
  class ValidateParamsPass
    extends SetupPass
  {
    public 
    ValidateParamsPass()
    {
      super("Validate Params", 
            "Make sure all the param values are good and all necessary nodes exist."); 
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      /* sets up the built-in parameters common to all builders */    
      validateBuiltInParams();
      
      /* Deal with the submit node. */
      pSubmitNode = getStringParamValue(new ParamMapping(aSubmitNode), false);
      if (!nodeExists(pSubmitNode))
        throw new PipelineException
          ("The node name submitted as the source node (" + pSubmitNode + ") is not a valid " +
           "Pipeline node.");
      
      String submitVer = getStringParamValue(new ParamMapping(aSubmitVersion), false);
      if (!VersionID.isValidVersionID(submitVer))
        throw new PipelineException
          ("The SubmitVersion param value (" + submitVer + ") is not a valid version ID");
      
      pSubmitVersion = new VersionID(submitVer);
      
      if (!checkedInVersionExists(pSubmitNode))
        throw new PipelineException
          ("The node name submitted as the source node (" + pSubmitNode + ") does not have " +
           "any checked-in versions to verify.");
      
      TreeSet<VersionID> vers = pClient.getCheckedInVersionIDs(pSubmitNode);
      if (!vers.contains(pSubmitVersion))
        throw new PipelineException
          ("The submit version (" + pSubmitVersion + ") is not a valid version of the " +
           "submit node (" + pSubmitNode + ")");
      

      TreeMap<NodePurpose, BaseAnnotation> submitAnnots = 
        new TreeMap<NodePurpose, BaseAnnotation>();
      String submitTaskInfo[] = lookupTaskAnnotations(pSubmitNode, submitAnnots);
      
      if (!submitAnnots.containsKey(NodePurpose.Submit))
        throw new PipelineException
          ("The node submitted as the source node (" + pSubmitNode + ") is not a " +
           "Submit node.");
      
      setTaskInformation(submitTaskInfo[0], submitTaskInfo[1], 
                         submitTaskInfo[2], submitTaskInfo[3]);
      
      NodeVersion ver = pClient.getCheckedInVersion(pSubmitNode, pSubmitVersion);
      
      pCheckInMessage = 
        "Verified the following submit version:\n" + 
        "SUBMIT NODE: " + pSubmitNode + " (v" + ver.getVersionID() + ")\n" +
        "SUBMITTED BY: " + ver.getAuthor() + "\n" +
        "SUBMISSION NOTES: " + ver.getMessage() + "\n";
      
      pVerifyWorkingArea = 
        submitTaskInfo[0] + "_" + submitTaskInfo[1] + "_" + submitTaskInfo[2] + "_" + 
        submitTaskInfo[3] + "_verify";
      
      pRunVerifyBuilderNode = getDefaultVerifyBuilderNodeName();
      
      if (!checkedInVersionExists(pRunVerifyBuilderNode))
        throw new PipelineException
        ("The node that is to be used to queue the verify node " +
         "(" + pRunVerifyBuilderNode + ") does not exist in the Pipeline repository.");
      
      pCheckInLevel = VersionID.Level.Minor;
      NodeVersion verifyVer = pClient.getCheckedInVersion(pRunVerifyBuilderNode, null);
      String message = verifyVer.getMessage();
      String buffer[] = message.split("\\(");
      if (buffer.length > 1) {
        buffer = buffer[1].split("\\)");
        String vid = buffer[0].replaceAll("v", "");
        if (VersionID.isValidVersionID(vid)) {
          VersionID id = new VersionID(vid);
          pCheckInLevel = id.compareLevel(pSubmitVersion);
        }
      }
      
      int count = 0;
      
      TreeMap<String, TreeSet<String>> areas  = pClient.getWorkingAreas();
      while (areas.get(getAuthor()) != null &&
             areas.get(getAuthor()).contains(pVerifyWorkingArea)) {
        if (count > 6)
          throw new PipelineException
            ("The working area (" + pVerifyWorkingArea + ") that would be used to run the " +
             "verify exists and has existed for over a half-hour.  Giving up on trying to " +
             "verify this submit node.");
        try {
          Thread.sleep(1000 * 60 * 5);
        }
        catch (InterruptedException ex) {
          throw new PipelineException(ex);
        }
        areas = pClient.getWorkingAreas();
        count++;
      }
      pClient.createWorkingArea(getAuthor(), pVerifyWorkingArea);
    }

    private static final long serialVersionUID = 8078082064441960846L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected
  class RunVerifyPass
    extends ConstructPass
  {
    public 
    RunVerifyPass()
    {
      super("Run Verify", 
            "Check-out the verify builder node, lock the submit node, hook the submit node" +
            "up to the verify builder node, and then queue the verify builder node.");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      setContext(new UtilContext(getAuthor(), pVerifyWorkingArea, getToolset()));
      
      pLog.log
        (Kind.Ops, Level.Fine, 
         "Checking out the run verify builder node: " + pRunVerifyBuilderNode);

      checkOutLatest
        (pRunVerifyBuilderNode, CheckOutMode.OverwriteAll,CheckOutMethod.FrozenUpstream);
      
      pLog.log
        (Kind.Ops, Level.Fine, 
         "Locking submit node: " + pSubmitNode + "(v" + pSubmitVersion + ")");
      
      pClient.lock(getAuthor(), getView(), pSubmitNode, pSubmitVersion);
      
      pClient.link(getAuthor(), getView(), pRunVerifyBuilderNode, pSubmitNode, 
                   LinkPolicy.Dependency);
      
      NodeMod mod = getWorkingVersion(pRunVerifyBuilderNode);
      
      BaseAction act = mod.getAction();
      act.setSingleParamValue("SourceNode", pSubmitNode);
      
      mod.setAction(act);
      pClient.modifyProperties(getAuthor(), getView(), mod);
      
      pClient.removeFiles(getAuthor(), getView(), pRunVerifyBuilderNode, null);
      
      addToQueueList(pRunVerifyBuilderNode);
      addToCheckInList(pRunVerifyBuilderNode);
    }

    private static final long serialVersionUID = -5689017573334036909L;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1294337476878107148L;
  
  public static final String aSubmitNode = "SubmitNode";
  public static final String aSubmitVersion = "SubmitVersion";


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private String pCheckInMessage;
  private String pSubmitNode;
  private VersionID pSubmitVersion;
  
  private VersionID.Level pCheckInLevel;
  
  private String pRunVerifyBuilderNode;
  
  private String pVerifyWorkingArea;
}
