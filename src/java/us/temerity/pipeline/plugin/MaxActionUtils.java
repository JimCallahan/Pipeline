// $Id: MaxActionUtils.java,v 1.2 2007/05/17 16:53:12 jim Exp $

package us.temerity.pipeline.plugin;

import  us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A X   A C T I O N   U T I L S                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of Action plugins which interact with 3dsmax scenes.<P> 
 * 
 * This class provides convenience methods which make it easier to write Action plugins 
 * which create dynamic MAXScripts and 3dsmax scenes. 
 */
public 
class MaxActionUtils
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the action.  
   * 
   * @param vid
   *   The action plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the action.
   */ 
  protected
  MaxActionUtils
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
  /*  M A X   S C R I P T S                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a MAXScript fragment which loads the path configuration file for the given
   * scene, makes sure all project directories exist and sets the current project. <P> 
   * 
   * The scene is assumed to live in a sub-directory called (scenes) of the main project
   * directory. If a 3dsmax path configuration file with the same prefix as the scene 
   * exists in the top-level project directory, it will be loaded before reinitializing
   * and setting the project.  This way scene specific paths can be setup each time the 
   * scene is loaded.
   * 
   * @param scene
   *   The fully resolved 3dsmax scene.
   */ 
  public static final String 
  getProjectInitScript
  (
   FileSeq scene
  ) 
    throws PipelineException
  {
    Path scenePath = new Path(scene.getFilePattern().getPrefix());
    Path parentPath = scenePath.getParentPath();
    if((parentPath == null) || !parentPath.getName().equals("scenes"))
      throw new PipelineException
        ("Cannot generate the MAXScript to initialize the 3dsmax scene (" + scene + ") " + 
         "which must live under the (scenes) directory of a standard 3dsmax project " + 
         "structure!");

    Path projPath = parentPath.getParentPath(); 
    if(projPath == null) 
      throw new PipelineException
        ("Cannot generate the MAXScript to initialize the 3dsmax scene (" + scene + ") " + 
         "because the (scenes) directory containing the scene has no parent project " + 
         "directory!");       

    StringBuilder buf = new StringBuilder(); 
    {
      Path mxpPath = new Path(projPath, projPath.getName() + ".mxp");
      if(mxpPath.toFile().isFile()) 
        buf.append("pathConfig.load(\"" + escPath(mxpPath) + "\")\n");

      String projStr = escPath(projPath);
      buf.append("pathConfig.doProjectSetupStepsUsingDirectory(\"" + projStr + "\")\n");
      
      buf.append("pathConfig.setCurrentProjectFolder(\"" + projStr + "\")\n");
    }

    return buf.toString();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   P A T H   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary 3dsmax scene associated with the target node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the target 3dsmax scene script. 
   */ 
  public Path 
  getMaxSceneTargetPath
  (
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    return getPrimaryTargetPath(agenda, "max", "3dsmax scene file");
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary MAXScript associated with a source node
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued MAXScript parameter.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the MAXScript or <CODE>null</CODE> if none was specified.
   */ 
  public Path 
  getMaxScriptSourcePath
  (
   String pname, 
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    return getPrimarySourcePath(pname, agenda, "ms", "MAXScript");
  }

  /**
   * Get the abstract path to the single primary 3dsmax scene file associated with a source
   * node specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued 3dsmax scene parameter.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the 3dsmax scene file or <CODE>null</CODE> if none was specified.
   */ 
  public Path 
  getMaxSceneSourcePath
  (
   String pname, 
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    return getPrimarySourcePath(pname, agenda, "max", "3dsmax scene file");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P Y T H O N   C O D E   G E N E R A T I O N                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convienence method for creating the Python code equivalent of 
   * {@link #createMaxSubProcess createMaxSubProcess} suitable for inclusion in an 
   * temporary Python script.<P> 
   * 
   * The returned OS level process will run 3dsmax in batch mode to perform some operations 
   * specified by a dynamically creatd MAXScript. <P> 
   * 
   * The Python code generated by this method requires the "launch" method defined by {@link 
   * PythonActionUtils#getPythonLaunchHeader PythngActionUtils.getPythonLaunchHeader}.  You 
   * must first include the code generate by <CODE>getPythonLaunchHeader</CODE> before the 
   * code generated by this method.<P> 
   * 
   * @param script
   *   The temporary MAXScript file to execute.
   */ 
  public static String
  createMaxPythonLauncher
  (
   File script
  ) 
    throws PipelineException
  {
    return createMaxPythonLauncher(new Path(script));
  }

  /** 
   * A convienence method for creating the Python code equivalent of 
   * {@link #createMaxSubProcess createMaxSubProcess} suitable for inclusion in an 
   * temporary Python script.<P> 
   * 
   * The returned OS level process will run 3dsmax in batch mode to perform some operations 
   * specified by a dynamically created MAXScript.<P> 
   * 
   * The Python code generated by this method requires the "launch" method defined by {@link 
   * PythonActionUtils#getPythonLaunchHeader PythonActionUtils.getPythonLaunchHeader}.  You 
   * must first include the code generate by <CODE>getPythonLaunchHeaderget</CODE> before 
   * the code generated by this method.<P> 
   * 
   * @param script
   *   The temporary MAXScript file to execute.
   */ 
  public static String
  createMaxPythonLauncher
  (
   Path script
  ) 
    throws PipelineException
  {
    return ("launch('3dsmax.exe', ['-U', 'MAXScript', '" + script + "', '-silent'])\n");
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S U B P R O C E S S   C R E A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method for 3dsmax scene manipulation Actions.<P> 
   * 
   * The returned OS level process will run 3dsmax in batch mode to perform some operations 
   * specified by a dynamically created MAXScript. <P> 
   * 
   * @param script
   *   The temporary MAXScript file to execute.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */ 
  public SubProcessHeavy
  createMaxSubProcess
  (
   File script, 
   ActionAgenda agenda,
   File outFile, 
   File errFile    
  ) 
    throws PipelineException
  {
    ArrayList<String> args = new ArrayList<String>();
    args.add("-U");
    args.add("MAXScript");
    args.add(script.toString());
    args.add("-silent");

    return createSubProcess(agenda, "3dsmax.exe", args, 
                            agenda.getEnvironment(), outFile, errFile);
  }

  /** 
   * A convienence method for creating a command-line string equivalent of 
   * {@link #createMaxSubProcess createMaxSubProcess} suitable for inclusion in an 
   * executable script.<P> 
   * 
   * The returned OS level process will run 3dsmax in batch mode to perform some operations 
   * specified by a dynamically creatd MEL script. 
   * 
   * @param script
   *   The temporary MEL script file to execute.
   */ 
  public static String
  createMaxCommand
  (
   File script 
  ) 
    throws PipelineException
  {
    return ("3dsmax.exe -U MAXScript " + script + " -silent");
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -167699568246816490L;

}





