// $Id: MayaActionUtils.java,v 1.1 2007/04/04 07:33:30 jim Exp $

package us.temerity.pipeline.plugin;

import  us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   A C T I O N   U T I L S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of Action plugins which interact with Maya scenes.<P> 
 * 
 * This class provides convenience methods which make it easier to write Action plugins 
 * which create dynamic MEL scripts and Maya scenes. 
 */
public 
class MayaActionUtils
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
  MayaActionUtils
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
  /*   C O M M O N   P A R A M E T E R S                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Adds Maya units parameters to the action.<P> 
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   Linear Unit <BR>
   *   <DIV style="margin-left: 40px;">
   *     The linear unit that the generated scene will use. 
   *   </DIV> <BR>
   * 
   *   Angular Unit <BR>
   *   <DIV style="margin-left: 40px;">
   *     The angular unit that the generated scene will use. 
   *   </DIV> <BR>
   *   
   *   Time Unit <BR>
   *   <DIV style="margin-left: 40px;">
   *     The unit of time and frame rate that the generated scene will use. 
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */ 
  protected void 
  addUnitsParams() 
  {
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("millimeter");
      choices.add("centimeter");
      choices.add("meter");
      choices.add("inch");
      choices.add("foot");
      choices.add("yard");
      
      ActionParam param = 
	new EnumActionParam
	(aLinearUnits,
	 "The linear units format the constructed maya scene should use.", 
	 "centimeter", 
	 choices);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("degrees");
      choices.add("radians");
      
      ActionParam param = 
	new EnumActionParam
	(aAngularUnits,
	 "The angular units format the constructed maya scene should use.", 
	 "degrees", 
	 choices);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("15 fps");
      choices.add("Film (24 fps)");
      choices.add("PAL (25 fps)");
      choices.add("NTSC (30 fps)");
      choices.add("Show (48 fps)");
      choices.add("PAL Field (50 fps)");
      choices.add("NTSC Field (60 fps)");
      choices.add("milliseconds");
      choices.add("seconds");
      choices.add("minutes");
      choices.add("hours");
      choices.add("2fps");
      choices.add("3fps");
      choices.add("4fps");
      choices.add("5fps");
      choices.add("6fps");
      choices.add("8fps");
      choices.add("10fps");
      choices.add("12fps");
      choices.add("16fps");
      choices.add("20fps");
      choices.add("40fps");
      choices.add("75fps");
      choices.add("80fps");
      choices.add("100fps");
      choices.add("120fps");
      choices.add("150fps");
      choices.add("200fps");
      choices.add("240fps");
      choices.add("250fps");
      choices.add("300fps");
      choices.add("375fps");
      choices.add("400fps");
      choices.add("500fps");
      choices.add("600fps");
      choices.add("750fps");
      choices.add("1200fps");
      choices.add("1500fps");
      choices.add("2000fps");
      choices.add("3000fps");
      choices.add("6000fps");
           
      
      ActionParam param = 
	new EnumActionParam
	(aTimeUnits,
	 "The time format the constructed maya scene should use.", 
	 "Film (24 fps)", 
	 choices);
      addSingleParam(param);
    }
  }

  /**
   * Add the parameters created by the {@link #addUnitsParams addUnitsParams} method to the 
   * given parameter layout group.
   */ 
  protected void 
  addUnitsParamsToLayout
  (
   LayoutGroup layout
  ) 
  {
    layout.addEntry(aLinearUnits);
    layout.addEntry(aAngularUnits);
    layout.addEntry(aTimeUnits);
  }
  
  /** 
   * Generate a MEL snippet which specifies the Linear, Angular and Time units for the 
   * Maya scene based on the parameters created by the {@link #addUnitsParams addUnitsParams} 
   * method.
   */ 
  protected String
  genUnitsMEL() 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder();
    buf.append("// UNITS\n");
    
    {
      String unit = (String) getSingleParamValue(aLinearUnits);
      if(unit == null)
        throw new PipelineException 
          ("The " + getName() + " Action requires a valid linear units value!");

      buf.append("changeLinearUnit(\"" + unit + "\");\n");
    }
    
    switch(((EnumActionParam) getSingleParam(aAngularUnits)).getIndex()) {
    case 0:
      buf.append("currentUnit -a degree;\n");
      break;
      
    case 1:
      buf.append("currentUnit -a radian;\n");
      break;
      
    default:
      throw new PipelineException 
        ("This " + getName() + " Action requires a valid angular units value!");
    }
    
    {
      int timeIndex = ((EnumActionParam) getSingleParam(aTimeUnits)).getIndex() ;
      if((timeIndex < 0) || (timeIndex >= sMelTimeUnits.length))
        throw new PipelineException 
          ("The " + getName() + " Action requires a valid time units value!");

      buf.append
        ("currentUnit -t " + sMelTimeUnits[timeIndex] + " -updateAnimation true;\n" + 
         "optionVar -fv playbackMin `playbackOptions -q -min`\n" +
         "          -fv playbackMax `playbackOptions -q -max`;\n\n");
    }

    return buf.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R   L O O K U P                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the MEL string representation of the file type of the primary Maya scene associated 
   * with the target node. <P>
   * 
   * The value returned by this method is suitable for use with the "-type" option of the 
   * MEL "file" command.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return
   *   Either "mayaAscii" or "mayaBinary" depending on the primary file suffix.
   */ 
  public String
  getMayaSceneType
  (
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    String suffix = agenda.getPrimaryTarget().getFilePattern().getSuffix();
    if(suffix == null) 
      throw new PipelineException
        ("Cannot determine the Maya scene file type without a filename suffix!");

    if(suffix.equals("ma"))
      return "mayaAscii";
    else if(suffix.equals("mb"))
      return "mayaBinary"; 
    else 
      throw new PipelineException
        ("Unknown Maya filename suffix (" + suffix + ") found!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P A T H   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary Maya scene associated with the target node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the target Maya scene script. 
   */ 
  public Path 
  getMayaSceneTargetPath
  (
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add("ma");
    suffixes.add("mb");

    return getPrimaryTargetPath(agenda, suffixes, "Maya scene file");
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary MEL script associated with a source node
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued MEL parameter.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the MEL script or <CODE>null</CODE> if none was specified.
   */ 
  public Path 
  getMelScriptSourcePath
  (
   String pname, 
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    return getPrimarySourcePath(pname, agenda, "mel", "MEL script");
  }

  /**
   * Get the abstract path to the single primary Maya scene file associated with a source
   * node specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued Maya scene parameter.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the Maya scene file or <CODE>null</CODE> if none was specified.
   */ 
  public Path 
  getMayaSceneSourcePath
  (
   String pname, 
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add("ma");
    suffixes.add("mb");

    return getPrimarySourcePath(pname, agenda, suffixes, "Maya scene file");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P Y T H O N   C O D E   G E N E R A T I O N                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convienence method for creating the Python code equivalent of 
   * {@link #createMayaSubProcess createMayaSubProcess} suitable for inclusion in an 
   * temporary Python script.<P> 
   * 
   * The returned OS level process will run Maya in batch mode to optionally load a input
   * Maya scene and then perform some operations specified by a dynamically creatd MEL 
   * script. <P> 
   * 
   * The Python code generated by this method requires the "launch" method defined by {@link 
   * BaseAction#getPythonLaunchHeaderget BaseAction.PythonLaunchHeader}.  You must first 
   * include the code generate by <CODE>getPythonLaunchHeaderget</CODE> before the code 
   * generated by this method.<P> 
   * 
   * @param scene
   *   The abstract path to the Maya scene to load or <CODE>null</CODE> to ignore.
   * 
   * @param script
   *   The temporary MEL script file to execute.
   */ 
  public static String
  createMayaPythonLauncher
  (
   Path scene, 
   File script
  ) 
    throws PipelineException
  {
    return createMayaPythonLauncher(scene, new Path(script));
  }

  /** 
   * A convienence method for creating the Python code equivalent of 
   * {@link #createMayaSubProcess createMayaSubProcess} suitable for inclusion in an 
   * temporary Python script.<P> 
   * 
   * The returned OS level process will run Maya in batch mode to optionally load a input
   * Maya scene and then perform some operations specified by a dynamically creatd MEL 
   * script. <P> 
   * 
   * The Python code generated by this method requires the "launch" method defined by {@link 
   * BaseAction#getPythonLaunchHeaderget BaseAction.PythonLaunchHeader}.  You must first 
   * include the code generate by <CODE>getPythonLaunchHeaderget</CODE> before the code 
   * generated by this method.<P> 
   * 
   * @param scene
   *   The abstract path to the Maya scene to load or <CODE>null</CODE> to ignore.
   * 
   * @param script
   *   The temporary MEL script file to execute.
   */ 
  public static String
  createMayaPythonLauncher
  (
   Path scene, 
   Path script
  ) 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder();
    
    String maya = "maya";
    if(PackageInfo.sOsType == OsType.Windows) 
      maya = "mayabatch.exe"; 

    buf.append
      ("launch('" + maya + "', ['-batch', '-script', '" + script + "'");

    if(scene != null) 
      buf.append(", '-file', '" + scene + "'");

    buf.append("])\n");

    return buf.toString(); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T   E N V I R O N M E N T  M O D I F I C A T I O N                       */
  /*----------------------------------------------------------------------------------------*/
   
  /**
   * Get the Toolset environment modified to include a custom mental ray shader path.<P> 
   * 
   * If the variable PIPELINE_MI_SHADER_PATH is set in the Toolset environment, then the 
   * variable MI_CUSTOM_SHADER_PATH will be added to the environment who's value is the 
   * constructed by appending the value of PIPELINE_MI_SHADER_PATH to the working area 
   * directory containing the target node's files.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @return 
   *   The modified Toolset environment.
   */ 
  public static Map<String,String>
  getMiCustomShaderEnv
  (
   ActionAgenda agenda
  ) 
    throws PipelineException
  {  
    return getMiCustomShaderEnv(agenda.getNodeID(), agenda.getEnvironment());
  }

  /**
   * Get the Toolset environment modified to include a custom mental ray shader path.<P> 
   * 
   * If the variable PIPELINE_MI_SHADER_PATH is set in the Toolset environment, then the 
   * variable MI_CUSTOM_SHADER_PATH will be added to the environment who's value is the 
   * constructed by appending the value of PIPELINE_MI_SHADER_PATH to the working area 
   * directory containing the target node's files.
   * 
   * @param nodeID
   *   The unique working version identifier of the target node.
   * 
   * @param env
   *   The original Toolset environment. 
   * 
   * @return 
   *   The modified Toolset environment.
   */ 
  public static Map<String,String>
  getMiCustomShaderEnv
  (
   NodeID nodeID, 
   Map<String,String> env
  ) 
    throws PipelineException
  {  
    Map<String,String> nenv = env;

    String midefs = env.get("PIPELINE_MI_SHADER_PATH");
    if(midefs != null) {
      nenv = new TreeMap<String, String>(env);
      Path dpath = getWorkingNodeFilePath(nodeID, midefs);
      nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toString());
    }
   
    return nenv;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S U B P R O C E S S   C R E A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method for Maya scene manipulation Actions.<P> 
   * 
   * The returned OS level process will run Maya in batch mode to optionally load a input
   * Maya scene and then perform some operations specified by a dynamically creatd MEL 
   * script. <P> 
   * 
   * If the <CODE>customShaders</CODE> parameter is set to <CODE>true</CODE> and the variable
   * PIPELINE_MI_SHADER_PATH is set in the Toolset environment, then the variable 
   * MI_CUSTOM_SHADER_PATH will be added to the environment who's value is the constructed 
   * by appending the value of PIPELINE_MI_SHADER_PATH to the working area directory
   * containing the target node's files.
   * 
   * @param scene
   *   The abstract path to the Maya scene to load or <CODE>null</CODE> to ignore.
   * 
   * @param script
   *   The temporary MEL script file to execute.
   * 
   * @param customShaders
   *   Whether to set the value of MI_CUSTOM_SHADER_PATH (see above).
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
  createMayaSubProcess
  (
   Path scene, 
   File script, 
   boolean customShaders, 
   ActionAgenda agenda,
   File outFile, 
   File errFile    
  ) 
    throws PipelineException
  {
    String program = "maya";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "mayabatch.exe";

    ArrayList<String> args = new ArrayList<String>();
    args.add("-batch");
    args.add("-script");

    Path spath = new Path(script);
    args.add(spath.toString());

    if(scene != null) {
      args.add("-file");
      args.add(scene.toString());      
    }

    Map<String,String> env = null;
    if(customShaders) 
      env = getMiCustomShaderEnv(agenda); 
    else 
      env = agenda.getEnvironment();

    return createSubProcess(agenda, program, args, env, outFile, errFile);
  }

  /** 
   * A convienence method for creating a command-line string equivalent of 
   * {@link #createMayaSubProcess createMayaSubProcess} suitable for inclusion in an 
   * executable script.<P> 
   * 
   * The returned OS level process will run Maya in batch mode to optionally load a input
   * Maya scene and then perform some operations specified by a dynamically creatd MEL 
   * script. 
   * 
   * @param scene
   *   The abstract path to the Maya scene to load or <CODE>null</CODE> to ignore.
   * 
   * @param script
   *   The temporary MEL script file to execute.
   */ 
  public static String
  createMayaCommand
  (
   Path scene, 
   File script 
  ) 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder();

    if(PackageInfo.sOsType == OsType.Windows) 
      buf.append("mayabatch.exe");
    else 
      buf.append("maya");

    Path spath = new Path(script);
    buf.append(" -batch -script \"" + spath + "\"");

    if(scene != null) 
      buf.append(" -file \"" + scene + "\"\n");

    return buf.toString(); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String sMelTimeUnits[] = {
    "game", 
    "film", 
    "pal", 
    "ntsc", 
    "show", 
    "palf", 
    "ntscf", 
    "millisec", 
    "sec", 
    "min", 
    "hour", 
    "2fps", 
    "3fps", 
    "4fps", 
    "5fps", 
    "6fps", 
    "8fps", 
    "10fps", 
    "12fps", 
    "16fps", 
    "20fps", 
    "40fps", 
    "75fps", 
    "80fps", 
    "100fps", 
    "120fps", 
    "125fps", 
    "150fps", 
    "200fps", 
    "240fps", 
    "250fps", 
    "300fps", 
    "375fps", 
    "400fps", 
    "500fps", 
    "600fps", 
    "750fps", 
    "1200fps", 
    "1500fps", 
    "2000fps", 
    "3000fps", 
    "6000fps"
  };


  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4571431896392188644L;
  
  public static final String aLinearUnits  = "LinearUnits";
  public static final String aAngularUnits = "AngularUnits";
  public static final String aTimeUnits    = "TimeUnits";

}




