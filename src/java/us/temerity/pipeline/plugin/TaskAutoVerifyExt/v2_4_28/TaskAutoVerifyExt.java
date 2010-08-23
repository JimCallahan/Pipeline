package us.temerity.pipeline.plugin.TaskAutoVerifyExt.v2_4_28;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_28.*;

/*------------------------------------------------------------------------------------------*/
/*  T A S K   A U T O   V E R I F Y   E X T                                                 */
/*------------------------------------------------------------------------------------------*/

/**
 * Extension which will automatically run the RunVerifyBuilder for tasks which have been 
 * submitted.
 */
public 
class TaskAutoVerifyExt
  extends BaseMasterExt
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TaskAutoVerifyExt()
  {
    super("TaskAutoVerify", new VersionID("2.4.28"), "Temerity",
          "Restricts access to node operations based on Task Annotations.");
    
    {
      ExtensionParam param =
        new StringExtensionParam
          (aCustomVerifyUser, 
           "The name of the user who will be running verify builders, " +
           "if it is not the pipeline user. Leave it null to have the pipeline user run verify " +
           "builders (not recommended due to permission level concerns).", 
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
      
      setLayout(group);
    }
    
    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public boolean 
  hasPostCheckInTask()
  {
    return true;
  }
  
  @Override
  public void 
  postCheckInTask
  (
    NodeVersion vsn
  )
  {
    MasterMgrLightClient mclient = getMasterMgrClient();

    String nname  = vsn.getName();
    try {
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
      /* No annotations, no service.*/
      if (nodeProjectName == null)
        return;
      /* No submit node, we're done here.*/
      if (!nodeAnnots.containsKey(NodePurpose.Submit))
        return;
      /* The submit node was also the verify node.  So the task is self-verifying.*/
      if (nodeAnnots.containsKey(NodePurpose.Verify))
        return;
      /* No auto-verify for the initial version, since it is probably made by a builder.*/
      if (vsn.getVersionID().equals(new VersionID("1.0.0")))
        return;
      
      /* Now we do some project validation*/
      TreeSet<String> projectList = null;
      String projectParam = (String) getParamValue(aProjectList);
      if (projectParam != null && projectParam.length() > 0) {
        String buffer[] = projectParam.split(",");
        projectList = new TreeSet<String>();
        Collections.addAll(projectList, buffer);
      }
      
      /* Is this project something this extension should be messing with.*/
      if (projectList != null && !projectList.contains(nodeProjectName))
        return;
      
      /* So this is a submit node that we're supposed to work on, so let's do it. */
     String user = "pipeline";
     String customUser = (String) getParamValue(aCustomVerifyUser);
     if (customUser != null && !customUser.equals(""))
       user = customUser;
     
     LogMgr.getInstance().log
     (Kind.Ops, Level.Warning, 
       "Running the RunVerify Builder on node: " + nname + "(v" + vsn.getVersionID() + ")");
     
      RunBuilder builder = 
        new RunBuilder(nname, vsn.getVersionID(), user, 
                       nodeProjectName, nodeTaskIdent1, nodeTaskIdent2, nodeTaskType);
      builder.start();
      
    } 
    catch (PipelineException ex) {
      LogMgr.getInstance().log
      (Kind.Ops, Level.Warning, 
       "PostCheckInTask (" + getName() + ") Failed on: " + nname + "\n" + 
       ex.getMessage());
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
  /*   T H R E A D S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private class
  RunBuilder
    extends Thread
  {
    private
    RunBuilder
    (
      String nodeName,
      VersionID nodeVersion,
      String user, 
      String projectName, 
      String taskIdent1, 
      String taskIdent2, 
      String taskType
    )
    {
      pNodeName    = nodeName;
      pNodeVersion = nodeVersion;
      pUser  = user;
      
      pProjectName = projectName;
      pTaskIdent1 = taskIdent1;
      pTaskIdent2 = taskIdent2;
      pTaskType = taskType;
    }
    
    @Override
    public void 
    run()
    {
      PluginMgrClient plug = PluginMgrClient.getInstance();
      
      String timestamp = String.valueOf(System.currentTimeMillis());
      
      String logMgrInstance = 
        pProjectName + "_" + pTaskIdent1 + "_" + pTaskIdent2 + "_" + 
        pTaskType + "_" + timestamp;
      
      Path logPath = new Path(new Path(new Path(new Path(new Path(new Path(new Path
         (PackageInfo.sTempPath), "pltask"), pProjectName), pTaskIdent1), 
          pTaskIdent2), pTaskType), "verify_" + timestamp);
      
      File logDir = logPath.getParentPath().toFile();
      if (!logDir.exists())
        logDir.mkdirs();
      
      LogMgr log = LogMgr.getInstance(logMgrInstance);
      log.logToFile(logPath, 10, 10485760L);
      log.setLevel(Kind.Bld, Level.Finest);
      log.setLevel(Kind.Ops, Level.Finest);
      
      logPath.toFile().setWritable(true, false);

      MasterMgrClient client = new MasterMgrClient();
      try {
        TreeMap<String, TreeSet<String>> wareas = client.getWorkingAreas();
        if (!wareas.keySet().contains(pUser))
          client.createWorkingArea(pUser, "default");

        BaseBuilderCollection collection = 
          plug.newBuilderCollection("Task", new VersionID("2.4.28"), "Temerity");
        
        MultiMap<String, String> params = new MultiMap<String, String>();
        
        LinkedList<String> bkey = new LinkedList<String>();  
        bkey.add("RunVerify");

        {
          LinkedList<String> keys = new LinkedList<String>(bkey); 
          keys.add(BaseUtil.aUtilContext);
          keys.add(UtilContextUtilityParam.aAuthor); 
            
          params.putValue(keys, pUser, true);
        }
        
        {
          LinkedList<String> keys = new LinkedList<String>(bkey); 
          keys.add(aSubmitNode); 
            
          params.putValue(keys, pNodeName, true);
        }

        {
          LinkedList<String> keys = new LinkedList<String>(bkey); 
          keys.add(aSubmitVersion); 
            
          params.putValue(keys, pNodeVersion.toString(), true);
        }
        
        BuilderInformation info = 
          new BuilderInformation(logMgrInstance, false, false, true, false, params);
        
        BaseBuilder builder = 
          collection.instantiateBuilder
            ("RunVerify", new MasterMgrClient(), new QueueMgrClient(), info);
        
        builder.run();
      }
      catch (PipelineException ex) {
        LogMgr.getInstance().log
        (Kind.Ops, Level.Warning, 
         "PostCheckInTask RunBuilder Thread Failed in the TaskAutoVerifyExt on: " + 
         pNodeName+ "\n" + ex.getMessage());
      }
      finally {
        log.cleanup();
        client.disconnect();
      }
    }
    
    private String pNodeName;
    private VersionID pNodeVersion;
    private String pUser;
    
    private String pProjectName; 
    private String pTaskIdent1;
    private String pTaskIdent2; 
    private String pTaskType;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4218875509634993948L;
  
  public static final String aSubmitNode       = "SubmitNode";
  public static final String aSubmitVersion    = "SubmitVersion";
  public static final String aCustomVerifyUser = "CustomVerifyUser";
  public static final String aProjectList      = "ProjectList";

  private static final String aProjectName     = "ProjectName";
  private static final String aTaskIdent1      = "TaskIdent1";
  private static final String aTaskIdent2      = "TaskIdent2";
  private static final String aTaskType        = "TaskType";
  private static final String aCustomTaskType  = "CustomTaskType";
  
  private static final String aCUSTOM          = "[[CUSTOM]]";   

  private static final String aPurpose         = "Purpose";
}
