package us.temerity.pipeline.plugin.ApprovalCollection.v2_3_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.VersionID.Level;
import us.temerity.pipeline.builder.*;

import java.util.*;

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
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, builderInformation);
    
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
	(aCheckInLevel,
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
    this("ApprovalBuilder",
         "This can be used for approve nodes with the most basic of setups.  In most" +
         "cases it will be necessary to override this class.",
         mclient,
         qclient,
         builderInformation);
    
    addSetupPass(new ApprovalInformationPass());
    addConstructPass(new ActionPass());
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aUtilContext);
      layout.addSeparator();
      layout.addEntry(aActionOnExistence);
      layout.addEntry(aReleaseOnError);
      layout.addSeparator();
      layout.addEntry(aApproveNode);
      layout.addEntry(aSubmitNode);
      layout.addEntry(aApprovedBy);
      layout.addEntry(aApprovalMessage);
      layout.addEntry(aCheckInLevel);
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected LinkedList<String> 
  getNodesToCheckIn()
  {
    return getCheckInList();
  }
  
  protected Level 
  getCheckInLevel() 
  {
    switch(pCheckInLevel) {
    case 0:
      return Level.Major;
    case 1:
      return Level.Minor;
    case 2:
      return Level.Micro;

    default:  
      throw new IllegalStateException("Unknown check-in versioning level!");
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
    String toReturn = "Submitted by: " + pSubmitVsn.getAuthor() + "\n";
    toReturn += "Notes: " + pSubmitVsn.getMessage() + "\n\n";
    toReturn += "Approved by: " + pApprovedBy + "\n";
    toReturn += "Notes: " + pApprovalMessage;
    
    return toReturn;
  }
  
  
  
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
      pLog.log
        (LogMgr.Kind.Ops,LogMgr.Level.Fine, 
         "Starting the validate phase in the Approval Information Pass.");
      validateBuiltInParams();

      /* make sure the Submit node exists, has the correct annotation and matches 
         task name/type passed to the builder. */ 
      {
        pSubmitNode = getStringParamValue(new ParamMapping(aSubmitNode), false);
        if (!nodeExists(pSubmitNode))
          throw new PipelineException
	  ("The node (" + pSubmitNode + ") passed in as the Submit Node is not valid!"); 
        
        boolean validated = false;
        TreeMap<String,BaseAnnotation> annotations = pClient.getAnnotations(pSubmitNode); 
        for(String aname : annotations.keySet()) {
          BaseAnnotation an = annotations.get(aname);
          if(an.getName().equals(aSubmitNode)) {
            pTaskName = lookupTaskName(pSubmitNode, an); 
            pTaskType = lookupTaskType(pSubmitNode, an); 
            validated = true;
            break;
          }
        }

        if(!validated) 
          throw new PipelineException
            ("Unable to find any SubmitNode Annotation on the given submit node " + 
             "(" + pSubmitNode + ")!");
        
        pSubmitVsn = pClient.getCheckedInVersion(pSubmitNode, null);
      }
    
      /* make sure the Submit node exists, has the correct annotation and matches 
         task name/type passed to the builder. */ 
      {
        pApproveNode = getStringParamValue(new ParamMapping(aApproveNode), false);
        if (!nodeExists(pApproveNode))
          throw new PipelineException
	  ("The node (" + pApproveNode + ") passed in as the Approve Node is not valid!"); 
        
        boolean validated = false;
        TreeMap<String,BaseAnnotation> annotations = pClient.getAnnotations(pApproveNode); 
        for(String aname : annotations.keySet()) {
          BaseAnnotation an = annotations.get(aname);
          if(an.getName().equals(aTask)) {
            String purpose = lookupPurpose(pApproveNode, an);
            if(purpose.equals(aApprove)) {
              verifyTask(aApproveNode, an, pTaskName, pTaskType);
              validated = true;
              break;
            }
          }
        }

        if(!validated) 
          throw new PipelineException
            ("Unable to find any Task Annotation with a Purpose of Approve on the given " + 
             "approve node (" + pApproveNode + ")!");
        
        pApproveVsn = pClient.getCheckedInVersion(pApproveNode, null);
      }
      
      /* derive a working area name from the task name and type */ 
      String workingArea = getName() + "-" + pTaskName + "-" + pTaskType;
      workingArea = workingArea.replaceAll(" ", "_");

      /* now we've got the context set so everything else that happens is correct */ 
      setContext(new UtilContext(PackageInfo.sPipelineUser, workingArea, 
                                 pContext.getToolset()));
      
      pApprovedBy = getStringParamValue(new ParamMapping(aApprovedBy), false);
      pApprovalMessage = getStringParamValue(new ParamMapping(aApprovalMessage), false);
      
      pCheckInLevel = getEnumParamIndex(new ParamMapping(aCheckInLevel));
      switch(pCheckInLevel) {
      case 0:
      case 1:
      case 2:
        break;

      default:
	throw new PipelineException
	  ("Invalid value specified for the CheckInLevel parameter.  " +
	   "Somehow the index returns was ("+ pCheckInLevel + ")");
      }
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
      pLog.log
        (LogMgr.Kind.Ops,LogMgr.Level.Fine, 
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


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup the Task Name from the annotation.
   */ 
  private String 
  lookupTaskName
  (
    String nodeName, 
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String taskName = (String) an.getParamValue(aTaskName);
    if((taskName == null) || (taskName.length() == 0))
      throw new PipelineException
        ("A TaskName must be supplied for the " + an.getName() + " annotation on node " + 
         "(" + nodeName +")!"); 

    return taskName;
  }

  /**
   * Lookup the Task Type from the annotation.
   */ 
  private String 
  lookupTaskType
  (
    String nodeName, 
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String taskType = (String) an.getParamValue(aTaskType);
    if((taskType == null) || (taskType.length() == 0))
      throw new PipelineException
        ("A TaskType must be supplied for the " + an.getName() + " annotation on node " + 
         "(" + nodeName +")!"); 

    return taskType;
  }

  /**
   * Lookup the annotation Purpose.
   */ 
  private String
  lookupPurpose
  (
    String nodeName, 
    BaseAnnotation an   
  ) 
    throws PipelineException
  {
    if(an.getName().equals(aSubmitNode)) 
      return aSubmit; 
   
    String purpose = (String) an.getParamValue(aPurpose);
    if(purpose == null) 
      throw new PipelineException
        ("A Purpose must be supplied for the Task annotation on node " + 
         "(" + nodeName +")!"); 

    return purpose;
  }

  /**
   * Verify that the TaskName and TaskType match those supplied to the builder.
   */ 
  private void 
  verifyTask
  (
   String nodeName, 
   BaseAnnotation an,
   String builderTaskName, 
   String builderTaskType
  ) 
    throws PipelineException
  {
    String taskName = lookupTaskName(nodeName, an); 
    String taskType = lookupTaskType(nodeName, an); 

    if(!taskName.equals(builderTaskName) || !taskType.equals(builderTaskType)) {
      throw new PipelineException
        ("The node (" + nodeName + ") belongs to a different task (" + taskName + ":" + 
         taskType + ") than the task specified to the builder (" + builderTaskName + ":" + 
         builderTaskType + ")!");
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  protected int pCheckInLevel;
  protected String pSubmitNode;
  protected String pApproveNode;
  protected String pTaskName;
  protected String pTaskType;
  protected String pApprovedBy;
  protected String pApprovalMessage;
  
  protected NodeVersion pSubmitVsn;
  protected NodeVersion pApproveVsn;
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1802339350325535540L;

  public static final String aApproveNode     = "ApproveNode";
  public static final String aSubmitNode      = "SubmitNode";
  public static final String aCheckInLevel    = "CheckInLevel";
  public static final String aApprovedBy      = "ApprovedBy";
  public static final String aApprovalMessage = "ApprovalMessage";
  
  /*----------------------------------------------------------------------------------------*/
    
  public static final String aTaskName         = "TaskName";
  public static final String aTaskType         = "TaskType";
  public static final String aPurpose          = "Purpose";
  public static final String aIsApproved       = "IsApproved";
  public static final String aAssignedTo       = "AssignedTo";
  public static final String aBuilderPath      = "BuilderPath";

  public static final String aTask             = "Task";

  public static final String aSubmit           = "Submit";
  public static final String aEdit             = "Edit";
  public static final String aPrepare          = "Prepare";
  public static final String aFocus            = "Focus";
  public static final String aThumbnail        = "Thumbnail";
  public static final String aProduct          = "Product";
  public static final String aApprove          = "Approve";

}

