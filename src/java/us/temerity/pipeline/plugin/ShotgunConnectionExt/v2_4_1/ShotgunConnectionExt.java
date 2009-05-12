// $Id: ShotgunConnectionExt.java,v 1.7 2009/05/12 22:46:04 jesse Exp $

package us.temerity.pipeline.plugin.ShotgunConnectionExt.v2_4_1;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.v2_4_1.NodePurpose;
import us.temerity.pipeline.builder.v2_4_1.TaskType;

/*------------------------------------------------------------------------------------------*/
/*   S H O T G U N   C O N N E C T I O N   E X T                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Extension for communicating Pipeline Task information with Shotgun.<p>
 * 
 * Nodes used with this extension should have one of the Task, SubmitTask or ApproveTask 
 * Annotations (v2.4.1) which provide a source for the information transmitted to Shotgun.<P>
 * 
 * Requires the <code>curl</code> application to be installed on MasterMgrClient and 
 * accessible through the default toolset.  This is for uploading thumbnails to the 
 * Shotgun server.<P> 
 * 
 * This plugin makes use of the 
 * <A HREF="http://ws.apache.org/xmlrpc/xmlrpc2/">Apache XML-RPC</A> package from the Apache 
 * Software Foundation.  The JAR file which packages this plugin contains a copies of the 
 * class files making up the following packages from Apache: <BR> 
 * <DIV style="margin-left: 40px;">
 *   xmlrpc-client<BR>
 *   xmlrpc-common<BR>
 *   ws-commons<BR>
 * </DIV><BR>
 * We've extended the functionality of the TypeSerializerImpl and TypeFactory classes in 
 * order to make this plugin work with the non-standard responses that Shotgun provides. 
 * The required Apache 2.0 "LICENSE.txt" and "NOTICE" files are also included in the plugin
 * JAR file and all Apache copyright notices have been retained in source files derived 
 * from Apache sources.
 */
