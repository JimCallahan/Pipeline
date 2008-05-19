package us.temerity.pipeline.plugin.ShotgunConnectionExt.v2_4_1;

import java.net.URL;
import java.util.*;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S H O T G U N   C O N N E C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Helper class for creating a connection and making calls to the Shotgun API.
 * <p>
 * This class requires certain things to be true on the Shotgun side.  If these things are
 * not true, then the methods in here may have unexpected and incorrect nodes, if not just
 * failing outright.
 * <ul>
 *   <li> Names are assumed to be unique and unchanging from the Pipeline side.  This is do
 *   to the fact that Pipeline cannot change the names of its files once they are checked-in.  
 *   If a Shotgun task had its name changed, it would not match the Pipeline node names any
 *   more which could be highly confusing for artists.  As well, the Task Policy v2.4.1 
 *   extensions that can be used with this setup are designed around name-based tasks, not
 *   unique IDs.  It would not be impossible (or even difficult) for a studio that desired
 *   renaming and non-unique Task names to write their own annotations, extensions, and 
 *   Shotgun communicator that supported IDs instead of names, but Temerity is not going to
 *   support this.
 *   <li> This class assumes a Scene --> Shot Hierarchy.  It should be possible to add 
 *   support for Sequence --> Shot Hierarchy (since Shotgun considers Scenes and Sequences
 *   as different Entities), without changing the code.  That will happen shortly.  However, 
 *   if a different hierarchy (like Shots without a parent or Scenes --> Sequence --> Shot)
 *   is desired, a custom version of this class will have to be created.
 *   <li> There are several custom fields that this class expects to exist before it is used.
 *   <ul>
 *     <li>Task Entity
 *     <ul>
 *       <li>TM Submit Node - A Text Field
 *       <li>TM Approve Node - A Text Field
 *       <li>TM Edit Nodes - A Text Field (that will hold multiple node names)
 *       <li>TM Approve Builder - A Text Field
 *     </ul>
 *     <li>Version Entity
 *     <ul>
 *       <li>TM Task - A Link to a Task.
 *       <li>TM Focus Nodes - A Text Field (that will hold multiple node names and versions)
 *       <li>TM Edit Nodes - A Text Field (that will hold multiple node names and versions)
 *       <li>TM VersionID = A Text Field that holds the VersionID
 *       <li>TM Proxy File Path - A Text Field with the full linux path to the proxy files
 *   </ul>
 * </ul><P> 
 */
