// $Id: TaskDb.java,v 1.8 2007/08/20 07:41:49 jim Exp $

package us.temerity.pipeline.plugin.TaskPolicyExt.v2_3_2;

import us.temerity.pipeline.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;


/*------------------------------------------------------------------------------------------*/
/*   T A S K   D B                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A shared helper class used by all TaskPolicyExt plugins to serialize communication
 * with the SQL server storing task information. 
 */
public 
class TaskDb
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new database helper class.
   */ 
  public 
  TaskDb()
  {}



  /*----------------------------------------------------------------------------------------*/
  /*  S E R V E R   C O N N E C T I O N                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Establish a connection with the given SQL server.
   * 
   * @param hostname
   *   The name of the host running the SQL server.
   * 
   * @param port
   *   The network port number listened to by the SQL server.
   * 
   * @param user
   *   The name of the SQL user making the connection.
   * 
   * @param password
   *   The password of the SQL user making the connection.
   */  
  public synchronized void
  connect
  (
   String hostname, 
   int port, 
   String user, 
   String password
  ) 
    throws PipelineException
  {
    /* init connection fields */ 
    pHostname = hostname;
    pPort     = port; 
    pUser     = user;
    pPassword = password;

    /* init JDBC driver */ 
    try {
      Class.forName("com.mysql.jdbc.Driver"); 

      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Finer, 
         "Initialized the MySQL JDBC driver used by the TaskPolicy extension!"); 
      LogMgr.getInstance().flush();
    }
    catch (Exception ex) {
      Path path = PackageInfo.getJavaRuntime(OsType.Unix);
      Path ext = new Path(path.getParentPath().getParentPath(), "lib/ext");

      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Severe, 
	 "Unable to inialize the MySQL JDBC driver for the TaskPolicy extension!\n" + 
	 "\n" + 
	 "The driver (Connector/J 5.0) needs to be installed before this plugin can " + 
	 "be used. The driver can be download from here:\n" + 
	 "\n" + 
	 "  http://dev.mysql.com/downloads/connector/j/5.0.html\n" + 
	 "\n" + 
	 "The JAR file for the driver will need to be installed in " + 
	 "(" + ext.toOsString() + ") in order for Pipeline to find it properly.");
    }

    /* establish an initial connection */ 
    verifyConnection();
  }

  /**
   * Close down any existing connection with the SQL server.
   */  
  public synchronized void
  disconnect() 
  {
    if(pConnect == null) 
      return;

    try {
      pConnect.close();  
    }
    catch(SQLException ex) {
    }

    pConnect = null;

    LogMgr.getInstance().log
      (LogMgr.Kind.Ext, LogMgr.Level.Fine, 
       "Closed the connection to the SQL server on (" + pHostname + ":" + pPort + ") " +
       "for the TaskPolicy extension."); 
    LogMgr.getInstance().flush();
  }
 

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Make sure the network connection to the SQL server has been established.  If the 
   * connection is down, try to reconnect.
   */
  private synchronized void
  verifyConnection() 
    throws PipelineException
  {   
    if(pConnect != null) 
      return;

    /* connect to the SQL server */ 
    try {
      String url = "jdbc:mysql://" + pHostname + ":" + pPort + "/pltasks";
      pConnect = DriverManager.getConnection(url, pUser, pPassword); 
      pConnect.setAutoCommit(false);

      String isolation = null;
      switch(pConnect.getTransactionIsolation()) {
      case Connection.TRANSACTION_READ_UNCOMMITTED:
        isolation = "Read Uncommited";
        break;

      case Connection.TRANSACTION_READ_COMMITTED:
        isolation = "Read Commited";
        break;

      case Connection.TRANSACTION_REPEATABLE_READ:
        isolation = "Repeatable Read";
        break;

      case Connection.TRANSACTION_SERIALIZABLE:
        isolation = "Serializable";
        break;

      case Connection.TRANSACTION_NONE:
        isolation = "None";
        break;
        
      default:
        isolation = "Uknown";
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Fine, 
         "Established a connection to the SQL server on (" + pHostname + ":" + pPort + ") " +
         "for the TaskPolicy extension using a " + isolation + " transaction isolation."); 
      LogMgr.getInstance().flush();
    }
    catch(SQLException ex) {
      pConnect = null;

      throw new PipelineException 
        ("Unable to establish a connection to the SQL server on " + 
         "(" + pHostname + ":" + pPort + ") for user (" + pUser + "):\n\n" + 
         ex.getMessage());
    }
   
    /* precompile the SQL prepared statements */ 
    try {
      txnStart(); 

      /* task titles */ 
      {
        String sql = ("SELECT title_id FROM task_titles WHERE title_name = ?"); 
        pGetTaskTitleSt = pConnect.prepareStatement(sql);
      }

      {
        String sql = ("INSERT INTO task_titles (title_name) VALUES (?)");
        pInsertTaskTitleSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }

      /* task types */ 
      {
        String sql = ("SELECT type_id FROM task_types WHERE type_name = ?"); 
        pGetTaskTypeSt = pConnect.prepareStatement(sql);
      }

      {
        String sql = ("INSERT INTO task_types (type_name) VALUES (?)");
        pInsertTaskTypeSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }

      /* task activity */ 
      {
        String sql = ("SELECT active_id FROM task_activity WHERE active_name = ?"); 
        pGetTaskActivitySt = pConnect.prepareStatement(sql);
      }

      {
        String sql = ("INSERT INTO task_activity (active_name) VALUES (?)");
        pInsertTaskActivitySt = 
          pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }
      
      /* task status */ 
      {
        String sql = ("SELECT status_id FROM task_status WHERE status_name = ?"); 
        pGetTaskStatusSt = pConnect.prepareStatement(sql);
      }

      {
        String sql = ("INSERT INTO task_status (status_name) VALUES (?)");
        pInsertTaskStatusSt = 
          pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }

      /* idents */ 
      {
        String sql = ("SELECT ident_id FROM idents WHERE ident_name = ? AND is_group = ?"); 
        pGetIdentSt = pConnect.prepareStatement(sql);
      }

      {
        String sql = ("INSERT INTO idents (ident_name, is_group) VALUES (?, ?)");
        pInsertIdentSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }
      
      /* supervisors */ 
      {
        String sql = ("INSERT INTO supervisors (task_id) VALUES (?)");
        pInsertNullTaskSupervisorSt = pConnect.prepareStatement(sql); 
      }

      /* memberships */ 
      {
        String sql = ("DELETE FROM memberships"); 
        pClearMembershipsSt = pConnect.prepareStatement(sql);
      }

      {
        String sql = 
          ("INSERT INTO memberships (user_id, group_id, is_manager) VALUES (?, ?, ?)");
        pInsertMembershipSt = pConnect.prepareStatement(sql); 
      }

      /* note */
      {
        String sql = ("INSERT INTO notes (note_text) VALUES (?)");
        pInsertNoteSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }
      
      /* tasks */ 
      {
        String sql = ("SELECT task_id FROM tasks WHERE title_id = ? AND type_id = ?"); 
        pGetTaskSt = pConnect.prepareStatement(sql);
      }

      {
        String sql = 
          ("INSERT INTO tasks (title_id, type_id, active_id, status_id, last_modified) " +
           "VALUES (?, ?, ?, ?, ?)");
        pInsertTaskSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }
      
      {
        String sql = ("UPDATE tasks SET active_id = ?, last_modified = ? WHERE task_id = ?");
        pSubmitTaskSt = pConnect.prepareStatement(sql); 
      }
      
      {
        String sql = ("UPDATE tasks SET last_modified = ? WHERE task_id = ?");
        pApproveTaskSt = pConnect.prepareStatement(sql); 
      }
      
      {
        String sql = ("UPDATE tasks SET assigned_to = ? WHERE task_id = ?");
        pAssignedToTaskSt = pConnect.prepareStatement(sql); 
      }
      
      /* events */ 
      {
        String sql = 
          ("INSERT INTO events (task_id, ident_id, stamp, note_id, builder_id) " + 
           "VALUES (?, ?, ?, ?, ?)");
        pInsertCreateEventSt = 
          pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }

      {
        String sql = 
          ("INSERT INTO events " + 
           "(task_id, ident_id, stamp, note_id, new_active_id, builder_id) " + 
           "VALUES (?, ?, ?, ?, ?, ?)");
        pInsertSubmitEventSt = 
          pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }
      
      {
        String sql = 
          ("INSERT INTO events " + 
           "(task_id, ident_id, stamp, note_id) " + 
           "VALUES (?, ?, ?, ?)");
        pInsertApproveEventSt = 
          pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }
      
      /* builders */ 
      {
        String sql = ("SELECT builder_id FROM builders WHERE builder_path = ?"); 
        pGetBuilderPathSt = pConnect.prepareStatement(sql);
      }

      {
        String sql = ("INSERT INTO builders (builder_path) VALUES (?)");
        pInsertBuilderPathSt = 
          pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }
      
      /* node names */ 
      {
        String sql = ("SELECT node_id FROM node_names WHERE node_name = ?"); 
        pGetNodeNameSt = pConnect.prepareStatement(sql);
      }

      {
        String sql = ("INSERT INTO node_names (node_name) VALUES (?)");
        pInsertNodeNameSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }
      
      /* node info */ 
      {
        String sql = 
          ("INSERT INTO node_info " + 
           "(node_id, node_version, event_id, " + 
            "is_edit, is_submit, is_focus, is_thumb, is_approve) " + 
           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        pInsertNodeInfoSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
      }

      /* thumb info */ 
      {
        String sql = 
          ("INSERT INTO thumb_info " + 
           "(thumb_info_id, focus_info_id, thumb_path) " + 
           "VALUES (?, ?, ?)");
        pInsertThumbInfoSt = pConnect.prepareStatement(sql); 
      }

      txnCommit(); 
    }
    catch(SQLException ex) {
      txnRollback("Unable to compile SQL prepared statements", ex);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Syncronize the users, work groups and group memberships.
   */
  public synchronized void
  updateWorkGroups
  (
   WorkGroups workGroups  
  ) 
    throws PipelineException
  {
    verifyConnection(); 
    try {
      txnStart(); 
    
      TreeMap<String,Integer> users = new TreeMap<String,Integer>();
      for(String uname : workGroups.getUsers()) 
        users.put(uname, lookupIdent(uname, false));

      TreeMap<String,Integer> groups = new TreeMap<String,Integer>();
      for(String gname : workGroups.getGroups()) 
        groups.put(gname, lookupIdent(gname, true));

      clearMemberships();
      for(String uname : users.keySet()) {
        Integer userID = users.get(uname);

        for(String gname : groups.keySet()) {
          Integer groupID = groups.get(gname);

          Boolean mm = workGroups.isMemberOrManager(uname, gname);

          if(mm != null) 
            insertMembership(userID, groupID, mm);
        }
      }

      txnCommit(); 
    }
    catch(SQLException ex) {
      txnRollback
        ("The TaskPolicy extension was unable to synchronize the WorkGroup information " + 
         "with the SQL database!", ex);
    }
  }
  
  /**
   * Synchronize the AssignedTo property of a task.
   * 
   * @param taskTitle
   *   The title of the task.
   * 
   * @param taskType
   *   The type of task.
   * 
   * @param assignedTo
   *   The name of the user/group assigned to the task 
   *   or <CODE>null</CODE> if unassigned.
   * 
   * @param isGroup
   *   Whether the assigned name is a work group.
   */
  public synchronized void
  updateAssignedTo
  (
   String taskTitle, 
   String taskType,
   String assignedTo, 
   boolean isGroup
  ) 
    throws PipelineException
  {
    verifyConnection(); 
    try {
      txnStart(); 

      /* lookup the task */ 
      Integer titleID = lookupTaskTitle(taskTitle);
      Integer typeID  = lookupTaskType(taskType);
      Integer taskID  = getTask(titleID, typeID); 

      /* make sure the task exists! */ 
      if(taskID == null)
        throw new PipelineException
          ("Somehow no task (" + taskTitle + ":" + taskType + ") does not exist!"); 
      

      /* get the identity of the user/group assigned to the task */ 
      Integer identID = null;
      if((assignedTo != null) && (assignedTo.length() > 0)) 
        identID = lookupIdent(assignedTo, isGroup);

      /* update the task */ 
      assignTask(taskID, identID);

      txnCommit(); 
    }
    catch(SQLException ex) {
      txnRollback
        ("The TaskPolicy extension was unable to synchronize the AssignedTo property for " + 
         "task (" + taskTitle + ":" + taskType + ") with the SQL database!", ex);
    }
  }

  /**
   * Insert a task into the SQL database after its initial submit node check-in.
   * 
   * @param taskTitle
   *   The title of the task.
   * 
   * @param taskType
   *   The type of task.
   * 
   * @param submitNode
   *   The initial version of the submit node being checked-in.
   * 
   * @param thumbToFocus
   *   The mapping of the names of the Thumbnail nodes to the corresponding Focus nodes 
   *   upstream of the submit node being checked-in.
   *
   * @param thumbNodes
   *   The current version of the Thumbnail nodes upstream of the Submit node being checked-in
   *   indexed by Thunbnail node name.
   *
   * @param focusNodes
   *   The current version of the Focus nodes upstream of the Submit node being checked-in
   *   indexed by Focus node name.
   *
   * @param editNodes
   *   The current version of the Edit nodes upstream of the Submit node being checked-in
   *   indexed by Edit node name.
   */  
  public synchronized void
  submitTask
  (
   String taskTitle, 
   String taskType,
   NodeVersion submitNode, 
   Path builderPath, 
   TreeMap<String,String> thumbToFocus,  
   TreeMap<String,NodeVersion> thumbNodes, 
   TreeMap<String,NodeVersion> focusNodes,  
   TreeMap<String,NodeVersion> editNodes
  )
    throws PipelineException
  {
    verifyConnection(); 
    try {
      txnStart(); 

      /* lookup the task */ 
      Integer titleID = lookupTaskTitle(taskTitle);
      Integer typeID  = lookupTaskType(taskType);
      Integer taskID  = getTask(titleID, typeID); 

      /* make sure only the "pipeline" user can create a task! */ 
      if((taskID == null) && !submitNode.getAuthor().equals(PackageInfo.sPipelineUser))
        throw new PipelineException
          ("Somehow no task (" + taskTitle + ":" + taskType + ") exists yet, but the " + 
           "check-in of the SubmitNode (" + submitNode.getName() + ") which will create " +
           "the task was not performed by the (" + PackageInfo.sPipelineUser + ") user!");

      /* create the submit event (maybe the task too) */ 
      Integer eventID = null;
      {
        Integer identID = lookupIdent(submitNode.getAuthor(), false); 
        Integer noteID  = insertNote(submitNode.getMessage());

        Integer builderID = null;
        if(builderPath != null) 
          builderID = lookupBuilderPath(builderPath);

        long stamp = submitNode.getTimeStamp();

        if(taskID == null) { 
          Integer activeID = lookupTaskActivity("Inactive");
          Integer statusID = lookupTaskStatus("Unapproved");
          taskID = insertTask(titleID, typeID, activeID, statusID, stamp); 
          eventID = insertCreateEvent(taskID, identID, stamp, noteID, builderID);
        }
        else {
          Integer submitID = lookupTaskActivity("Submitted");
          eventID = insertSubmitEvent(taskID, identID, stamp, noteID, builderID, submitID);
          submitTask(taskID, submitID, stamp);
        }
      }

      /* attach info for the submit node to the event */ 
      Integer submitNodeID = lookupNodeName(submitNode.getName());
      insertNodeInfo(submitNodeID, submitNode.getVersionID(), eventID, 
                     false, true, false, false, false);
      
      /* attach info for any upstream thumbnail nodes to the event */ 
      TreeMap<String,Integer> thumbNodeInfos = new TreeMap<String,Integer>();
      for(NodeVersion thumbNode : thumbNodes.values()) {
        Integer nodeID = lookupNodeName(thumbNode.getName());
        Integer infoID = insertNodeInfo(nodeID, thumbNode.getVersionID(), eventID, 
                                        false, false, false, true, false); 
        thumbNodeInfos.put(thumbNode.getName(), infoID); 
      }

      /* attach info for any upstream focus nodes to the event */ 
      TreeMap<String,Integer> focusNodeInfos = new TreeMap<String,Integer>();
      for(NodeVersion focusNode : focusNodes.values()) {
        Integer nodeID = lookupNodeName(focusNode.getName());
        Integer infoID = insertNodeInfo(nodeID, focusNode.getVersionID(), eventID, 
                                        false, false, true, false, false); 
        focusNodeInfos.put(focusNode.getName(), infoID); 
      }

      /* attach thumbnail to focus node mappings for the event */ 
      for(String tname : thumbToFocus.keySet()) {
        Integer tinfoID = thumbNodeInfos.get(tname);
        Integer finfoID = focusNodeInfos.get(thumbToFocus.get(tname));

        NodeVersion thumbNode = thumbNodes.get(tname); 
        Path tpath = thumbNode.getPrimarySequence().getPath(0); 
        Path rpath = new Path(tname + "/" + thumbNode.getVersionID() + "/" + tpath); 
        insertThumbInfo(tinfoID, finfoID, rpath); 
      }
      
      /* attach info for any upstream edit nodes to the event */ 
      for(NodeVersion editNode : editNodes.values()) {
        Integer nodeID = lookupNodeName(editNode.getName());
        insertNodeInfo(nodeID, editNode.getVersionID(), eventID, 
                       true, false, false, false, false); 
      }

      txnCommit(); 
    }
    catch(SQLException ex) {
      txnRollback
        ("The TaskPolicy extension was unable to register the submission of task " + 
         "(" + taskTitle + ":" + taskType + ") with the SQL database!", ex);
    }
  }

  /**
   * Insert a task into the SQL database after its initial approve node check-in.
   * 
   * @param taskTitle
   *   The title of the task.
   * 
   * @param taskType
   *   The type of task.
   * 
   * @param approveNode
   *   The initial version of the Approve node being checked-in.
   */  
  public synchronized void
  approveTask
  (
   String taskTitle, 
   String taskType,
   NodeVersion approveNode
  )
    throws PipelineException
  {
    verifyConnection(); 
    try {
      txnStart(); 

      /* lookup the task */ 
      Integer titleID = lookupTaskTitle(taskTitle);
      Integer typeID  = lookupTaskType(taskType);
      Integer taskID  = getTask(titleID, typeID); 

      /* make sure task exists */ 
      if(taskID == null) 
        throw new PipelineException
          ("Somehow no task (" + taskTitle + ":" + taskType + ") exists yet, but an  " + 
           "attempt to check-in of the ApproveNode (" + approveNode.getName() + ") is " + 
           "being performed!"); 
      
      /* create the approve event */ 
      Integer eventID = null;
      {
        Integer identID = lookupIdent(approveNode.getAuthor(), false); 
        Integer noteID  = insertNote(approveNode.getMessage());
        
        long stamp = approveNode.getTimeStamp();

        eventID = insertApproveEvent(taskID, identID, stamp, noteID);
        approveTask(taskID, stamp);
      }

      /* attach info for the approve node to the event */ 
      Integer approveNodeID = lookupNodeName(approveNode.getName());
      insertNodeInfo(approveNodeID, approveNode.getVersionID(), eventID, 
                     false, false, false, false, true);

      txnCommit(); 
    }
    catch(SQLException ex) {
      txnRollback
        ("The TaskPolicy extension was unable to register the initial approval of task " + 
         "(" + taskTitle + ":" + taskType + ") with the SQL database!", ex);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*  T A B L E   H E L P E R S                                                             */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the ID for a specific task title creating a new entry if needed.
   * 
   * @param title
   *   The new task title. 
   * 
   * @return 
   *   The ID of the entry. 
   */ 
  private synchronized Integer
  lookupTaskTitle
  (
   String title
  ) 
    throws SQLException
  {
    Integer id = getTaskTitle(title); 
    if(id == null) 
      id = insertTaskTitle(title); 

    return id;
  }

  /**
   * Get the ID for a specific task title.
   * 
   * @param title
   *   The task title. 
   * 
   * @return 
   *   The ID of the entry or <CODE>null</CODE> if it does not yet exist. 
   */ 
  private synchronized Integer
  getTaskTitle
  (
   String title   
  ) 
    throws SQLException
  {
    Integer id = null;

    pGetTaskTitleSt.setString(1, title);
    ResultSet rs = pGetTaskTitleSt.executeQuery();
    while(rs.next()) 
      id = rs.getInt(1);
    rs.close();

    return id;
  }
  
  /**
   * Insert a new task title.
   * 
   * @param title
   *   The new task title. 
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertTaskTitle
  (
   String title
  ) 
    throws SQLException
  {
    pInsertTaskTitleSt.setString(1, title);
    pInsertTaskTitleSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertTaskTitleSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ID for a specific task type creating a new entry if needed.
   * 
   * @param type
   *   The new task type. 
   * 
   * @return 
   *   The ID of the entry. 
   */ 
  private synchronized Integer
  lookupTaskType
  (
   String type
  ) 
    throws SQLException
  {
    Integer id = getTaskType(type); 
    if(id == null) 
      id = insertTaskType(type); 

    return id;
  }

  /**
   * Get the ID for a specific task type.
   * 
   * @param type
   *   The task type. 
   * 
   * @return 
   *   The ID of the entry or <CODE>null</CODE> if it does not yet exist. 
   */ 
  private synchronized Integer
  getTaskType
  (
   String type   
  ) 
    throws SQLException
  {
    Integer id = null;

    pGetTaskTypeSt.setString(1, type);
    ResultSet rs = pGetTaskTypeSt.executeQuery();
    while(rs.next()) 
      id = rs.getInt(1);
    rs.close();

    return id;
  }
  
  /**
   * Insert a new task type.
   * 
   * @param type
   *   The new task type. 
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertTaskType
  (
   String type
  ) 
    throws SQLException
  {
    pInsertTaskTypeSt.setString(1, type);
    pInsertTaskTypeSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertTaskTypeSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ID for a specific task activity creating a new entry if needed.
   * 
   * @param activity
   *   The new task activity. 
   * 
   * @return 
   *   The ID of the entry. 
   */ 
  private synchronized Integer
  lookupTaskActivity
  (
   String activity
  ) 
    throws SQLException
  {
    Integer id = getTaskActivity(activity); 
    if(id == null) 
      id = insertTaskActivity(activity); 

    return id;
  }

  /**
   * Get the ID for a specific task activity.
   * 
   * @param activity
   *   The task activity. 
   * 
   * @return 
   *   The ID of the entry or <CODE>null</CODE> if it does not yet exist. 
   */ 
  private synchronized Integer
  getTaskActivity
  (
   String activity   
  ) 
    throws SQLException
  {
    Integer id = null;

    pGetTaskActivitySt.setString(1, activity);
    ResultSet rs = pGetTaskActivitySt.executeQuery();
    while(rs.next()) 
      id = rs.getInt(1);
    rs.close();

    return id;
  }
  
  /**
   * Insert a new task activity.
   * 
   * @param activity
   *   The new task activity. 
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertTaskActivity
  (
   String activity
  ) 
    throws SQLException
  {
    pInsertTaskActivitySt.setString(1, activity);
    pInsertTaskActivitySt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertTaskActivitySt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ID for a specific task status creating a new entry if needed.
   * 
   * @param status
   *   The new task status. 
   * 
   * @return 
   *   The ID of the entry. 
   */ 
  private synchronized Integer
  lookupTaskStatus
  (
   String status
  ) 
    throws SQLException
  {
    Integer id = getTaskStatus(status); 
    if(id == null) { 
      id = insertTaskStatus(status); 
      insertNullTaskSupervisor(id);
    }

    return id;
  }

  /**
   * Get the ID for a specific task status.
   * 
   * @param status
   *   The task status. 
   * 
   * @return 
   *   The ID of the entry or <CODE>null</CODE> if it does not yet exist. 
   */ 
  private synchronized Integer
  getTaskStatus
  (
   String status   
  ) 
    throws SQLException
  {
    Integer id = null;

    pGetTaskStatusSt.setString(1, status);
    ResultSet rs = pGetTaskStatusSt.executeQuery();
    while(rs.next()) 
      id = rs.getInt(1);
    rs.close();

    return id;
  }
  
  /**
   * Insert a new task status.
   * 
   * @param status
   *   The new task status. 
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertTaskStatus
  (
   String status
  ) 
    throws SQLException
  {
    pInsertTaskStatusSt.setString(1, status);
    pInsertTaskStatusSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertTaskStatusSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ID for a specific user/group identity creating a new entry if needed.
   * 
   * @param ident
   *   The new user/group identity. 
   * 
   * @param isGroup
   *   Whether the identity is a work group.
   * 
   * @return 
   *   The ID of the entry. 
   */ 
  private synchronized Integer
  lookupIdent
  (
   String ident, 
   boolean isGroup
  ) 
    throws SQLException
  {
    Integer id = getIdent(ident, isGroup); 
    if(id == null) 
      id = insertIdent(ident, isGroup); 

    return id;
  }

  /**
   * Get the ID for a specific user/group identity.
   * 
   * @param ident
   *   The user/group identity. 
   * 
   * @param isGroup
   *   Whether the identity is a work group.
   * 
   * @return 
   *   The ID of the entry or <CODE>null</CODE> if it does not yet exist. 
   */ 
  private synchronized Integer
  getIdent
  (
   String ident, 
   boolean isGroup  
  ) 
    throws SQLException
  {
    Integer id = null;

    pGetIdentSt.setString(1, ident);
    pGetIdentSt.setBoolean(2, isGroup);
    ResultSet rs = pGetIdentSt.executeQuery();
    while(rs.next()) 
      id = rs.getInt(1);
    rs.close();

    return id;
  }
  
  /**
   * Insert a new user/group identity.
   * 
   * @param ident
   *   The new user/group identity. 
   * 
   * @param isGroup
   *   Whether the identity is a work group.
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertIdent
  (
   String ident, 
   boolean isGroup
  ) 
    throws SQLException
  {
    pInsertIdentSt.setString(1, ident);
    pInsertIdentSt.setBoolean(2, isGroup);
    pInsertIdentSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertIdentSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Insert a NULL entry for the supervistor of the given task.
   * 
   * @param taskID
   *   The ID of the task. 
   */ 
  private synchronized void
  insertNullTaskSupervisor
  (
   int taskID 
  ) 
    throws SQLException
  {
    pInsertNullTaskSupervisorSt.setInt(1, taskID);
    pInsertNullTaskSupervisorSt.executeUpdate();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove all entries from the memberships table.
   */
  private synchronized void
  clearMemberships()
    throws SQLException
  {
    pClearMembershipsSt.executeUpdate();
  }

  /**
   * Insert work group membership information for a user.
   * 
   * @param userID
   *   The ID of the user. 
   * 
   * @param groupID
   *   The ID of the group.
   * 
   * @param isManager
   *   Whether the user is a manager of the work group (or just a member).
   */ 
  private synchronized void
  insertMembership
  (
   int userID, 
   int groupID, 
   boolean isManager
  ) 
    throws SQLException
  {
    pInsertMembershipSt.setInt(1, userID);
    pInsertMembershipSt.setInt(2, groupID);
    pInsertMembershipSt.setBoolean(3, isManager);
    pInsertMembershipSt.executeUpdate();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Insert a new note.
   * 
   * @param text
   *   The new note text. 
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertNote
  (
   String text
  ) 
    throws SQLException
  {
    pInsertNoteSt.setString(1, text);
    pInsertNoteSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertNoteSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ID for a specific task.
   * 
   * @param titleID
   *   The ID of the task title. 
   * 
   * @param typeID
   *   The ID of the task type. 
   * 
   * @return 
   *   The ID of the entry or <CODE>null</CODE> if it does not yet exist. 
   */ 
  private synchronized Integer
  getTask
  (
   int titleID, 
   int typeID
  ) 
    throws SQLException
  {
    Integer id = null;

    pGetTaskSt.setInt(1, titleID);
    pGetTaskSt.setInt(2, typeID);
    ResultSet rs = pGetTaskSt.executeQuery();
    while(rs.next()) 
      id = rs.getInt(1);
    rs.close();

    return id;
  }
  
  /**
   * Insert a new task.
   * 
   * @param titleID
   *   The ID of the task title. 
   * 
   * @param typeID
   *   The ID of the task type. 
   * 
   * @param activityID
   *   The ID of the task activity. 
   * 
   * @param statusID
   *   The ID of the task status. 
   * 
   * @param stamp
   *   The timestamp of the last modification.
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertTask
  (
   int titleID, 
   int typeID, 
   int activityID, 
   int statusID, 
   long stamp
  ) 
    throws SQLException
  {
    pInsertTaskSt.setInt(1, titleID);
    pInsertTaskSt.setInt(2, typeID);
    pInsertTaskSt.setInt(3, activityID);
    pInsertTaskSt.setInt(4, statusID);
    pInsertTaskSt.setTimestamp(5, new Timestamp(stamp));
    pInsertTaskSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertTaskSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }

  /**
   * Change the task activity to "Submitted" for a task.
   * 
   * @param taskID
   *   The ID of the task. 
   * 
   * @param activityID
   *   The ID of the task activity. 
   * 
   * @param stamp
   *   The timestamp of the last modification.
   */
  private synchronized void
  submitTask
  (
   int taskID, 
   int activityID,
   long stamp
  ) 
    throws SQLException
  {
    pSubmitTaskSt.setInt(1, activityID); 
    pSubmitTaskSt.setTimestamp(2, new Timestamp(stamp));
    pSubmitTaskSt.setInt(3, taskID); 
    pSubmitTaskSt.executeUpdate();
  }

  /**
   * Update the last modified timestamp after an initial approval for a task.
   * 
   * @param taskID
   *   The ID of the task. 
   * 
   * @param stamp
   *   The timestamp of the last modification.
   */
  private synchronized void
  approveTask
  (
   int taskID, 
   long stamp
  ) 
    throws SQLException
  {
    pApproveTaskSt.setTimestamp(1, new Timestamp(stamp));
    pApproveTaskSt.setInt(2, taskID); 
    pApproveTaskSt.executeUpdate();
  }

  /**
   * Update who is assigned to the task.
   * 
   * @param taskID
   *   The ID of the task. 
   * 
   * @param assignedTo
   *   The ID of the user/group assinged to the task.
   */
  private synchronized void
  assignTask
  (
   int taskID, 
   Integer identID
  ) 
    throws SQLException
  {
    if(identID != null) 
      pAssignedToTaskSt .setInt(1, identID);
    else 
      pAssignedToTaskSt.setNull(1, Types.INTEGER);

    pAssignedToTaskSt.setInt(2, taskID); 
    pAssignedToTaskSt.executeUpdate();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Insert a new event for task creation.
   * 
   * @param taskID
   *   The ID of the task. 
   * 
   * @param identID
   *   The ID of the user creating the task. 
   * 
   * @param stamp
   *   The timestamp of when the task was created. 
   * 
   * @param noteID
   *   The ID of the note associated with the event.
   * 
   * @param builderID
   *   The ID of post approval builder or <CODE>null</CODE> if unset.
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertCreateEvent
  (
   int taskID, 
   int identID, 
   long stamp, 
   int noteID, 
   Integer builderID
  ) 
    throws SQLException
  {
    pInsertCreateEventSt.setInt(1, taskID);
    pInsertCreateEventSt.setInt(2, identID);
    pInsertCreateEventSt.setTimestamp(3, new Timestamp(stamp));
    pInsertCreateEventSt.setInt(4, noteID);
    if(builderID != null) 
      pInsertCreateEventSt.setInt(5, builderID); 
    else 
      pInsertCreateEventSt.setNull(5, Types.INTEGER);
    pInsertCreateEventSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertCreateEventSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }

  /**
   * Insert a new event for the submission of a task for review. 
   * 
   * @param taskID
   *   The ID of the task. 
   * 
   * @param identID
   *   The ID of the user creating the task. 
   * 
   * @param stamp
   *   The timestamp of when the task was created. 
   * 
   * @param noteID
   *   The ID of the note associated with the event.
   * 
   * @param builderID
   *   The ID of post approval builder or <CODE>null</CODE> if unset.
   * 
   * @param activityID
   *   The ID of the "Submitted" new task activity.
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertSubmitEvent
  (
   int taskID, 
   int identID, 
   long stamp, 
   int noteID,
   Integer builderID,
   int activityID
  ) 
    throws SQLException
  {
    pInsertSubmitEventSt.setInt(1, taskID);
    pInsertSubmitEventSt.setInt(2, identID);
    pInsertSubmitEventSt.setTimestamp(3, new Timestamp(stamp));
    pInsertSubmitEventSt.setInt(4, noteID);
    pInsertSubmitEventSt.setInt(5, activityID);
    if(builderID != null) 
      pInsertSubmitEventSt.setInt(6, builderID); 
    else 
      pInsertSubmitEventSt.setNull(6, Types.INTEGER);
    pInsertSubmitEventSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertSubmitEventSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }

  /**
   * Insert a new event for the initian approval of a task.
   * 
   * @param taskID
   *   The ID of the task. 
   * 
   * @param identID
   *   The ID of the user creating the task. 
   * 
   * @param stamp
   *   The timestamp of when the task was created. 
   * 
   * @param noteID
   *   The ID of the note associated with the event.
   *
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertApproveEvent
  (
   int taskID, 
   int identID, 
   long stamp, 
   int noteID
  ) 
    throws SQLException
  {
    pInsertApproveEventSt.setInt(1, taskID);
    pInsertApproveEventSt.setInt(2, identID);
    pInsertApproveEventSt.setTimestamp(3, new Timestamp(stamp));
    pInsertApproveEventSt.setInt(4, noteID);
    pInsertApproveEventSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertApproveEventSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }


  /*----------------------------------------------------------------------------------------*/
    
  /**
   * Get the ID for a specific builder path creating a new entry if needed.
   * 
   * @param path
   *   The new builder path. 
   * 
   * @return 
   *   The ID of the entry. 
   */ 
  private synchronized Integer
  lookupBuilderPath
  (
   Path path
  ) 
    throws SQLException
  {
    Integer id = getBuilderPath(path); 
    if(id == null) 
      id = insertBuilderPath(path); 

    return id;
  }

  /**
   * Get the ID for a specific builder path.
   * 
   * @param path
   *   The builder path. 
   * 
   * @return 
   *   The ID of the entry or <CODE>null</CODE> if it does not yet exist. 
   */ 
  private synchronized Integer
  getBuilderPath
  (
   Path path   
  ) 
    throws SQLException
  {
    Integer id = null;

    pGetBuilderPathSt.setString(1, path.toString());
    ResultSet rs = pGetBuilderPathSt.executeQuery();
    while(rs.next()) 
      id = rs.getInt(1);
    rs.close();

    return id;
  }
  
  /**
   * Insert a new builder path.
   * 
   * @param path
   *   The new builder path. 
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertBuilderPath
  (
   Path path
  ) 
    throws SQLException
  {
    pInsertBuilderPathSt.setString(1, path.toString());
    pInsertBuilderPathSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertBuilderPathSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }


  /*----------------------------------------------------------------------------------------*/
    
  /**
   * Get the ID for a specific node name creating a new entry if needed.
   * 
   * @param name
   *   The new node name. 
   * 
   * @return 
   *   The ID of the entry. 
   */ 
  private synchronized Integer
  lookupNodeName
  (
   String name
  ) 
    throws SQLException
  {
    Integer id = getNodeName(name); 
    if(id == null) 
      id = insertNodeName(name); 

    return id;
  }

  /**
   * Get the ID for a specific node name.
   * 
   * @param name
   *   The node name. 
   * 
   * @return 
   *   The ID of the entry or <CODE>null</CODE> if it does not yet exist. 
   */ 
  private synchronized Integer
  getNodeName
  (
   String name   
  ) 
    throws SQLException
  {
    Integer id = null;

    pGetNodeNameSt.setString(1, name);
    ResultSet rs = pGetNodeNameSt.executeQuery();
    while(rs.next()) 
      id = rs.getInt(1);
    rs.close();

    return id;
  }
  
  /**
   * Insert a new node name.
   * 
   * @param name
   *   The new node name. 
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertNodeName
  (
   String name
  ) 
    throws SQLException
  {
    pInsertNodeNameSt.setString(1, name);
    pInsertNodeNameSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertNodeNameSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Insert a new set of task event related information for a node. 
   * 
   * @param nodeID
   *   The ID of the node. 
   * 
   * @param vid
   *   The revision number of the node. 
   * 
   * @param eventID
   *   The ID of the event. 
   * 
   * @param isEdit
   *   Whether this is an Edit node.
   * 
   * @param isSubmit
   *   Whether this is an Submit node.
   * 
   * @param isFocus
   *   Whether this is an Focus node.
   * 
   * @param isThumb
   *   Whether this is an Thumbnail node.
   * 
   * @param isApprove
   *   Whether this is an Approve node.
   * 
   * @return 
   *   The ID of the newly inserted entry.
   */ 
  private synchronized Integer
  insertNodeInfo
  (
   int nodeID, 
   VersionID vid, 
   int eventID, 
   boolean isEdit, 
   boolean isSubmit, 
   boolean isFocus, 
   boolean isThumb, 
   boolean isApprove
  ) 
    throws SQLException
  {
    pInsertNodeInfoSt.setInt(1, nodeID);
    pInsertNodeInfoSt.setString(2, vid.toString());
    pInsertNodeInfoSt.setInt(3, eventID);
    pInsertNodeInfoSt.setBoolean(4, isEdit); 
    pInsertNodeInfoSt.setBoolean(5, isSubmit); 
    pInsertNodeInfoSt.setBoolean(6, isFocus); 
    pInsertNodeInfoSt.setBoolean(7, isThumb); 
    pInsertNodeInfoSt.setBoolean(8, isApprove); 
    pInsertNodeInfoSt.executeUpdate();

    Integer id = null;
    {
      ResultSet rs = pInsertNodeInfoSt.getGeneratedKeys();
      if(rs.next()) 
        id = rs.getInt(1);
      rs.close();
    }
    
    return id;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Insert information about a particular thumbnail version.
   * 
   * @param thumbInfoID
   *   The ID of the node info record for the thumbnail node.
   * 
   * @param focusInfoID
   *   The ID of the node info record for the corresponding focus node.
   * 
   * @param path
   *   The full repository path to the thumbnail image file. 
   */ 
  private synchronized void
  insertThumbInfo
  (
   int thumbInfoID, 
   int focusInfoID, 
   Path path
  ) 
    throws SQLException
  {
    pInsertThumbInfoSt.setInt(1, thumbInfoID);
    pInsertThumbInfoSt.setInt(2, focusInfoID);
    pInsertThumbInfoSt.setString(3, path.toString()); 
    pInsertThumbInfoSt.executeUpdate();
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   T R A N S A C T I O N   H E L P E R S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Start a new transaction.
   */ 
  private synchronized void 
  txnStart() 
    throws PipelineException     
  {
    try {
      pConnect.setAutoCommit(false);
    }
    catch(SQLException ex) {
      sqlError("Unable to begin a new SQL transaction", ex);
    }
  }

  /**
   * Commit the transaction changes.
   */ 
  private synchronized void 
  txnCommit() 
    throws PipelineException     
  {
    try {
      pConnect.commit();
    }
    catch(SQLException ex) {
      sqlError("Unable to commit the changes for a SQL transaction", ex);
    }
  }

  /**
   * Abort the current transaction rolling back all changes and log the 
   */ 
  private synchronized void 
  txnRollback
  (
   String msg,
   SQLException ex
  ) 
    throws PipelineException 
  {  
    String postMsg = null;
    try {
      pConnect.rollback();
    }
    catch(SQLException ex2) {
      StringBuilder buf = new StringBuilder(); 
      buf.append("Unable to rollback SQL transaction after the error:"); 
      
      SQLException e = ex2; 
      while(e != null) {
        buf.append("\n    " + e.getMessage() + "\n");
        e = ex.getNextException();
      }
    }

    sqlError(msg, postMsg, ex);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   E R R O R   H A N D L I N G                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Generate a PipelineException with a full log of the SQL errors that occured
   * and invalidate the connection so that it will be reestablished at the next 
   * high level SQL operation.
   * 
   * @param preMsg
   *   Message to print before the SQL errors.
   * 
   * @param ex
   *   The SQL error.
   */ 
  private synchronized void 
  sqlError
  (
   String preMsg, 
   SQLException ex
  ) 
    throws PipelineException 
  {
    sqlError(preMsg, null, ex); 
  }

  /**
   * Generate a PipelineException with a full log of the SQL errors that occured
   * and invalidate the connection so that it will be reestablished at the next 
   * high level SQL operation.
   * 
   * @param preMsg
   *   Message to print before the SQL errors.
   * 
   * @param postMsg
   *   Message to print after the SQL errors.
   * 
   * @param ex
   *   The SQL error.
   */ 
  private synchronized void 
  sqlError
  (
   String preMsg, 
   String postMsg, 
   SQLException ex 
  ) 
    throws PipelineException 
  {  
    StringBuilder buf = new StringBuilder(); 
    buf.append(preMsg + ":"); 

    {
      SQLException e = ex; 
      while(e != null) {
        buf.append("\n\n" + getFullMessage(e)); 
        e = ex.getNextException();
      }
    }

    if(postMsg != null) 
      buf.append("\n\n" + postMsg); 

    pConnect = null;

    throw new PipelineException(buf.toString());
  }

  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  private String 
  getFullMessage
  (
   Throwable ex
  ) 
  {
    StringBuilder buf = new StringBuilder();
     
    if(ex.getMessage() != null) 
      buf.append(ex.getMessage() + "\n\n"); 	
    else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
      
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = ex.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");
   
    return (buf.toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the host running the SQL server.
   */ 
  private String  pHostname; 

  /**
   * The network port number listened to by the SQL server.
   */ 
  private Integer  pPort; 

  /**
   * The name of the SQL user making the connection.
   */ 
  private String  pUser;  

  /**
   * The password of the SQL user making the connection.
   */ 
  private String  pPassword; 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The database connection.
   */ 
  private Connection pConnect; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Prepared SQL statements.
   */ 
  private PreparedStatement  pGetTaskTitleSt;
  private PreparedStatement  pInsertTaskTitleSt;

  private PreparedStatement  pGetTaskTypeSt;
  private PreparedStatement  pInsertTaskTypeSt;

  private PreparedStatement  pGetTaskActivitySt;
  private PreparedStatement  pInsertTaskActivitySt;

  private PreparedStatement  pGetTaskStatusSt;
  private PreparedStatement  pInsertTaskStatusSt;

  private PreparedStatement  pInsertNullTaskSupervisorSt;

  private PreparedStatement  pGetIdentSt;
  private PreparedStatement  pInsertIdentSt;

  private PreparedStatement  pClearMembershipsSt;
  private PreparedStatement  pInsertMembershipSt;

  private PreparedStatement  pInsertNoteSt;

  private PreparedStatement  pGetTaskSt;
  private PreparedStatement  pInsertTaskSt;  
  private PreparedStatement  pSubmitTaskSt;
  private PreparedStatement  pApproveTaskSt;
  private PreparedStatement  pAssignedToTaskSt;

  private PreparedStatement  pInsertCreateEventSt;
  private PreparedStatement  pInsertSubmitEventSt;
  private PreparedStatement  pInsertApproveEventSt;
  
  private PreparedStatement  pGetNodeNameSt;
  private PreparedStatement  pInsertNodeNameSt;

  private PreparedStatement  pGetBuilderPathSt;
  private PreparedStatement  pInsertBuilderPathSt;

  private PreparedStatement  pInsertNodeInfoSt;

  private PreparedStatement  pInsertThumbInfoSt;
  
}