public 
class ShotgunConnectionExt 
  extends BaseMasterExt
{

  public 
  ShotgunConnectionExt()
  {
    super("ShotgunConnection", new VersionID("2.4.1"), "Temerity",
          "Communicates Pipeline Task information with Shotgun.");
    
    {
      ExtensionParam param = 
        new StringExtensionParam
        (aShotgunServer, 
         "The full web path to the shotgun server, including the initial http " +
         "(but not the api2 extension) and a trailing slash", 
         "http://shotgun/");
      addParam(param);
    }
    
    {
      ExtensionParam param = 
        new StringExtensionParam
        (aShotgunUser, 
         "The user in Shotgun that Pipeline is going to use to make changes.", 
         "pipeline");
      addParam(param);
    }
    
    {
      ExtensionParam param = 
        new PasswordExtensionParam
        (aShotgunApiKey, 
         "The API Key for the user in Shotgun that Pipeline is going to use to make changes.", 
         null);
      addParam(param);
    }
    
    {
      String[] temp = {"Scene", "Sequence"};
      ArrayList<String> choices = new ArrayList<String>(Arrays.asList(temp));
      ExtensionParam param = 
        new EnumExtensionParam
        (aShotContainer, 
         "The Type of Shotgun entity that is being used as a container for shots.  " +
         "Changing this once this extension has been in use will result in everything breaking.",
         "Scene",
         choices);
      addParam(param);
    }
    
    {
      ExtensionParam param = 
        new BooleanExtensionParam
        (aNoteOnTask, 
         "Should notes for check-ins be added to the task the related version belongs to.", 
         true);
      addParam(param);
    }
    
    {
      ExtensionParam param = 
        new BooleanExtensionParam
        (aNoteOnEntity, 
         "Should notes for check-ins be added to the entity (shot or asset) " +
         "the related version belongs to.", 
         true);
      addParam(param);
    }
    
    {
      ExtensionParam param = 
        new BooleanExtensionParam
        (aThumbOnEntity, 
         "Should thumbnails be added to the entity (shot or asset) the related " +
         "version belongs to.", 
         true);
      addParam(param);
    }
    
    {
      ExtensionParam param = 
        new BooleanExtensionParam
        (aLongTaskName, 
         "Should the task name in shotgun contain information about the shot or asset " +
         "or should it simply be the task type.", 
         true);
      addParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      Collections.addAll(choices, "_", "-", ".");
      ExtensionParam param = 
        new EnumExtensionParam
        (aTaskSeparator, 
         "The characters used to separate the fields in the task name.",
         "_",
         choices);
      addParam(param);
    }

    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aShotgunServer);
    layout.addEntry(aShotgunUser);
    layout.addEntry(aShotgunApiKey);
    layout.addSeparator();
    layout.addEntry(aShotContainer);
    layout.addEntry(aNoteOnTask);
    layout.addEntry(aNoteOnEntity);
    layout.addEntry(aThumbOnEntity);
    layout.addSeparator();
    layout.addEntry(aTaskSeparator);
    layout.addEntry(aLongTaskName);
    
    setLayout(layout);
    
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
  @Override
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
  @Override
  public void 
  postEnableTask()
  {
    try {
      String hostname = (String) getParamValue(aShotgunServer);
      if((hostname == null) || (hostname.length() == 0))
        throw new PipelineException("No Shotgun Server URL was specified!");
      sHostname = hostname;
      
      String user = (String) getParamValue(aShotgunUser);
      if((user == null) || (user.length() == 0))
        throw new PipelineException("No Shotgun API User was specified!");
      
      String password = (String) getParamValue(aShotgunApiKey);
      if((password == null) || (password.length() == 0))
        throw new PipelineException("No Shotgun API Key was specified!");
      
      sConnection.connectToServer(hostname + "api2", user, password);
      LogMgr.getInstance().log(Kind.Ext, Level.Info, "Shotgun Connection has been enabled.");
      
      String entity = (String) getParamValue(aShotContainer);
      if (entity.equals("Scene")) {
        sConnection.setShotContatinerEntity(ShotgunEntity.Scene);
      }
      else if (entity.equals("Sequence")) {
        sConnection.setShotContatinerEntity(ShotgunEntity.Sequence);
      }
      
      sNoteOnEntity = (Boolean) getParamValue(aNoteOnEntity);
      sNoteOnTask   = (Boolean) getParamValue(aNoteOnTask);
      sThumbOnEntity = (Boolean) getParamValue(aThumbOnEntity);
      
      sTaskSeparator = (String) getParamValue(aTaskSeparator);
      
      boolean longTaskName = (Boolean) getParamValue(aLongTaskName);
      sConnection.setLongTaskNames(longTaskName);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
        ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
        Exceptions.getFullMessage(ex));
    }
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a task after this extension plugin is disabled.<P> 
   * 
   * A extension plugin can be disabled either during server shutdown or during normal
   * server operation when this specific plugin is disabled or removed manually.
   */ 
  @Override
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
  @Override
  public void 
  preDisableTask()
  {
    try {
      sConnection.disconnectFromServer();
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
        Exceptions.getFullMessage(ex));
    }
  }
  
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
    synchronized(sLock) {
      String nodeName = vsn.getName();
      MasterMgrLightClient mclient = getMasterMgrClient();
      try {
        TreeMap<String, BaseAnnotation> byPurpose = new TreeMap<String, BaseAnnotation>();
        String data[] = lookupTaskAnnotations(nodeName, mclient, byPurpose);
        if (byPurpose.keySet().contains(NodePurpose.Submit.toString())) {
          doSubmit(vsn, data, byPurpose.get(NodePurpose.Submit.toString()), mclient);
        }
        else if (byPurpose.keySet().contains(NodePurpose.Approve.toString())) {
          doApprove(vsn, data, byPurpose.get(NodePurpose.Approve.toString()), mclient);
        }
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().log
        (Kind.Ops, Level.Warning, 
         "PostCheckInTask (" + getName() + ") Failed on: " + nodeName + "\n" + 
         ex.getMessage());      
      }
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   P O L I C Y                                                                          */
  /*----------------------------------------------------------------------------------------*/

  private void
  doApprove
  (
    NodeVersion vsn, 
    String[] data, 
    BaseAnnotation approveAnnot, 
    MasterMgrLightClient mclient
  )
    throws PipelineException
  {
    String projectName = data[0];
    
    String nodeName = vsn.getName();
    String taskName = data[1];
    String taskType = data[2];
    
    String entityType = lookupEntityType(approveAnnot);
    Integer taskID = null;
    if (entityType.equals("Asset")) {
      String pieces[] = taskName.split("_");
      if (pieces.length != 2)
        throw new PipelineException
          ("The task name (" + taskName + ") is not a valid asset name for " +
           "Shotgun and Pipeline");
      taskID = sConnection.getAssetTaskID(projectName, pieces[0], taskType);
    }
    else if (entityType.equals("Shot")) {
      String pieces[] = taskName.split("_");
      if (pieces.length != 2)
        throw new PipelineException
          ("The task name (" + taskName + ") is not a valid shot name for " +
           "Shotgun and Pipeline");
      taskID = sConnection.getShotTaskID(projectName, pieces[0], pieces[1], taskType);
    }
    
    VersionID id = vsn.getVersionID();
    if (id.equals(new VersionID("1.0.0"))) {
      sConnection.setStatusOnTask(taskID, ShotgunTaskStatus.WaitingToStart);
      BuilderID builderID = (BuilderID) approveAnnot.getParamValue(aApprovalBuilder);
      sConnection.setApproveBuilderOnTask(taskID, builderID);
      sConnection.setApproveTaskNodeOnTask(taskID, nodeName);
    }
    else
      sConnection.setStatusOnTask(taskID, ShotgunTaskStatus.Approved);
    
    Integer shotgunVersionID = sConnection.getVersionID(taskID, id);

  }
  
  private void
  doSubmit
  (
    NodeVersion vsn, 
    String[] data, 
    BaseAnnotation submitAnnot, 
    MasterMgrLightClient mclient
  )
    throws PipelineException
  {
    TreeMap<String,String> thumbToFocus    = new TreeMap<String,String>();
    TreeMap<String,NodeVersion> thumbNodes = new TreeMap<String,NodeVersion>();
    TreeMap<String,NodeVersion> focusNodes = new TreeMap<String,NodeVersion>();
    TreeMap<String,NodeVersion> editNodes  = new TreeMap<String,NodeVersion>();
    TreeMap<String,NodeVersion> deliveryNodes  = new TreeMap<String,NodeVersion>();
    TreeSet<String> masterFocusNodes = new TreeSet<String>();
    mineSubmitTree(data, vsn, null, thumbToFocus, thumbNodes, focusNodes, editNodes, 
                   deliveryNodes, masterFocusNodes, mclient);
    
    if (masterFocusNodes.size() > 1)
      throw new PipelineException
        ("Some how there was more than one master focus node associated with the task.");
    
    
    String masterFocusNode = null;
    if (masterFocusNodes.size() == 1) 
      masterFocusNode = masterFocusNodes.first();
    
    String projectName = data[0];
    
    String nodeName = vsn.getName();
    String taskName = data[1];
    String taskType = data[2];
    
    String entityType = lookupEntityType(submitAnnot);
    
    /* This is all lazy creation since the exceptionOnDup was set to false */
    sConnection.createProject(projectName);
    
    Integer taskID = null;
    Integer entityID = null;
    ShotgunEntity shotgunEntity = null;

    if (entityType.equals("Asset")) {
      String pieces[] = taskName.split(sTaskSeparator);
      if (pieces.length != 2)
        throw new PipelineException
        ("The task name (" + taskName + ") is not a valid asset name for " +
         "Shotgun and Pipeline");
      entityID = sConnection.createAsset(projectName, pieces[0], pieces[1]);
      taskID = sConnection.createTaskOnAsset(projectName, pieces[0], taskType);
      shotgunEntity = ShotgunEntity.Asset;
    }
    else if (entityType.equals("Shot")) {
      String pieces[] = taskName.split(sTaskSeparator);
      if (pieces.length != 2)
        throw new PipelineException
        ("The task name (" + taskName + ") is not a valid shot name for " +
        "Shotgun and Pipeline");
      entityID = sConnection.createShot(projectName, pieces[0], pieces[1]);
      taskID = sConnection.createTaskOnShot(projectName, pieces[0], pieces[1], taskType);
      shotgunEntity = ShotgunEntity.Shot;
    }

    sConnection.setSubmitTaskNodesOnTask(taskID, nodeName, editNodes);

    //get the approve node...
    //TODO, this all needs to be fixed.

    String approveNodeName = nodeName.replaceAll(sSubmitSuffix, sApproveSuffix);
    approveNodeName = approveNodeName.replaceAll(sSubmitDir, sApproveDir);

    BaseAnnotation annot = null;
    try {
      annot = mclient.getAnnotation(approveNodeName, sTaskAnnotName);
    }
    catch (PipelineException pe) {
    }

    Integer versionID = null;

    //TODO this should never be null, why are we allowing this?
    if (annot == null) {
      versionID = sConnection.createTaskVersion
        (projectName, taskID, vsn.getAuthor(), vsn.getVersionID(), vsn.getMessage(), focusNodes, 
         editNodes, deliveryNodes);
    }
    else {
      BuilderID builderID = (BuilderID) annot.getParamValue(aApprovalBuilder);

      versionID = sConnection.createTaskVersion
        (projectName, taskID, vsn.getAuthor(), vsn.getVersionID(), vsn.getMessage(), focusNodes, 
         editNodes, deliveryNodes, nodeName, approveNodeName, builderID);
    }
    
    TreeMap<String, Integer> temerityNodes = 
      sConnection.createTemerityNodes(versionID, projectName, focusNodes);
    
    Integer noteID = null;
    {
      ArrayList<ShotgunEntityBundle> bundles = new ArrayList<ShotgunEntityBundle>();
      bundles.add(new ShotgunEntityBundle(ShotgunEntity.Version, versionID));
      if (sNoteOnEntity)
        bundles.add(new ShotgunEntityBundle(shotgunEntity, entityID));
      if (sNoteOnTask)
        bundles.add(new ShotgunEntityBundle(ShotgunEntity.Task, taskID));
      
      String noteTitle = 
        "Version (" + vsn.getVersionID() + ") of Task (" + taskName + ") in Project " +
        "(" + projectName + ") submitted for approval.";
      noteID = sConnection.createNote(projectName, noteTitle, vsn.getMessage(), vsn.getAuthor(), bundles);
    }

    sConnection.setLatestSubmittedVersion(shotgunEntity, entityID, versionID);

    sConnection.setStatusOnTask(taskID, ShotgunTaskStatus.PendingReview);

    if (masterFocusNode == null && !focusNodes.isEmpty())
      masterFocusNode = focusNodes.firstKey();
    
    // This should be correct now?
    if (masterFocusNode != null) {
      String thumbName = null;
      for (String each : thumbToFocus.keySet()) {
        if (thumbToFocus.get(each).equals(masterFocusNode)) {
          thumbName = each;
          break;
        }
      }
      
      NodeVersion focusVer = focusNodes.get(masterFocusNode);

      if (focusVer.getPrimarySequence().getFilePattern().getSuffix().equals("mov") || 
          focusVer.getPrimarySequence().getFilePattern().getSuffix().equals("avi")) {
        
        ShotgunEntityBundle ebundle = new ShotgunEntityBundle(ShotgunEntity.Version, versionID);

        uploadFile(ebundle, focusVer, mclient, "sg_preview_qt", "v"+focusVer.getVersionID().toString());
      }
      
      if (thumbName != null) {
        NodeVersion thumb = thumbNodes.get(thumbName);
        ShotgunEntityBundle verBundle = new ShotgunEntityBundle(ShotgunEntity.Version, versionID);
        uploadThumbnail(verBundle, thumb, mclient);

        if (sThumbOnEntity) {
          ShotgunEntityBundle submitBundle = new ShotgunEntityBundle(shotgunEntity, entityID);
          uploadThumbnail(submitBundle, thumb, mclient);
        }
      }
    }
    
    // This should be correct
    for (String thumbName : thumbToFocus.keySet() ) {
      NodeVersion thumb = thumbNodes.get(thumbName);
      String focusName = thumbToFocus.get(thumbName);
      Integer nodeID = temerityNodes.get(focusName);
      ShotgunEntityBundle nodeBundle = new ShotgunEntityBundle(ShotgunEntity.TemerityNode, nodeID);
      uploadThumbnail(nodeBundle, thumb, mclient);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Find the focus/edit nodes in the tree of nodes rooted at the given submit node.
   */ 
  private void 
  mineSubmitTree
  (
    String[] submitData,
    NodeVersion vsn, 
    String currentThumb,  
    TreeMap<String,String> thumbToFocus, 
    TreeMap<String,NodeVersion> thumbNodes,
    TreeMap<String,NodeVersion> focusNodes, 
    TreeMap<String,NodeVersion> editNodes,
    TreeMap<String,NodeVersion> deliveryNodes,
    TreeSet<String> masterFocusNodes,
    MasterMgrLightClient mclient
  )
    throws PipelineException 
  {
    String nodeName = vsn.getName();
    String thumb = currentThumb; 
    TreeMap<String,BaseAnnotation> tAnnots = new TreeMap<String, BaseAnnotation>();
    LogMgr.getInstance().logAndFlush(Kind.Ext, Level.Finer, "NodeName: " + nodeName + "  Annots: " + tAnnots + "\n");
    String[] data = lookupTaskAnnotations(nodeName, mclient, tAnnots);

    if (tAnnots.containsKey(NodePurpose.Focus.toString())) {
      verifyTask(submitData, data); 
      focusNodes.put(nodeName, vsn);
      if(currentThumb != null) {
        thumbToFocus.put(currentThumb, nodeName);
      }
      BaseAnnotation annot = tAnnots.get(NodePurpose.Focus.toString());
      if (annot.getParam("Master") != null) {
        if ((Boolean) annot.getParamValue("Master"))
          masterFocusNodes.add(nodeName);
      }
    }
    else if (tAnnots.containsKey(NodePurpose.Thumbnail.toString())) {
      verifyTask(submitData, data);  
      thumbNodes.put(nodeName, vsn); 
      thumb = nodeName; 
    }
    else if (tAnnots.containsKey(NodePurpose.Product.toString())) {
      return;
    }
    
    if(tAnnots.containsKey(NodePurpose.Deliver.toString())) {
      verifyTask(submitData, data);  
      deliveryNodes.put(nodeName, vsn);
    }
    
    if(tAnnots.containsKey(NodePurpose.Edit.toString())) {
      verifyTask(submitData, data);  
      editNodes.put(nodeName, vsn); 
    }
      
    for(LinkVersion link : vsn.getSources()) {
      if(!link.isLocked()) {
        NodeVersion source = mclient.getCheckedInVersion(link.getName(), link.getVersionID());
        mineSubmitTree(submitData, source, thumb, thumbToFocus, thumbNodes, focusNodes, 
                       editNodes, deliveryNodes, masterFocusNodes, mclient);
      }
    }
  }
  
  private String
  getApproveNode
  (
    String editNode,
    NodeVersion nodeVersion
  )
  {
    String toReturn = null;
    
    return toReturn;
  }

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
   * Lookup the annotation EntityType.
   */ 
  private String
  lookupEntityType
  (
    BaseAnnotation an   
  ) 
    throws PipelineException
  {
    String entityType = (String) an.getParamValue(aEntityType);
    if((entityType != null) && (entityType.length() == 0))
      entityType = null;

    return entityType;
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
    
    if((taskType != null) && taskType.equals(TaskType.CUSTOM.toString())) {
      taskType = (String) an.getParamValue(aCustomTaskType);
      if((taskType != null) && (taskType.length() == 0))
        taskType = null;
    }
    
    return taskType;
  }
  
  /**
   * Verify that the TaskName and TaskType match those of the root node.
   * 
   * @param submitData
   *   The string array obtained by calling {@link #lookupTaskAnnotations(String,
   *   MasterMgrLightClient, TreeMap) lookupTaskAnnotations} on the submit node.
   *   
   * @param data
   *   The string array obtained by calling {@link #lookupTaskAnnotations(String,
   *   MasterMgrLightClient, TreeMap) lookupTaskAnnotations} on the current node.
   */ 
  private void 
  verifyTask
  (
   String submitData[], 
   String data[]
  ) 
    throws PipelineException
  {
    String project  = data[0];
    String taskName = data[1]; 
    String taskType = data[2]; 
    
    
    String rootProject  = submitData[0];
    String rootTaskName = submitData[1];
    String rootTaskType = submitData[2];


    if(rootTaskName != null) {
      if(!taskName.equals(rootTaskName) || !taskType.equals(rootTaskType)
          || !project.equals(rootProject)) {
        throw new PipelineException
          ("Cannot update Shotgun for task (" + project + ":" + 
            taskName + ":" +  taskType + ") because the root node of the check-in belongs " +
            "to a different task (" + rootProject + ":" + rootTaskName + ":" + 
            rootTaskType + ")!  Please limit a check-in operation to nodes which share " +
            "the same task.");
      }
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C U R L                                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  private void
  uploadThumbnail
  (
    ShotgunEntityBundle bundle,
    NodeVersion thumbVsn,
    MasterMgrLightClient client
  ) 
    throws PipelineException
  {
    String curlUrl = sHostname + "upload/api2_publish_thumbnail";
    Path fileName = new Path(new Path(new Path(
      PackageInfo.sRepoPath, 
      thumbVsn.getName()), 
      thumbVsn.getVersionID().toString()), 
      thumbVsn.getPrimarySequence().getPath(0));
    
    ArrayList<String> args = new ArrayList<String>();
    args.add("-F");
    args.add("entity_type=" + bundle.getEntity().toEntity());
    args.add("-F");
    args.add("entity_id=" + bundle.getID().toString());
    args.add("-F");
    args.add("thumb_image=@" + fileName.toString());
    args.add(curlUrl);
    TreeMap<String, String> env = client.getToolsetEnvironment
      (PackageInfo.sPipelineUser, "default", client.getDefaultToolsetName());
    
    for (String arg : args) {
      LogMgr.getInstance().log(Kind.Ext, Level.Finest, arg + " ");
    }

    SubProcessLight proc = 
      new SubProcessLight("Curl-Upload", "curl", args, env, 
                          fileName.getParentPath().toFile());
    proc.run();
  }
  
  private void
  uploadFile
  (
    ShotgunEntityBundle bundle,
    NodeVersion thumbVsn,
    MasterMgrLightClient client, 
    String fieldName,
    String displayName
  ) 
  throws PipelineException
  {
    String curlUrl = sHostname + "upload/api2_upload_file";
    Path fileName = new Path(new Path(new Path(
      PackageInfo.sRepoPath, 
      thumbVsn.getName()), 
      thumbVsn.getVersionID().toString()), 
      thumbVsn.getPrimarySequence().getPath(0));

    ArrayList<String> args = new ArrayList<String>();
    args.add("-F");
    args.add("entity_type=" + bundle.getEntity().toEntity());
    args.add("-F");
    args.add("entity_id=" + bundle.getID().toString());
    args.add("-F");
    args.add("field_name=" + fieldName);
    args.add("-F");
    args.add("display_name=" + displayName);
    args.add("-F");
    args.add("file=@" + fileName.toString());
    args.add(curlUrl);
    TreeMap<String, String> env = client.getToolsetEnvironment
    (PackageInfo.sPipelineUser, "default", client.getDefaultToolsetName());

    for (String arg : args) {
      LogMgr.getInstance().log(Kind.Ext, Level.Finest, arg + " ");
    }

    SubProcessLight proc = 
      new SubProcessLight("Curl-Upload-File", "curl", args, env, 
        fileName.getParentPath().toFile());
    proc.run();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1428025364159140119L;
  
  public static final String aShotgunServer = "ShotgunServer";
  public static final String aShotgunUser   = "ShotgunUser";
  public static final String aShotgunApiKey = "ShotgunApiKey";
  public static final String aShotContainer = "ShotContainer";
  public static final String aNoteOnTask    = "NoteOnTask";
  public static final String aNoteOnEntity  = "NoteOnEntity";
  public static final String aThumbOnEntity = "ThumbOnEntity";
  public static final String aTaskSeparator = "TaskSeparator";
  public static final String aLongTaskName  = "LongTaskName";
  
  public static final String aProjectName     = "ProjectName";
  public static final String aEntityType      = "EntityType";
  public static final String aTaskName        = "TaskName";
  public static final String aTaskType        = "TaskType";
  public static final String aCustomTaskType  = "CustomTaskType";
  public static final String aApprovalBuilder = "ApprovalBuilder";
  public static final String aPurpose         = "Purpose";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private static ShotgunConnection sConnection = new ShotgunConnection(false);
  private static String sHostname;
  private static Object sLock = new Object();
  private static String sSubmitDir = "submit";
  private static String sApproveDir = "approve";
  private static String sSubmitSuffix = "_submit";
  private static String sApproveSuffix = "_approve";
  private static String sTaskAnnotName = "Task";
  
  private static boolean sNoteOnTask;
  private static boolean sNoteOnEntity;
  private static boolean sThumbOnEntity;
  private static String  sTaskSeparator;
}
