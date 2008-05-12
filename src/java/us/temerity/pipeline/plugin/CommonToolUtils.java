// $Id: CommonToolUtils.java,v 1.2 2008/05/12 17:51:23 jesse Exp $

package us.temerity.pipeline.plugin;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M M O N   T O O L   U T I L S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of most Tool plugins which augments BaseTool with many commonly
 * useful helper methods.<P> 
 * 
 * This class provides convenience methods for constructing node related file system paths, 
 * creating subprocesses and other common operations performed in tool Phases.
 */
public abstract
class CommonToolUtils
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the tool.  
   * 
   * @param vid
   *   The tool plugin revision number.
   * 
   * @param vendor
   *   The name of the tool vendor.
   * 
   * @param desc 
   *   A short description of the tool.
   */ 
  protected
  CommonToolUtils
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   M I S C   F I L E   U T I L S                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract working area file system paths in the given node file sequence. 
   * 
   * @param name
   *   The unique node name.
   * 
   * @param fseq
   *   The file sequence associated with the node. 
   */
  public final ArrayList<Path> 
  getWorkingNodeFilePaths
  (
   String name, 
   FileSeq fseq
  ) 
  {
    NodeID nodeID = new NodeID(getAuthor(), getView(), name);
    Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());

    ArrayList<Path> paths = new ArrayList<Path>();
    for(Path fpath : fseq.getPaths()) 
      paths.add(new Path(wpath, fpath));
        
    return paths;
  }

  /**
   * Get the abstract working area file system path to the first file in the given 
   * node file sequence. 
   * 
   * @param name
   *   The unique node name.
   * 
   * @param fseq
   *   The file sequence associated with the node. 
   */
  public final Path
  getWorkingNodeFilePath
  (
   String name, 
   FileSeq fseq
  ) 
  {
    return getWorkingNodeFilePath(name, fseq.getPath(0)); 
  }
  
  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param name
   *   The unique node name.
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public final Path
  getWorkingNodeFilePath
  (
   String name, 
   Path file
  ) 
  {
    return getWorkingNodeFilePath(new NodeID(getAuthor(), getView(), name), file); 
  }
  

  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public final Path
  getWorkingNodeFilePath
  (
   NodeID nodeID,
   Path file
  ) 
  {
    return getWorkingNodeFilePath(nodeID, file.toString());
  }

  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public final Path
  getWorkingNodeFilePath
  (
   NodeID nodeID,
   String file
  ) 
  {
    return new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/" + file);     
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a unique temporary script file appropriate for the current operating 
   * system type.<P> 
   * 
   * On Unix/MacOS this will be an bash(1) script, while on Windows the script will
   * be a BAT file suitable for evaluation by "Cmd".  The returned script file is suitable
   * for use as with the {@link #createScriptSubProcess createScriptSubProcess} method.
   * 
   * @throws PipelineException 
   *   If there is an error attempting to create the script file.
   */ 
  public final File
  createTempScript() 
    throws PipelineException 
  {
    File toReturn = null;
    try {
      if(PackageInfo.sOsType == OsType.Windows)
        toReturn = File.createTempFile
          (getName() + ".", ".bat", PackageInfo.sTempPath.toFile()); 
      else 
        toReturn = File.createTempFile
          (getName() + ".", ".bash", PackageInfo.sTempPath.toFile());
      FileCleaner.add(toReturn);
      } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to create the temporary script (" + toReturn + ") for the " + 
         getName() + " tool!\n\n" + ex.getMessage()); 
    }
    return toReturn;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   P A T H   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Escape any backslashes in the given file system path string.
   */ 
  public static final String 
  escPath
  (
   String str
  ) 
  {
    return str.replaceAll("\\\\", "\\\\\\\\");
  }

  /**
   * Convert an abstract file system path into an OS specific path string with any
   * backslashes escaped.
   */ 
  public static final String 
  escPath
  (
   Path path 
  ) 
  {
    return escPath(path.toOsString());
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B P R O C E S S   C R E A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convenience method for creating the {@link SubProcessLight} instance to be run by 
   * the tool when a temporary script is needed to execute some OS-specific commands. <P>
   * 
   * If running on Unix or MacOS, the supplied script must be a bash(1) shell script while on
   * Windows the script must be an executable BAT/CMD file. The Tool is responsible for 
   * making sure that the contents of these scripts are portable.  This method simply takes 
   * care of the common wrapper code needed to instantiate a {@link SubProcessLight} to run 
   * the script.
   * 
   * @param script
   *   The temporary script file to execute.
   * 
   */ 
  public final SubProcessLight
  createScriptSubProcess
  (
   File script,
   TreeMap<String, String> env 
  ) 
    throws PipelineException
  {
    String program = null;
    ArrayList<String> args = new ArrayList<String>();

    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      program = "bash";
      args.add(script.getPath());
      break;

    case Windows:
      program = script.getPath(); 
    }

    return new SubProcessLight
      (getName() + "Script", program, args, env, PackageInfo.sTempPath.toFile());
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   T R E E   S E A R C H E S                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns all the paths that are located directly underneath a given path.
   * 
   * @param start
   *   The path to start the search underneath
   * @param mclient
   *   The instance of MasterMgrClient used to look up node names.
   * @return An {@link ArrayList} containing all the paths (both directories and nodes)
   *         located directly under the given path.
   */
  protected ArrayList<String> 
  findChildNodeNames
  (
    Path start,
    MasterMgrClient mclient
  ) 
    throws PipelineException
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start.toString(), false);
    NodeTreeComp treeComps = mclient.updatePaths(getAuthor(), getView(), comps);
    Path p = new Path(start);
    ArrayList<String> parts = p.getComponents();
    for(String comp : parts) {
      if (treeComps == null)
        break;
      treeComps = treeComps.get(comp);
    }
    if (treeComps != null) {
      for(String s : treeComps.keySet()) {
        toReturn.add(s);
      }
    }
    return toReturn;
  }
  
  /**
   * Returns all the directories that are located directly underneath a given path.
   * 
   * @param start
   *   The path to start the search underneath
   * @param mclient
   *   The instance of MasterMgrClient used to look up node names.
   * @return An {@link ArrayList} containing the names of all the directories located
   *         directly under the given path.
   */
  protected ArrayList<String> 
  findChildBranchNames
  (
    Path start,
    MasterMgrClient mclient
  ) 
  throws PipelineException
  {
    ArrayList<String> toReturn = new ArrayList<String>();
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start.toString(), false);
    NodeTreeComp treeComps = mclient.updatePaths(getAuthor(), getView(), comps);
    Path p = new Path(start);
    ArrayList<String> parts = p.getComponents();
    for(String comp : parts) {
      if ( treeComps == null )
        break;
      treeComps = treeComps.get(comp);
    }
    if(treeComps != null) {
      for(String s : treeComps.keySet()) {
        NodeTreeComp comp = treeComps.get(s);
        if ( comp.getState() == NodeTreeComp.State.Branch )
          toReturn.add(s);
      }
    }
    return toReturn;
  }
  
  /**
   * Returns all the fully resolved node names that are located underneath a given path.
   * 
   * @param start
   *   The path to start the search underneath
   * @param mclient
   *   The instance of MasterMgrClient used to look up node names.
   * @return An {@link ArrayList} containing all the node paths located under the given path.
   */
  protected ArrayList<String> 
  findAllChildNodeNames
  (
    String start,
    MasterMgrClient mclient
  ) 
  throws PipelineException
  {
    TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
    comps.put(start, true);
    NodeTreeComp treeComps = mclient.updatePaths(getAuthor(), getView(), comps);
    ArrayList<String> toReturn = new ArrayList<String>();
    for(String s : treeComps.keySet()) {
      findNodes(treeComps.get(s), toReturn, "/");
    }
    return toReturn;
  }
  
  /**
   * Recursive function to search for nodes under a given path.
   * <p>
   * Starts with the current {@link NodeTreeComp}, travels down the tree, and adds any nodes
   * it finds to an {@link ArrayList} being passed as a parameter. It is important to note
   * when using this method that the {@link ArrayList} is being modified inside the method.
   * The {@link ArrayList} will contain the fully resolved node names for all the nodes.
   * 
   * @param treeComps
   *        A {@link NodeTreeComp} that should contain information about the node name
   *        specified by scene. The most common way to acquire this data structure is with the
   *        <code>updatePaths</code> method in {@link MasterMgrClient}.
   * @param toReturn
   *        An {@link ArrayList} that will hold every node that is found by this method.
   * @param path
   *        The full path that leads up to the current {@link NodeTreeComp}. This is needed
   *        to build the full node name being stored in the ArrayList.
   */
  protected void 
  findNodes
  (
    NodeTreeComp treeComps, 
    ArrayList<String> toReturn, 
    String path
  )
  {
    State state = treeComps.getState();
    if(state.equals(State.Branch))
      for(String s : treeComps.keySet())
        findNodes(treeComps.get(s), toReturn, path + treeComps.getName() + "/");
    else
      toReturn.add(path + treeComps.getName());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   E X I S T A N C E                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Does a checked-out version of the node exist in the current working area?
   * 
   * @param nodeName
   *   The name of the node.
   * @param mclient
   *   The instance of MasterMgrClient to search with.
   */
   protected boolean
   hasLocalWorkingVersion
   (
     String nodeName,
     MasterMgrClient mclient
   )
     throws PipelineException
   {
     TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
     comps.put(nodeName, true);
     NodeTreeComp treeComps = mclient.updatePaths(getAuthor(), getView(), comps);
     State state =  treeComps.getState(nodeName);
     switch (state) {
     case WorkingCurrentCheckedInNone:
     case WorkingCurrentCheckedInSome:
       return true;
     default:
       return false;
     }
   }
   
   /**
    * Does a checked-in version of the node exist?
    * 
    * @param nodeName
    *   The name of the node.
    * @param mclient
    *   The instance of MasterMgrClient to search with.
    */
    protected boolean
    hasCheckedInVersion
    (
      String nodeName,
      MasterMgrClient mclient
    )
      throws PipelineException
    {
      TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
      comps.put(nodeName, true);
      NodeTreeComp treeComps = mclient.updatePaths(getAuthor(), getView(), comps);
      State state =  treeComps.getState(nodeName);
      switch (state) {
      case WorkingCurrentCheckedInSome:
      case WorkingNoneCheckedInSome:
      case WorkingOtherCheckedInSome:
        return true;
      default:
        return false;
      }
    }
}