package us.temerity.pipeline.plugin.TaskCollection.v2_4_28;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_28.*;
import us.temerity.pipeline.builder.v2_4_28.TaskBuilder;
import us.temerity.pipeline.plugin.TaskCollection.v2_4_28.RunVerifyBuilder.*;

/*------------------------------------------------------------------------------------------*/
/*   R U N   P U B L I S H   B U I L D E R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Builder which is meant to be run by a tool or an  automated process, which will find 
 * and checkout the run approve node for a task, lock the right version of the verify node 
 * (and connect it), and then queue the node.
 */
public 
class RunPublishBuilder
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
  RunPublishBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("RunPublish",
         "A builder which finds and runs the node that runs the publish builder.", 
         mclient, qclient, builderInfo, EntityType.Ignore);
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (aVerifyNode, 
         "The verify node that is being published.", 
         null);
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (aVerifyVersion, 
         "The version of the verify node that is being published.", 
         null);
      addParam(param);
    }
    
    addReleaseViewParam();
    
    /* not really applicable to this builder, so hide it from the users */ 
    disableParam(new ParamMapping(aActionOnExistence));
    
    addSetupPass(new ValidateParamsPass());
    addConstructPass(new RunPublishPass());
    
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aUtilContext);
    layout.addSeparator();
    layout.addEntry(aActionOnExistence);  
    layout.addEntry(aReleaseOnError);     
    layout.addEntry(aReleaseView);
    layout.addSeparator();
    layout.addEntry(aVerifyNode);
    layout.addEntry(aVerifyVersion);
    
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
      pVerifyNode = getStringParamValue(new ParamMapping(aVerifyNode), false);
      if (!nodeExists(pVerifyNode))
        throw new PipelineException
          ("The node name submitted as the source node (" + pVerifyNode + ") is not a valid " +
           "Pipeline node.");
      
      String verifyVer = getStringParamValue(new ParamMapping(aVerifyVersion), false);
      if (!VersionID.isValidVersionID(verifyVer))
        throw new PipelineException
          ("The VerifyVersion param value (" + verifyVer + ") is not a valid version ID");
      
      pVerifyVersion = new VersionID(verifyVer);
      
      if (!checkedInVersionExists(pVerifyNode))
        throw new PipelineException
          ("The node name submitted as the source node (" + pVerifyNode + ") does not have " +
           "any checked-in versions to publish.");
      
      TreeSet<VersionID> vers = pClient.getCheckedInVersionIDs(pVerifyNode);
      if (!vers.contains(pVerifyVersion))
        throw new PipelineException
          ("The verify version (" + pVerifyVersion + ") is not a valid version of the " +
           "verify node (" + pVerifyNode + ")");
      

      TreeMap<NodePurpose, BaseAnnotation> verifyAnnots = 
        new TreeMap<NodePurpose, BaseAnnotation>();
      String verifyTaskInfo[] = lookupTaskAnnotations(pVerifyNode, verifyAnnots);
      
      if (!verifyAnnots.containsKey(NodePurpose.Verify))
        throw new PipelineException
          ("The node submitted as the source node (" + pVerifyNode + ") is not a " +
           "Verify node.");
      
      setTaskInformation(verifyTaskInfo[0], verifyTaskInfo[1], 
                         verifyTaskInfo[2], verifyTaskInfo[3]);
      
      NodeVersion ver = pClient.getCheckedInVersion(pVerifyNode, pVerifyVersion);
      
      pCheckInMessage = 
        "Publishing the following verified version:\n" + 
        "VERIFY NODE: " + pVerifyNode + " (v" + ver.getVersionID() + ")\n" +
        "SUBMITTED BY: " + ver.getAuthor() + "\n" +
        "SUBMISSION NOTES: " + ver.getMessage() + "\n";
      
      pPublishWorkingArea = 
        verifyTaskInfo[0] + "_" + verifyTaskInfo[1] + "_" + verifyTaskInfo[2] + "_" + 
        verifyTaskInfo[3] + "_publish";
      
      pRunPublishBuilderNode = getDefaultPublishBuilderNodeName();
      
      if (!checkedInVersionExists(pRunPublishBuilderNode))
        throw new PipelineException
        ("The node that is to be used to queue the publish node " +
         "(" + pRunPublishBuilderNode + ") does not exist in the Pipeline repository.");
      
      pCheckInLevel = VersionID.Level.Minor;
      NodeVersion publishVer = pClient.getCheckedInVersion(pRunPublishBuilderNode, null);
      String message = publishVer.getMessage();
      String buffer[] = message.split("\\(");
      if (buffer.length > 1) {
        buffer = buffer[1].split("\\)");
        String vid = buffer[0].replaceAll("v", "");
        if (VersionID.isValidVersionID(vid)) {
          VersionID id = new VersionID(vid);
          pCheckInLevel = id.compareLevel(pVerifyVersion);
        }
      }
      
      int count = 0;
      
      TreeMap<String, TreeSet<String>> areas  = pClient.getWorkingAreas();
      while (areas.get(getAuthor()) != null && 
             areas.get(getAuthor()).contains(pPublishWorkingArea)) {
        if (count > 6)
          throw new PipelineException
            ("The working area (" + pPublishWorkingArea + ") that would be used to run the " +
             "verify exists and has existed for over a half-hour.  Giving up on trying to " +
             "verify this submit node.");
        try {
          Thread.sleep(1000 * 60 * 5);
        }
        catch (InterruptedException ex) {
          throw new PipelineException(ex);
        }
        count++;
        areas = pClient.getWorkingAreas();
      }
      pClient.createWorkingArea(getAuthor(), pPublishWorkingArea);
    }

    private static final long serialVersionUID = 6129199628367536599L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected
  class RunPublishPass
    extends ConstructPass
  {
    public 
    RunPublishPass()
    {
      super("Run Publish", 
            "Check-out the publish builder node, lock the verify node, hook the verify node" +
            "up to the publish builder node, and then queue the publish builder node.");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      setContext(new UtilContext(getAuthor(), pPublishWorkingArea, getToolset()));
      checkOutLatest
        (pRunPublishBuilderNode, CheckOutMode.OverwriteAll,CheckOutMethod.FrozenUpstream);
      
      pClient.lock(getAuthor(), getView(), pVerifyNode, pVerifyVersion);
      
      pClient.link(getAuthor(), getView(), pRunPublishBuilderNode, pVerifyNode, 
                   LinkPolicy.Dependency);
      
      NodeMod mod = getWorkingVersion(pRunPublishBuilderNode);
      
      BaseAction act = mod.getAction();
      act.setSingleParamValue("SourceNode", pVerifyNode);
      
      mod.setAction(act);
      pClient.modifyProperties(getAuthor(), getView(), mod);
      
      addToQueueList(pRunPublishBuilderNode);
      addToCheckInList(pRunPublishBuilderNode);
    }

    private static final long serialVersionUID = 709315262923523639L;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3177381020177255328L;
  
  public static final String aVerifyNode = "VerifyNode";
  public static final String aVerifyVersion = "VerifyVersion";


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private String pCheckInMessage;
  private String pVerifyNode;
  private VersionID pVerifyVersion;
  
  private VersionID.Level pCheckInLevel;
  
  private String pRunPublishBuilderNode;
  
  private String pPublishWorkingArea;
}