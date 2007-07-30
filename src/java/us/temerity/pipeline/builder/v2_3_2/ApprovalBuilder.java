package us.temerity.pipeline.builder.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.VersionID.Level;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.SubmitNodeAnnotation.v2_3_2.SubmitNodeAnnotation;
import us.temerity.pipeline.plugin.TaskAnnotation.v2_3_2.TaskAnnotation;

/*------------------------------------------------------------------------------------------*/
/*   A P P R O V A L   B U I L D E R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 *  This class is the parent class for Builders which are meant to run as part
 *  of a submit/approve setup after a task is approved.
 *  <p>
 *  This class also includes a basic implementation of the Approval stage
 *  which can be used by tasks which do not need to implement their own
 *  Approval stage. 
 *
 */
public 
class ApprovalBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * @param name
   * @param vid
   * @param vendor
   * @param desc
   * @param mclient
   * @param qclient
   * @param builderInformation
   * @throws PipelineException
   */
  protected 
  ApprovalBuilder
  (
    String name,
    VersionID vid,
    String vendor,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super(name, vid, vendor, desc, mclient, qclient, builderInformation);
    
    {
      UtilityParam param = 
	new StringUtilityParam
	(aApproveNode,
	 "The name of the approval node for the task",
	 null);
      addParam(param);
    }
    {
      UtilityParam param = 
	new StringUtilityParam
	(aSubmitNode,
	 "The name of the submit node for the task",
	 null);
      addParam(param);
    }
    {
      String each[] = {"Major", "Minor", "Micro"};
      ArrayList<String> choices = new ArrayList<String>(Arrays.asList(each));
      UtilityParam param = 
	new EnumUtilityParam
	(aCheckinLevel,
	 "The level of the check-in for the approval nodea",
	 "Minor",
	 choices);
      addParam(param);
    }
    {
      UtilityParam param = 
	new StringUtilityParam
	(aApprovedBy,
	 "The user whose approval triggered this builder to run.",
	 null);
      addParam(param);
    }
    {
      UtilityParam param = 
	new StringUtilityParam
	(aApprovalMessage,
	 "The message that the Approved By user entered when approving the node.",
	 null);
      addParam(param);
    }
  }
  
  public
  ApprovalBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    this("BasicApprovalBuilder",
         new VersionID("2.3.2"),
         "Temerity",
         "This can be used for approve nodes with the most basic of setups.  In most" +
         "cases it will be necessary to override this class.",
         mclient,
         qclient,
         builderInformation);
    
    addSetupPass(new ApprovalInformationPass());
    addConstuctPass(new ActionPass());
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aUtilContext);
      layout.addSeparator();
      layout.addEntry(aActionOnExistance);
      layout.addEntry(aReleaseOnError);
      layout.addSeparator();
      layout.addEntry(aApproveNode);
      layout.addEntry(aSubmitNode);
      layout.addEntry(aApprovedBy);
      layout.addEntry(aApprovalMessage);
      layout.addEntry(aCheckinLevel);
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected TreeSet<String> 
  getNodesToCheckIn()
  {
    return getCheckInList();
  }
  
  @Override
  protected Level 
  getCheckinLevel() 
  {
    switch(pCheckinLevel) {
    case 0:
      return Level.Major;
    case 1:
      return Level.Minor;
    case 2:
      return Level.Micro;
   // This code is never reachable, since any other index will throw an exception earlier.
    default:  
      return Level.Minor;
    }
  }
  
  @Override
  protected boolean 
  performCheckIn()
  {
    return true;
  }
  
  @Override
  protected String 
  getCheckInMessage()
  {
    String toReturn = "Submitted by: " + pSubmitVer.getAuthor() + "\n";
    toReturn += "Notes: " + pSubmitVer.getMessage() + "\n\n";
    toReturn += "Approved by: " + pApprovedBy + "\n";
    toReturn += "Notes: " + pApprovalMessage;
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aApproveNode     = "ApproveNode";
  public static final String aSubmitNode      = "SubmitNode";
  public static final String aCheckinLevel    = "CheckinLevel";
  public static final String aApprovedBy      = "ApprovedBy";
  public static final String aApprovalMessage = "ApprovalMessage";
  
  private static final long serialVersionUID = -1802339350325535540L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  protected int pCheckinLevel;
  protected String pSubmitNode;
  protected String pApproveNode;
  protected String pTaskName;
  protected String pTaskType;
  protected String pApprovedBy;
  protected String pApprovalMessage;
  
  protected NodeVersion pSubmitVer;
  protected NodeVersion pApproveVer;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class ApprovalInformationPass
    extends SetupPass
  {
    public 
    ApprovalInformationPass()
    {
      super("Information Pass", 
            "Information pass for the Approval Builder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the validate phase in the Approval Information Pass.");
      validateBuiltInParams();
      
      // Make sure both nodes exist
      validateSubmitNode();
      validateApproveNode();
      
      //Derive a working area name from the task name and type.
      String workingArea = pTaskName + "_" + pTaskType;
      workingArea = workingArea.replaceAll(" ", "_");

      // Now we've got the context set so everything else that happens is correct.
      setContext(new UtilContext(PackageInfo.sPipelineUser, workingArea, pContext.getToolset()));
      
      pApprovedBy = getStringParamValue(new ParamMapping(aApprovedBy));
      pApprovalMessage = getStringParamValue(new ParamMapping(aApprovalMessage));
      
      pCheckinLevel = getEnumParamIndex(new ParamMapping(aCheckinLevel));
      if (!(pCheckinLevel == 0 || pCheckinLevel == 1 || pCheckinLevel == 2 ))
	throw new PipelineException
	  ("Invalid value specified for the CheckinLevel parameter.  " +
	   "Somehow the index returns was ("+ pCheckinLevel + ")");
    }
    
    protected final void
    validateSubmitNode()
      throws PipelineException
    {
      pSubmitNode = getStringParamValue(new ParamMapping(aSubmitNode));
      if (!nodeExists(pSubmitNode))
	throw new PipelineException
	  ("The nodename (" + pSubmitNode + ") passed in as the Submit Node is not a " +
	   "node in Pipeline.");
      
      BaseAnnotation annot = pClient.getAnnotation(pSubmitNode, "Submit");
      if (annot == null)
	throw new PipelineException
	  ("The node (" + pSubmitNode + ") passed in as the Submit Node does not have an annotation " +
	   "named (Submit) on it.");
      if (! (annot instanceof SubmitNodeAnnotation))
	throw new PipelineException
	  ("The annotation named (Submit) on the Submit Node (" + pSubmitNode + ") " +
	   "is not the SubmitNodeAnnotation (v2_3_2)");
      pTaskType = (String) annot.getParamValue(SubmitNodeAnnotation.aTaskType);
      pTaskName = (String) annot.getParamValue(SubmitNodeAnnotation.aTaskName);
      
      pSubmitVer = pClient.getCheckedInVersion(pSubmitNode, null);
    }
    
    protected final void
    validateApproveNode()
      throws PipelineException
    {
      pApproveNode = getStringParamValue(new ParamMapping(aApproveNode));
      if (!nodeExists(pApproveNode))
	throw new PipelineException
	  ("The nodename (" + pApproveNode + ") passed in as the Approve Node is not a " +
	   "node in Pipeline.");
      BaseAnnotation annot = pClient.getAnnotation(pApproveNode, "Task");
      if (annot == null)
	throw new PipelineException
	  ("The node (" + pApproveNode+ ") passed in as the Approve Node does not have an annotation " +
	   "named (Task) on it.");
      if (! (annot instanceof TaskAnnotation))
	throw new PipelineException
	  ("The annotation named (Task) on the Approve Node (" + pApproveNode + ") " +
	   "is not the TaskAnnotation (v2_3_2)");
      String purpose = (String) annot.getParamValue(TaskAnnotation.aPurpose);
      if (!purpose.equals(TaskAnnotation.aApprove))
	throw new PipelineException
	  ("The Task Annotation on the Approve Node (" + pApproveNode + ") does not have its " +
	   "Purpose parameter set to Approve.");
      String taskName = (String) annot.getParamValue(TaskAnnotation.aTaskName);
      String taskType = (String) annot.getParamValue(TaskAnnotation.aTaskType);
      if ( !taskName.equals(pTaskName) || !taskType.equals(pTaskType) )
	throw new PipelineException
	  ("The Task Name (" + taskName + ") and Task Type (" + taskType + ") on the " +
	   "Approval Node (" + pApproveNode + ") do not match the " +
	   "Task Name (" + pTaskName + ") and Task Type (" + pTaskType + ") on the " +
	   "Submit Node (" + pSubmitNode + ").");
      
      pApproveVer = pClient.getCheckedInVersion(pApproveNode, null);
    }
    private static final long serialVersionUID = -3515531414594993800L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected
  class ActionPass
    extends ConstructPass
  {
    public 
    ActionPass()
    {
      super("Action Pass", 
            "Action pass for the Approval Builder");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the build phase in the Action Pass.");
      
      pClient.checkOut(getAuthor(), getView(), pSubmitNode, null, 
	CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
      pClient.checkOut(getAuthor(), getView(), pApproveNode, null, 
	CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
      addToQueueList(pApproveNode);
      addToCheckInList(pApproveNode);
    }
    private static final long serialVersionUID = -807715390256064504L;
  }
}

