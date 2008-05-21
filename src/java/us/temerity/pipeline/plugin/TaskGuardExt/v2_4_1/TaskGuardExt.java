// $Id: TaskGuardExt.java,v 1.3 2008/05/21 21:23:19 jesse Exp $

package us.temerity.pipeline.plugin.TaskGuardExt.v2_4_1;

import us.temerity.pipeline.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*  T A S K   G A U R D   E X T                                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * Restricts access to node operations based on the SubmitTask, ApproveTask and 
 * CommonTask annotations.
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
    super("TaskGuard", new VersionID("2.4.1"), "Temerity",
          "Restricts access to node operations based on the SubmitTask, ApproveTask and " + 
          "CommonTask annotations."); 
    
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

    String nname  = nodeID.getName();
    String author = nodeID.getAuthor();

    WorkGroups wgroups = mclient.getWorkGroups();
    PrivilegeDetails privs = mclient.getPrivilegeDetails(author); 

    TreeMap<String, BaseAnnotation> rootAnnots = new TreeMap<String, BaseAnnotation>(); 
    String rootProjectName = null;
    String rootTaskName = null;
    String rootTaskType = null;
    String rootTask = null;
    {
      String[] rtn = lookupTaskAnnotations(rname, mclient, rootAnnots); 
      if(rtn != null) {
        rootProjectName = rtn[0]; 
        rootTaskName    = rtn[1]; 
        rootTaskType    = rtn[2]; 
	rootTask = (rootProjectName + "|" + rootTaskName + "|" + rootTaskType); 
      }
    }

    TreeMap<String, BaseAnnotation> nodeAnnots = new TreeMap<String, BaseAnnotation>(); 
    String nodeProjectName = null;
    String nodeTaskName = null;
    String nodeTaskType = null;
    String nodeTask = null;
    {
      String[] rtn = lookupTaskAnnotations(nname, mclient, nodeAnnots); 
      if(rtn != null) {
        nodeProjectName = rtn[0]; 
        nodeTaskName    = rtn[1]; 
        nodeTaskType    = rtn[2]; 
	nodeTask = (nodeProjectName + "|" + nodeTaskName + "|" + nodeTaskType); 
      }
    }
    
    /* validate the Task credentials of the root node of the check-in */ 
    if(rootAnnots.isEmpty()) {
      /* just ignore nodes without Task annotations when the root node is without them too */
      if(nodeAnnots.isEmpty()) 
        return;
      else {
        throw new PipelineException
          ("Check-in aborted for node (" + nname + ") because it has Task annotations " + 
           "but the root node of the check-in operation (" + rname + ") does not have " + 
           "any Task annotations.  Incidental check-in of nodes which are part of a " + 
           "is only allowed when the root node of check-in is also a member of the same " + 
           "task."); 
      }
    }
    else {
      if(rootAnnots.containsKey(aPrereq)) {
        if(!privs.isAnnotator()) 
          throw new PipelineException
            ("Only users with Annotator privileges are allowed to check-in a task " + 
             "prerequisites node!  Any node with an annotation with a " + aPurpose + " " + 
             "parameter set to " + aPrereq + " will be considered a task prerequisites " + 
             "node."); 
      }
      else if(rootAnnots.containsKey(aEdit) || rootAnnots.containsKey(aDeliver)) {
        /* okay, let everyone do this... */ 
      }
      else if(rootAnnots.containsKey(aSubmit)) {
        BaseAnnotation an = rootAnnots.get(aSubmit); 
        String assigned = (String) an.getParamValue(aAssignedTo);
        if((assigned != null) && (assigned.length() == 0))
          assigned = null;
        
        if(!author.equals(PackageInfo.sPipelineUser) && (assigned != null)) {
          boolean isGroup = wgroups.isGroup(assigned); 
          if((!isGroup && !assigned.equals(author)) || 
             (isGroup && wgroups.isMemberOrManager(author, assigned) == null)) 
            throw new PipelineException 
              ("The task (" + rootTask + ") defined by the root node of the check-in " + 
	       "operation (" + rname + ") is currently " + aAssignedTo + " to the " + 
               (isGroup ? "Pipeline work group [" + assigned + "]" : 
                          "user (" + assigned + ")") + ".  " + 
               "Since the user (" + author + ") attempting the check-in does not match the " +
	       aAssignedTo + " user/group, they are not allowed to check-in this " + 
	       aSubmit + " node.");
        }
      }
      else if(rootAnnots.containsKey(aApprove)) {
        BaseAnnotation an = rootAnnots.get(aApprove); 
        String supervise = (String) an.getParamValue(aSupervisedBy); 
        if((supervise != null) && (supervise.length() == 0))
          supervise = null;
        
        if(!author.equals(PackageInfo.sPipelineUser) && (supervise != null)) {
          boolean isGroup = wgroups.isGroup(supervise); 
          if((!isGroup && !supervise.equals(author)) || 
             (isGroup && wgroups.isMemberOrManager(author, supervise) == null)) 
            throw new PipelineException 
              ("The task (" + rootTask + ") defined by the root node of the check-in " + 
	       "operation (" + rname + ") is currently " + aSupervisedBy + " the " + 
               (isGroup ? "Pipeline work group [" + supervise + "]" : 
                          "user (" + supervise + ")") + ".  " + 
               "Since the user (" + author + ") attempting the check-in does not match the " +
	       aSupervisedBy + " user/group, they are not allowed to check-in this " + 
	       aSubmit + " node.");
        }
      }
      else {
        throw new PipelineException
          ("Check-in aborted for node (" + nname + ") because the root node of " + 
           "the check-in operation (" + rname + ") does not have a " + aPurpose + " of " + 
           "either " + aPrereq + ", " + aEdit + ", " + aSubmit + ", " + aApprove + " or " + 
	   aDeliver + "! Only nodes with these type of " + aPurpose + " may be used as " + 
	   "the root node of a check-in operation."); 
      }
    }
    
    /* is the node being checked-in is part of the same project as the root node? */ 
    if(!rootProjectName.equals(nodeProjectName)) 
      throw new PipelineException
        ("Check-in aborted for node (" + nname + ") because it has a different " + 
         aProjectName + " (" + nodeProjectName + ") than the root node of the check-in " + 
         "operation (" + rname + ") which has a " + aProjectName + 
         " (" + nodeProjectName + ")!");  
    
    /* is the node being checked-in is part of the same task as the root node? */ 
    if(!rootTaskName.equals(nodeTaskName)) 
      throw new PipelineException
        ("Check-in aborted for node (" + nname + ") because it has a different " + 
         aTaskName + " (" + nodeTaskName + ") than the root node of the check-in " + 
         "operation (" + rname + ") which has a " + aTaskName + 
         " (" + nodeTaskName + ")!");  
    
    /* compare the Purpose of the root and current node */ 
    {
      boolean isAlsoEdit = false;
      String productError = null;
      for(String purpose : nodeAnnots.keySet()) {
	if(purpose.equals(aFocus) || purpose.equals(aThumbnail)) {
	  if(!rootAnnots.containsKey(aSubmit)) 
	    throw new PipelineException
	      ("Check-in aborted for node (" + nname + ") because nodes with a " + aPurpose + 
	       " of (" + aFocus + " | " + aThumbnail + ") can only be checked-in when the " + 
	       "root node of the check-in operation has a " + aPurpose + " of " + 
	       "(" + aSubmit + ")!"); 
	}
	else if(purpose.equals(aPrepare)) {
	  if(!rootAnnots.containsKey(aSubmit) && 
	     !rootAnnots.containsKey(aApprove) && 
	     !rootAnnots.containsKey(aDeliver)) 
	    throw new PipelineException
	      ("Check-in aborted for node (" + nname + ") because nodes with a " + aPurpose + 
	       " of (" + aPrepare + ") can only be checked-in when the root node of the " + 
	       "check-in operation has a " + aPurpose + " of " + aSubmit + ", " + aApprove + 
	       " or " + aDeliver + ")!"); 
	}
	else if(purpose.equals(aProduct)) {
	  if(!rootAnnots.containsKey(aApprove)) 
	    productError = 
	      ("Check-in aborted for node (" + nname + ") because nodes with a " + aPurpose + 
	       " of (" + aProduct + ") can only be checked-in when the root node of the " + 
	       "check-in operation has a " + aPurpose + " of (" + aApprove + ")!"); 
	}
	else if(purpose.equals(aSubmit) || 
		purpose.equals(aApprove) || 
		purpose.equals(aDeliver)) {  
	  if(!nname.equals(rname)) 
	    throw new PipelineException
	      ("Check-in aborted for node (" + nname + ") unless it is the root node of " + 
	       "the check-in operation because it has a " + aPurpose + " of " + 
	       "(" + purpose + ")!"); 
	}
	else if(purpose.equals(aEdit)) {
	  if(nname.equals(rname) || rootAnnots.containsKey(aSubmit))
	    isAlsoEdit = true;
	}
      }

      if((productError != null) && !isAlsoEdit)
	throw new PipelineException(productError);
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
   *   The [ProjectName, TaskName, TaskType] array.
   */ 
  private String[] 
  lookupTaskAnnotations
  (
   String name, 
   MasterMgrLightClient mclient, 
   TreeMap<String, BaseAnnotation> byPurpose
  ) 
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = mclient.getAnnotations(name);
    String projectName = null; 
    String taskName    = null; 
    String taskType    = null; 
    for(String aname : annots.keySet()) {
      if(aname.equals("Task") || aname.startsWith("AltTask")) {
        BaseAnnotation an = annots.get(aname);
        
        /* Skip old annotation plugins*/
        if (an.getVendor().equals("Temerity") && 
            an.getVersionID().equals(new VersionID("2.3.2")) &&
            an.getName().equals("Task"))
          continue;
            
        
        String purpose = lookupPurpose(an); 
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
                ("The " + aProjectName + " was set in multiple Task annotations on node " + 
                 "(" + name + "), but the did not match!  Both (" + projectName + ") and " + 
                 "(" + pname + ") where given as the " + aProjectName + ".");
  
            projectName = pname;
          }
  
          {
            String tname = lookupTaskName(an);  
            if(tname == null) 
              throw new PipelineException
                ("The " + aTaskName + " was not set for Task annotation on node " + 
                 "(" + name + ")!"); 
            
            if((taskName != null) && !taskName.equals(tname)) 
              throw new PipelineException 
                ("The " + aTaskName + " was set in multiple Task annotations on node " + 
                 "(" + name + "), but the did not match!  Both (" + taskName + ") and " + 
                 "(" + tname + ") where given as the " + aTaskName + ".");
  
            taskName = tname; 
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
      String names[] = { projectName, taskName, taskType };
      return names;
    }

    return null;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup the annotation Purpose.
   */ 
  private String
  lookupPurpose
  (
    BaseAnnotation an   
  ) 
    throws PipelineException
  {
    String purpose = (String) an.getParamValue(aPurpose);
    if((purpose != null) && (purpose.length() == 0))
      purpose = null;

    return purpose;
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
   * Lookup the TaskName from the annotation.
   */ 
  private String 
  lookupTaskName
  (
    BaseAnnotation an
  ) 
    throws PipelineException
  {
    String taskName = (String) an.getParamValue(aTaskName);
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
  
  private static final long serialVersionUID = -7220177428359011686L;
  
  public static final String aProjectName     = "ProjectName";
  public static final String aTaskName        = "TaskName";
  public static final String aTaskType        = "TaskType";
  public static final String aCustomTaskType  = "CustomTaskType";
  public static final String aAssignedTo      = "AssignedTo";
  public static final String aSupervisedBy    = "SupervisedBy";
  public static final String aApprovalBuilder = "ApprovalBuilder";
  
  public static final String aSimpleAsset   = "Simple Asset";  
  public static final String aModeling      = "Modeling";        
  public static final String aRigging       = "Rigging";         
  public static final String aLookDev       = "Look Dev";        
  public static final String aLayout        = "Layout";          
  public static final String aAnimation     = "Animation";       
  public static final String aEffects       = "Effects";         
  public static final String aLighting      = "Lighting"; 
  public static final String aPlates        = "Plates"; 
  public static final String aTracking      = "Tracking"; 
  public static final String aMattes        = "Mattes"; 
  public static final String aMattePainting = "MattePainting"; 
  public static final String aCompositing   = "Compositing"; 
  public static final String aCUSTOM        = "[[CUSTOM]]";   

  public static final String aPurpose   = "Purpose";
  public static final String aPrereq    = "Prereq";
  public static final String aEdit      = "Edit";
  public static final String aPrepare   = "Prepare";
  public static final String aFocus     = "Focus";
  public static final String aThumbnail = "Thumbnail";
  public static final String aSubmit    = "Submit";
  public static final String aProduct   = "Product";
  public static final String aDeliver   = "Deliver";
  public static final String aApprove   = "Approve";
      
}
