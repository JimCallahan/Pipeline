// $Id: ApproveTaskBuilder.java,v 1.4 2008/02/25 05:03:07 jesse Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A P P R O V E   T A S K   B U I L D E R                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder which implements a generic task approval operation. <P> 
 * 
 * This builder relies on the ApproveTask, SubmitTask and CommonTask annotation plugins
 * to be associated with the proper node in order for it to function properly.
 */
public 
class ApproveTaskBuilder
  extends BaseBuilder
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
    this("ApproveTask",
         "A builder which implements a generic task approval operation.", 
         mclient, qclient, builderInfo);
    
    /* not really applicable to this builder, so hide it from the users */ 
    disableParam(new ParamMapping(aActionOnExistence));

    addSetupPass(new LookupAndValidate());
    addConstructPass(new CheckOutNetworks());
    
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
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
    }
  }

  /**
   * Constructor used by subclasses extending the base approve builder functionality.
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
  protected 
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
    super(name, desc, mclient, qclient, builderInfo);
    
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
        TreeMap<String,BaseAnnotation> annotations = pClient.getAnnotations(pApproveNode); 
        for(String aname : annotations.keySet()) {
	  if(aname.equals("Task") || aname.startsWith("AltTask")) {
	    BaseAnnotation annot = annotations.get(aname);
	    if(lookupPurpose(pApproveNode, aname, annot).equals(aApprove)) { 
	      
	      /* save the task information */ 
	      pProjectName = lookupProjectName(pApproveNode, aname, annot);
	      pTaskName    = lookupTaskName(pApproveNode, aname, annot);
	      pTaskType    = lookupTaskType(pApproveNode, aname, annot);

	      /* determine if the user running the builder should be allowed to 
	           approve the task */ 	     
	      String vised = (String) annot.getParamValue(aSupervisedBy);
	      if((vised != null) && (vised.length() > 0)) {
		WorkGroups wgroups = pClient.getWorkGroups();
		boolean isGroup = wgroups.isGroup(vised); 
		String author = pContext.getAuthor();
		if((!isGroup && !vised.equals(author)) || 
		   (isGroup && wgroups.isMemberOrManager(author, vised) == null)) 
		  throw new PipelineException
		    ("The " + aSupervisedBy + " parameter for the " + aApproveNode + " " +
		     "(" + pApproveNode + ") is assigned to the " + 
		     (isGroup ? "Pipeline work group [" + vised + "]" : 
		      "user (" + vised + ")") + 
		     ".  The (" + author + ") user is not allowed to approve the task!");
	      }
	    
	      validated = true;
	      break;
	    }
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
        TreeMap<String,BaseAnnotation> annotations = pClient.getAnnotations(pSubmitNode); 
        for(String aname : annotations.keySet()) {
	  if(aname.equals("Task") || aname.startsWith("AltTask")) {
	    BaseAnnotation annot = annotations.get(aname);

	    String projectName = lookupProjectName(pSubmitNode, aname, annot); 
	    if(!projectName.equals(pProjectName)) 
	      throw new PipelineException
		("The " + aProjectName + " parameter supplied for the (" + aname + ") " + 
		 "annotation on the submit node (" + pSubmitNode + ") does not match the " + 
		 aProjectName + " parameter (" + pProjectName + ") of the approve node!"); 

	    String taskName = lookupTaskName(pSubmitNode, aname, annot); 
	    if(!taskName.equals(pTaskName)) 
	      throw new PipelineException
		("The " + aTaskName + " parameter supplied for the (" + aname + ") " + 
		 "annotation on the submit node (" + pSubmitNode + ") does not match the " + 
		 aTaskName + " parameter (" + pTaskName + ") of the approve node!"); 
	    
	    String taskType = lookupTaskType(pSubmitNode, aname, annot); 
	    if(!taskType.equals(pTaskType)) 
	      throw new PipelineException
		("The " + aTaskType + " parameter supplied for the (" + aname + ") " + 
		 "annotation on the submit node (" + pSubmitNode + ") does not match the " + 
		 aTaskType + " parameter (" + pTaskType + ") of the approve node!"); 
	    
	    validated = true;
	    break;
          }
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
    
    private static final long serialVersionUID = -8916876544016187878L;
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
      pClient.checkOut(getAuthor(), getView(), pSubmitNode, null, 
                       CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);

      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Checking Out: " + pApproveNode);
      pClient.checkOut(getAuthor(), getView(), pApproveNode, null, 
                       CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);

      addToQueueList(pApproveNode);
      addToCheckInList(pApproveNode);
    }

    private static final long serialVersionUID = 8846626699659699447L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S  (these should become part of a CommonTaskUtils eventually)            */
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
  private String
  lookupProjectName
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String projectName = (String) annot.getParamValue(aProjectName);
    if(projectName == null) 
      throw new PipelineException
        ("No " + aProjectName + " parameter was specified for the (" + aname + ") " + 
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
  private String
  lookupTaskName
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskName = (String) annot.getParamValue(aTaskName);
    if(taskName == null) 
      throw new PipelineException
        ("No " + aTaskName + " parameter was specified for the (" + aname + ") " + 
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
  private String
  lookupTaskType
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String taskType = (String) annot.getParamValue(aTaskType);
    if(taskType == null) 
      throw new PipelineException
        ("No " + aTaskType + " parameter was specified for the (" + aname + ") " + 
	 "annotation on the node (" + name + ")!"); 

    if(taskType.equals(aCUSTOM)) {
      taskType = (String) annot.getParamValue(aCustomTaskType);
      if(taskType == null) 
	throw new PipelineException
	  ("No " + aCustomTaskType + " parameter was specified for the (" + aname + ") " + 
	   "annotation on the node (" + name + ") even though the " + aTaskType + " " + 
	   "parameter was set to (" + aCUSTOM + ")!"); 
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
  private String
  lookupPurpose
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String purpose = (String) annot.getParamValue(aPurpose);
    if(purpose == null) 
      throw new PipelineException
        ("No " + aPurpose + " parameter was specified for the (" + aname + ") " + 
	 "annotation on the node (" + name + ")!"); 

    return purpose;
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8846626699659699447L;
    
  public static final String aApproveNode     = "ApproveNode";    
  public static final String aSubmitNode      = "SubmitNode"; 
  public static final String aSubmitVersion   = "SubmitVersion";
  public static final String aApprovalMessage = "ApprovalMessage";
  public static final String aCheckInLevel    = "CheckInLevel";

  public static final String aProjectName    = "ProjectName";
  public static final String aTaskName       = "TaskName";
  public static final String aTaskType       = "TaskType";
  public static final String aCustomTaskType = "CustomTaskType";
  public static final String aCUSTOM         = "[[CUSTOM]]";  
  public static final String aSupervisedBy   = "SupervisedBy";

  public static final String aPurpose   = "Purpose";
  public static final String aSubmit    = "Submit";
  public static final String aApprove   = "Approve";



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
  protected NodeVersion pSubmitVersionID;
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

