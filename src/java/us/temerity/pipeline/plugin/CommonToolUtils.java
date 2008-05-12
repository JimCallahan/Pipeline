// $Id: CommonToolUtils.java,v 1.1 2008/05/12 16:42:44 jesse Exp $

package us.temerity.pipeline.plugin;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

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
public 
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


}