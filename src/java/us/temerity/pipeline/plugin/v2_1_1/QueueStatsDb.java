// $Id: QueueStatsDb.java,v 1.3 2007/02/17 11:46:59 jim Exp $

package us.temerity.pipeline.plugin.v2_1_1;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S T A T S    D B                                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * A shared helper class used by all QueueStats extension plugins to serialize communication
 * with the SQL server. <P> 
 * 
 * Note that this class has only minimal SQL error handling.  Before QueueStats is ready for 
 * produciton use, it should be improved to use SQL transactions and have the ability to 
 * recover gracefully when the connection is lost to the SQL server.  This version of the 
 * extension intended really just a demonstration that basic SQL interaction is possible.
 * We plan to provide a more robust and sophisticated version of this extension in the 
 * future which will be suitable for serious production use.  See the 
 * <A href="http://temerity.us/community/forums/viewtopic.php?t=568"><B>Long Term Queue
 * Statistics</B></A> feature request for 
 * details. 
 */
public class 
QueueStatsDb
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  QueueStatsDb()
  {
    /* init JDBC driver */ 
    try {
      Class.forName("com.mysql.jdbc.Driver"); 
    }
    catch (Exception ex) {
      Path path = PackageInfo.getJavaRuntime(OsType.Unix);
      Path ext = new Path(path.getParentPath().getParentPath(), "lib/ext");

      LogMgr.getInstance().log
	(LogMgr.Kind.Ext, LogMgr.Level.Severe, 
	 "Unable to inialize the MySQL JDBC driver for the QueueStats extension!\n" + 
	 "\n" + 
	 "The driver (Connector/J 5.0) needs to be installed before this plugin can " + 
	 "be used. The driver can be download from here:\n" + 
	 "\n" + 
	 "  http://dev.mysql.com/downloads/connector/j/5.0.html\n" + 
	 "\n" + 
	 "The JAR file for the driver will need to be installed in " + 
	 "(" + ext.toOsString() + ") in order for Pipeline to find it properly.");
    }
  }



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
    throws SQLException
  {
    pHostname = hostname;
    pPort     = port; 
    pUser     = user;
    pPassword = password;

    reconnect();
  }

  /**
   * Reestablish a connection with the given SQL server.
   */
  public synchronized void
  reconnect() 
    throws SQLException
  {   
    if(pConnect != null) 
      disconnect();

    /* connect to the SQL server */ 
    String url = "jdbc:mysql://" + pHostname + ":" + pPort + "/plstats";
    pConnect = DriverManager.getConnection(url, pUser, pPassword); 
    pConnect.setAutoCommit(false);
   
    /* Precompile the SQL statements. */ 
    prepareStatements();    
  }

  /**
   * Close down the connection with the SQL server.
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
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Insert the given jobs into the SQL database.
   * 
   * @param jobs
   *   The completed jobs indexed by job ID.
   * 
   * @param infos
   *   Information about when and where the job was executed indexed by job ID.
   */  
  public synchronized void
  insertJobs
  (
   TreeMap<Long,QueueJob> jobs,
   TreeMap<Long,QueueJobInfo> infos
  ) 
    throws SQLException
  {
    /* lookup the existing component IDs */ 
    TreeMap<String,Integer> authors = getAuthors();
    TreeMap<String,Integer> views = getViews();
    TripleMap<String,String,VersionID,Integer> actions = getActions();
    TreeMap<String,Integer> hosts = getHosts();
    TreeMap<String,Integer> nodes = new TreeMap<String,Integer>();

    /* find the unregistered components and create new entry for them */ 
    {
      TreeSet<String> missingAuthors = new TreeSet<String>();
      TreeSet<String> missingViews = new TreeSet<String>();    
      DoubleMap<String,String,TreeSet<VersionID>> missingActions = 
	new DoubleMap<String,String,TreeSet<VersionID>>();
      TreeSet<String> missingHosts = new TreeSet<String>();  
      TreeSet<String> missingNodes = new TreeSet<String>();    
      
      for(Long jobID : jobs.keySet()) {	
	QueueJob job = jobs.get(jobID);
	QueueJobInfo info = infos.get(jobID);
	
	NodeID nodeID = job.getNodeID();
	
	String author = nodeID.getAuthor();
	if(!authors.containsKey(author)) 
	  missingAuthors.add(author); 
	
	String view = nodeID.getView();
	if(!views.containsKey(view)) 
	  missingViews.add(view); 
	
	{
	  BaseAction act = job.getAction();
	  
	  String aname = act.getName();
	  String vendor = act.getVendor();
	  VersionID vid = act.getVersionID();
	  
	  if(actions.get(vendor, aname, vid) == null) {
	    TreeSet<VersionID> nvids = missingActions.get(vendor, aname); 
	    if(nvids == null) {
	      nvids = new TreeSet<VersionID>();
	      missingActions.put(vendor, aname, nvids);
	    }
	    nvids.add(vid);
	  }
	}
	
	String host = info.getHostname(); 
	if((host != null) && !hosts.containsKey(host)) 
	  missingHosts.add(host); 
	
	String nname = nodeID.getName();
	if(!nodes.containsKey(nname) && !missingNodes.contains(nname)) {
	  Integer nid = lookupNode(nname);
	  if(nid != null) 
	    nodes.put(nname, nid);
	  else 
	    missingNodes.add(nname);
	}
      }
      
      /* register the missing components */ 
      {
	for(String name : missingAuthors) 
	  authors.put(name, insertAuthor(name));
	
	for(String name : missingViews) 
	  views.put(name, insertView(name));
	
	for(String vendor : missingActions.keySet()) 
	  for(String name : missingActions.keySet(vendor)) 
	  for(VersionID vid : missingActions.get(vendor, name)) 
	    actions.put(vendor, name, vid, insertAction(vendor, name, vid));
	
	for(String name : missingHosts) 
	  hosts.put(name, insertHost(name));
	
	for(String name : missingNodes) 
	  nodes.put(name, insertNode(name));
      }
    }
    
    /* insert the jobs */ 
    for(Long jobID : jobs.keySet()) {
      QueueJob job = jobs.get(jobID);
      QueueJobInfo info = infos.get(jobID);

      NodeID nid = job.getNodeID();

      Integer nodeID = nodes.get(nid.getName());
      Integer authorID = authors.get(nid.getAuthor());
      Integer viewID = views.get(nid.getView());

      BaseAction act = job.getAction();
      Integer actionID = actions.get(act.getVendor(), act.getName(), act.getVersionID());

      Timestamp submitted = new Timestamp(info.getSubmittedStamp().getTime());
      Timestamp completed = new Timestamp(info.getCompletedStamp().getTime());

      try {
	switch(info.getState()) {
	case Aborted:
	  pInsertAbortedJobSt.setLong(1, jobID);
	  pInsertAbortedJobSt.setLong(2, nodeID);
	  pInsertAbortedJobSt.setInt(3, authorID);
	  pInsertAbortedJobSt.setInt(4, viewID); 
	  pInsertAbortedJobSt.setInt(5, actionID); 
	  pInsertAbortedJobSt.setTimestamp(6, submitted);
	  pInsertAbortedJobSt.setTimestamp(7, completed);
	  pInsertAbortedJobSt.executeUpdate();
	  break;
	  
	case Finished:
	case Failed:
	  {
	    QueueJobResults results = info.getResults();
	    Timestamp started = new Timestamp(info.getStartedStamp().getTime());
	    
	    pInsertCompletedJobSt.setLong(1, jobID);
	    pInsertCompletedJobSt.setLong(2, nodeID);
	    pInsertCompletedJobSt.setInt(3, authorID);
	    pInsertCompletedJobSt.setInt(4, viewID); 
	    pInsertCompletedJobSt.setInt(5, actionID); 
	    pInsertCompletedJobSt.setInt(6, hosts.get(info.getHostname())); 
	    pInsertCompletedJobSt.setString(7, info.getState().toString());
	    pInsertCompletedJobSt.setInt(8, results.getExitCode()); 
	    pInsertCompletedJobSt.setTimestamp(9, submitted);
	    pInsertCompletedJobSt.setTimestamp(10, started);
	    pInsertCompletedJobSt.setTimestamp(11, completed);
	    pInsertCompletedJobSt.setDouble(12, results.getUserTime());
	    pInsertCompletedJobSt.setDouble(13, results.getSystemTime());
	    pInsertCompletedJobSt.setLong(14, results.getPageFaults()); 
	    pInsertCompletedJobSt.setLong(15, results.getVirtualSize());
	    pInsertCompletedJobSt.setLong(16, results.getResidentSize());
	    pInsertCompletedJobSt.setLong(17, results.getSwappedSize());
	    pInsertCompletedJobSt.executeUpdate();
	  }
	}
      }
      catch(SQLException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ext, LogMgr.Level.Warning, 
	   "Insert of Job (" + jobID + ") failed:\n  " + 
	   ex.getMessage());
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Insert the given resource samples into the SQL database.
   * 
   * @param samples
   *   The dynamic resource samples indexed by fully resolved hostname.
   */  
  public synchronized void
  insertResourceSamples
  (
   TreeMap<String,ResourceSampleCache> samples   
  ) 
    throws SQLException
  {
    /* lookup the existing component IDs */ 
    TreeMap<String,Integer> hosts = getHosts();
    
    /* find the unregistered components and create new entry for them */ 
    {
      TreeSet<String> missingHosts = new TreeSet<String>();  

      for(String host : samples.keySet()) 
	if(!hosts.containsKey(host)) 
	  missingHosts.add(host); 
      
      for(String name : missingHosts) 
	hosts.put(name, insertHost(name));
    }

    /* insert the samples */ 
    for(String host : samples.keySet()) {
      ResourceSampleCache cache = samples.get(host);
      Integer hostID = hosts.get(host); 

      int size = cache.getNumSamples();
      int wk;
      for(wk=0; wk<size; wk++) {
	pInsertSampleSt.setInt(1, hostID);
	pInsertSampleSt.setTimestamp(2, new Timestamp(cache.getTimeStamp(wk).getTime()));
	pInsertSampleSt.setInt(3, cache.getNumJobs(wk));
	pInsertSampleSt.setFloat(4, cache.getLoad(wk));
	pInsertSampleSt.setLong(5, cache.getMemory(wk));
	pInsertSampleSt.setLong(6, cache.getDisk(wk));
	pInsertSampleSt.executeUpdate();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Modify the host information.
   * 
   * @param hostInfo
   *   The information about the modified hosts indexed by fully resolved hostname.
   */  
  public synchronized void
  modifyHosts
  (
   TreeMap<String,QueueHostInfo> hostInfo
  ) 
    throws SQLException
  {
    /* lookup the existing component IDs */ 
    TreeMap<String,Integer> hosts = getHosts();
    
    /* find the unregistered components and create new entry for them */ 
    {
      TreeSet<String> missingHosts = new TreeSet<String>();  

      for(String host : hostInfo.keySet()) 
	if(!hosts.containsKey(host)) 
	  missingHosts.add(host); 
      
      for(String name : missingHosts) 
	hosts.put(name, insertHost(name));
    }
    
    /* update the host information */ 
    for(String host : hostInfo.keySet()) {
      QueueHostInfo info = hostInfo.get(host);
      switch(info.getStatus()) {
      case Enabled:
	updateHost(hosts.get(host), info.getOsType(), info.getNumProcessors(), 
		   info.getTotalMemory(), info.getTotalDisk());
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*  H E L P E R S                                                                         */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of the registered authors and their IDs.
   */ 
  private synchronized TreeMap<String,Integer> 
  getAuthors() 
    throws SQLException
  {
    
    TreeMap<String,Integer> table = new TreeMap<String,Integer>();

    ResultSet rs = pGetAuthorsSt.executeQuery();
    while(rs.next()) 
      table.put(rs.getString(1), rs.getInt(2));
    rs.close();

    return table;
  }
  
  /**
   * Insert a new author into the registered authors table. 
   * 
   * @param author
   *   The name the the author to insert.
   * 
   * @return 
   *   The ID of the newly inserted author.
   */ 
  private synchronized Integer
  insertAuthor
  (
   String author
  ) 
    throws SQLException
  {
    pInsertAuthorSt.setString(1, author);
    pInsertAuthorSt.executeUpdate();

    Integer authorID = null;
    {
      ResultSet rs = pInsertAuthorSt.getGeneratedKeys();
      if(rs.next()) 
	authorID = rs.getInt(1);
      rs.close();
    }
    
    return authorID;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the registered views and their IDs.
   */ 
  private synchronized TreeMap<String,Integer> 
  getViews() 
    throws SQLException
  {
    TreeMap<String,Integer> table = new TreeMap<String,Integer>();

    ResultSet rs = pGetViewsSt.executeQuery();
    while(rs.next()) 
      table.put(rs.getString(1), rs.getInt(2));
    rs.close();

    return table;
  }
  
  /**
   * Insert a new view into the registered views table. 
   * 
   * @param view
   *   The name the the view to insert.
   * 
   * @return 
   *   The ID of the newly inserted view.
   */ 
  private synchronized Integer
  insertView
  (
   String view
  ) 
    throws SQLException
  {
    pInsertViewSt.setString(1, view);
    pInsertViewSt.executeUpdate();

    Integer viewID = null;
    {
      ResultSet rs = pInsertViewSt.getGeneratedKeys();
      if(rs.next()) 
	viewID = rs.getInt(1);
      rs.close();
    }
    
    return viewID;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the registered actions and their IDs.
   */ 
  private synchronized TripleMap<String,String,VersionID,Integer>
  getActions()
    throws SQLException
  {
    TripleMap<String,String,VersionID,Integer> table = 
      new TripleMap<String,String,VersionID,Integer>();
    
    ResultSet rs = pGetActionsSt.executeQuery();
    while(rs.next()) 
      table.put(rs.getString(1), rs.getString(3), new VersionID(rs.getString(2)), 
		    rs.getInt(4));
    rs.close();
    
    return table;
  }

  /**
   * Insert a new action into the registered actions table. 
   * 
   * @param vendor
   *   The name the action's vendor. 
   * 
   * @param name
   *   The name the action. 
   * 
   * @param vid
   *   The revision number of the action. 
   * 
   * @return 
   *   The ID of the newly inserted action.
   */ 
  private synchronized Integer
  insertAction
  (
   String vendor,
   String name, 
   VersionID vid 
  ) 
    throws SQLException
  {
    pInsertActionSt.setString(1, name);
    pInsertActionSt.setString(2, vid.toString());
    pInsertActionSt.setString(3, vendor);
    pInsertActionSt.executeUpdate();

    Integer actionID = null;
    {
      ResultSet rs = pInsertActionSt.getGeneratedKeys();
      if(rs.next()) 
	actionID = rs.getInt(1);
      rs.close();
    }
    
    return actionID;
  }
  
 
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the registered hosts and their IDs.
   */ 
  private synchronized TreeMap<String,Integer> 
  getHosts() 
    throws SQLException
  {
    TreeMap<String,Integer> table = new TreeMap<String,Integer>();

    ResultSet rs = pGetHostsSt.executeQuery();
    while(rs.next()) 
      table.put(rs.getString(1), rs.getInt(2));
    rs.close();

    return table;
  }
  
  /**
   * Insert a new host into the registered hosts table. 
   * 
   * @param host
   *   The name the the host to insert.
   * 
   * @return 
   *   The ID of the newly inserted host.
   */ 
  private synchronized Integer
  insertHost
  (
   String host
  ) 
    throws SQLException
  {
    pInsertHostSt.setString(1, host);
    pInsertHostSt.executeUpdate();

    Integer hostID = null;
    {
      ResultSet rs = pInsertHostSt.getGeneratedKeys();
      if(rs.next()) 
	hostID = rs.getInt(1);
      rs.close();
    }
    
    return hostID;
  }

  /**
   * Insert a new host into the registered hosts table. 
   * 
   * @param hostID
   *   The ID the the host to update.
   * 
   * @param os
   *   The operating system type.
   * 
   * @param numProcs
   *   The number of processors on the host.
   * 
   * @param totalMemory
   *   The total amount of memory (in bytes) on the host.
   * 
   * @param totalDisk
   *   The total amount of temporary disk space (in bytes) on the host.
   */ 
  private synchronized void
  updateHost
  (
   Integer hostID, 
   OsType os, 
   Integer numProcs, 
   Long totalMemory, 
   Long totalDisk
  ) 
    throws SQLException
  {
    if((os == null) || (numProcs == null) || (totalMemory == null) || (totalDisk == null))
      return;

    pUpdateHostSt.setString(1, os.toString());
    pUpdateHostSt.setInt(2, numProcs); 
    pUpdateHostSt.setLong(3, totalMemory);
    pUpdateHostSt.setLong(4, totalDisk);
    pUpdateHostSt.setInt(5, hostID);
    pUpdateHostSt.executeUpdate();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup the ID for the given node name. 
   */ 
  private synchronized Integer
  lookupNode
  (
   String name
  ) 
    throws SQLException
  {
    Integer nodeID = null;

    pLookupNodeSt.setString(1, name);
    ResultSet rs = pLookupNodeSt.executeQuery();
    if(rs.next()) 
      nodeID = rs.getInt(1);
    rs.close();

    return nodeID;
  }
  
  /**
   * Insert a new node into the registered nodes table. 
   * 
   * @param node
   *   The name the the node to insert.
   * 
   * @return 
   *   The ID of the newly inserted node.
   */ 
  private synchronized Integer
  insertNode
  (
   String node
  ) 
    throws SQLException
  {
    pInsertNodeSt.setString(1, node);
    pInsertNodeSt.executeUpdate();

    Integer nodeID = null;
    {
      ResultSet rs = pInsertNodeSt.getGeneratedKeys();
      if(rs.next()) 
	nodeID = rs.getInt(1);
      rs.close();
    }
    
    return nodeID;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*  H E L P E R S                                                                         */
  /*----------------------------------------------------------------------------------------*/
 
  /* Precompile the SQL statements. */ 
  private synchronized void
  prepareStatements()
    throws SQLException
  {
    {
      String sql = ("SELECT author_name, author_id FROM authors"); 
      pGetAuthorsSt = pConnect.prepareStatement(sql);
    }
    
    {
      String sql = ("INSERT INTO authors (author_name) VALUES (?)");
      pInsertAuthorSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
    }


    {
      String sql = ("SELECT view_name, view_id FROM views"); 
      pGetViewsSt = pConnect.prepareStatement(sql);
    }

    {
      String sql = ("INSERT INTO views (view_name) VALUES (?)");
      pInsertViewSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }


    {
      String sql = 
	("SELECT action_name, action_version, action_vendor, action_id FROM actions"); 
      pGetActionsSt = pConnect.prepareStatement(sql);
    }

    {
      String sql = 
	("INSERT INTO actions (action_name, action_version, action_vendor) " + 
	 "VALUES (?, ?, ?)");
      pInsertActionSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }


    {
      String sql = ("SELECT host_name, host_id FROM hosts"); 
      pGetHostsSt = pConnect.prepareStatement(sql);
    }

    {
      String sql = ("INSERT INTO hosts (host_name) VALUES (?)");
      pInsertHostSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    {
      String sql = 
	("UPDATE hosts " + 
	 "SET host_os = ?, num_procs = ?, total_memory = ?, total_disk = ? " + 
	 "WHERE host_id = ?");
      pUpdateHostSt = pConnect.prepareStatement(sql);
    }


    {
      String sql = ("SELECT node_id FROM nodes " + 
		    "WHERE node_name = ?"); 
      pLookupNodeSt = pConnect.prepareStatement(sql);
    }
    
    {
      String sql = ("INSERT INTO nodes (node_name) VALUES (?)");
      pInsertNodeSt = pConnect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }


    {
      String sql = 
	("INSERT INTO jobs (job_id, node_id, author_id, view_id, action_id, " + 
	                   "job_state, submitted, completed) " + 
	 "VALUES (?, ?, ?, ?, ?, 'Aborted', ?, ?)");
      pInsertAbortedJobSt = pConnect.prepareStatement(sql);
    }

    {
      String sql = 
	("INSERT INTO jobs (job_id, node_id, author_id, view_id, action_id, " + 
	                   "host_id, job_state, exit_code, " +
	                   "submitted, started, completed, " + 
	                   "user_time, system_time, page_faults, " + 
	                   "virtual_size, resident_size, swapped_size) " + 
	 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      pInsertCompletedJobSt = pConnect.prepareStatement(sql);
    }

    
    {
      String sql = 
	("INSERT INTO samples (host_id, stamp, num_jobs, system_load, " + 
	                      "free_memory, free_disk) " + 
	 "VALUES (?, ?, ?, ?, ?, ?)");
      pInsertSampleSt = pConnect.prepareStatement(sql);
    }
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


  /**
   * The database connection.
   */ 
  private Connection pConnect; 


  /**
   * Prepared SQL statements.
   */ 
  private PreparedStatement  pGetAuthorsSt;
  private PreparedStatement  pInsertAuthorSt;

  private PreparedStatement  pGetViewsSt;
  private PreparedStatement  pInsertViewSt;

  private PreparedStatement  pGetActionsSt;
  private PreparedStatement  pInsertActionSt;

  private PreparedStatement  pGetHostsSt;
  private PreparedStatement  pInsertHostSt;
  private PreparedStatement  pUpdateHostSt;

  private PreparedStatement  pLookupNodeSt; 
  private PreparedStatement  pInsertNodeSt;

  private PreparedStatement  pLookupJobSt; 
  private PreparedStatement  pInsertAbortedJobSt; 
  private PreparedStatement  pInsertCompletedJobSt; 

  private PreparedStatement  pInsertSampleSt;
}
