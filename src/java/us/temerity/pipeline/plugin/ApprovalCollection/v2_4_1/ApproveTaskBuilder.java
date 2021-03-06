// $Id: ApproveTaskBuilder.java,v 1.7 2009/04/06 00:53:11 jesse Exp $

package us.temerity.pipeline.plugin.ApprovalCollection.v2_4_1;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;

/*------------------------------------------------------------------------------------------*/
/*   A P P R O V E   T A S K   B U I L D E R                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder which implements a generic task approval operation. <P> 
 * 
 * This builder relies on the ApproveTask, SubmitTask and Task annotation plugins (v2.4.1)
 * to be associated with the proper node in order for it to function properly. <P> 
 * 
 * This builder defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Approve Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     The fully resolved name of the approve node for the task.
 *   </DIV> <BR>
 * 
 *   Submit Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     The fully resolved name of the submit node for the task.
 *   </DIV> <BR>
 * 
 *   Submit Version <BR>
 *   <DIV style="margin-left: 40px;">
 *     The revision number of the submit node being approved.  If unset, the latest 
 *     checked-in version will be approved.
 *   </DIV> <BR>
 * 
 *   Check In Level <BR>
 *   <DIV style="margin-left: 40px;">
 *     The level of the check-in for the approve node.
 *   </DIV> <BR>
 * 
 *   Approval Message <BR>
 *   <DIV style="margin-left: 40px;">
 *     The check-in message given by the user approving this task.
 *   </DIV> <BR>
 * </DIV>
 */
