package us.temerity.pipeline.plugin.TaskPolicyExt.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;

/*------------------------------------------------------------------------------------------*/
/*  T A S K   P O L I C Y   E X T                                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * Restricts access to node operations based on all the Task Annotations.
 */
public 
class TaskPolicyExt 
  extends BaseMasterExt
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  TaskPolicyExt()
  {
    super("TaskPolicy", new VersionID("2.3.2"), "Temerity",
	  "Restricts access to node operations based on all the Task Annotations.");
    
    
    /* server configuration */ 
    {
      {
	ExtensionParam param = 
	  new StringExtensionParam
	  (aDatabaseHostname, 
	   "The hostname running the SQL database server.", 
	   "localhost");
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new IntegerExtensionParam
	  (aDatabasePort, 
	   "The network port to use to contact the SQL database server.",
	   3306);
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new StringExtensionParam
	  (aDatabaseUser, 
	   "The user name to use when connecting to the SQL database.",
	   "pipeline");
	addParam(param);
      }

      {
	ExtensionParam param = 
	  new StringExtensionParam    // Make a PasswordExtensionParam 
	  (aDatabasePassword, 
	   "The password to use when connecting to the SQL database.",
	   null);
	addParam(param);
      }
    }

    {  
      LayoutGroup layout = new LayoutGroup(true); 
      layout.addEntry(aDatabaseHostname);
      layout.addEntry(aDatabasePort);
      layout.addSeparator();
      layout.addEntry(aDatabaseUser);
      layout.addEntry(aDatabasePassword);
      
      setLayout(layout);  
    }

    underDevelopment(); 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*  P L U G I N   O P S                                                                   */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to run a task after this extension plugin is first enabled.<P> 
   * 
   * A extension plugin can be enabled either during server initialization or during normal
   * server operation when this specific plugin is enabled manually. 
   */  
  public boolean
  hasPostEnableTask()
  {
    return true;
  }

  /**
   * The task to perform after this extension plugin is first enabled.<P> 
   * 
   * A extension plugin can be enabled either during server initialization or during normal
   * server operation when this specific plugin is enabled manually.
   */ 
  public void 
  postEnableTask()
  {
    try {
      String hostname = (String) getParamValue(aDatabaseHostname);
      if((hostname == null) || (hostname.length() == 0))
	throw new PipelineException("No Database Hostname was specified!");
      
      Integer port = (Integer) getParamValue(aDatabasePort);
      if(port == null)
	throw new PipelineException("No Database Port was specified!");
      if(port <= 0)
	throw new PipelineException("Invalid Database Port (" + port + ")!");
      
      String user = (String) getParamValue(aDatabaseUser);
      if((user == null) || (user.length() == 0))
	throw new PipelineException("No Database User was specified!");
      
      String password = (String) getParamValue(aDatabasePassword);
      if((password == null) || (password.length() == 0))
	throw new PipelineException("No Database Password was specified!");
      
      sDatabase.connect(hostname, port, user, password);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Severe, 
         ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Severe, 
	 getFullMessage(ex));
    }
  }
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */  
  public boolean
  hasPreDisableTask()
  {
    return true;
  }

  /**
   * The task to perform after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */ 
  public void 
  preDisableTask()
  {
    try {
      sDatabase.disconnect();
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Severe, 
	 getFullMessage(ex));
    }
  }
 
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before checking-in an individual node.
   */  
  @Override
  public boolean
  hasPreCheckInTest() 
  {
    return true;
  }	 

  /**
   * Test to perform before checking-in an individual node.
   * 
   * @param rname
   *   The fully resolved node name of the root node of the check-in operation.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version of the node.
   * 
   * @param level  
   *   The revision number component level to increment.
   * 
   * @param msg 
   *   The check-in message text.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */ 
  @Override
  public void
  preCheckInTest
  (
   String rname, 
   NodeID nodeID, 
   NodeMod mod,
   VersionID.Level level, 
   String msg
  ) 
    throws PipelineException
  {
    String nodeName = nodeID.getName();
    String author   = nodeID.getAuthor();
    
    MasterMgrLightClient mclient = getMasterMgrClient();
    TreeMap<String,BaseAnnotation> rootAnnotations = mclient.getAnnotations(rname);
    TreeMap<String,BaseAnnotation> nodeAnnotations = mclient.getAnnotations(nodeName);
    
    WorkGroups wgroups = mclient.getWorkGroups();
    
    // DEBUG
    LogMgr.getInstance().log
      (Kind.Ops, Level.Info, 
       "VALIDATE = " + rname);
    // DEBUG

    /* lookup the annotations on the root node of the check-in and validate whether the 
         check-in is allowed based on the annotation parameters */ 
    boolean rootApprove = false;
    boolean rootSubmit  = false;
    String rootTaskName = null;
    String rootTaskType = null;
    String rootPurpose  = null;
    for(String aname : rootAnnotations.keySet()) {
      BaseAnnotation an = rootAnnotations.get(aname);
      
      // DEBUG
      LogMgr.getInstance().log
        (Kind.Ops, Level.Info, 
         "Annotation = " + an.getName()); 
      // DEBUG

      if(an.getName().equals(aTask) || an.getName().equals(aSubmitNode)) {

        String purpose = lookupPurpose(rname, an); 
        rootPurpose = purpose;

        // DEBUG
        LogMgr.getInstance().log
          (Kind.Ops, Level.Info, 
           "Purpose = " + purpose); 
        // DEBUG

        if(purpose.equals(aFocus) || 
           purpose.equals(aThumbnail) || 
           purpose.equals(aPrepare) || 
           purpose.equals(aProduct)) {

          String goodRoot = purpose.equals(aProduct) ? aApprove : aSubmit; 
          throw new PipelineException
            ("Aborted for node (" + rname + "), because it is both the root node of " + 
             "the check-in operation and a " + purpose + " node! You may only check-in a " + 
             purpose + " node as part of the check-in of a " + goodRoot + " node."); 
        }
        else {
          if(purpose.equals(aSubmit) || 
             purpose.equals(aEdit) ||
             purpose.equals(aApprove)) {
            
            rootTaskName = lookupTaskName(rname, an);
            rootTaskType = lookupTaskType(rname, an);
          }
          
          if(purpose.equals(aApprove)) {
            rootApprove = true;
            if(!author.equals(PackageInfo.sPipelineUser)) 
              throw new PipelineException
                ("Aborted for node (" + rname + "), because it is an Approve node " + 
                 "of the task (" + rootTaskName + ":" + rootTaskType + ") which can only " + 
                 "be checked-in by the (" + PackageInfo.sPipelineUser + ") user as part " + 
                 "of the automated post-approval process!"); 
          }
          else if(purpose.equals(aSubmit)) {
            rootSubmit = true;
            if(!author.equals(PackageInfo.sPipelineUser)) {
              String assigned = (String) an.getParamValue(aAssignedTo);
              if((assigned == null) || (assigned.length() == 0)) 
                throw new PipelineException 
                  ("Aborted for node (" + rname + ") because no one was AssignedTo " + 
                   "the complete the task (" + rootTaskName + ":" + rootTaskType + ")!"); 
              
              boolean isGroup = wgroups.isGroup(assigned); 
              if((!isGroup && !assigned.equals(author)) || 
                 (isGroup && wgroups.isMemberOrManager(author, assigned) == null)) 
                throw new PipelineException
                  ("The task (" + rootTaskName + ":" + rootTaskType + ") for node " + 
                   "(" + rname + ") is assigned to the " + 
                   (isGroup ? "Pipeline work group [" + assigned + "]" : 
                    "artist (" + assigned + ")") + 
                   ".  The (" + author + ") user is not assigned to this task and " + 
                   "therefore is not allowed to check-in this Submit node.");
            }
          }
        }
      }
    }

    // DEBUG
    LogMgr.getInstance().log
      (Kind.Ops, Level.Info, 
       "CHECK = " + rname);
    // DEBUG

    /* perform checks based on comparing annotation parameters of the root node of the 
         check-in with the current node */
    for(String aname : nodeAnnotations.keySet()) {
      BaseAnnotation an = nodeAnnotations.get(aname);

      // DEBUG
      LogMgr.getInstance().log
        (Kind.Ops, Level.Info, 
         "Annotation = " + an.getName()); 
      // DEBUG

      if(an.getName().equals(aTask) || an.getName().equals(aSubmitNode)) {

        String purpose = lookupPurpose(rname, an);

        // DEBUG
        LogMgr.getInstance().log
          (Kind.Ops, Level.Info, 
           "Purpose = " + purpose); 
        // DEBUG

        if(purpose.equals(aFocus) || 
           purpose.equals(aThumbnail) || 
           purpose.equals(aPrepare)) {
          
          if(rootSubmit) 
            verifyTask(nodeName, an, rname, rootTaskName, rootTaskType); 
          else
            throw new PipelineException
              ("Aborted for node (" + nodeName + "), because it is a " + purpose + " " + 
               "node which can only be checked-in when the root node of the check-in " + 
               "operation is a Submit node!.  However in this case, the root node " + 
               "(" + rname + ") was a " + rootPurpose + " node."); 
        }
        else if(purpose.equals(aProduct)) {
          if(rootApprove) 
            verifyTask(nodeName, an, rname, rootTaskName, rootTaskType); 
          else
            throw new PipelineException
              ("Aborted for node (" + nodeName + "), because it is a " + purpose + " " + 
               "node which can only be checked-in when the root node of the check-in " + 
               "operation is an Approve node!.  However in this case, the root node " + 
               "(" + rname + ") was a " + rootPurpose + " node."); 
        }
        else if(purpose.equals(aEdit)) {
          verifyTask(nodeName, an, rname, rootTaskName, rootTaskType); 
        }
        else if(purpose.equals(aSubmit) || purpose.equals(aApprove)) {
          if(!nodeName.equals(rname)) 
            throw new PipelineException
              ("Aborted for node (" + nodeName + ") unless it is the root node of the " + 
               "check-in operation because it is a " + rootPurpose + " node!"); 
        }
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether to run a tesk after checking-in an individual node.
   */  
  @Override
  public boolean
  hasPostCheckInTask() 
  {
    return true;
  }
  
  /** 
   * The task to perform after checking-in an individual node. 
   * 
   * @param vsn 
   *  The newly created checked-in node version.
   */
  @Override
  public void 
  postCheckInTask
  (
   NodeVersion vsn
  )
  {
    String nodeName = vsn.getName();
    try {
      MasterMgrLightClient mclient = getMasterMgrClient();
      TreeMap<String,BaseAnnotation> nodeAnnotations = mclient.getAnnotations(nodeName);
      for (String aname : nodeAnnotations.keySet()) {
        BaseAnnotation an = nodeAnnotations.get(aname);
        if(an.getName().equals(aTask) || an.getName().equals(aSubmitNode)) {

          String taskName = lookupTaskName(nodeName, an);
          String taskType = lookupTaskType(nodeName, an);
          String purpose  = lookupPurpose(nodeName, an); 

          /* submit node check-in */ 
          if(purpose.equals(aSubmit)) {
            Path builderPath = (Path) an.getParamValue(aBuilderPath);

            TreeMap<String,String> thumbToFocus    = new TreeMap<String,String>();
            TreeMap<String,NodeVersion> thumbNodes = new TreeMap<String,NodeVersion>();
            TreeMap<String,NodeVersion> focusNodes = new TreeMap<String,NodeVersion>();
            TreeMap<String,NodeVersion> editNodes  = new TreeMap<String,NodeVersion>();
            mineSubmitTree(nodeName, taskName, taskType, 
                            vsn, null, thumbToFocus, thumbNodes, focusNodes, editNodes, 
                            mclient);

            /* DEBUG */ 
            {
              LogMgr.getInstance().log
                (Kind.Ops, Level.Info, 
                 "The " + purpose + " (" + nodeName + " v" + vsn.getVersionID() + ") of " + 
                 "task (" + taskName + ":" + taskType + ") has been checked-in.");

              for(NodeVersion fvsn : focusNodes.values()) {
                LogMgr.getInstance().log
                  (Kind.Ops, Level.Info, 
                   "The Focus node ("+fvsn.getName()+" v"+fvsn.getVersionID()+") is "+
                   "associated with task ("+taskName+":"+taskType+") submitted for "+
                   "approval by checking-in the Submit node ("+nodeName+" v"+
                   vsn.getVersionID()+").");
              }
            
              for(NodeVersion tvsn : thumbNodes.values()) {
                LogMgr.getInstance().log
                  (Kind.Ops, Level.Info, 
                   "The Thumbnail node ("+tvsn.getName()+" v"+tvsn.getVersionID()+") "+
                   "is associated with task ("+taskName+":"+taskType+") submitted "+
                   "for approval by checking-in the Submit node ("+nodeName+" v"+
                   vsn.getVersionID()+").");

                String focus = thumbToFocus.get(tvsn.getName());
                if(focus != null) {
                  NodeVersion fvsn = focusNodes.get(focus); 
                  if(fvsn != null) {
                    LogMgr.getInstance().log
                      (Kind.Ops, Level.Info, 
                       "The Thumbnail node ("+tvsn.getName()+" v"+tvsn.getVersionID()+") "+
                       "corresponds to Focus Node ("+fvsn.getName()+" v"+
                       fvsn.getVersionID()+").");              
                  }
                }
              }
            
              for(NodeVersion evsn : editNodes.values()) {
                LogMgr.getInstance().log
                  (Kind.Ops, Level.Info, 
                   "The FocusNode ("+evsn.getName()+" v"+evsn.getVersionID()+") is "+
                   "associated with task ("+taskName+":"+taskType+") submitted for "+
                   "approval by checking-in the SubmitNode ("+nodeName+" v"+
                   vsn.getVersionID()+").");
              }
            }
            /* DEBUG */

            int tries = 0; 
            while(true) {
              try {
                sDatabase.submitTask(taskName, taskType, vsn, builderPath, 
                                     thumbToFocus, thumbNodes, focusNodes, editNodes); 
                break;
              }
              catch(PipelineException ex) {
                String msg = 
                  ("TaskSubmission for (" + taskName + ":" + taskType + ") Failed:\n" +
                   ex.getMessage());      

                if(tries < sMaxTries) {
                  LogMgr.getInstance().log
                    (Kind.Ops, Level.Warning, 
                     msg + "\nRetrying...");
                  tries++;
                }
                else {
                  LogMgr.getInstance().log
                    (Kind.Ops, Level.Warning, 
                     msg + "\nAborted after (" + tries + ") attempts!"); 
                  break;
                }
              }
            }
          }

          /* initial approve node check-in */ 
          else if(purpose.equals(aApprove) && vsn.getVersionID().equals(new VersionID())) {
            NodeVersion submitVsn = 
              mineApproveTree(nodeName, taskName, taskType, vsn, mclient);

            /* DEBUG */ 
            {
              LogMgr.getInstance().log
                (Kind.Ops, Level.Info, 
                 "The " + purpose + " (" + nodeName + " v" + vsn.getVersionID() + ") of " + 
                 "task (" + taskName + ":" + taskType + ") has been checked-in.");

              if(submitVsn != null) {
                LogMgr.getInstance().log
                  (Kind.Ops, Level.Info, 
                   "The Submit node (" + submitVsn.getName() + " v" + 
                   submitVsn.getVersionID()+ ") is associated with task (" + taskName + ":" + 
                   taskType + ") by checking-in the Approve node (" + nodeName + " v" +
                   vsn.getVersionID() + ").");
              }
            }
            /* DEBUG */
            
            int tries = 0; 
            while(true) {
              try {
                sDatabase.approveTask(taskName, taskType, vsn, submitVsn); 
                break;
              }
              catch(PipelineException ex) {
                String msg = 
                  ("TaskApproval for (" + taskName + ":" + taskType + ") Failed:\n" +
                   ex.getMessage());      

                if(tries < sMaxTries) {
                  LogMgr.getInstance().log
                    (Kind.Ops, Level.Warning, 
                     msg + "\nRetrying...");
                  tries++;
                }
                else {
                  LogMgr.getInstance().log
                    (Kind.Ops, Level.Warning, 
                     msg + "\nAborted after (" + tries + ") attempts!"); 
                  break;
                }
              }
            }                
          }
        }
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
        (Kind.Ops, Level.Warning, 
         "PostCheckInTask (" + getName() + ") Failed on: " + nodeName + "\n" + 
         ex.getMessage());      
    }
  }

  /**
   * Find the focus/edit nodes in the tree of nodes rooted at the given submit node.
   */ 
  private void 
  mineSubmitTree
  (
   String submitNodeName, 
   String submitTaskName, 
   String submitTaskType, 
   NodeVersion vsn, 
   String currentThumb,  
   TreeMap<String,String> thumbToFocus, 
   TreeMap<String,NodeVersion> thumbNodes,
   TreeMap<String,NodeVersion> focusNodes, 
   TreeMap<String,NodeVersion> editNodes, 
   MasterMgrLightClient mclient
  )
    throws PipelineException 
  {
    String nodeName = vsn.getName();
    String thumb = currentThumb; 
    TreeMap<String,BaseAnnotation> annotations = mclient.getAnnotations(nodeName);
    for (String aname : annotations.keySet()) {
      BaseAnnotation an = annotations.get(aname);
      if(an.getName().equals(aTask)) {
        String purpose = lookupPurpose(nodeName, an); 
        if(purpose.equals(aFocus)) {
          verifyTask(nodeName, an, submitNodeName, submitTaskName, submitTaskType); 
          focusNodes.put(nodeName, vsn);
          if(currentThumb != null) {
            thumbToFocus.put(currentThumb, nodeName);
            thumb = null;
          }
        }
        else if(purpose.equals(aThumbnail)) {
          verifyTask(nodeName, an, submitNodeName, submitTaskName, submitTaskType); 
          thumbNodes.put(nodeName, vsn); 
          thumb = nodeName; 
        }
        else if(purpose.equals(aEdit)) {
          verifyTask(nodeName, an, submitNodeName, submitTaskName, submitTaskType); 
          editNodes.put(nodeName, vsn); 
        }
        else if(purpose.equals(aProduct)) {
          return;
        }
      }
    }
      
    for(LinkVersion link : vsn.getSources()) {
      if(!link.isLocked()) {
        NodeVersion source = mclient.getCheckedInVersion(link.getName(), link.getVersionID());
        mineSubmitTree(submitNodeName, submitTaskName, submitTaskType, 
                       source, thumb, thumbToFocus, thumbNodes, focusNodes, editNodes, 
                       mclient);
      }
    }
  }

  /**
   * Find the the submit node in the tree of nodes rooted at the given approve node.
   */ 
  private NodeVersion
  mineApproveTree
  (
   String approveNodeName, 
   String approveTaskName, 
   String approveTaskType, 
   NodeVersion vsn, 
   MasterMgrLightClient mclient
  )
    throws PipelineException 
  {
    String nodeName = vsn.getName();    
    TreeMap<String,BaseAnnotation> annotations = mclient.getAnnotations(nodeName);
    for (String aname : annotations.keySet()) {
      BaseAnnotation an = annotations.get(aname);
      if(an.getName().equals(aSubmitNode)) {
        verifyTask(nodeName, an, approveNodeName, approveTaskName, approveTaskType); 
        return vsn; 
      }
    }

    for(LinkVersion link : vsn.getSources()) {
      if(!link.isLocked()) {
        NodeVersion source = mclient.getCheckedInVersion(link.getName(), link.getVersionID());
        NodeVersion submitVsn = 
          mineApproveTree(approveNodeName, approveTaskName, approveTaskType, source, mclient);
        if(submitVsn != null) 
          return submitVsn;
      }
    }

    return null;
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
   * Verify that the TaskName and TaskType match those of the root node.
   */ 
  private void 
  verifyTask
  (
   String nodeName, 
   BaseAnnotation an,
   String rootNodeName, 
   String rootTaskName, 
   String rootTaskType
  ) 
    throws PipelineException
  {
    String taskName = lookupTaskName(nodeName, an); 
    String taskType = lookupTaskType(nodeName, an); 

    if(rootTaskName != null) {
      if(!taskName.equals(rootTaskName) || !taskType.equals(rootTaskType)) {
        throw new PipelineException
          ("Cannot check-in node (" + nodeName + ") of the task (" + taskName + ":" + 
           taskType + ") because the root node of the check-in (" + rootNodeName + ") " + 
           "belongs to a different task (" + rootTaskName + ":" + rootTaskType + ")!  " + 
           "Please limit a check-in operation to nodes which share the same task.");
      }
    }
  }
      
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The shared SQL database connection.
   */ 
  private static TaskDb sDatabase = new TaskDb();

  private static final int sMaxTries = 2; 


  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5625120891197882080L;
  
  
  public static final String aDatabaseHostname = "DatabaseHostname"; 
  public static final String aDatabasePort     = "DatabasePort"; 
  public static final String aDatabaseFlavor   = "DatabaseFlavor"; 
  public static final String aDatabaseUser     = "DatabaseUser"; 
  public static final String aDatabasePassword = "DatabasePassword"; 

  public static final String aTaskName         = "TaskName";
  public static final String aTaskType         = "TaskType";
  public static final String aPurpose          = "Purpose";
  public static final String aIsApproved       = "IsApproved";
  public static final String aAssignedTo       = "AssignedTo";
  public static final String aBuilderPath      = "BuilderPath";
  
  public static final String aTask             = "Task";
  public static final String aSubmitNode       = "SubmitNode";

  public static final String aSubmit           = "Submit";
  public static final String aEdit             = "Edit";
  public static final String aPrepare          = "Prepare";
  public static final String aFocus            = "Focus";
  public static final String aThumbnail        = "Thumbnail";
  public static final String aProduct          = "Product";
  public static final String aApprove          = "Approve";
      
}
