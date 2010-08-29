// $Id: TaskGuardExt.java,v 1.5 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TaskGuardExt.v2_4_28;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_28.*;

/*------------------------------------------------------------------------------------------*/
/*  T A S K   G U A R D   E X T                                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * Restricts access to node operations based on the v2.4.28 Task Annotations.
 */
public 
class TaskGuardExt 
  extends BaseMasterExt
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  TaskGuardExt()
  {
    super("TaskGuard", new VersionID("2.4.28"), "Temerity",
          "Restricts access to node operations based on Task Annotations.");
    
    {
      ExtensionParam param =
        new WorkGroupExtensionParam
          (aCustomPublishGroup, 
           "The name of the group of users who will be running publish builders, " +
           "if it is not the pipeline user.", 
           false,
           true,
           null);
      addParam(param);
    }
    
    {
      ExtensionParam param =
        new StringExtensionParam
          (aCustomVerifyUser, 
           "The name of the user who will be running verify builders, " +
           "if it is not the pipeline user.", 
           null);
      addParam(param);
    }
    
    {
      ExtensionParam param =
        new StringExtensionParam
          (aProjectList, 
           "The comma separated list of projects to run this extension on.  If this is null" +
           "then all projects will be subject to this extension.", 
           null);
      addParam(param);
    }
    
    {
      LayoutGroup group = new LayoutGroup(true);
      group.addEntry(aProjectList);
      group.addEntry(aCustomVerifyUser);
      group.addEntry(aCustomPublishGroup);
      
      setLayout(group);
    }
    
    underDevelopment();
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
    MasterMgrLightClient mclient = getMasterMgrClient();
    
    boolean initialRootCheckIn = mclient.getCheckedInNames(rname).isEmpty();    
    
    TreeSet<String> projectList = null;
    String projectParam = (String) getParamValue(aProjectList);
    if (projectParam != null && projectParam.length() > 0) {
      String buffer[] = projectParam.split(",");
      projectList = new TreeSet<String>();
      Collections.addAll(projectList, buffer);
    }

    String nname  = nodeID.getName();
    String author = nodeID.getAuthor();
    
    TreeMap<NodePurpose, BaseAnnotation> rootAnnots = 
      new TreeMap<NodePurpose, BaseAnnotation>(); 
    String rootProjectName = null;
    String rootTaskIdent1 = null;
    String rootTaskIdent2 = null;
    String rootTaskType = null;
    String rootTask = null;
    {
      String[] rtn = lookupTaskAnnotations(rname, mclient, rootAnnots); 
      if(rtn != null) {
        rootProjectName = rtn[0]; 
        rootTaskIdent1  = rtn[1]; 
        rootTaskIdent2  = rtn[2];
        rootTaskType    = rtn[3]; 
	rootTask = (rootProjectName + "|" + rootTaskIdent1 + "|" + rootTaskIdent2 + "|" + 
	            rootTaskType); 
      }
    }

    TreeMap<NodePurpose, BaseAnnotation> nodeAnnots = 
      new TreeMap<NodePurpose, BaseAnnotation>(); 
    String nodeProjectName = null;
    String nodeTaskIdent1  = null;
    String nodeTaskIdent2  = null;
    String nodeTaskType    = null;
    {
      String[] rtn = lookupTaskAnnotations(nname, mclient, nodeAnnots); 
      if(rtn != null) {
        nodeProjectName = rtn[0]; 
        nodeTaskIdent1  = rtn[1]; 
        nodeTaskIdent2  = rtn[2];
        nodeTaskType    = rtn[3]; 
      }
    }
    
    /* So if the node being checked-in is part of a task, but this task is not in the task 
     * list, we can return, since we are not going to prevent any check-ins of this node. 
     * We do not have to care about the state of the root node's project.  There are a couple
     * of cases ('our project' refers to the current node's project:
     * 1.  Root project is different from our project.  If our project is monitored by this 
     * ext, then the check-in will be blocked anyway for non-matching projects.
     * 2. Root node has no annotations.  If our project is not in the list, then we've already
     * short-circuited, so it doesn't matter.  Otherwise it will be blocked for being part
     * of a non-task check-in.
     * 3. Root project is the same as our project.  Well, we're just about to check it, and 
     * if it is exempt, then we'll have short-circuited.  Otherwise all normal checks will
     * apply, as they should.
     */
    if (!nodeAnnots.isEmpty()) {
      if (projectList != null && !projectList.contains(nodeProjectName))
        return;
    }
    /* If there are no annotations on the node, then we don't care if it gets checked in.*/
    else
      return;
    
    /* validate the Task credentials of the root node of the check-in */ 
    if(rootAnnots.isEmpty()) {
      /* just ignore nodes without Task annotations when the root node is without them too */
      if(nodeAnnots.isEmpty()) 
        return;
      else {
          throw new PipelineException
            ("Check-in aborted for node (" + nname + ") because it has Task annotations " + 
             "but the root node of the check-in operation (" + rname + ") does not have " + 
             "any Task annotations.  Incidental check-in of nodes which are part of a task " + 
             "is only allowed when the root node of check-in is also a member of the same " + 
             "task."); 
      }
    }
    else {
      if(rootAnnots.containsKey(NodePurpose.Edit)) {
        /* This statement intentionally left empty.  May later be populated.*/ 
      }
      /* By doing Submit before Verify, we allow for Self-Verifying task submission.*/
      else if(rootAnnots.containsKey(NodePurpose.Submit)) {
        /* This statement intentionally left empty.  May later be populated.*/
        
        /* 
         * We used to be doing user verification in here, checking who tasks were assigned 
         * to. Since assignments can take many forms, I am breaking this functionality out
         * from this server-side extension.  I will be adding a new annotation/extension pair
         * that would allow for basic user/group assignments similar to the old task system. 
         */
      }
      else if(rootAnnots.containsKey(NodePurpose.Verify)) {
        String verifyUser = (String) getParamValue(aCustomVerifyUser);
        
        if(!author.equals(PackageInfo.sPipelineUser) && 
           (verifyUser == null || !author.equals(verifyUser))) {
          /* Allow the initial check-in, since it might be from a builder. */
          if (!initialRootCheckIn)
            throw new PipelineException 
              ("The task (" + rootTask + ") defined by the root node of the check-in " + 
               "operation (" + rname + ") is a verify node.\n" + 
               "Since the user (" + author + ") attempting the check-in is not the " +
               "pipeline user and does not match the authorized verify user " +
               "(" + verifyUser + "), this check-in will not be allowed to proceed.");
        }
      }
      else if(rootAnnots.containsKey(NodePurpose.Publish)) {
        String publishGroup = (String) getParamValue(aCustomPublishGroup);
        TreeSet<String> allowedUsers = null;
        
        if (publishGroup != null && publishGroup.length() > 0)
          allowedUsers = mclient.getWorkGroups().getUsersInGroup(publishGroup);
        
        if(!author.equals(PackageInfo.sPipelineUser) && 
           (allowedUsers == null || !allowedUsers.contains(author))) {
          /* Allow the initial check-in, since it might be from a builder. */
          if (!initialRootCheckIn)
            throw new PipelineException 
              ("The task (" + rootTask + ") defined by the root node of the check-in " + 
	       "operation (" + rname + ") is a publish node.\n" + 
               "Since the user (" + author + ") attempting the check-in is not the " +
               "pipeline user and is not in the authorized publish group" +
               "(" + publishGroup + "), this check-in will not be allowed to proceed.");
        }
      }
      else if (rootAnnots.containsKey(NodePurpose.Execution)) {
        if (!rname.equals(nname))
          throw new PipelineException
            ("A node with a Purpose of Execution must be checked-in by itself with no " +
             "upstream dependencies.");
        
        String verifyUser = (String) getParamValue(aCustomVerifyUser);
        
        String publishGroup = (String) getParamValue(aCustomPublishGroup);
        TreeSet<String> allowedUsers = null;
        
        if (publishGroup != null && publishGroup.length() > 0)
          allowedUsers = mclient.getWorkGroups().getUsersInGroup(publishGroup);
        
        /* 
         * Check-in restricted to pipeline, the auto-verify user, and users who have publish
         * privileges.  We may also want to add a more general class that can check in 
         * execute nodes, since they may well be modified while conforming the submit network.
         */
        if(!author.equals(PackageInfo.sPipelineUser) && 
          (verifyUser == null || !author.equals(verifyUser)) &&
          (allowedUsers == null || !allowedUsers.contains(author)))
          /* Allow the initial check-in, since it might be from a builder. */
          if (!initialRootCheckIn)
            throw new PipelineException 
              ("The task (" + rootTask + ") defined by the root node of the check-in " + 
               "operation (" + rname + ") is an execution node.\n" + 
               "Since the user (" + author + ") attempting the check-in is not the " +
               "pipeline user this check-in will not be allowed to proceed.");
      }
      else {
        throw new PipelineException
          ("Check-in aborted for node (" + nname + ") because the root node of " + 
           "the check-in operation (" + rname + ") does not have a " + aPurpose + " of " + 
           "either " + NodePurpose.Edit + ", " + NodePurpose.Submit+ ", " + 
           NodePurpose.Publish + ", or " + NodePurpose.Verify + "! Only nodes with these " +
           "types of " + aPurpose + " may be used as the root node of a check-in operation."); 
      }
    }
    
    /* is the node being checked-in is part of the same project as the root node? */ 
    if(!rootProjectName.equals(nodeProjectName)) 
      throw new PipelineException
        ("Check-in aborted for node (" + nname + ") because it has a different " + 
         aProjectName + " (" + nodeProjectName + ") than the root node of the check-in " + 
         "operation (" + rname + ") which has a " + aProjectName + 
         " (" + rootProjectName + ")!");  
    
    /* is the node being checked-in is part of the same task as the root node? */ 
    if(!rootTaskIdent1.equals(nodeTaskIdent1) || !rootTaskIdent2.equals(nodeTaskIdent2)) 
      throw new PipelineException
        ("Check-in aborted for node (" + nname + ") because it has a different TaskName " + 
         " (" + nodeTaskIdent1 + "|" + nodeTaskIdent2 +  ") than the root node of the " +
         "check-in operation (" + rname + ") which has a TaskName of (" + rootTaskIdent1 + 
         "|" +  rootTaskIdent2 + ")!");
    
    /* is the node being checked-in the same type of task as the root node? */ 
    if(!rootTaskType.equals(nodeTaskType)) 
      throw new PipelineException
        ("Check-in aborted for node (" + nname + ") because it has a different " + 
         aTaskType + " (" + nodeTaskType + ") than the root node of the check-in " + 
         "operation (" + rname + ") which has a " + aTaskType + 
         " (" + rootTaskType + ")!");  
    
    /* compare the Purpose of the root and current node */ 
    {
      for(NodePurpose purpose : nodeAnnots.keySet()) {
	if(purpose.equals(NodePurpose.Focus) || purpose.equals(NodePurpose.Thumbnail)) {
	  if(!rootAnnots.containsKey(NodePurpose.Submit) && 
	     !rootAnnots.containsKey(NodePurpose.Verify)) 
	    throw new PipelineException
	      ("Check-in aborted for node (" + nname + ") because nodes with a " + aPurpose + 
	       " of (" + purpose + ") can only be checked-in when the " + 
	       "root node of the check-in operation has a " + aPurpose + " of " + 
	       "(" + NodePurpose.Submit + " or " + NodePurpose.Verify+ ")!"); 
	}

	else if(purpose.equals(NodePurpose.Prepare) ||
	        purpose.equals(NodePurpose.Product) ) {
	  if(!rootAnnots.containsKey(NodePurpose.Submit) && 
	     !rootAnnots.containsKey(NodePurpose.Verify) && 
	     !rootAnnots.containsKey(NodePurpose.Publish)) 
	    throw new PipelineException
	      ("Check-in aborted for node (" + nname + ") because nodes with a " + aPurpose + 
	       " of (" + purpose + ") can only be checked-in when the root " +
	       "node of the check-in operation has a " + aPurpose + " of " + 
	       NodePurpose.Submit + ", " + NodePurpose.Verify + " or " + 
	       NodePurpose.Publish + ")!"); 
	}
	else if(purpose.equals(NodePurpose.Submit) || 
		purpose.equals(NodePurpose.Verify) || 
		purpose.equals(NodePurpose.Publish)) 
	{  
	  if(!nname.equals(rname)) 
	    throw new PipelineException
	      ("Check-in aborted for node (" + nname + ") unless it is the root node of " + 
	       "the check-in operation because it has a " + aPurpose + " of " + 
	       "(" + purpose + ")!"); 
	}
	else if(purpose.equals(NodePurpose.Edit)) {
	  /* This statement intentionally left empty.  May later be populated.*/
	}
      }
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Searches the set of annotations associated with the given node for Task related 
   * annotations. 
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param mclient
   *   The connection to the Master Manager daemon. 
   *
   * @param byPurpose
   *   A table of those that match indexed by Purpose parameter.
   * 
   * @return 
   *   The [ProjectName, TaskIdent1, TaskIdent2, TaskType] array.
   */ 
  private String[] 
  lookupTaskAnnotations
  (
    String name, 
    MasterMgrLightClient mclient, 
    TreeMap<NodePurpose, BaseAnnotation> byPurpose
  ) 
    throws PipelineException
  {
    PluginID pid = new PluginID("Task", new VersionID("2.4.28"), "Temerity");
    
    TreeMap<String, BaseAnnotation> annots = mclient.getAnnotations(name);
    String projectName = null; 
    String taskIdent1  = null; 
    String taskIdent2  = null;
    String taskType    = null; 
    for(Entry<String, BaseAnnotation> entry : annots.entrySet()) {
      String aname = entry.getKey();
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        BaseAnnotation an = entry.getValue();
        
        if (!an.getPluginID().equals(pid))
          continue;
            
        
        NodePurpose purpose = lookupPurpose(an); 
        if(purpose != null) {
          if(byPurpose.containsKey(purpose)) 
          throw new PipelineException
            ("More than one Task related annotation with a " + aPurpose + " of " + 
             purpose + " was found on node (" + name + ")!"); 
  
          {
            String pname = lookupProjectName(an); 
            if(pname == null) 
              throw new PipelineException
                ("The " + aProjectName + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((projectName != null) && !projectName.equals(pname)) 
              throw new PipelineException 
                ("The " + aProjectName + " was set in multiple Task annotations on " +
                 "node (" + name + "), but the did not match!  Both (" + projectName + ") " +
                 "and (" + pname + ") where given as the " + aProjectName + ".");
  
            projectName = pname;
          }
  
          {
            String tname = lookupTaskIdent1(an);  
            if(tname == null) 
              throw new PipelineException
                ("The " + aTaskIdent1 + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskIdent1 != null) && !taskIdent1.equals(tname)) 
              throw new PipelineException 
                ("The " + aTaskIdent1 + " was set in multiple Task annotations on " +
                 "node (" + name + "), but they did not match!  Both (" + taskIdent1 + ") " +
                 "and (" + tname + ") where given as the " + aTaskIdent1 + ".");
  
            taskIdent1 = tname; 
          }
          
          {
            String tname = lookupTaskIdent2(an);  
            if(tname == null) 
              throw new PipelineException
                ("The " + aTaskIdent2 + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskIdent2 != null) && !taskIdent2.equals(tname)) 
              throw new PipelineException 
                ("The " + aTaskIdent2 + " was set in multiple Task annotations on " +
                 "node (" + name + "), but they did not match!  Both (" + taskIdent2 + ") " +
                 "and (" + tname + ") where given as the " + aTaskIdent2 + ".");
  
            taskIdent2 = tname; 
          }
  
          {
            String ttype = lookupTaskType(an);  
            if(ttype == null) 
              throw new PipelineException
                ("The " + aTaskType + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskType != null) && !taskType.equals(ttype)) 
              throw new PipelineException 
                ("The " + aTaskType + " was set in multiple Task annotations on node " + 
                 "(" + name + "), but the did not match!  Both (" + taskType + ") and " + 
                 "(" + ttype + ") where given as the " + aTaskType + ".");
  
            taskType = ttype;
          }
  
          byPurpose.put(purpose, an); 
        }
      }
    }

    if(!byPurpose.isEmpty()) {
      String names[] = { projectName, taskIdent1, taskIdent2, taskType };
      return names;
    }

    return null;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup the annotation Purpose.
   */ 
  private NodePurpose
  lookupPurpose
  (
    BaseAnnotation an   
  ) 
  {
    EnumAnnotationParam param = (EnumAnnotationParam) an.getParam(aPurpose);
    if (param == null )
      return null;
    return NodePurpose.values()[param.getIndex()];
  }

  /**
   * Lookup the ProjectName from the annotation.
   */ 
  private String 
  lookupProjectName
  (
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String projectName = (String) an.getParamValue(aProjectName);
    if((projectName != null) && (projectName.length() == 0))
      projectName = null; 

    return projectName;
  }

  /**
   * Lookup the TaskIdent1 from the annotation.
   */ 
  private String 
  lookupTaskIdent1
  (
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String taskName = (String) an.getParamValue(aTaskIdent1);
    if((taskName != null) && (taskName.length() == 0))
      taskName = null;

    return taskName;
  }
  
  /**
   * Lookup the TaskIdent2 from the annotation.
   */ 
  private String 
  lookupTaskIdent2
  (
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String taskName = (String) an.getParamValue(aTaskIdent2);
    if((taskName != null) && (taskName.length() == 0))
      taskName = null;

    return taskName;
  }

  /**
   * Lookup the TaskType from the annotation.
   */ 
  private String 
  lookupTaskType
  (
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String taskType = (String) an.getParamValue(aTaskType);
    if((taskType != null) && (taskType.length() == 0))
      taskType = null;
    
    if((taskType != null) && taskType.equals(aCUSTOM)) {
      taskType = (String) an.getParamValue(aCustomTaskType);
      if((taskType != null) && (taskType.length() == 0))
        taskType = null;
    }
    
    return taskType;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4890708392802172011L;
  
  public static final String aCustomPublishGroup = "CustomPublishGroup";
  public static final String aCustomVerifyUser   = "CustomVerifyUser";
  public static final String aProjectList        = "ProjectList";
  
  private static final String aProjectName       = "ProjectName";
  private static final String aTaskIdent1        = "TaskIdent1";
  private static final String aTaskIdent2        = "TaskIdent2";
  private static final String aTaskType          = "TaskType";
  private static final String aCustomTaskType    = "CustomTaskType";
  
  public static final String aCUSTOM        = "[[CUSTOM]]";   

  public static final String aPurpose   = "Purpose";
}