public 
class ApproveTaskBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Required constructor for to launch the builder.
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
  ApproveTaskBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("ApproveTask",
         "A builder which implements a generic task approval operation.", 
         mclient, qclient, builderInfo, EntityType.Ignore);
    
    addDefaultParams();
    
    addReleaseViewParam();
    
    /* create the setup passes */ 
    addSetupPass(new LookupAndValidate());

    /* create the construct passes */ 
    addConstructPass(new CheckOutNetworks());

    setLayout(getDefaultLayoutGroup());
  }

  /**
   * Constructor used by subclasses extending the base approve builder functionality.
   * <p>
   * This does not setup any of the parameters or layouts that the ApprovalBuilder normally
   * has.  These are available in two methods, the {@link #addDefaultParams()} method to add
   * the parameters and the {@link #getDefaultLayoutGroup()} method to get the default layout
   * group for those parameters.  The SetupPass functionality can be added by adding an 
   * instance of the {@link LookupAndValidate} class.  The ConstructPass functionality can 
   * be added by adding an instance of the {@link CheckOutNetworks} class.
   * 
   * @param name
   *   Name of the builder.
   * 
   * @param desc
   *   Description of the builder's functionality.
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
  ApproveTaskBuilder
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, builderInfo , null);
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S U B - C L A S S   F U N C T I O N A L I T Y                                        */
  /*----------------------------------------------------------------------------------------*/

  protected final PassLayoutGroup
  getDefaultLayoutGroup()
  {
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aUtilContext);
    layout.addSeparator();
    layout.addEntry(aActionOnExistence);  
    layout.addEntry(aReleaseOnError);     
    layout.addSeparator();
    layout.addEntry(aSubmitNode);
    layout.addEntry(aSubmitVersion);
    layout.addSeparator();
    layout.addEntry(aApproveNode);
    layout.addEntry(aCheckInLevel);
    layout.addEntry(aApprovalMessage);
    layout.addSeparator();
    layout.addEntry(aReleaseView);

    PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
    return finalLayout;
  }
  
  /**
   * Adds the generic approval builder params to the builder.
   * 
   * <DIV style="margin-left: 40px;">
   *   Approve Node <BR>
   *   <DIV style="margin-left: 40px;">
   *     The fully resolved name of the approve node for the task.
   *   </DIV> <BR>
   * 
   *   Submit Node <BR>
   *   <DIV style="margin-left: 40px;">
   *     The fully resolved name of the submit node for the task.
   *   </DIV> <BR>
   * 
   *   Submit Version <BR>
   *   <DIV style="margin-left: 40px;">
   *     The revision number of the submit node being approved.  If unset, the latest 
   *     checked-in version will be approved.
   *   </DIV> <BR>
   * 
   *   Check In Level <BR>
   *   <DIV style="margin-left: 40px;">
   *     The level of the check-in for the approve node.
   *   </DIV> <BR>
   * 
   *   Approval Message <BR>
   *   <DIV style="margin-left: 40px;">
   *     The check-in message given by the user approving this task.
   *   </DIV> <BR>
   * </DIV>
   */
  protected final void
  addDefaultParams()
  {
    /* setup builder parameters */ 
    {
      /* not really applicable to this builder, so hide it from the users */ 
      disableParam(new ParamMapping(aActionOnExistence));
      
      {
        UtilityParam param = 
          new StringUtilityParam
          (aApproveNode,
           "The fully resolved name of the approve node for the task.",
           null);
        addParam(param);
      }

      {
        UtilityParam param = 
          new StringUtilityParam
          (aSubmitNode,
           "The fully resolved name of the submit node for the task.",
           null);
        addParam(param);
      }

      {
        UtilityParam param = 
          new StringUtilityParam
          (aSubmitVersion, 
           "The revision number of the submit node being approved.  If unset, the latest " + 
           "checked-in version will be approved.", 
           null);
        addParam(param);
      }

      {
        ArrayList<String> choices = new ArrayList<String>(); 
        for(VersionID.Level level : VersionID.Level.all()) 
          choices.add(level.toString()); 

        UtilityParam param = 
          new EnumUtilityParam
          (aCheckInLevel,
           "The level of the check-in for the approve node.",
           VersionID.Level.Minor.toString(), 
           choices);
        addParam(param);
      }

      {
        UtilityParam param = 
          new StringUtilityParam
          (aApprovalMessage,
           "The check-in message given by the user approving this task.", 
           null);
        addParam(param);
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N   O V E R R I D E S                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns a list of nodes to be checked-in.
   */ 
  @Override
  public LinkedList<String> 
  getNodesToCheckIn()
  {
    return getCheckInList();
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
  public boolean 
  performCheckIn()
  {
    return true;
  }
  
  /**
   * The check-in message that is associated with this Builder.
   */
  @Override
  public String 
  getCheckInMessage()
  {
    return 
      (pApprovalMessage + "\n\n" + 
       "---------------------------------------------------\n" + 
       "SUBMIT NODE: " + pSubmitVsn.getName() + " (v" + pSubmitVsn.getVersionID() + ")\n" +
       "SUBMITTED BY: " + pSubmitVsn.getAuthor() + "\n" +
       "SUBMISSION NOTES: " + pSubmitVsn.getMessage() + "\n"); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Validates an annotation's param values against the values extracted from the Approve
   * Node.
   * <p>
   * This method should only be called after the validatePhase() method of the 
   * {@link LookupAndValidate} pass has been called.  If that method is not being used, then 
   * {@link #pProjectName}, {@link #pTaskName}, and {@link #pTaskType} all need to have valid,
   * non-null values before this method is called.
   * 
   * @param aname
   *   The name of the annotation.
   *   
   * @param projectName
   *   The name of the project being checked for correctness.
   *   
   * @param taskName
   *   The name of the task being checked for correctness
   *   
   * @param taskType
   *   The type of the task being checked for correctness.
   */
  protected void
  validateTaskAnnotation
  (
    String aname,
    String projectName,
    String taskName,
    String taskType
  )
    throws PipelineException
  {
    if(!projectName.equals(pProjectName)) 
      throw new PipelineException
        ("The " + aAnnotProjectName + " parameter supplied for the " + 
         "(" + aname + ") annotation on the submit node (" + pSubmitNode + ") " + 
         "does not match the " + aAnnotProjectName + " parameter " + 
         "(" + pProjectName + ") of the approve node!"); 

    if(!taskName.equals(pTaskName)) 
      throw new PipelineException
        ("The " + aAnnotTaskName + " parameter supplied for the (" + aname + ") " + 
         "annotation on the submit node (" + pSubmitNode + ") does not match the " + 
         aAnnotTaskName + " parameter (" + pTaskName + ") of the approve node!"); 
    
    if(!taskType.equals(pTaskType)) 
      throw new PipelineException
        ("The " + aAnnotTaskType + " parameter supplied for the (" + aname + ") " + 
         "annotation on the submit node (" + pSubmitNode + ") does not match the " + 
         aAnnotTaskType + " parameter (" + pTaskType + ") of the approve node!"); 
    
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
            "Lookup the submit/approve nodes and validate their annotations and other " + 
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

      /* make sure the approve node exists and has a valid task annotation */ 
      {
        pApproveNode = getStringParamValue(new ParamMapping(aApproveNode), false);
        if(!nodeExists(pApproveNode))
          throw new PipelineException
	    ("The node specified as the " + aApproveNode + " node (" + pApproveNode + ") " + 
	     "does not exist!"); 

        boolean validated = false;
	String approvePurpose = NodePurpose.Approve.toString();
        TreeMap<String,BaseAnnotation> annotations = getTaskAnnotations(pApproveNode);
        for(String aname : annotations.keySet()) {
	    BaseAnnotation annot = annotations.get(aname);
	    if(lookupPurpose(pApproveNode, aname, annot).equals(approvePurpose)) { 
	      
	      /* save the task information */ 
	      pProjectName = lookupProjectName(pApproveNode, aname, annot);
	      pTaskName    = lookupTaskName(pApproveNode, aname, annot);
	      pTaskType    = lookupTaskType(pApproveNode, aname, annot);

	      /* determine if the user running the builder should be allowed to 
	           approve the task */ 	     
	      String vised = (String) annot.getParamValue(aAnnotSupervisedBy);
	      if((vised != null) && (vised.length() > 0)) {
		WorkGroups wgroups = pClient.getWorkGroups();
		boolean isGroup = wgroups.isGroup(vised); 
		String author = pContext.getAuthor();
		if((!isGroup && !vised.equals(author)) || 
		   (isGroup && wgroups.isMemberOrManager(author, vised) == null)) 
		  throw new PipelineException
		    ("The " + aAnnotSupervisedBy + " parameter for the " + aApproveNode + 
		     " (" + pApproveNode + ") is assigned to the " + 
		     (isGroup ? "Pipeline work group [" + vised + "]" : 
		      "user (" + vised + ")") + 
		     ".  The (" + author + ") user is not allowed to approve the task!");
	      }
	    
	      validated = true;
	      break;
	    }
	}

        if(!validated) 
          throw new PipelineException
            ("Unable to find an valid task annotation for the " + aApproveNode + 
	     " node (" + pApproveNode + ")!"); 
      }

      /* make sure the submit node exists, has a valid annotation which matches the
	  task of the approve node */ 
      {
        pSubmitNode = getStringParamValue(new ParamMapping(aSubmitNode), false);
        if(!nodeExists(pSubmitNode))
          throw new PipelineException
	    ("The node specified as the " + aSubmitNode + " node (" + pSubmitNode + ") " + 
	     "does not exist!"); 

        boolean validated = false;
        TreeMap<String,BaseAnnotation> annotations = getTaskAnnotations(pSubmitNode); 
        for(String aname : annotations.keySet()) {
	    BaseAnnotation annot = annotations.get(aname);

	    String projectName = lookupProjectName(pSubmitNode, aname, annot); 
	    String taskName = lookupTaskName(pSubmitNode, aname, annot); 
	    String taskType = lookupTaskType(pSubmitNode, aname, annot); 
	    
	    validateTaskAnnotation(aname, projectName, taskName, taskType);
	    validated = true;
	    break;
        }

        if(!validated) 
          throw new PipelineException
            ("Unable to find an valid task annotation for the " + aSubmitNode + 
	     " node (" + pSubmitNode + ")!"); 

        VersionID vid = null;
	{
	  String vstr = getStringParamValue(new ParamMapping(aSubmitVersion), true);
	  if((vstr != null) && (vstr.length() > 0)) {
	    try {
	      vid = new VersionID(vstr);
	      pSubmitVersionID = vid;
	    }
	    catch(IllegalArgumentException ex) {
	      throw new PipelineException
		("The " + aSubmitVersion + " (" + vstr + ") was not a legal revision " + 
		 "number!"); 
	    }
	  }
	}

        pSubmitVsn = pClient.getCheckedInVersion(pSubmitNode, vid);
      }

      /* generate a temporary working area where the approval process will take place
           and change the util context to use it instead for all future operations */
      { 
	String tempView = 
	  ("ApproveTask" + "-" + pProjectName + "-" + pTaskName + "-" + pTaskType); 
        tempView = tempView.replaceAll(" ", "_");
	
	setContext(new UtilContext(pContext.getAuthor(), tempView, pContext.getToolset()));
      }

      /* get the approval check-in message */ 
      pApprovalMessage = getStringParamValue(new ParamMapping(aApprovalMessage), false);
      
      /* get the check-in level */ 
      {
	int level = getEnumParamIndex(new ParamMapping(aCheckInLevel));
	try {
	  pCheckInLevel = VersionID.Level.all().get(level); 
	}
	catch(IndexOutOfBoundsException ex) {
	  throw new PipelineException
	    ("Invalid " + aCheckInLevel + " parameter value (" + level + ")!"); 
	}
      }
    }
    private static final long serialVersionUID = 1067249062762825567L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected
  class CheckOutNetworks
    extends ConstructPass
  {
    public 
    CheckOutNetworks()
    {
      super("Check-Out Networks", 
            "Check-out the approve and submit node networks, regenerate any stale nodes " + 
	    "in the approval network and check-in the changes.");
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
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pSubmitNode);
      pClient.checkOut(getAuthor(), getView(), pSubmitNode, pSubmitVersionID, 
                       CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);

      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pApproveNode);
      pClient.checkOut(getAuthor(), getView(), pApproveNode, null, 
                       CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);

      addToQueueList(pApproveNode);
      addToCheckInList(pApproveNode);
    }
    private static final long serialVersionUID = 8540275476705668113L;
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2127853743467641728L;
    
  public static final String aApproveNode     = "ApproveNode";    
  public static final String aSubmitNode      = "SubmitNode"; 
  public static final String aSubmitVersion   = "SubmitVersion";
  public static final String aApprovalMessage = "ApprovalMessage";
  public static final String aCheckInLevel    = "CheckInLevel";

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The fully resolved name of the approve node.
   */ 
  protected String pApproveNode;

  /**
   * The fully resolved name, revision number and checked-in version of the 
   * submit node being approved.
   */ 
  protected String pSubmitNode;
  protected VersionID pSubmitVersionID;
  protected NodeVersion pSubmitVsn;

  /**
   * The check-in level.
   */ 
  protected VersionID.Level pCheckInLevel;

  /**
   * The approval check-in message.
   */ 
  protected String pApprovalMessage;
    
  /** 
   * The task identifiers.
   */ 
  protected String pProjectName;
  protected String pTaskName;
  protected String pTaskType;

}