public 
class ShotgunConnection
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * No-arg Constructor.
   * <p>
   * ExceptionOnDup is set to false
   */
  public 
  ShotgunConnection()
  {
    this(false);
  }
  
  /**
   * Constructor.
   * 
   * @param exceptionOnDup
   *   Should an exception be thrown when an attempt is made to create an Entity which already
   *   exists.  If this is set to <code>true</code>, then an attempt to create a duplicate
   *   Entity will cause a {@link PipelineException} to be thrown.  If this is set to 
   *   <code>false</code> then no exception will be thrown and the id of the existing Entity
   *   will simple be returned. 
   */
  public 
  ShotgunConnection
  (
    boolean exceptionOnDup    
  )
  {
    pClient = null;
    pToken = null;
    pExceptionOnDup = exceptionOnDup;
  }
  
  public static void
  main(String args[]) 
    throws PipelineException
  {
    ShotgunConnection connection = new ShotgunConnection(true);
    connection.connectToServer("http://shotgun-dev/api2", "javatest", "5f158debba5a61311b30cdd8f0ff4c04104c806e");
    //System.out.println(connection.getSceneList("mikesProject"));
    //System.out.println(connection.createScene("Temerity", "sc01"));
    //System.out.println(connection.createShot("Temerity", "sc01", "001"));
    //System.out.println(connection.createTaskOnShot("Temerity", "sc01", "001", "Animation"));
    //System.out.println(connection.createTaskOnShot("Temerity", "sc01", "001", "Lighting"));
    //System.out.println(connection.getShotList("Temerity", "sc01"));
    //System.out.println(connection.getShotID("Temerity", "sc01", "001"));
    //System.out.println(connection.getEntity(ShotgunEntity.Shot, null, null));
    //System.out.println(connection.createAsset("shotgun", "Harry", "Character"));
    //System.out.println(connection.createAsset("Temerity", "Kevin", "Character"));
    //System.out.println(connection.getAssetTaskID("Temerity", "Bob", "Modeling"));
    Integer taskID = connection.getAssetTaskID("Temerity", "Bob", "Modeling");
//    ShotgunEntityBundle bundle = connection.getArtistEntity("jpitchford");
    //System.out.println(bundle);
    //System.out.println(connection.getTaskEntity(taskID));
//    TreeMap<String, VersionID> focuses = new TreeMap<String, VersionID>();
//    focuses.put("/tests/love/war", new VersionID("2.0.0"));
//    focuses.put("/tests/love/hate", new VersionID("2.4.5"));
//     connection.createTaskVersion("Temerity", taskID, "jpitchford", new VersionID("1.1.1"), "I love sheep even more", focuses, focuses);
//    ArrayList<String> list = new ArrayList<String>();
//    list.add("/test/node/edit");
//    list.add("/test/node/edit2");
//    connection.setSubmitTaskNodesOnTask(taskID, "/test/node/submit", list);
    //connection.setApproveBuilderOnTask(taskID, new BuilderID("ApproveCollection", new VersionID("2.4.1"), "Temerity", "ApproveTask" ));
    //System.out.println(connection.createTaskOnAsset("Temerity", "Bob", "Rigging"));
    //System.out.println(connection.createTaskOnAsset("Temerity", "Bob", "Shading"));
    //System.out.println(connection.getSceneList("Temerity"));
    connection.disconnectFromServer();
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N N E C T I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Creates a new connection to the Shotgun server.
   * <p>
   * This method will fail if a connection has already been made.
   * 
   * @param urlName
   *   The full path to the Shotgun API RPC server, including the initial http.  
   * @param userName
   *   The user whose API key is being used to connect.
   * @param apiKey
   *   The API key for the user.
   * @throws PipelineException
   *   If there are any failures during connection or if a connection has already been made.
   */
  public void 
  connectToServer
  (
    String urlName,
    String userName,
    String apiKey
  )
    throws PipelineException
  {
    if (pClient != null)
      throw new PipelineException("Already connected to the server.");
    
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    URL url;

    try {
      url = new URL(urlName);
    }
    catch (Exception ex) {
      throw new PipelineException
        ("The Url (" + urlName +") was not a valid URL.  The following error was thrown " +
         "when attempting to parse the URL.\n" + ex.getMessage());
    }
    
    config.setServerURL(url);
    config.setEnabledForExtensions(true);
    
    pClient = new XmlRpcClient();
    pClient.setConfig(config);
    pClient.setTypeFactory(new TemerityTypeFactory(pClient));
    
    Object tokenParam[] = new Object[1];
    tokenParam[0] = new Object[]{userName, apiKey};
    try {
      pToken = pClient.execute("getSessionToken", tokenParam);
    }
    catch(Exception e) {
      throw new PipelineException
        ("Unable to get a Session Token from Shotgun.  " +
         "The following error was reported.\n " + e.getMessage() );
    } 
    
    pProjectIDCache = new TreeMap<String, Integer>();
  }
  
  /**
   * Disconnects from the Shotgun server.
   * <p>
   * This method should always be called when the connection is done to release load on the
   * Shotgun server.
   */
  public void
  disconnectFromServer()
    throws PipelineException
  {
    Object tokenParam[] = new Object[1];
    tokenParam[0] = pToken;
    try {
      pClient.execute("releaseSessionToken", tokenParam);
    }     
    catch(Exception e) {
      throw new PipelineException
      ("Unable to release the Shotgun API Token.  " +
       "The following error was reported.\n " + e.getMessage() );
    }
    pClient = null;
    pToken = null;
    pProjectIDCache.clear();
  }
  
  /**
   * Checks if there is a valid connection to the server before attempting to access it.
   * @throws PipelineException  If there is no active connection.
   */
  private void
  checkConnection()
    throws PipelineException
  {
    if (pToken == null || pClient == null)
      throw new PipelineException
        ("No connection was made to Shotgun server before an attempt was made to access it.  " +
         "Please make sure the connectToServer() method is called before attempts are made " +
         "to talk to the server.");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I D   R E T R I E V A L                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique ID of a project in Shotgun.
   * 
   * @param project
   *   The name of the project
   * @return
   *   The id of the project or <code>null</code> if the project does not exist in Shotgun.
   */
  public Integer
  getProjectID
  (
    String project
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = null;
    String columns[] = {"id"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    Object filter[] = {"name", "is", project};
    filters.add(filter);
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Project, filters, columns);
    if (results.size() > 1)
      throw new PipelineException
      ("There were multiple projects in Shotgun with the name (" + project + ").  " +
       "This needs to be corrected if Shotgun is going to be used with Pipeline.");
    for (Map<String, Object> result : results) {
      toReturn = (Integer) result.get("id");
    }
    
    return toReturn;
  }
  
  /**
   * Get the unique ID of a scene in a project in Shotgun.
   * 
   * @param project
   *   The name of the project
   * @param scene
   *   The name of the scene
   */
  public Integer
  getSceneID
  (
    String project,
    String scene
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = null;
    String columns[] = {"id"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      String filter[] = {"project_names", "is", project};
      filters.add(filter);
    }
    {
      String filter[] = {"code", "is", scene};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Scene, filters, columns);
    if (results.size() > 1)
      throw new PipelineException
      ("There were multiple scenes in Shotgun in the (" + project + ") project " +
       "with the name (" + scene +").  This needs to be corrected if Shotgun is going " +
       "to be used with Pipeline.");
    for (Map<String, Object> result : results) {
      toReturn = (Integer) result.get("id");
    }
    
    return toReturn;
  }
  
  /**
   * Get the unique ID of a shot in a scene in a project in Shotgun.
   * 
   * @param project
   *   The name of the project
   * @param scene
   *   The name of the scene
   * @param name
   *   The name of the shot
   */
  public Integer
  getShotID
  (
    String project,
    String scene,
    String shot
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = null;
    String columns[] = {"id"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      String filter[] = {"project_names", "is", project};
      filters.add(filter);
    }
    {
      String filter[] = {"sg_scene", "is", scene};
      filters.add(filter);
    }
    {
      String filter[] = {"code", "is", join(scene, shot)};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Shot, filters, columns);
    if (results.size() > 1)
      throw new PipelineException
      ("There were multiple shots in Shotgun in the (" + scene + ") scene in the " +
       "(" + project +") project with the name (" + shot +").  This needs to be corrected " +
       "if Shotgun is going to be used with Pipeline.");
    for (Map<String, Object> result : results) {
      toReturn = (Integer) result.get("id");
    }
    
    return toReturn;
  }
  
  /**
   * Get the unique ID of an asset in a project in Shotgun.
   * 
   * @param project
   *   The name of the project
   * @param scene
   *   The name of the scene
   */
  public Integer
  getAssetID
  (
    String project,
    String asset
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = null;
    String columns[] = {"id"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      String filter[] = {"project_names", "is", project};
      filters.add(filter);
    }
    {
      String filter[] = {"code", "is", asset};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Asset, filters, columns);
    if (results.size() > 1)
      throw new PipelineException
      ("There were multiple assets in Shotgun in the (" + project + ") project " +
       "with the name (" + asset +").  This needs to be corrected if Shotgun is going " +
       "to be used with Pipeline.");
    for (Map<String, Object> result : results) {
      toReturn = (Integer) result.get("id");
    }
    
    return toReturn;
  }
  
  /**
   * Get the unique ID of a task on an asset in a project in Shotgun.
   * 
   * @param project
   *   The name of the project
   * @param asset
   *   The name of the asset
   * @param taskType
   *   The type of the task (not the task name)
   */
  public Integer
  getAssetTaskID
  (
    String project,
    String asset,
    String taskType
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = null;
    String columns[] = {"id"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      String filter[] = {"project_names", "is", project};
      filters.add(filter);
    }
    {
      TreeMap<String, Object> entityMap = new TreeMap<String, Object>();
      entityMap.put("type", ShotgunEntity.Asset.toEntity());
      entityMap.put("id", getAssetID(project, asset));
      Object filter[] = {"entity", "is", entityMap};
      filters.add(filter);
    }
    {
      String filter[] = {"content", "is", join(asset, taskType)};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Task, filters, columns);
    if (results.size() > 1)
      throw new PipelineException
      ("There were multiple tasks in Shotgun on the (" + asset + ") asset in the " +
       "(" + project + ") project with the name (" + join(asset, taskType) +").  This needs " +
       "to be corrected if Shotgun is going to be used with Pipeline.");
    for (Map<String, Object> result : results) {
      toReturn = (Integer) result.get("id");
    }
    
    return toReturn;
  }
  
  /**
   * Get the unique ID of a task on a shot in a project in Shotgun.
   * 
   * @param project
   *   The name of the project
   * @param scene
   *   The name of the scene
   * @param shot
   *   The name of the shot
   * @param taskType
   *   The type of the task (not the task name)
   */
  public Integer
  getShotTaskID
  (
    String project,
    String scene,
    String shot,
    String taskType
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = null;
    String columns[] = {"id"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      String filter[] = {"project_names", "is", project};
      filters.add(filter);
    }
    {
      TreeMap<String, Object> entityMap = new TreeMap<String, Object>();
      entityMap.put("type", ShotgunEntity.Shot.toEntity());
      entityMap.put("id", getShotID(project, scene, shot));
      Object filter[] = {"entity", "is", entityMap};
      filters.add(filter);
    }
    {
      String filter[] = {"content", "is", join(join(scene, shot), taskType)};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Task, filters, columns);
    if (results.size() > 1)
      throw new PipelineException
        ("There were multiple tasks in Shotgun on the shot (" + join(scene, shot) + ") in the " +
         "(" + project + ") project with the name ("+ join(join(scene, shot), taskType) +").  " +
         "This needs to be corrected if Shotgun is going to be used with Pipeline.");
    for (Map<String, Object> result : results) 
      toReturn = (Integer) result.get("id");
   
    return toReturn;
  }
  
  public Integer
  getTaskVersionID
  (
    Integer taskID,
    String versionName
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = null;
    String columns[] = {"id"};
    
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    
    {
      TreeMap<String, Object> entityMap = new TreeMap<String, Object>();
      entityMap.put("type", ShotgunEntity.Task.toEntity());
      entityMap.put("id", taskID);
      Object filter[] = {aVersionTaskField, "is", entityMap};
      filters.add(filter);
    }
    {
      String filter[] = {"code", "is", versionName};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Version, filters, columns);
    
    if (results.size() > 1)
      throw new PipelineException
        ("There were multiple version in Shotgun on the task (" + taskID + ") with the name " +
         "("+ versionName +").  This needs to be corrected if Shotgun is going to be used " +
         "with Pipeline.");
    for (Map<String, Object> result : results) 
      toReturn = (Integer) result.get("id");

    return toReturn;
  }
  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   E N T I T Y   R E T R I E V A L                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the information about a task entity necessary to add it as a filter or a link.
   * 
   * @param taskID
   *   The task ID.
   */
  @SuppressWarnings("unchecked")
  public ShotgunEntityBundle
  getTaskEntity
  (
    Integer taskID
  ) 
    throws PipelineException
  {
    String columns[] = {"id", "entity"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      Object filter[] = {"id", "is", taskID};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Task, filters, columns);
    
    for (Map<String, Object> result : results) { 
      Map<String, Object> entityMap= (Map<String, Object>) result.get("entity");
      String entityType = (String) entityMap.get("type");
      Integer id = (Integer) entityMap.get("id");
      return new ShotgunEntityBundle(ShotgunEntity.valueOf(entityType), id);
    }
    throw new PipelineException("Could not find the specified task entity.");
  }

  /**
   * Get the information about a task entity necessary to add it as a filter or a link.
   * 
   * @param artist
   *   The artist name.
   */
  public ShotgunEntityBundle
  getArtistEntity
  (
    String artist
  ) 
    throws PipelineException
  {
    String columns[] = {"id"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      Object filter[] = {"login", "is", artist};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.User, filters, columns);
    
    if (results.size() > 1)
      throw new PipelineException
        ("There were multiple users in Shotgun with the login (" + artist + ").  " +
         "This needs to be corrected if Shotgun is going to be used with Pipeline.");
    
    ShotgunEntityBundle toReturn = null;
    for (Map<String, Object> result : results) {
      Integer id = (Integer) result.get("id");
      toReturn = new ShotgunEntityBundle(ShotgunEntity.User, id, artist);
    }
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   N A M E   R E T R I E V A L                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the full name of a task from its id.
   */
  public String
  getTaskName
  (
    Integer taskID    
  )
    throws PipelineException
  {
    String toReturn = null;
    
    String columns[] = {"id", "content"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      Object filter[] = {"id", "is", taskID};
      filters.add(filter);
    }
    
    ArrayList<Map<String, Object>> results = 
      getEntity(ShotgunEntity.Task, filters, columns);
    
    for (Map<String, Object> result : results) {
      toReturn = (String) result.get("content");
    }
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   E N T I T Y   C R E A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new project, with the given name.
   */
  public Integer 
  createProject
  (
    String project    
  )
    throws PipelineException
  {
    checkConnection(); 
    
    Integer toReturn = getProjectID(project);
    if (toReturn != null) {
      if (pExceptionOnDup)
        throw new PipelineException
          ("There is already a project in Shotgun with the name (" + project +").  " +
           "Another one cannot be created.");
      else
        return toReturn;
    }
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>();
    someMap.put("name", project);
    
    try {
      toReturn = createEntity(ShotgunEntity.Project, someMap);
    } 
    catch ( Exception ex ) {
      throw new PipelineException
        ("An error occured while attempting to create a Project named " +
         "(" + project + ") in Shotgun.\n" + ex.getMessage());
    }
    
    return toReturn;
  }
  
  /**
   * Create a new scene, with the given name, in a project.
   */
  public Integer 
  createScene
  (
    String project,
    String scene
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = getSceneID(project, scene);
    if (toReturn != null) {
      if (pExceptionOnDup)
        throw new PipelineException
          ("There is already a scene in Shotgun in the (" + project +") project " +
           "with the name (" + scene +").  Another one cannot be created.");
      else
        return toReturn;
    }
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>();
    someMap.put("project_names", project);
    someMap.put("code", scene);
    
    try {
      toReturn = createEntity(ShotgunEntity.Scene, someMap);
    } 
    catch ( Exception ex ) {
      throw new PipelineException
        ("An error occured while attempting to create a scene in the (" + project +") " +
         "project with the name (" + scene +").\n" + ex.getMessage());
    }
    
    return toReturn;
  }
  
  /**
   * Create a new shot, with the given name, in a scene in a project.
   */
  public Integer 
  createShot
  (
    String project,
    String scene,
    String shot
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = getShotID(project, scene, shot);
    if (toReturn != null) {
      if (pExceptionOnDup)
        throw new PipelineException
          ("There is already a shot in Shotgun in the (" + scene + ") scene in the " +
           "(" + project +") project with the name (" + shot +").  " +
           "Another one cannot be created.");
      else
        return toReturn;
    }
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>();
    someMap.put("project_names", project);
    {
      TreeMap<String, Object> entityMap = new TreeMap<String, Object>();
      entityMap.put("type", ShotgunEntity.Scene.toEntity());
      entityMap.put("id", getSceneID(project, scene));
      someMap.put("sg_scene", entityMap);
    }
    someMap.put("code", join(scene, shot));
    
    try {
      toReturn = createEntity(ShotgunEntity.Shot, someMap);
    } 
    catch ( Exception ex ) {
      throw new PipelineException
        ("An error occured while attempting to create a shot in the scene (" + scene +") " +
         "in the (" + project +") project with the name (" + shot +").\n" + ex.getMessage());
    }
    
    return toReturn;
  }
  
  /**
   * Create a new asset, with the given name, in a project.
   * 
   * @param project
   *   The name of the project
   * @param asset
   *   The name of the asset
   * @param assetType
   *   Optional type of the asset.  Will be ignored if set to <code>null</code>.
   */
  public Integer 
  createAsset
  (
    String project,
    String asset,
    String assetType
  )
    throws PipelineException
  {
    checkConnection();
    
    Integer toReturn = getAssetID(project, asset);
    if (toReturn != null) {
      if (pExceptionOnDup)
        throw new PipelineException
          ("There is already a scene in Shotgun in the (" + project +") project " +
           "with the name (" + asset +").  Another one cannot be created.");
      else
        return toReturn;
    }
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>();
    someMap.put("project_names", project);
    someMap.put("code", asset);
    if (assetType != null)
      someMap.put("sg_asset_type", assetType);
    
    try {
      toReturn = createEntity(ShotgunEntity.Asset, someMap);
    } 
    catch ( Exception ex ) {
      throw new PipelineException
        ("An error occured while attempting to create a asset in the (" + project +") " +
         "project with the name (" + asset +").\n" + ex.getMessage());
    }
    
    return toReturn;
  }
  
  /**
   * Create a new task on an asset, with the given name, in a project.
   * 
   * @param project
   *   The name of the project
   * @param
   *   The name of the asset.
   * @param taskType
   *   The type of the task.
   *
   */
  public Integer 
  createTaskOnAsset
  (
    String project,
    String asset,
    String taskType
  )
    throws PipelineException
  {
    checkConnection();
    
    String taskName = join(asset, taskType);
    Integer toReturn = getAssetTaskID(project, asset, taskType);
    if (toReturn != null) {
      if (pExceptionOnDup)
        throw new PipelineException
          ("There is already a task in Shotgun on the asset (" + asset +") in the " +
           "(" + project +") project with the name (" + taskName +").  Another one " +
           "cannot be created.");
      else
        return toReturn;
    }
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>();
    someMap.put("project_names", project);
    {
      TreeMap<String, Object> entityMap = new TreeMap<String, Object>();
      entityMap.put("type", ShotgunEntity.Asset.toEntity());
      entityMap.put("id", getAssetID(project, asset));
      someMap.put("entity", entityMap);
    }
    someMap.put("content", taskName);
    
    try {
      toReturn = createEntity(ShotgunEntity.Task, someMap);
    } 
    catch ( Exception ex ) {
      throw new PipelineException
        ("An error occured while attempting to create a task on the asset (" + asset + ") " +
         "in the (" + project +") project with the name (" + taskName +").\n" + 
         ex.getMessage());
    }
    
    return toReturn;
  }
  
  /**
   * Create a new task on an asset, with the given name, in a project.
   */
  public Integer 
  createTaskOnShot
  (
    String project,
    String scene,
    String shot,
    String taskType
  )
    throws PipelineException
  {
    checkConnection();
    
    String shotName = join(scene, shot);
    String taskName = join(shotName, taskType);
    Integer toReturn = getShotTaskID(project, scene, shot, taskType);
    if (toReturn != null) {
      if (pExceptionOnDup)
        throw new PipelineException
          ("There is already a task in Shotgun on the shot (" + shotName +") in the " +
           "(" + project +") project with the name (" + taskName +").  Another one " +
           "cannot be created.");
      else
        return toReturn;
    }
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>();
    someMap.put("project_names", project);
    {
      TreeMap<String, Object> entityMap = new TreeMap<String, Object>();
      entityMap.put("type", ShotgunEntity.Shot.toEntity());
      entityMap.put("id", getShotID(project, scene, shot));
      someMap.put("entity", entityMap);
    }
    someMap.put("content", taskName);
    
    try {
      toReturn = createEntity(ShotgunEntity.Task, someMap);
    } 
    catch ( Exception ex ) {
      throw new PipelineException
      ("An error occured while attempting to create a task on the shot (" + shotName + ") " +
       "in the (" + project +") project with the name (" + taskName +").\n" + 
       ex.getMessage());
    }
    
    return toReturn;
  }
  
  /**
   * Creates a new version of a task.
   * 
   * @param project
   *   The project the task is in.
   * @param taskID
   *   The ID of the Task
   * @param artist
   *   The artist who is creating the version.
   * @param versionID
   *   The VersionID of the version.
   * @param checkinMessage
   *   The check-in message for the version
   * @param focusNodes
   *   The list of focus nodes and their versions
   * @param editNodes
   *   The list of edit nodes and their versions 
   * @param deliveryNodes
   *   The list of delivery nodes and their versions

   */
  public Integer
  createTaskVersion
  (
    String project,
    Integer taskID,
    String artist,
    VersionID versionID,
    String checkinMessage,
    TreeMap<String, NodeVersion> focusNodes,
    TreeMap<String, NodeVersion> editNodes,
    TreeMap<String, NodeVersion> deliveryNodes
  )
    throws PipelineException
  {
    checkConnection();
    String taskName = getTaskName(taskID);
    String versionName = join(taskName, versionID.toString());

    Integer toReturn = getTaskVersionID(taskID, versionName);

    if (toReturn != null) {
      if (pExceptionOnDup)
        throw new PipelineException
          ("There is already a version in Shotgun on the task (" + taskID +") with the name " +
           "(" + versionName +").  Another one cannot be created.");
      else
        return toReturn;
    }
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>();
    someMap.put("project_names", project);
    {
      TreeMap<String, Object> entityMap = new TreeMap<String, Object>();
      entityMap.put("type", ShotgunEntity.Task.toEntity());
      entityMap.put("id", taskID);
      someMap.put(aVersionTaskField, entityMap);
    }
    {
      ShotgunEntityBundle entityMap = getTaskEntity(taskID);
      someMap.put("entity", entityMap.formatForShotgun());
    }
    {
      ShotgunEntityBundle entityMap = getArtistEntity(artist);
      someMap.put("user", entityMap.formatForShotgun());
    }
    someMap.put("code", versionName);
    someMap.put("description", checkinMessage);
    someMap.put(aVersionIDField, versionID.toString());
    String focus = "";
    for (String node : focusNodes.keySet()) {
      NodeVersion ver = focusNodes.get(node);
      focus += createCheckedInLink(node, ver.getPrimarySequence().toString(), ver.getVersionID()) + " ";
    }
    someMap.put(aFocusNodesField, focus);
    String edit= "";
    for (String node : editNodes.keySet()) {
      NodeVersion ver = editNodes.get(node);
      edit += createCheckedInLink(node, ver.getPrimarySequence().toString(), ver.getVersionID()) + " ";
    }
    someMap.put(aEditNodesField, edit);
    
    if (deliveryNodes != null && !deliveryNodes.isEmpty()) {
      String deliver = "";
      for (String node : deliveryNodes.keySet()) {
        NodeVersion ver = deliveryNodes.get(node);
        Path fileName = new Path(new Path(new Path(
          PackageInfo.sRepoPath, 
          ver.getName()), 
          ver.getVersionID().toString()), 
          ver.getPrimarySequence().getPath(0));
        deliver = fileName.toString() + " ";
      }
      someMap.put(aDeliveryNodesField, deliver);
    }

    try {
      toReturn = createEntity(ShotgunEntity.Version, someMap);
    } 
    catch ( Exception ex ) {
      throw new PipelineException
      ("An error occured while attempting to create a version on the task (" + taskID + ") " +
       "named (" + versionName +").\n" + 
       ex.getMessage());
    }
    return toReturn;
  }
  

  
  @SuppressWarnings("unchecked")
  private Integer
  createEntity
  (
    ShotgunEntity entity,
    TreeMap <String, Object> params
  ) 
    throws XmlRpcException  
  {
    Integer toReturn = null;
    
    Object[] pass = new Object[2];
    pass[0] = pToken;
    pass[1] = params;
    
    Map<String, Object> resultmap = 
      (Map<String, Object>) pClient.execute(entity.toString() + ".create", pass);
    toReturn = (Integer) resultmap.get("id");
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   T A S K   P O L I C Y                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the submit and edit nodes for the specified task.
   * <p>
   * The Task ID is the Shotgun ID and can be retrieved with the {@link
   * #getAssetTaskID(String, String, String) getAssetTaskID} or the {@link
   * #getShotTaskID(String, String, String, String) getShotTaskID} method.
   * 
   * @param taskID
   *   The Shotgun unique ID for the task.
   * @param submitNode
   *   The fully-resolved node name of the task submit node.
   * @param editNodes
   *   The fully-resolved node names of the task edit node.
   */
  public void
  setSubmitTaskNodesOnTask
  (
    Integer taskID,
    String submitNode,
    TreeMap<String, NodeVersion> editNodes
  )
    throws PipelineException
  {
    checkConnection();
    
    TreeMap<String, Object> values = new TreeMap<String, Object>();
    values.put(aSubmitNodeField, submitNode);
    String editString = "";
    for (String node : editNodes.keySet()) {
      NodeVersion ver = editNodes.get(node);
      editString += createWorkingLink(node, ver.getPrimarySequence().toString()) + " ";
    }
    values.put(aEditNodesField, editString);
    try {
      setEntityValues(ShotgunEntity.Task, taskID, values);
    } catch ( Exception ex ) {
      throw new PipelineException
        ("Error when attempting to set the task nodes on the task (" + taskID + ").\n" + 
         ex.getMessage());
    }
  }
  
  /**
   * Sets the approve nodes for the specified task.
   * <p>
   * The Task ID is the Shotgun ID and can be retrieved with the {@link
   * #getAssetTaskID(String, String, String) getAssetTaskID} or the {@link
   * #getShotTaskID(String, String, String, String) getShotTaskID} method.
   * 
   * @param taskID
   *   The Shotgun unique ID for the task.
   * @param approveNode
   *   The fully-resolved node name of the task submit node.
   */
  public void
  setApproveTaskNodeOnTask
  (
    Integer taskID,
    String approveNode
  )
    throws PipelineException
  {
    checkConnection();
    
    TreeMap<String, Object> values = new TreeMap<String, Object>();
    values.put(aApproveNodeField, approveNode);
    try {
      setEntityValues(ShotgunEntity.Task, taskID, values);
    } catch ( Exception ex ) {
      throw new PipelineException
        ("Error when attempting to set the task nodes on the task (" + taskID + ").\n" + 
         ex.getMessage());
    }
  }
  
  /**
   * Sets the approve builder for the specified task.
   * <p>
   * The Task ID is the Shotgun ID and can be retrieved with the {@link
   * #getAssetTaskID(String, String, String) getAssetTaskID} or the {@link
   * #getShotTaskID(String, String, String, String) getShotTaskID} method.
   * 
   * @param taskID
   *   The Shotgun unique ID for the task.
   * @param builderID
   *   The unique pipeline ID for the builder.
   */
  public void
  setApproveBuilderOnTask
  (
    Integer taskID,
    BuilderID builderID
  )
    throws PipelineException
  {
    checkConnection();
    String cmd = "--collection=" + builderID.getName();
    cmd += " --version-id=" + builderID.getVersionID().toString();
    cmd += " --vendor=" + builderID.getVendor();
    cmd += " --builder-name=" + builderID.getBuilderName();
    
    TreeMap<String, Object> values = new TreeMap<String, Object>();
    values.put(aApproveBuilderField, cmd);
    try {
      setEntityValues(ShotgunEntity.Task, taskID, values);
    } catch ( Exception ex ) {
      throw new PipelineException
        ("Error when attempting to set the approve builder on the task (" + taskID + ").\n" + 
         ex.getMessage());
    }
  }
  
  /**
   * Sets the status of the specified task.
   * <p>
   * The Task ID is the Shotgun ID and can be retrieved with the {@link
   * #getAssetTaskID(String, String, String) getAssetTaskID} or the {@link
   * #getShotTaskID(String, String, String, String) getShotTaskID} method.
   * 
   * @param taskID
   *   The Shotgun unique ID for the task.
   * @param status
   *   The status of the shot..
   */
  public void
  setStatusOnTask
  (
    Integer taskID,
    ShotgunTaskStatus status
  )
    throws PipelineException
  {
    checkConnection();
    
    TreeMap<String, Object> values = new TreeMap<String, Object>();
    values.put("sg_status_list", status.toKey());
    try {
      setEntityValues(ShotgunEntity.Task, taskID, values);
    } catch ( Exception ex ) {
      throw new PipelineException
        ("Error when attempting to set the status on the task (" + taskID + ").\n" + 
         ex.getMessage());
    }
  }
  
  private void 
  setEntityValues
  (
    ShotgunEntity entity, 
    Integer taskID, 
    TreeMap<String, Object> values
  )
    throws XmlRpcException
  {
    Object[] params2 = new Object[2];
    
    params2[0] = pToken;
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>(values);
    someMap.put("id", taskID);
    params2[1] = someMap;
    pClient.execute(entity.toString() + ".update", params2);
  }
  
 

  
  /*----------------------------------------------------------------------------------------*/
  /*   Q U E R I E S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get a list of all of projects in Shotgun and their associated ids.
   */
  public TreeMap<String, Integer>
  getProjectList()
    throws PipelineException
  {
    checkConnection();

    TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();

    String columns[] = {"name"};

    ArrayList<Map<String, Object>> results = getEntity(ShotgunEntity.Project, null, columns);
    for (Map<String, Object> result : results) {
      String project = (String) result.get("name");
      Integer id = (Integer) result.get("id");
      if(toReturn.containsKey(project)) 
        throw new PipelineException
          ("There were multiple projects in the Shotgun with the name (" + project +").  " +
           "This needs to be corrected if Shotgun is going to be used with Pipeline.");
      toReturn.put(project, id);
    }
    return toReturn;
  }
  
  /**
   * Get a list of all the scenes in a given project in Shotgun and their associated ids.
   */
  public TreeMap<String, Integer>
  getSceneList
  (
    String project  
  )
    throws PipelineException
  {
    checkConnection();

    TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();

    String columns[] = {"code"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      String filter[] = {"project_names", "is", project};
      filters.add(filter);
    }

    ArrayList<Map<String, Object>> results = getEntity(ShotgunEntity.Scene, filters, columns);
    for (Map<String, Object> result : results) {
      String scene = (String) result.get("code");
      Integer id = (Integer) result.get("id");
      if(toReturn.containsKey(scene)) 
        throw new PipelineException
          ("There were multiple scenes in the Shotgun project (" + project + ") " +
          	"with the name (" + scene +").  This needs to be corrected if Shotgun " +
          	"is going to be used with Pipeline.");
      toReturn.put(scene, id);
    }
    return toReturn;
  }
  
  /**
   * Get a list of all the shots in a given scene in a given project in Shotgun and their
   * associated ids.
   */
  public TreeMap<String, Integer>
  getShotList
  (
    String project,
    String scene
  )
    throws PipelineException
  {
    checkConnection();

    TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();

    String columns[] = {"code"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      String filter[] = {"project_names", "is", project};
      filters.add(filter);
    }
    {
      String filter[] = {"sg_scene", "is", scene};
      filters.add(filter);
    }

    ArrayList<Map<String, Object>> results = getEntity(ShotgunEntity.Shot, filters, columns);
    for (Map<String, Object> result : results) {
      String shot = (String) result.get("code");
      Integer id = (Integer) result.get("id");
      if(toReturn.containsKey(shot)) 
        throw new PipelineException
          ("There were multiple shots in the Shotgun scene (" + scene + ") in the project " +
           "(" + project + ") with the name (" + shot +").  This needs to be corrected " +
           "if Shotgun is going to be used with Pipeline.");
      toReturn.put(shot, id);
    }
    return toReturn;
  }
  
  /**
   * Get a list of all the assets in a given project in Shotgun and their associated ids.
   */
  public TreeMap<String, Integer>
  getAssetList
  (
    String project  
  )
    throws PipelineException
  {
    checkConnection();

    TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();

    String columns[] = {"code"};
    
    ArrayList<Object[]> filters = new ArrayList<Object[]>();
    {
      String filter[] = {"project_names", "is", project};
      filters.add(filter);
    }

    ArrayList<Map<String, Object>> results = getEntity(ShotgunEntity.Asset, filters, columns);
    for (Map<String, Object> result : results) {
      String asset = (String) result.get("code");
      Integer id = (Integer) result.get("id");
      if(toReturn.containsKey(asset)) 
        throw new PipelineException
          ("There were multiple assets in the Shotgun project (" + project + ") " +
            "with the name (" + asset +").  This needs to be corrected if Shotgun " +
            "is going to be used with Pipeline.");
      toReturn.put(asset, id);
    }
    return toReturn;
  }
  
  @SuppressWarnings("unchecked")
  private ArrayList<Map<String, Object>> 
  getEntity
  (
    ShotgunEntity entity, 
    ArrayList<Object[]> filters, 
    String[] columns    
  )
    throws PipelineException
  {
    ArrayList<Map<String, Object>> toReturn = new ArrayList<Map<String,Object>>();
    
    TreeMap <String, Object> someMap = new TreeMap<String, Object>();
    
    Object[] params2 = new Object[2]; 
    Object[] results;
    
    if (filters != null && filters.size() > 0) {
      Object[] projectNameFilter = null;
      for (Object[] filter : filters) {
        if (filter[0].equals("project_names")) {
          projectNameFilter = filter;
          break;
        }
      }
      if (projectNameFilter != null) {
        filters.remove(projectNameFilter);
        someMap.put("project_names", projectNameFilter[2]);
      }
      if (!filters.isEmpty())
        someMap.put("column_filters", filters);
    }
    
    if (columns != null && columns.length > 0)
      someMap.put("columns", columns);    
    
    params2[0] = pToken;
    params2[1] = someMap;
    try {
      results = (Object[]) pClient.execute(entity.toString() + ".find", params2);
      for (Object result : results) {
        Map<String, Object> detail = (Map <String, Object>) result;
        toReturn.add(detail);
      }
    }
    catch(Exception ex) {
     throw new PipelineException
       ("An exception was thrown while searching Shotgun.\n" + ex.getMessage()); 
    }
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L S                                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Join two strings by putting a '_' between them.
   */
  private String
  join
  (
    String part1,
    String part2
  )
  {
    return part1 + "_" + part2;
  }
  
  private String
  createCheckedInLink
  (
    String nodeName,
    String fileSeq,
    VersionID id
  )
  {
    String toReturn = "<a class=\"temerity_link\" href=\"javascript:Temerity.bin.plremote('checked-in --view=" + 
      nodeName + " --version=" + id.toString() + "')\">" + fileSeq + "," + id.toString() + 
      "</a>";
    return toReturn;
  }
  
  private String
  createWorkingLink
  (
    String nodeName,
    String fileSeq
  )
  {
    String toReturn = "<a class=\"temerity_link\" href=\"javascript:Temerity.bin.plremote('working --select=" + 
      nodeName + "')\">" + fileSeq + "</a>";
    return toReturn;
  }
  
  private XmlRpcClient pClient;
  private Object pToken;
  
  private boolean pExceptionOnDup;
  
  public static final String aFocusNodesField     = "sg_tm_focus_nodes";
  public static final String aVersionIDField      = "sg_tm_versionid";
  public static final String aSubmitNodeField     = "sg_tm_submit_node";
  public static final String aApproveNodeField    = "sg_tm_approve_node";
  public static final String aApproveBuilderField = "sg_tm_approve_builder";
  public static final String aVersionTaskField    = "sg_tm_task";
  public static final String aEditNodesField      = "sg_tm_edit_nodes";
  public static final String aDeliveryNodesField  = "sg_tm_proxy_file_path";
  
  private TreeMap<String, Integer> pProjectIDCache;
  
  private DoubleMap<String, String, Integer> pSceneIDCache;
  
  private TripleMap<String, String, String, Integer> pShotIDCache;
  
  private DoubleMap<String, String, Integer> pAssetIDCache;
  
}
